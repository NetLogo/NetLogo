package org.nlogo.tools

import java.awt.EventQueue
import java.io.File
import java.net.URI
import java.nio.file.Paths

import scala.sys.process.Process
import scala.util.{ Success, Failure }

import org.nlogo.api.Version
import org.nlogo.app.App
import org.nlogo.workspace.{ OpenModel, SaveModel },
  OpenModel.{ Controller => OpenModelController },
  SaveModel.{ Controller => SaveModelController }
import org.nlogo.workspace.ModelsLibrary.getModelPaths
import org.nlogo.headless.HeadlessWorkspace
import org.nlogo.fileformat.NLogoFormat
import org.nlogo.sdm.NLogoSDMFormat

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

  def wait(block: => Unit) {
    EventQueue.invokeAndWait(
      new Runnable() {
        def run() { block }
      })
  }

  def main(args: Array[String]): Unit = {
    var systemDynamicsModels: Seq[String] = Seq()
    for (path <- getModelPaths) {
      val ws = HeadlessWorkspace.newInstance
      val modelLoader =
        org.nlogo.fileformat.standardLoader(ws.compiler.compilerUtilities, ws.getExtensionManager, ws.getCompilationEnvironment)
          .addSerializer[Array[String], NLogoFormat](new NLogoSDMFormat())
      val uri = Paths.get(path).toUri
      val controller = new ResaveController(uri)
      OpenModel(uri, controller, modelLoader, Version).foreach { model =>
        if (model.hasValueForOptionalSection("org.nlogo.modelsection.systemdynamics"))
          systemDynamicsModels = systemDynamicsModels :+ path
        else
          SaveModel(model, modelLoader, controller, ws, Version).map(_.apply()) match {
            case Some(Success(u)) => println("resaved: " + u)
            case Some(Failure(e)) => println("errored resaving: " + path + " " + e.toString)
            case None => println("failed to resave: " + path)
          }
      }
    }

    val failedModels = resaveSystemDynamicsModels(systemDynamicsModels)

    println("FAILED MODELS:")
    println(failedModels.mkString("\n"))
  }

  def resaveSystemDynamicsModels(paths: Seq[String]): Seq[(String, String)] = {
    App.main(Array[String]())

    var failedModels = List[(String, String)]()

    for (path <- paths) {
      wait {
        try {
          App.app.open(path)
          App.app.saveOpenModel()
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
      Some(path)
    }
    def shouldSaveModelOfDifferingVersion(version: String): Boolean = true
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
    def shouldOpenModelOfDifferingArity(arity: Int,version: String): Boolean = false
    def shouldOpenModelOfLegacyVersion(version: String): Boolean = true
    def shouldOpenModelOfUnknownVersion(version: String): Boolean = false
  }
}
