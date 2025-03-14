package org.nlogo.tools

import java.io.File
import java.nio.file.{ Files, Paths }
import javax.imageio.ImageIO

import org.nlogo.api.{ ModelType, Version }
import org.nlogo.app.App
import org.nlogo.awt.EventQueue
import org.nlogo.workspace.ModelsLibrary

import scala.collection.JavaConverters.asScalaIteratorConverter

object ExportResizedModels {
  def main(args: Array[String]): Unit = {
    val extension = if (Version.is3D) ".nlogox3d" else ".nlogox"

    App.main(Array())

    val fileManager = App.app.fileManager
    val interfacePanel = App.app.tabManager.interfaceTab.iP

    for (path <- Files.walk(Paths.get(ModelsLibrary.modelsRoot)).iterator.asScala) {
      if (path.toString.endsWith(extension)) {
        EventQueue.invokeAndWait(() => {
          fileManager.openFromPath(path.toString, ModelType.Library)

          val file = new File(path.toString.stripSuffix(extension) + " (old).png")

          ImageIO.write(interfacePanel.interfaceImage, "png", file)

          interfacePanel.convertWidgetSizes(true)
        })

        Thread.sleep(1000)

        val file = new File(path.toString.stripSuffix(extension) + " (new).png")

        EventQueue.invokeAndWait(() => {
          ImageIO.write(interfacePanel.interfaceImage, "png", file)
        })
      }
    }
  }
}
