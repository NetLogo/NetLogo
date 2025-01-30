package org.nlogo.headless

import java.nio.file.{ Files, Paths }

import org.nlogo.api.Version
import org.nlogo.core.{ Femto, LiteralParser }
import org.nlogo.fileformat.FileFormat
import org.nlogo.workspace.ModelsLibrary

import scala.collection.JavaConverters._

// updates models in the old .nlogo format to the new .nlogox format
object ModelUpdater {
  private val loader = FileFormat.standardAnyLoader(
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities"))

  private def resaveModel(path: String) {
    val newPath = "(.nlogo3d|.nlogo)$".r.replaceFirstIn(path, ".nlogox")

    loader.readModel(Paths.get(path).toUri).foreach(model => {
      println("Resaving \"" + path + "\" as \"" + newPath + "\"")

      loader.save(model.copy(version = Version.version), Paths.get(newPath).toUri)

      Files.delete(Paths.get(path))
    })
  }

  def run() {
    Files.walk(Paths.get(ModelsLibrary.modelsRoot)).iterator.asScala.toArray.foreach(path => {
      if (Files.isRegularFile(path) && (path.toString.endsWith(".nlogo") || path.toString.endsWith(".nlogo3d")))
        resaveModel(path.toString)
    })
  }
}
