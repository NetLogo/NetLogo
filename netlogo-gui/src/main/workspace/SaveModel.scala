// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.URI
import java.nio.file.Paths

import org.nlogo.core.Model
import org.nlogo.api.{ ModelLoader, ModelType, Version }

import scala.annotation.tailrec
import scala.util.Try

import SaveModel.Controller

// this returns a thunk so that it can be run on a background thread, if desired
trait SaveModel {
  def apply(model:  Model,
    loader:         ModelLoader,
    controller:     Controller,
    modelTracker:   ModelTracker,
    currentVersion: Version): Option[() => Try[URI]] = {
      val savePath =
        fileFromTracker(modelTracker) orElse validFilePath(controller, loader, modelTracker.getModelType)
      savePath.flatMap { path =>
        if (currentVersion.compatibleVersion(model.version) ||
          controller.shouldSaveModelOfDifferingVersion(model.version)) {
            Some({ () => loader.save(model.copy(version = currentVersion.version), path) })
          } else
            None
      }
  }

  protected def fileFromTracker(modelTracker: ModelTracker): Option[URI] = {
    if (modelTracker.getModelType == ModelType.Normal)
      modelTracker.getModelFileUri
    else
      None
  }

  @tailrec
  protected final def validFilePath(controller: Controller, loader: ModelLoader, modelType: ModelType): Option[URI] = {
    controller.chooseFilePath(modelType) match {
      case res@Some(uri) =>
        val extension = ModelLoader.getURIExtension(uri)
        if (extension.exists(ext => loader.formats.map(_.name).contains(ext)))
          res
        else {
          controller.warnInvalidFileFormat(extension.getOrElse(""))
          validFilePath(controller, loader, modelType)
        }
      case other => other
    }
  }
}

object SaveModel extends SaveModel {
  trait Controller {
    def chooseFilePath(modelType: ModelType): Option[URI]
    def shouldSaveModelOfDifferingVersion(version: String): Boolean
    def warnInvalidFileFormat(format: String): Unit
  }
}

object SaveModelAs extends SaveModel {

  override protected def fileFromTracker(modelTracker: ModelTracker): Option[URI] =
    None
}

