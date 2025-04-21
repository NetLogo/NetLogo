// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.io.{ File, PrintWriter }
import java.nio.file.{ Path, Paths }

import scala.io.Source
import scala.util.hashing.MurmurHash3

// helpers for cross-session model configuration settings (Isaac B 4/21/25)
object ModelConfig {
  def getModelConfigPath(modelPath: String): Path =
    Paths.get(System.getProperty("user.home"), ".nlogo", "modelConfigs", MurmurHash3.stringHash(modelPath).toString)

  def getLastModified(modelPath: String): Option[String] = {
    val file = getModelConfigPath(modelPath).resolve("lastModified.txt").toFile

    if (file.exists) {
      Option(Source.fromFile(file).getLines.next.trim)
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
}
