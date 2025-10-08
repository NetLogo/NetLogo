// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.File
import java.net.URL
import java.nio.file.{ Files, FileAlreadyExistsException, Path, Paths }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

import com.typesafe.config.{ Config, ConfigException, ConfigFactory, ConfigRenderOptions, ConfigValueFactory }

import org.nlogo.core.LibraryInfo
import org.nlogo.core.{ LibraryManager => CoreLibraryManager }

import scala.concurrent.ExecutionContext.Implicits.global

object LibraryManager {

  private val libsLocationSite = "https://ccl.northwestern.edu/netlogo/config"
  private val libsLocation     = "libraries-location.conf"
  private val allLibsName      = "libraries.conf"
  private val bundledsConfig   = ConfigFactory.parseResources("system/bundled-libraries.conf")

  private val branchURLFuture = {

    val baseURLOptF =
      if (LibraryInfoDownloader.enabled)
        LibraryInfoDownloader(new URL(s"$libsLocationSite/$libsLocation")).map(
          _.flatMap {
            case (file, _) =>
              Try(ConfigFactory.parseFile(file).getString("location")).toOption
          }
        )
      else
        Future(None)

    baseURLOptF.map {
      opt =>
        val baseURL = opt.getOrElse(bundledsConfig.getString("fallback-libraries-location"))
        val url = new URL(s"$baseURL/refs/heads/${APIVersion.version}")
        url
    }

  }

  private val metadataURLFuture: Future[URL] = branchURLFuture.map(url => new URL(s"$url/$allLibsName"))

  private var loadedOnce = false

  private def reloadMetadata(isFirstLoad: Boolean, complete: () => Unit): Future[Unit] = {
    // If not first load (user clicked a button) or metadata not loaded once, load it!
    // This is an attempt to avoid multiple redundant remote fetches during test runs.
    // -JeremyB April 2019
    if (!isFirstLoad || !loadedOnce) {
      metadataURLFuture.map {
        metadataURL =>
          LibraryInfoDownloader.invalidateCache(metadataURL)
          LibraryInfoDownloader(metadataURL).foreach {
            case Some(file, rewrite) if rewrite => complete()
            case _                              => ()
          }
          loadedOnce = true
      }
    } else {
      Future(complete())
    }
  }
}

class LibraryManager(userExtPath: Path, unloadExtensions: () => Unit) extends CoreLibraryManager {

  import LibraryManager.{ allLibsName, bundledsConfig }

  private type InfoChangeCallback = Seq[LibraryInfo] => Unit

  private val userInstalledsPath = FileIO.perUserExtensionFile("installed-libraries.conf").toString
  private val extInstaller       = new ExtensionInstaller(userExtPath, unloadExtensions)

  private var libraries           = Seq[       LibraryInfo]()
  private var infoChangeCallbacks = Seq[InfoChangeCallback]()

  val allLibsPath = FileIO.perUserExtensionFile(allLibsName).toString

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
    if (LibraryInfoDownloader.enabled) {
      extInstaller.install(ext)
      updateInstalledVersion("extensions", ext)
    }
  }

  def uninstallExtension(ext: LibraryInfo): Unit = {
    if (LibraryInfoDownloader.enabled) {
      extInstaller.uninstall(ext)
      updateInstalledVersion("extensions", ext, uninstall = true)
    }
  }

  override def reloadMetadata(): Unit = reloadMetadata(false)

  def reloadMetadata(isFirstLoad: Boolean = false, useBundled: Boolean = true): Unit = {
    LibraryManager.reloadMetadata(isFirstLoad, () => updateLists(new File(allLibsPath), isFirstLoad, useBundled))
  }

  def onLibInfoChange(callback: InfoChangeCallback): Unit = {
    infoChangeCallbacks = infoChangeCallbacks :+ callback
  }

  def updateMetadata(): Future[Unit] = {
    LibraryManager.metadataURLFuture.map {
      metadataURL =>
        LibraryInfoDownloader(metadataURL).flatMap {
          _.flatMap {
            case (file, true) => Option(updateLists(file))
            case _            => None
          }.getOrElse(Future.unit)
        }
    }
  }

  def updateLists(configFile: File, isFirstLoad: Boolean = false, useBundled: Boolean = true): Future[Unit] = {

    try {

      val config = ConfigFactory.parseFile(configFile)
      val installedLibsConf = if (useBundled)
        ConfigFactory.parseFile(new File(userInstalledsPath)).withFallback(bundledsConfig)
      else
        ConfigFactory.parseFile(new File(userInstalledsPath))

      Future(updateList(config, installedLibsConf, "extensions", useBundled))

    } catch {
      case ex: ConfigException =>
        if (isFirstLoad)
          // In case only the local copy got messed up somehow -- EL 2018-06-02
          LibraryManager.metadataURLFuture.map(url => LibraryInfoDownloader.invalidateCache(url))
        else
          throw new MetadataLoadingException(ex)
    }

  }

  private def updateList(config: Config, installedLibsConf: Config, category: String, useBundled: Boolean): Future[Unit] = {

    import scala.jdk.CollectionConverters.ListHasAsScala

    def getStringOption(c: Config, path: String, default: Option[String] = None): Option[String] =
      if (c.hasPath(path)) Option(c.getString(path)) else default

    LibraryManager.branchURLFuture.map {
      branchURL =>

        libraries =
          config.getConfigList(category).asScala.map {
            c =>

              val name        = c.getString("name")
              val codeName    = c.getString("codeName")
              val shortDesc   = c.getString("shortDescription")
              val longDesc    = c.getString("longDescription")
              val version     = c.getString("version")
              val homepage    = new URL(c.getString("homepage"))

              val installedVersionPath = s"""$category."$codeName".installedVersion"""
              val installedVersion     = getStringOption(installedLibsConf, installedVersionPath)
              val bundled              = useBundled && bundledsConfig.hasPath(installedVersionPath) && installedVersion.isEmpty
              val minNetLogoVersion    = getStringOption(c, "minNetLogoVersion")

              LibraryInfo(name, codeName, shortDesc, longDesc, version, homepage, bundled, installedVersion,
                          minNetLogoVersion, branchURL)

          }.toSeq

        infoChangeCallbacks.foreach(_.apply(libraries))

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

    updateLists(new File(allLibsPath), false)

  }

}

class DummyLibraryManager extends LibraryManager(ExtensionManager.userExtensionsPath, () => ())

class MetadataLoadingException(cause: Throwable) extends RuntimeException(cause)
