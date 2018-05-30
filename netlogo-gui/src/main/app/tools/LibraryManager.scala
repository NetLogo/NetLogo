// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.io.File
import java.net.{ HttpURLConnection, URL }
import java.nio.file.{ Files, Paths, StandardCopyOption }
import java.security.{ DigestInputStream, MessageDigest }
import java.util.Arrays
import java.util.prefs.{ Preferences => JavaPreferences }
import javax.swing.{ DefaultListModel, ListModel, SwingWorker }

import com.typesafe.config.{ Config, ConfigException, ConfigFactory }

import org.nlogo.api.APIVersion

object LibraryManager {
  private val configFilename = "libraries.conf"
  private val prefs = JavaPreferences.userNodeForPackage(getClass)
  private val hashKey = "metadata-md5"
}

class LibraryManager(categories: Map[String, (String, URL) => Unit]) {
  import LibraryManager._

  private val categoryNames = categories.keys
  private val lists = categoryNames.map(c => c -> new DefaultListModel[LibraryInfo]).toMap
  val listModels: Map[String, ListModel[LibraryInfo]] = lists

  private var initialLoading = true

  updateLists()
  initialLoading = false
  new MetadataFetcher().execute()

  def installer(categoryName: String) = categories(categoryName)

  private def updateLists(): Unit = {
    val configFile = new File(configFilename)
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
          // In case only the local file got messed up somehow. This line
          // ensures that we update the GUI according to the newly downloaded file
          prefs.put(hashKey, "")
        else
          throw new MetadataLoadingException(ex)
    }
  }

  private class MetadataFetcher extends SwingWorker[Any, Any] {
    private var changed = false

    override def doInBackground(): Unit = {
      val md = MessageDigest.getInstance("MD5")
      val metadataURL = s"https://raw.githubusercontent.com/NetLogo/NetLogo-Libraries/${APIVersion.version}/$configFilename"
      val conn = new URL(metadataURL).openConnection.asInstanceOf[HttpURLConnection]
      if (conn.getResponseCode == 200) {
        val response = new DigestInputStream(conn.getInputStream, md)
        Files.copy(response, Paths.get(configFilename), StandardCopyOption.REPLACE_EXISTING)
      }
      val localHash = prefs.getByteArray(hashKey, null)
      val newHash = md.digest
      if (!Arrays.equals(localHash, newHash)) {
        prefs.putByteArray(hashKey, newHash)
        changed = true
      }
    }

    override def done(): Unit = if (changed) updateLists()
  }
}

class MetadataLoadingException(cause: Throwable = null) extends RuntimeException(cause)
