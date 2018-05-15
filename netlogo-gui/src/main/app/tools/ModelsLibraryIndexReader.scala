// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

// This is a little piece of ModelsLibraryDialog I wanted to write in Scala without having to
// convert the whole thing to Scala. - ST 2/27/11

import java.io.File
import java.util.{ ArrayList => JArrayList, List => JList }

import com.typesafe.config.{ Config, ConfigException, ConfigFactory, ConfigParseOptions, ConfigSyntax }

import org.nlogo.workspace.ModelsLibrary

object ModelsLibraryIndexReader {
  def readInfoMap: Map[String, String] = {
    import scala.collection.JavaConverters._

    val parsingConfiguration = ConfigParseOptions.defaults.setSyntax(ConfigSyntax.CONF)
    val index = ConfigFactory.parseFile(new File(ModelsLibrary.modelsRoot, "index.conf"), parsingConfiguration)
    val indexItems: JList[_ <: Config] =
      try {
        index.getConfigList("models.indexes")
      } catch {
        case missing: ConfigException.Missing =>
          new JArrayList[Config]()
      }

    indexItems.asScala.foldLeft(Map.empty[String, String]) {
      case (acc: Map[String, String], c: Config) =>
        try {
          val modelPath = c.getString("path")
          val modelInfo = c.getString("info")
          acc + (modelPath -> modelInfo)
        } catch {
          case missing: ConfigException.Missing => acc
        }
    }
  }
}
