// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.io.File
import java.net.{ HttpURLConnection, URL }
import java.nio.file.{ Files, Paths, StandardCopyOption }
import javax.swing.{ AbstractListModel, SwingWorker }

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigFactory

import org.nlogo.api.APIVersion

object LibraryManager {
  private val configFilename = "libraries.conf"
}

class LibraryManager extends AbstractListModel[ExtensionInfo] {
  import LibraryManager._

  private var extensions = Seq.empty[ExtensionInfo]

  updateExtensionsList()
  new LibrariesListUpdater().execute()

  private def updateExtensionsList(): Unit = {
    val configFile = new File(configFilename)
    if (configFile.exists) {
      val config = ConfigFactory.parseFile(configFile)
      if (config.hasPath("extensions")) {
        fireIntervalRemoved(this, 0, extensions.length)
        extensions = config.getConfigList("extensions").asScala map { extensionConfig =>
          val name        = extensionConfig.getString("name")
          val shortDesc   = extensionConfig.getString("shortDescription")
          val longDesc    = extensionConfig.getString("longDescription")
          val homepage    = new URL(extensionConfig.getString("homepage"))
          val downloadURL = new URL(extensionConfig.getString("downloadURL"))
          val status = ExtensionStatus.CanInstall

          ExtensionInfo(name, shortDesc, longDesc, homepage, downloadURL, status)
        }
        fireIntervalAdded(this, 0, extensions.length)
      }
    }
  }

  override def getElementAt(i: Int) = extensions(i)
  override def getSize = extensions.length

  class LibrariesListUpdater extends SwingWorker[Any, Any] {
    override def doInBackground(): Unit = {
      val metadataURL = s"https://raw.githubusercontent.com/NetLogo/NetLogo-Libraries/${APIVersion.version}/$configFilename"
      val conn = new URL(metadataURL).openConnection.asInstanceOf[HttpURLConnection]
      if (conn.getResponseCode == 200)
        Files.copy(conn.getInputStream, Paths.get(configFilename), StandardCopyOption.REPLACE_EXISTING)
    }

    override def done(): Unit = updateExtensionsList()
  }
}
