import sbt._

import java.io.{ IOException, File }
import java.nio.charset.Charset
import java.nio.file.{ Files, FileSystems, Path, Paths }
import java.util.Properties
import java.util.jar.JarFile

import scala.collection.JavaConverters.{ collectionAsScalaIterableConverter, mapAsJavaMapConverter }

import NetLogoPackaging.RunProcess

object PackageWinAggregate {
  sealed abstract trait PlatformVars {
    val platformArch: String
    val bitness: String
  }

  object PlatformVars {
    case object Vars32 extends PlatformVars {
      override val platformArch = "x86"
      override val bitness = "always32"
    }

    case object Vars64 extends PlatformVars {
      override val platformArch = "x64"
      override val bitness = "always64"
    }
  }

  val WiXPath = {
    val fs = FileSystems.getDefault
    val basePaths =
      Set(fs.getPath("C:", "Program Files (x86)"), fs.getPath("C:", "Program Files"))
        .map(_.toFile).filter(_.exists)
    basePaths.flatMap(_.listFiles).find(_.getName.contains("WiX")).getOrElse(
      sys.error("Could not find WiX installation, please ensure WiX is installed"))
  }

  def wixCommand(commandName: String) =
    WiXPath / "bin" / (commandName + ".exe")

  def generateUUIDs: Map[String, String] = {
    Seq("product",
        "upgrade",
        "NetLogoStartMenuShortcutId",
        "NetLogo3DStartMenuShortcutId",
        "HubNetClientStartMenuShortcutId",
        "NetLogoDesktopShortcutId",
        "NetLogo3DDesktopShortcutId",
        "HubNetClientDesktopShortcutId",
        "nlogoExecutableId", "nlogo3DExecutableId", "hubNetClientExecutableId",
        "behaviorSearchExecutableId",
        "behaviorSearchDesktopShortcutId",
        "behaviorSearchStartMenuShortcutId",
        "startMenuFolderId"
      ).flatMap(k => Seq(k + ".32", k + ".64")).map(k => k -> java.util.UUID.randomUUID.toString.toUpperCase).toMap
  }

  def archive(archiveFile: File, uuids: Map[String, String]) = {
    val properties = new Properties()
    uuids.foreach {
      case (k, v) => properties.setProperty(k, v)
    }
    val writer = Files.newBufferedWriter(archiveFile.toPath, Charset.forName("US-ASCII"))
    properties.store(writer, "")
    writer.flush()
    writer.close()
    println(s"writing new UUIDs to ${archiveFile.getPath}. Remember to check this into source control!")
  }

  def loadUUIDs(archiveFile: File): Map[String, String] = {
    val properties = new Properties()
    val reader = Files.newBufferedReader(archiveFile.toPath, Charset.forName("US-ASCII"))
    properties.load(reader)
    reader.close()
    properties.stringPropertyNames.asScala.map {
      case name => (name -> properties.getProperty(name))
    }.toMap
  }

