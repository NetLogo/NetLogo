// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.io.File
import java.net.URL
import javax.swing.{ DefaultListModel, ListModel }

import com.typesafe.config.{ Config, ConfigException, ConfigFactory }

import org.nlogo.api.APIVersion
import org.nlogo.swing.{ ProgressListener, SwingUpdater }

object LibraryManager {
  private val ConfigFilename = "libraries.conf"
  private val MetadataURL = new URL(s"https://raw.githubusercontent.com/NetLogo/NetLogo-Libraries/${APIVersion.version}/$ConfigFilename")
}

class LibraryManager(categories: Map[String, (String, URL) => Unit], progressListener: ProgressListener) {
  import LibraryManager._

  private val categoryNames = categories.keys
  private val lists = categoryNames.map(c => c -> new DefaultListModel[LibraryInfo]).toMap
  val listModels: Map[String, ListModel[LibraryInfo]] = lists

  private val metadataFetcher = new SwingUpdater(MetadataURL, updateLists _, progressListener)
  private var initialLoading = true

  updateLists(new File(ConfigFilename))
  initialLoading = false
  updateMetadataFromGithub()

  def installer(categoryName: String) = categories(categoryName)

  def updateMetadataFromGithub() = metadataFetcher.reload()

  private def updateLists(configFile: File): Unit = {
    if (configFile.exists) {
      val config = ConfigFactory.parseFile(configFile)
      categoryNames.foreach(c => updateList(config, c, lists(c)))
    }
  }

  private def updateList(config: Config, category: String, listModel: DefaultListModel[LibraryInfo]) = {
    try {
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
    } catch {
      case ex: ConfigException =>
        if (initialLoading)
          // In case only the local copy got messed up somehow -- EL 2018-06-02
          metadataFetcher.invalidateCache()
        else
          throw new MetadataLoadingException(ex)
    }
  }
}

class MetadataLoadingException(cause: Throwable = null) extends RuntimeException(cause)
