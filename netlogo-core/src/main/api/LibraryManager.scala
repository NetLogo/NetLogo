// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.File
import java.net.URL
import java.nio.file.{ Files, FileAlreadyExistsException, Path, Paths }

import com.typesafe.config.{ Config, ConfigException, ConfigFactory, ConfigRenderOptions, ConfigValueFactory }

import org.nlogo.core.LibraryInfo
import org.nlogo.core.{ LibraryManager => CoreLibraryManager }

object LibraryManager {
  private val libsLocationSite = "https://ccl.northwestern.edu/netlogo/config"
  private val libsLocation     = "libraries-location.conf"
  private val allLibsName      = "libraries.conf"
  private val bundledsConfig   = ConfigFactory.parseResources("system/bundled-libraries.conf")
  private val metadataURL      = getMetadataURL()

  private def getMetadataURL(): URL = {
    val locationURL    = new URL(s"$libsLocationSite/$libsLocation")
    LibraryInfoDownloader(locationURL)
    val locationPath   = FileIO.perUserFile(libsLocation)
    val locationConfig = ConfigFactory.parseFile(new File(locationPath))
    val location       = try {
      locationConfig.getString("location")
    } catch {
      case ex: ConfigException => bundledsConfig.getString("fallback-libraries-location")
    }
    new URL(s"$location/${APIVersion.version}/$allLibsName")
  }

  private var loadedOnce = false

  private def reloadMetadata(isFirstLoad: Boolean): Unit = {
    // If not first load (user clicked a button) or metadata not loaded once, load it!
    // This is an attempt to avoid multiple redundant remote fetches during test runs.
    // -JeremyB April 2019
    if (!isFirstLoad || !loadedOnce) {
      LibraryInfoDownloader.invalidateCache(metadataURL)
      LibraryInfoDownloader(metadataURL)
      loadedOnce = true
    }
  }
}

class LibraryManager(userExtPath: Path, unloadExtensions: () => Unit) extends CoreLibraryManager {

  import LibraryManager.{ allLibsName, bundledsConfig }

  private type InfoChangeCallback = Seq[LibraryInfo] => Unit

  private val userInstalledsPath = FileIO.perUserFile("installed-libraries.conf")
  private val extInstaller       = new ExtensionInstaller(userExtPath, unloadExtensions)

  private var libraries           = Seq[       LibraryInfo]()
  private var infoChangeCallbacks = Seq[InfoChangeCallback]()

  val allLibsPath = FileIO.perUserFile(allLibsName)
  val metadataURL = LibraryManager.metadataURL

  if (!Files.exists(Paths.get(userInstalledsPath)))
    // Concurrency during testing can cause the above check to be true but the exception to still be thrown,
    // so ignore it if it happens. -Jeremy B April 2019
    try {
      Files.createFile(Paths.get(userInstalledsPath))
    } catch {
      case _: FileAlreadyExistsException =>
    }

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

  def reloadMetadata(isFirstLoad: Boolean = false, useBundled: Boolean = true): Unit = {
    LibraryManager.reloadMetadata(isFirstLoad)
    updateLists(new File(allLibsPath), isFirstLoad, useBundled)
  }

  def onLibInfoChange(callback: InfoChangeCallback): Unit = {
    infoChangeCallbacks = infoChangeCallbacks :+ callback
  }

  def updateLists(configFile: File, isFirstLoad: Boolean = false, useBundled: Boolean = true): Unit = {

    try {

      val config = ConfigFactory.parseFile(configFile)
      val installedLibsConf = if (useBundled)
        ConfigFactory.parseFile(new File(userInstalledsPath)).withFallback(bundledsConfig)
      else
        ConfigFactory.parseFile(new File(userInstalledsPath))

      updateList(config, installedLibsConf, "extensions", useBundled)

    } catch {
      case ex: ConfigException =>
        if (isFirstLoad)
          // In case only the local copy got messed up somehow -- EL 2018-06-02
          LibraryInfoDownloader.invalidateCache(metadataURL)
        else
          throw new MetadataLoadingException(ex)
    }

  }

  private def updateList(config: Config, installedLibsConf: Config, category: String, useBundled: Boolean) = {

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
          val bundled              = useBundled && bundledsConfig.hasPath(installedVersionPath)
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
