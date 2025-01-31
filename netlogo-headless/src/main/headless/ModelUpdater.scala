package org.nlogo.headless

import java.nio.file.{ Files, Paths }

import org.nlogo.api.Version
import org.nlogo.core.{ Femto, LiteralParser }
import org.nlogo.fileformat.FileFormat
import org.nlogo.workspace.ModelsLibrary

import scala.collection.JavaConverters._

// updates models in the old .nlogo format to the new .nlogox format
// also resaves models that are already in the .nlogox format
object ModelUpdater {
  private val loader = FileFormat.standardAnyLoader(
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities"))

  private def resaveModel(oldPath: String, newPath: String) {
    loader.readModel(Paths.get(oldPath).toUri).foreach(model => {
      println("Resaving \"" + oldPath + "\" as \"" + newPath + "\"")

      loader.save(model.copy(version = Version.version), Paths.get(newPath).toUri)

      if (oldPath != newPath)
        Files.delete(Paths.get(oldPath))
    })
  }

  def run() {
    Files.walk(Paths.get(ModelsLibrary.modelsRoot)).iterator.asScala.toArray.foreach(path => {
      if (Files.isRegularFile(path)) {
        if (path.toString.endsWith(".nlogo") || path.toString.endsWith(".nlogo3d"))
          resaveModel(path.toString, "(.nlogo3d|.nlogo)$".r.replaceFirstIn(path.toString, ".nlogox"))
        else if (path.toString.endsWith(".nlogox"))
          resaveModel(path.toString, path.toString)
      }
    })
  }
}
