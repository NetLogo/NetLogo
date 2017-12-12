package org.nlogo.tools

import java.awt.EventQueue
import java.net.URI
import java.nio.file.{ Path, Paths }

import scala.util.{ Failure, Success }

import org.nlogo.core.{ Femto, LiteralParser, Model }
import org.nlogo.api.{ NetLogoLegacyDialect, NetLogoThreeDDialect, Version }
import org.nlogo.app.App
import org.nlogo.workspace.{ OpenModel, OpenModelFromURI, SaveModel },
  OpenModel.{ Controller => OpenModelController },
  SaveModel.{ Controller => SaveModelController }
import org.nlogo.fileformat, fileformat.{ FailedConversionResult, NLogoFormat, NLogoXFormat }
import org.nlogo.workspace.ModelsLibrary.modelsRoot
import org.nlogo.headless.HeadlessWorkspace
import org.nlogo.sdm.{ NLogoSDMFormat, NLogoXSDMFormat, SDMAutoConvertable }
import org.nlogo.xmllib.ScalaXmlElementFactory
import org.nlogo.util.PathTools

/**
 *
 * The original motivation for that was that the version number saved in the model file
 * appears on the Modeling Commons website and it looked weird to have most model from
 * the NetLogo Model Library appear to have been saved with various older earlier versions
 * of NetLogo. Ideally, this script should be run before every release.
 *
 * This script opens and saves all models in the library, excluding 3D and HubNet models.
 * (3D and HubNet are excluded because they are not on the Modeling Commons, but if someone
 * wants to bother adding support for those models in this script, it would be a good thing.)
 *
 * The script fires up the NetLogo GUI because headless NetLogo has no saving facility.
 *
 * Nicolas 2012-10-31
 *
 * Moved to org.nlogo.tools, NP 2013-08-07
 *
 */
object ModelResaver {
  sealed trait ResaveMode {
    def matchesModel(p: Path): Boolean
  }
  case object LegacyToNLogoX extends ResaveMode {
    def matchesModel(p: Path): Boolean = {
      val name = p.getFileName.toString
      name.endsWith(".nlogo") || name.endsWith(".nlogo3d")
    }
  }
  case object NLogoX extends ResaveMode {
    def matchesModel(p: Path): Boolean = {
      val name = p.getFileName.toString
      name.endsWith(".nlogox")
    }
  }

  def wait(block: => Unit) {
    EventQueue.invokeAndWait(
      new Runnable() {
        def run() { block }
      })
  }

  var systemDynamicsModels: Seq[Path] = Seq()

  def main(args: Array[String]): Unit = {
    System.setProperty("org.nlogo.preferHeadless", "true")

    val (mode, recursive, startArg) = {
      val mode =
        if (args.take(2).contains("--nlogo")) LegacyToNLogoX
        else NLogoX
      val recursive =
        if (args.take(2).contains("-r")) true
        else false
      (mode, recursive, 0 + (if (recursive) 1 else 0) + (if (mode == LegacyToNLogoX) 1 else 0))
    }

    // recurse and resave
    if (recursive)            resaveModelsRecursive(args(startArg), mode)
    else if (args.length > 0) resaveModels(args.toSeq, mode)
    else                      resaveAllModels(mode)
  }

  def resaveModels(paths: Seq[String], mode: ResaveMode): Unit = {
    val (systemDynamicsModels, otherModels) =
      paths.map((s: String) => Paths.get(s)).partition(_.toString.contains("System Dynamics"))
    resaveCollection(new PathCollection(otherModels, systemDynamicsModels))
  }

  def resaveModelsRecursive(pathString: String, mode: ResaveMode): Unit =
    resaveCollection(traverseModels(Paths.get(pathString), mode, Seq("BIN", "SRC", "PROJECT")))

  def resaveAllModels(mode: ResaveMode): Unit =
    resaveCollection(traverseModels(Paths.get(modelsRoot), mode))

  def resaveCollection(collection: PathCollection): Unit = {
    collection.normalModels.foreach(resaveModel _)

    println(s"Resaved ${collection.normalModels.length} non-system-dynamics models")
    if (collection.systemDynamicsModels.nonEmpty) {
      val failedModels = resaveSystemDynamicsModels(collection.systemDynamicsModels)

      println("FAILED MODELS:")
      println(failedModels.mkString("\n"))
    } else {
      System.exit(0)
    }
  }

