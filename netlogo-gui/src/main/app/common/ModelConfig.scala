// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.io.{ File, IOException, PrintWriter }
import java.nio.file.{ Files, Path, Paths }
import java.text.SimpleDateFormat
import java.util.{ Date, Locale }

import org.nlogo.api.ModelReader
import org.nlogo.app.common.CommandLine.ExecutionString
import org.nlogo.core.AgentKind

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
      val source = Source.fromFile(file, "UTF-8")
      val result = Option(source.getLines.next.trim)

      source.close()

      result
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
      val lastModified = new File(modelPath).lastModified

      Files.list(autosaves).toScala(Seq).maxByOption(_.toFile.lastModified)
           .filter(_.toFile.lastModified > lastModified)
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

  // discard all autosave files for the current model that are newer than its most recent manual save (Isaac B 7/1/25)
  def discardNewAutoSaves(modelPath: String): Unit = {
    val autosaves = getModelConfigPath(modelPath).resolve("autosaves")

    if (autosaves.toFile.exists) {
      val lastModified = new File(modelPath).lastModified

      Files.list(autosaves).toScala(Seq).filter(_.toFile.lastModified > lastModified).foreach { path =>
        try {
          path.toFile.delete()
        } catch {
          case e: IOException =>
        }
      }
    }
  }

  // for each tracked model, including the empty/new model, get rid of any autosaves that
  // are older than 10 versions. if the model hasn't been modified in a while, get rid of
  // the whole config directory instead. (Isaac B 7/1/25, 12/13/25)
  def pruneModelConfigs(): Unit = {
    val configDir = Paths.get(System.getProperty("user.home"), ".nlogo", "modelConfigs")

    if (Files.exists(configDir)) {
      Files.list(configDir).toScala(Seq).foreach { configPath =>
        val lastModified: Array[Long] = listRecursive(configPath.toFile).map(_.lastModified)

        // if not modified in more than 30 days
        if (lastModified.nonEmpty && System.currentTimeMillis - lastModified.max > 2592000000L) {
          try {
            deleteRecursive(configPath.toFile)
          } catch {
            case e: IOException =>
          }
        } else {
          val autosaves = configPath.resolve("autosaves")

          if (autosaves.toFile.exists) {
            Files.list(autosaves).toScala(Seq).sortBy(-_.toFile.lastModified).drop(10).foreach { path =>
              try {
                Files.delete(path)
              } catch {
                case e: IOException =>
              }
            }
          }
        }
      }
    }

    val autosaveRegex = s"^autosave_\\d\\d\\d\\d-\\d\\d-\\d\\d.\\d\\d_\\d\\d_\\d\\d.${ModelReader.modelSuffix}$$".r

    Files.list(Paths.get(System.getProperty("java.io.tmpdir"))).toScala(Seq).filter { path =>
      autosaveRegex.matches(path.getFileName.toString)
    }.sortBy(-_.toFile.lastModified).drop(10).foreach { path =>
      try {
        path.toFile.delete()
      } catch {
        case e: IOException =>
      }
    }
  }

  def getCommandHistory(modelPath: String): Seq[ExecutionString] = {
    val history = getModelConfigPath(modelPath).resolve("commandHistory.txt").toFile

    if (history.exists) {
      val source = Source.fromFile(history, "UTF-8")
      val commands = source.getLines.collect {
        _.split(">", 2) match {
          case Array("O", command) => ExecutionString(AgentKind.Observer, command)
          case Array("T", command) => ExecutionString(AgentKind.Turtle, command)
          case Array("P", command) => ExecutionString(AgentKind.Patch, command)
          case Array("L", command) => ExecutionString(AgentKind.Link, command)
        }
      }.toSeq

      source.close()

      commands
    } else {
      Seq()
    }
  }

  def updateCommandHistory(modelPath: String, commands: Seq[ExecutionString]): Unit = {
    val history = getModelConfigPath(modelPath).resolve("commandHistory.txt").toFile

    if (!history.exists)
      history.getParentFile.mkdirs()

    new PrintWriter(history) {
      commands.foreach { exec =>
        exec.agentClass match {
          case AgentKind.Observer => this.println(s"O>${exec.string}")
          case AgentKind.Turtle => this.println(s"T>${exec.string}")
          case AgentKind.Patch => this.println(s"P>${exec.string}")
          case AgentKind.Link => this.println(s"L>${exec.string}")
        }
      }
    }.close()
  }

  private def listRecursive(file: File): Array[File] = {
    if (file.isDirectory) {
      file.listFiles.flatMap(listRecursive)
    } else {
      Array(file)
    }
  }

  private def deleteRecursive(file: File): Unit = {
    if (file.isDirectory) {
      file.listFiles.foreach(deleteRecursive)
      file.delete()
    } else {
      file.delete()
    }
  }
}
