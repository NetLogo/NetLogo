package org.nlogo.tools

import java.awt.EventQueue
import java.nio.file.{ Files, Paths }

import org.nlogo.api.{ ModelType, Version }
import org.nlogo.app.App
import org.nlogo.fileformat.FileFormat
import org.nlogo.workspace.ModelsLibrary

import scala.collection.JavaConverters.asScalaIteratorConverter

object ConvertWidgetSizes {
  def main(args: Array[String]): Unit = {
    val extension = if (Version.is3D) ".nlogox3d" else ".nlogox"

    App.main(Array())

    App.app.setIgnorePopups(true)

    val fileManager = App.app.fileManager
    val interfacePanel = App.app.tabManager.interfaceTab.iP

    val modelLoader = FileFormat.standardXMLLoader(false, App.app.workspace.compiler.utilities)

    for (path <- Files.walk(Paths.get(ModelsLibrary.modelsRoot)).iterator.asScala) {
      if (path.toString.endsWith(extension)) {
        EventQueue.invokeAndWait(() => {
          fileManager.openFromPath(path.toString, ModelType.Library)

          interfacePanel.convertWidgetSizes(true)

          modelLoader.save(fileManager.currentModel, path.toUri)
        })
      }
    }
  }
}