  def apply(
    log: sbt.util.Logger
  , version: String
  , arch: String
  , configDir: File
  , appImageDir: File
  , targetDir: File
  , webDir: File
  , launchers: Seq[Launcher]
  ): File = {
    val platformConfigDir = configDir / "windows"

    log.info("Generating Windows UUIDs")
    val uuidArchiveFileName =
      version.replace("-", "").replace(".", "") + ".properties"
    val uuidArchiveFile = platformConfigDir / "archive" / uuidArchiveFileName
    val uuids =
      if (! uuidArchiveFile.exists) {
        val ids = generateUUIDs
        archive(uuidArchiveFile, ids)
        ids
      } else {
        log.info("loading UUIDs from: " + uuidArchiveFile.toString)
        loadUUIDs(uuidArchiveFile)
      }

    val archUUIDs = uuids.filter {
      case (k, _) => (arch == "64" && k.endsWith("64")) || (arch != "64" && k.endsWith("32"))
    }.map {
      case (k, v) => k.stripSuffix(".64").stripSuffix(".32") -> v
    }

    val platformVars: PlatformVars = {
      if (arch == "64") {
        PlatformVars.Vars64
      } else {
        PlatformVars.Vars32
      }
    }

    val iconDir = targetDir.getAbsolutePath
    val msiBuildDir = appImageDir.getParentFile

    log.info("Generating WiX config files")

    val componentConfig = Map(
      "upgradeCode" -> archUUIDs("upgrade"),
      "platformArch" -> platformVars.platformArch,
      "bitness" -> platformVars.bitness,
      "version" -> version,
      "iconDir" -> iconDir,
      "configDir" -> platformConfigDir,
      "product" -> archUUIDs("product"),
      "startMenuFolderId" -> archUUIDs("startMenuFolderId"),
      "components" -> Array(
        Map(
          "componentFriendlyName" -> "HubNet Client",
          "noSpaceName"           -> "HubNetClient",
          "componentId"           -> "HubNet_Client.exe",
          "componentFileName"     -> "HubNet Client.exe",
          "lowerDashName"         -> "hubnet-client",
          "componentGuid"         -> archUUIDs("hubNetClientExecutableId"),
          "desktopShortcutId"     -> archUUIDs("HubNetClientDesktopShortcutId"),
          "startMenuShortcutId"   -> archUUIDs("HubNetClientStartMenuShortcutId"),
          "hasFileAssociation"    -> Boolean.box(false)
        ).asJava,
        Map(
          "componentFriendlyName" -> "NetLogo",
          "noSpaceName"           -> "NetLogo",
          "componentId"           -> "NetLogo.exe",
          "componentFileName"     -> "NetLogo.exe",
          "lowerDashName"         -> "netlogo",
          "componentGuid"         -> archUUIDs("nlogoExecutableId"),
          "desktopShortcutId"     -> archUUIDs("NetLogoDesktopShortcutId"),
          "startMenuShortcutId"   -> archUUIDs("NetLogoStartMenuShortcutId"),
          "hasFileAssociation"    -> Boolean.box(true),
          "fileAssociations"      -> Array(
            Map(
              "extension" -> "nlogo",
              "icon"      -> "ModelOldIcon",
              "type"      -> "NetLogo Model"
            ).asJava,
            Map(
              "extension" -> "nlogox",
              "icon"      -> "ModelIcon",
              "type"      -> "NetLogo Model"
            ).asJava
          ),
          "launchArgs" -> """--launch "%1""""
        ).asJava,
        Map[String, AnyRef](
          "componentFriendlyName" -> "NetLogo 3D",
          "noSpaceName"           -> "NetLogo3D",
          "componentId"           -> "NetLogo_3D.exe",
          "componentFileName"     -> "NetLogo 3D.exe",
          "lowerDashName"         -> "netlogo-3d",
          "componentGuid"         -> archUUIDs("nlogo3DExecutableId"),
          "desktopShortcutId"     -> archUUIDs("NetLogo3DDesktopShortcutId"),
          "startMenuShortcutId"   -> archUUIDs("NetLogo3DStartMenuShortcutId"),
          "hasFileAssociation"    -> Boolean.box(true),
          "fileAssociations"      -> Array(
            Map(
              "extension" -> "nlogo3d",
              "icon"      -> "ModelOldIcon",
              "type"      -> "NetLogo 3D Model"
            ).asJava,
            Map(
              "extension" -> "nlogox3d",
              "icon"      -> "ModelIcon",
              "type"      -> "NetLogo 3D Model"
            ).asJava
          ),
          "launchArgs" -> """--launch "%1""""
        ).asJava,
        Map[String, AnyRef](
          "componentFriendlyName" -> "BehaviorSearch",
          "noSpaceName"           -> "BehaviorSearch",
          "componentId"           -> "BehaviorSearch.exe",
          "componentFileName"     -> "BehaviorSearch.exe",
          "lowerDashName"         -> "behaviorsearch",
          "componentGuid"         -> archUUIDs("behaviorSearchExecutableId"),
          "desktopShortcutId"     -> archUUIDs("behaviorSearchDesktopShortcutId"),
          "startMenuShortcutId"   -> archUUIDs("behaviorSearchStartMenuShortcutId"),
          "hasFileAssociation"    -> Boolean.box(true),
          "fileAssociations"      -> Array(
            Map(
              "extension" -> "bsearch",
              "icon"      -> "BehaviorSearchExperimentIcon",
              "type"      -> "BehaviorSearch Experiment"
            ).asJava
          ),
          "launchArgs" -> """"%1""""
        ).asJava
      )
    )

    Mustache(platformConfigDir / "NetLogo.wxs.mustache", msiBuildDir / "NetLogo.wxs", componentConfig)

    Seq("NetLogoTranslation.wxl", "NetLogoUI.wxs", "ShortcutDialog.wxs").foreach { wixFile =>
      FileActions.copyFile(platformConfigDir / wixFile, msiBuildDir / wixFile)
    }

    val launcherExes = launchers.map( (launcher) => s"${launcher.name}.exe" ).toSet - "NetLogo_Console.exe"
    val generatedUUIDs =
      HarvestResources.harvest(appImageDir.toPath, "INSTALLDIR", "NetLogoApp",
        launcherExes, platformVars,
        (msiBuildDir / "NetLogoApp.wxs").toPath)

    log.info("Running WiX MSI packager")
    val msiName = s"NetLogo-$version-$arch.msi"
    val buildCommand = Seq[String](
      wixCommand("wix").getPath,
      "build",
      "NetLogo.wxs",
      "NetLogoApp.wxs",
      "NetLogoUI.wxs",
      "ShortcutDialog.wxs",
      "-arch", platformVars.platformArch,
      "-culture", "en-us",
      "-loc", "NetLogoTranslation.wxl",
      "-ext", "WixToolset.UI.wixext",
      "-sw69",
      "-sw1026",
      "-sw1076",
      "-o", msiName,
      "-b", appImageDir.toString)

    val archiveFile  = webDir / msiName
    RunProcess(buildCommand, msiBuildDir, buildCommand.head)

    log.info("Moving MSI to final location.")
    FileActions.createDirectory(webDir)
    IO.delete(archiveFile)
    FileActions.moveFile(msiBuildDir / msiName, archiveFile)

    archiveFile
  }
}
