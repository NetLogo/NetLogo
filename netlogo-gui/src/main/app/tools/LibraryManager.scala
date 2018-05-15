// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.io.File
import java.net.{ HttpURLConnection, URL }
import java.nio.file.{ Files, Paths, StandardCopyOption }
import javax.swing.{ DefaultListModel, ListModel, SwingWorker }

import com.typesafe.config.{ Config, ConfigFactory }

import org.nlogo.api.APIVersion

object LibraryManager {
  private val configFilename = "libraries.conf"
}

class LibraryManager(categories: String*) {
  import LibraryManager._

  private val lists = categories.map(c => c -> new DefaultListModel[LibraryInfo]).toMap
  val listModels: Map[String, ListModel[LibraryInfo]] = lists

  updateLists()
  new MetadataFetcher().execute()

  private def updateLists(): Unit = {
    val configFile = new File(configFilename)
    if (configFile.exists) {
      val config = ConfigFactory.parseFile(configFile)
      categories.foreach(c => updateList(config, c, lists(c)))
    }
  }

  private def updateList(config: Config, category: String, listModel: DefaultListModel[LibraryInfo]) = {
    if (config.hasPath(category)) {
      import scala.collection.JavaConverters._

      val configList = config.getConfigList(category).asScala
      listModel.clear()
      listModel.ensureCapacity(configList.length)
      configList foreach { c =>
        val name        = c.getString("name")
        val shortDesc   = c.getString("shortDescription")
        val longDesc    = c.getString("longDescription")
        val homepage    = new URL(c.getString("homepage"))
        val downloadURL = new URL(c.getString("downloadURL"))
        val status = LibraryStatus.CanInstall

        listModel.addElement(
          LibraryInfo(name, shortDesc, longDesc, homepage, downloadURL, status))
      }
    }
  }

  private class MetadataFetcher extends SwingWorker[Any, Any] {
    override def doInBackground(): Unit = {
      val metadataURL = s"https://raw.githubusercontent.com/NetLogo/NetLogo-Libraries/${APIVersion.version}/$configFilename"
      val conn = new URL(metadataURL).openConnection.asInstanceOf[HttpURLConnection]
      if (conn.getResponseCode == 200)
        Files.copy(conn.getInputStream, Paths.get(configFilename), StandardCopyOption.REPLACE_EXISTING)
    }

    override def done(): Unit = updateLists()
  }
}
