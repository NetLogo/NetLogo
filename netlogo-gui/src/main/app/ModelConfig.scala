// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.io.{ File, IOException, PrintWriter }
import java.nio.file.{ Files, Path, Paths }
import java.text.SimpleDateFormat
import java.util.{ Date, Locale }

import org.nlogo.api.ModelReader

import scala.io.Source
import scala.jdk.StreamConverters.StreamHasToScala
import scala.util.hashing.MurmurHash3

// helpers for cross-session model configuration settings (Isaac B 4/21/25)
object ModelConfig {
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd.HH_mm_ss", Locale.US)

  def getModelConfigPath(modelPath: String): Path = {
    Paths.get(System.getProperty("user.home"), ".nlogo", "modelConfigs",
              (MurmurHash3.stringHash(modelPath) & Int.MaxValue).toString)
  }

  def getLastModified(modelPath: String): Option[String] = {
    val file = getModelConfigPath(modelPath).resolve("lastModified.txt").toFile

    if (file.exists) {
      Option(Source.fromFile(file).getLines().next().trim)
    } else {
      None
    }
  }

  def setLastModified(modelPath: String): Unit = {
    val file = getModelConfigPath(modelPath).resolve("lastModified.txt").toFile

    if (!file.exists)
      file.getParentFile.mkdirs()

    new PrintWriter(file) {
      this.println(new File(modelPath).lastModified)
    }.close()
  }

  def findAutoSave(modelPath: String): Option[Path] = {
    val autosaves = getModelConfigPath(modelPath).resolve("autosaves")

    if (autosaves.toFile.exists) {
      Files.list(autosaves).toScala(Seq).maxByOption(_.toFile.lastModified)
           .filter(_.toFile.lastModified > new File(modelPath).lastModified)
    } else {
      None
    }
  }

  def getAutoSavePath(modelPath: Option[String]): Path = {
    val name = s"autosave_${dateFormat.format(new Date)}"

    modelPath match {
      case Some(path) =>
        val save = getModelConfigPath(path).resolve(s"autosaves/$name.${path.split('.').last}")

        if (!save.toFile.exists)
          save.toFile.getParentFile.mkdirs()

        save

      case _ =>
        Paths.get(System.getProperty("java.io.tmpdir"), s"$name.${ModelReader.modelSuffix}")
    }
  }

  def pruneAutoSaves(modelPath: String): Unit = {
    val autosaves = getModelConfigPath(modelPath).resolve("autosaves")

    if (autosaves.toFile.exists) {
      Files.list(autosaves).toScala(Seq).sortBy(-_.toFile.lastModified).drop(10).foreach { path =>
        try {
          path.toFile.delete()
        } catch {
          case e: IOException =>
        }
      }
    }
  }
}
