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
import org.nlogo.fileformat, fileformat.{ FailedConversionResult, NLogoFormat, NLogoThreeDFormat, NLogoXFormat }
import org.nlogo.workspace.ModelsLibrary.modelsRoot
import org.nlogo.headless.HeadlessWorkspace
import org.nlogo.sdm.{ NLogoSDMFormat, NLogoThreeDSDMFormat, NLogoXSDMFormat, SDMAutoConvertable }
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
  case class ResaveMode(srcFormats: Seq[String] = Seq(".nlogox"), destFormat: String = "nlogox") {
    def matchesModel(p: Path): Boolean = {
      val name = p.getFileName.toString
      srcFormats.exists(extension => name.endsWith(extension))
    }

    def destName(s: String, m: Model): String = {
      val pathSegments = s.split("\\.")
      val extension = destFormat match {
        case "nlogo" if (Version.is3D(m.version)) => "nlogo3d"
        case "nlogo" => "nlogo"
        case "nlogox" => "nlogox"
        case other => other
      }
      (pathSegments.init :+ extension).mkString(".")
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
      val options = args.takeWhile(_.startsWith("-"))
      val src = options
        .find(_.startsWith("--srcFormat="))
        .map(srcFormat =>
            if (srcFormat.endsWith("nlogo")) Seq(".nlogo", ".nlogo3d")
            else Seq(srcFormat.stripPrefix("--srcFormat=")))
        .getOrElse(Seq(".nlogox"))
      val dest = options
        .find(_.startsWith("--destFormat="))
        .map(destFormat =>
            if (destFormat.endsWith("nlogo")) "nlogo"
            else destFormat.stripPrefix("--destFormat=").stripPrefix("."))
        .getOrElse("nlogox")
      val recursive =
        if (options.contains("-r")) true
        else false
      (ResaveMode(src, dest), recursive, options.length)
    }

    // recurse and resave
    if (recursive)            resaveModelsRecursive(args(startArg), mode)
    else if (args.length > 0) resaveModels(args.drop(startArg).toSeq, mode)
    else                      resaveAllModels(mode)
  }

  def resaveModels(paths: Seq[String], mode: ResaveMode): Unit = {
    val (systemDynamicsModels, otherModels) =
      paths.map((s: String) => Paths.get(s)).partition(_.toString.contains("System Dynamics"))
    resaveCollection(new PathCollection(otherModels, systemDynamicsModels), mode)
  }

  def resaveModelsRecursive(pathString: String, mode: ResaveMode): Unit =
    resaveCollection(traverseModels(Paths.get(pathString), mode, Seq("BIN", "SRC", "PROJECT")), mode)

  def resaveAllModels(mode: ResaveMode): Unit =
    resaveCollection(traverseModels(Paths.get(modelsRoot), mode), mode)

  def resaveCollection(collection: PathCollection, mode: ResaveMode): Unit = {
    collection.normalModels.foreach(resaveModel(mode))

    println(s"Resaved ${collection.normalModels.length} models")
  }

  lazy val literalParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  def resaveModel(mode: ResaveMode)(modelPath: Path): Unit = {
    val version = fileformat.modelVersionAtPath(modelPath.toString)
      .getOrElse(throw new Exception(s"Unable to determine version of ${modelPath.toString}"))
    val ws = HeadlessWorkspace.newInstance(version.is3D)
    val converter =
      fileformat.converter(ws.getExtensionManager, ws.getCompilationEnvironment,
        literalParser, fileformat.defaultAutoConvertables :+ SDMAutoConvertable) _
    val modelLoader =
      fileformat.standardLoader(ws.compiler.utilities)
        .addSerializer[Array[String], NLogoFormat](new NLogoSDMFormat())
        .addSerializer[Array[String], NLogoThreeDFormat](new NLogoThreeDSDMFormat())
        .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXSDMFormat(ScalaXmlElementFactory))
    val controller = new ResaveOpenController(modelPath.toUri)
    val dialect =
      if (modelPath.toString.toUpperCase.endsWith("3D")) NetLogoThreeDDialect
      else NetLogoLegacyDialect
    OpenModelFromURI(modelPath.toUri, controller, modelLoader, converter(dialect), version).foreach { model =>
      val saveController = new ResaveSaveController(modelPath.toUri, mode, model)
      SaveModel(model, modelLoader, saveController, ws.modelTracker, version).map(_.apply()) match {
        case Some(Success(u)) => println("resaved: " + u)
        case Some(Failure(e)) => println("errored resaving: " + modelPath.toString + " " + e.toString)
        case None => println("failed to resave: " + modelPath.toString)
      }
    }
    ws.dispose()
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

  def resaveSystemDynamicsModels(paths: Seq[Path], mode: ResaveMode): Seq[(Path, String)] = {
    App.main(Array[String]())

    var failedModels = List[(Path, String)]()

    for (path <- paths) {
      wait {
        try {
          App.app.open(path.toString)
          App.app.saveOpenModel(new ResaveSaveController(path.toUri, mode, App.app.workspace.modelTracker.model))
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

  class ResaveOpenController(path: URI) extends OpenModelController {
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

  class ResaveSaveController(path: URI, mode: ResaveMode, model: Model) extends SaveModelController {
    // SaveModelController
    def chooseFilePath(modelType: org.nlogo.api.ModelType): Option[java.net.URI] = {
      val filePath = Paths.get(path)
      val destFileName = mode.destName(filePath.getFileName.toString, model)
      Some(filePath.getParent.resolve(Paths.get(destFileName)).toUri)
    }
    def shouldSaveModelOfDifferingVersion(currentVersion: Version, saveVersion: String): Boolean = true
    def warnInvalidFileFormat(format: String): Unit = {
      println("asked to save model in invalid format \"" + format + "\"")
    }
  }
}
