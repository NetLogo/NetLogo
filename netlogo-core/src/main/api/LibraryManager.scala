// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.File
import java.net.URL
import java.nio.file.{ Files, Path, Paths }

import com.typesafe.config.{ Config, ConfigException, ConfigFactory, ConfigRenderOptions, ConfigValueFactory }

import org.nlogo.core.LibraryInfo
import org.nlogo.core.{ LibraryManager => CoreLibraryManager }

class LibraryManager(userExtPath: Path, unloadExtensions: () => Unit) extends CoreLibraryManager {

  private type InfoChangeCallback = Seq[LibraryInfo] => Unit

  private val allLibsName        = "libraries.conf"
  private val bundledsConfig     = ConfigFactory.parseResources("system/bundled-libraries.conf")
  private val userInstalledsPath = FileIO.perUserFile("installed-libraries.conf")
  private val extInstaller       = new ExtensionInstaller(userExtPath, unloadExtensions)

  private var libraries           = Seq[       LibraryInfo]()
  private var infoChangeCallbacks = Seq[InfoChangeCallback]()

  val allLibsPath = FileIO.perUserFile(allLibsName)
  val metadataURL = new URL(s"https://raw.githubusercontent.com/NetLogo/NetLogo-Libraries/${APIVersion.version}/$allLibsName")

  if (!Files.exists(Paths.get(userInstalledsPath)))
    Files.createFile(Paths.get(userInstalledsPath))

  reloadMetadata(true)

  def getExtensionInfos = libraries

  override def lookupExtension(name: String, version: String): Option[LibraryInfo] =
    libraries.find(ext => ext.codeName == name)

  override def installExtension(ext: LibraryInfo): Unit = {
    extInstaller.install(ext)
    updateInstalledVersion("extensions", ext)
  }

  def uninstallExtension(ext: LibraryInfo): Unit = {
    extInstaller.uninstall(ext)
    updateInstalledVersion("extensions", ext, uninstall = true)
  }

  override def reloadMetadata(): Unit = reloadMetadata(false)

  def reloadMetadata(isFirstLoad: Boolean = false): Unit = {
    LibraryInfoDownloader.invalidateCache(metadataURL)
    LibraryInfoDownloader(metadataURL)
    updateLists(new File(allLibsPath), isFirstLoad)
  }

  def onLibInfoChange(callback: InfoChangeCallback): Unit = {
    infoChangeCallbacks = infoChangeCallbacks :+ callback
  }

  def updateLists(configFile: File, isFirstLoad: Boolean = false): Unit = {

    try {

      val config = ConfigFactory.parseFile(configFile)
      val installedLibsConf =
        ConfigFactory.parseFile(new File(userInstalledsPath)).withFallback(bundledsConfig)

      updateList(config, installedLibsConf, "extensions")

    } catch {
      case ex: ConfigException =>
        if (isFirstLoad)
          // In case only the local copy got messed up somehow -- EL 2018-06-02
          LibraryInfoDownloader.invalidateCache(metadataURL)
        else
          throw new MetadataLoadingException(ex)
    }

  }

  private def updateList(config: Config, installedLibsConf: Config, category: String) = {

    import scala.collection.JavaConverters._

    libraries =
      config.getConfigList(category).asScala.map {
        c =>

          val name        = c.getString("name")
          val codeName    = c.getString("codeName")
          val shortDesc   = c.getString("shortDescription")
          val longDesc    = c.getString("longDescription")
          val version     = c.getString("version")
          val homepage    = new URL(c.getString("homepage"))
          val downloadURL = new URL(c.getString("downloadURL"))

          val installedVersionPath = s"""$category."$codeName".installedVersion"""
          val bundled              = bundledsConfig.hasPath(installedVersionPath)
          val installedVersion     =
            if (!installedLibsConf.hasPath(installedVersionPath))
              None
            else
              Option(installedLibsConf.getString(installedVersionPath))

          LibraryInfo(name, codeName, shortDesc, longDesc, version, homepage, downloadURL, bundled, installedVersion)

      }

    infoChangeCallbacks.foreach(_.apply(libraries))

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

    updateLists(new File(allLibsPath), false)

  }

}

class DummyLibraryManager extends LibraryManager(ExtensionManager.userExtensionsPath, () => ())

class MetadataLoadingException(cause: Throwable) extends RuntimeException(cause)