  lazy val literalParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  def resaveModel(modelPath: Path): Unit = {
    if (modelPath.toString.contains("System Dynamics"))
      systemDynamicsModels = systemDynamicsModels :+ modelPath
    else {
      val version = fileformat.modelVersionAtPath(modelPath.toString)
        .getOrElse(throw new Exception(s"Unable to determine version of ${modelPath.toString}"))
      val ws = HeadlessWorkspace.newInstance(version.is3D)
      val converter =
        fileformat.converter(ws.getExtensionManager, ws.getCompilationEnvironment,
          literalParser, fileformat.defaultAutoConvertables :+ SDMAutoConvertable) _
      val modelLoader =
        fileformat.standardLoader(ws.compiler.utilities)
          .addSerializer[Array[String], NLogoFormat](new NLogoSDMFormat())
          .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXSDMFormat(ScalaXmlElementFactory))
      val controller = new ResaveController(modelPath.toUri)
      val dialect =
        if (modelPath.toString.toUpperCase.endsWith("3D")) NetLogoThreeDDialect
        else NetLogoLegacyDialect
      OpenModelFromURI(modelPath.toUri, controller, modelLoader, converter(dialect), version).foreach { model =>
        SaveModel(model, modelLoader, controller, ws.modelTracker, version).map(_.apply()) match {
          case Some(Success(u)) => println("resaved: " + u)
          case Some(Failure(e)) => println("errored resaving: " + modelPath.toString + " " + e.toString)
          case None => println("failed to resave: " + modelPath.toString)
        }
      }
      ws.dispose()
    }
  }

  case class PathCollection(normalModels: Seq[Path], systemDynamicsModels: Seq[Path])

  def traverseModels(
    modelRoot:      Path,
    mode:           ResaveMode,
    excludeFolders: Seq[String] = Seq("TEST", "BIN", "PROJECT", "SRC")): PathCollection = {
    val childPaths =
      PathTools
        .findChildrenRecursive(modelRoot,
          filterDirectories = (p: Path) => ! excludeFolders.contains(p.getFileName.toString.toUpperCase))
        .filter(mode.matchesModel _)

    val (sdModels, normalModels) = childPaths.partition(p => p.toString.contains("System Dynamics"))

    PathCollection(normalModels, sdModels)
  }

  def resaveSystemDynamicsModels(paths: Seq[Path]): Seq[(Path, String)] = {
    App.main(Array[String]())

    var failedModels = List[(Path, String)]()

    for (path <- paths) {
      wait {
        try {
          val controller = new ResaveController(path.toUri)
          App.app.open(path.toString)
          App.app.saveOpenModel(controller)
        }
        catch {
          case e: Exception => failedModels :+= ((path, e.getMessage))
        }
      }
    }
    wait {
      App.app.quit()
    }
    failedModels
  }

  class ResaveController(path: URI) extends OpenModelController with SaveModelController {
    // SaveModelController
    def chooseFilePath(modelType: org.nlogo.api.ModelType): Option[java.net.URI] = {
      val pathSegments = path.toString.split("\\.")
      val nlogoXPath = pathSegments.init
      Some(new URI((nlogoXPath :+ "nlogox").mkString(".")))
    }
    def shouldSaveModelOfDifferingVersion(currentVersion: Version, saveVersion: String): Boolean = true
    def warnInvalidFileFormat(format: String): Unit = {
      println("asked to save model in invalid format \"" + format + "\"")
    }

    // OpenModelController
    def errorOpeningURI(uri: java.net.URI,exception: Exception): Unit = {
      println("error opening model at: " + uri.toString + " - " + exception.toString)
    }
    def invalidModel(uri: java.net.URI): Unit = {
      println("invalid NetLogo model at: " + uri.toString)
    }
    def invalidModelVersion(uri: java.net.URI,version: String): Unit = {
      println("invalid Model version: \"" + version + "\" at: " + uri.toString)
    }
    def errorAutoconvertingModel(res: FailedConversionResult): Option[Model] = {
      println("Autoconversion failed for model at: " + path)
      println("errors:")
      res.errors.foreach(_.errors.foreach { e =>
        println(e.getMessage)
        e.printStackTrace()
      })
      None
    }
    def shouldOpenModelOfDifferingArity(arity: Int,version: String): OpenModel.VersionResponse =
      OpenModel.OpenAsSaved
    def shouldOpenModelOfLegacyVersion(currentVersion: String, openVersion: String): Boolean = true
    def shouldOpenModelOfUnknownVersion(currentVersion: String, openVersion: String): Boolean = false
  }
}
