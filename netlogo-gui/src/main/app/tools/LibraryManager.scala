// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.io.File
import java.net.URL
import java.nio.file.{ Files, Paths }
import javax.swing.{ DefaultListModel, ListModel }

import com.typesafe.config.{ Config, ConfigException, ConfigFactory, ConfigRenderOptions, ConfigValueFactory }

import org.nlogo.api.{ APIVersion, FileIO }
import org.nlogo.swing.ProgressListener
import org.nlogo.workspace.ExtensionManager

class LibraryManager(extManager: ExtensionManager, progressListener: ProgressListener) {

  private val allLibsName        = "libraries.conf"
  private val metadataURL        = new URL(s"https://raw.githubusercontent.com/NetLogo/NetLogo-Libraries/${APIVersion.version}/$allLibsName")
  private val bundledsConfig     = ConfigFactory.parseResources("system/bundled-libraries.conf")
  private val metadataFetcher    = new SwingUpdater(metadataURL, updateLists _, progressListener)
  private val userInstalledsPath = FileIO.perUserFile("installed-libraries.conf")
  private val extInstaller       = new ExtensionInstaller(extManager)
  private val extList            = new DefaultListModel[LibraryInfo]

  val allLibsPath = FileIO.perUserFile(allLibsName)

  private var initialLoading  = true

  if (!Files.exists(Paths.get(userInstalledsPath)))
    Files.createFile(Paths.get(userInstalledsPath))

  reloadMetadata()
  initialLoading = false

  def getExtList: ListModel[LibraryInfo] = extList

  def installExtension(ext: LibraryInfo): Unit = {
    extInstaller.install(ext)
    updateInstalledVersion("extensions", ext)
  }

  def uninstallExtension(ext: LibraryInfo): Unit = {
    extInstaller.uninstall(ext)
    updateInstalledVersion("extensions", ext, uninstall = true)
  }

  def reloadMetadata() = updateLists(new File(allLibsName))
  def updateMetadataFromRemote() = metadataFetcher.reload()

  private def updateLists(configFile: File): Unit = {
    if (configFile.exists) {
      try {

        val config = ConfigFactory.parseFile(configFile)
        val installedLibsConf =
          ConfigFactory.parseFile(new File(userInstalledsPath))
            .withFallback(bundledsConfig)

        updateList(config, installedLibsConf, "extensions", extList)

      } catch {
        case ex: ConfigException =>
          if (initialLoading)
            // In case only the local copy got messed up somehow -- EL 2018-06-02
            metadataFetcher.invalidateCache()
          else
            throw new MetadataLoadingException(ex)
      }
    } else {
      metadataFetcher.invalidateCache()
    }
  }

  private def updateList(config: Config, installedLibsConf: Config, category: String, listModel: DefaultListModel[LibraryInfo]) = {

    import scala.collection.JavaConverters._

    val configList = config.getConfigList(category).asScala

    listModel.clear()
    listModel.ensureCapacity(configList.length)

    configList foreach { c =>

      val name        = c.getString("name")
      val codeName    = if (c.hasPath("codeName")) c.getString("codeName") else name.toLowerCase
      val shortDesc   = c.getString("shortDescription")
      val longDesc    = c.getString("longDescription")
      val version     = c.getString("version")
      val homepage    = new URL(c.getString("homepage"))
      val downloadURL = new URL(c.getString("downloadURL"))

      val installedVersionPath = s"""$category."$codeName".installedVersion"""
      val bundled              = bundledsConfig.hasPath(installedVersionPath)

      val status =
        if (!installedLibsConf.hasPath(installedVersionPath))
          LibraryStatus.CanInstall
        else if (installedLibsConf.getString(installedVersionPath) == version)
          LibraryStatus.UpToDate
        else
          LibraryStatus.CanUpdate

      listModel.addElement(
        LibraryInfo(name, codeName, shortDesc, longDesc, version, homepage, downloadURL, bundled, status)
      )

    }

  }

  private def updateInstalledVersion(category: String, lib: LibraryInfo, uninstall: Boolean = false) = synchronized {

    val userInstalleds = ConfigFactory.parseFile(new File(userInstalledsPath))

    val updatedInstalleds =
      if (uninstall)
        userInstalleds.withoutPath(s"""$category."${lib.codeName}"""")
      else
        userInstalleds.withValue(
          s"""$category."${lib.codeName}".installedVersion"""
        , ConfigValueFactory.fromAnyRef(lib.version)
        )

    val renderOpts    = ConfigRenderOptions.defaults.setOriginComments(false)
    val newInstalleds = updatedInstalleds.root.render(renderOpts)
    FileIO.writeFile(userInstalledsPath, newInstalleds, false)

  }

}

class MetadataLoadingException(cause: Throwable) extends RuntimeException(cause)
