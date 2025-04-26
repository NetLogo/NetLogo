import sbt._

import java.io.{ IOException, File }
import java.nio.charset.Charset
import java.nio.file.{ Files, FileSystems, Path, Paths }
import java.util.Properties
import java.util.jar.JarFile

import scala.collection.JavaConverters._

import NetLogoPackaging.RunProcess

object PackageWinAggregate {
  val vars32 = Map[String, String](
    "upgradeCode"                     -> "7DEBD71E-5C9C-44C5-ABBB-B39A797CA851",
    "platformArch"                    -> "x86",
    "bitness"                         -> "always32"
  )

  val vars64 = Map[String, String](
    "upgradeCode"                     -> "891140E9-912C-4E62-AC55-97129BD46DEF",
    "platformArch"                    -> "x64",
    "bitness"                         -> "always64"
  )

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
  , webDir: File
  , variables: Map[String, String]
  , launchers: Seq[Launcher]
  ): File = {
    val platformConfigDir = configDir / "windows"

    log.info("Generating Windows UUIDs")
    val uuidArchiveFileName =
      variables("version").replace("-", "").replace(".", "") + ".properties"
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

    val winVariables: Map[String, String] =
      variables ++ Seq("iconDir" -> platformConfigDir.toString) ++ (if (arch == "64") vars64 else vars32) ++ archUUIDs

    val msiBuildDir = appImageDir.getParentFile

    log.info("Generating WiX config files")
    val baseComponentVariables =
      Map[String, AnyRef](
          "bitness"               -> winVariables("bitness"),
          "version"               -> winVariables("version"),
          "iconDir"               -> winVariables("iconDir"))
    val componentConfig = Map[String, AnyRef](
      "components" -> Seq(
        Map[String, AnyRef](
          "componentFriendlyName" -> "HubNet Client",
          "noSpaceName"           -> "HubNetClient",
          "componentId"           -> "HubNet_Client.exe",
          "componentFileName"     -> "HubNet Client.exe",
          "lowerDashName"         -> "hubnet-client",
          "componentGuid"         -> winVariables("hubNetClientExecutableId"),
          "desktopShortcutId"     -> winVariables("HubNetClientDesktopShortcutId"),
          "startMenuShortcutId"   -> winVariables("HubNetClientStartMenuShortcutId"),
          "hasFileAssociation"    -> Boolean.box(false)
        ) ++ baseComponentVariables,
        Map[String, AnyRef](
          "componentFriendlyName"  -> "NetLogo",
          "noSpaceName"            -> "NetLogo",
          "componentId"            -> "NetLogo.exe",
          "componentFileName"      -> "NetLogo.exe",
          "lowerDashName"          -> "netlogo",
          "componentGuid"          -> winVariables("nlogoExecutableId"),
          "desktopShortcutId"      -> winVariables("NetLogoDesktopShortcutId"),
          "startMenuShortcutId"    -> winVariables("NetLogoStartMenuShortcutId"),
          "hasFileAssociation"     -> Boolean.box(true),
          "fileAssociationOld"     -> "nlogo",
          "fileAssociationNew"     -> "nlogox",
          "fileIcon"               -> "ModelIcon",
          "launchArgs"             -> """--launch "%1"""",
          "associationDescription" -> "NetLogo Model"
        ) ++ baseComponentVariables,
        Map[String, AnyRef](
          "componentFriendlyName"  -> "NetLogo 3D",
          "noSpaceName"            -> "NetLogo3D",
          "componentId"            -> "NetLogo_3D.exe",
          "componentFileName"      -> "NetLogo 3D.exe",
          "lowerDashName"          -> "netlogo-3d",
          "componentGuid"          -> winVariables("nlogo3DExecutableId"),
          "desktopShortcutId"      -> winVariables("NetLogo3DDesktopShortcutId"),
          "startMenuShortcutId"    -> winVariables("NetLogo3DStartMenuShortcutId"),
          "hasFileAssociation"     -> Boolean.box(true),
          "launchArgs"             -> """--launch "%1"""",
          "fileAssociationOld"     -> "nlogo3d",
          "fileAssociationNew"     -> "nlogox3d",
          "fileIcon"               -> "ModelIcon",
          "associationDescription" -> "NetLogo 3D Model"
        ) ++ baseComponentVariables,
        Map[String, AnyRef](
          "componentFriendlyName"  -> "Behaviorsearch",
          "noSpaceName"            -> "Behaviorsearch",
          "componentId"            -> "Behaviorsearch.exe",
          "componentFileName"      -> "Behaviorsearch.exe",
          "lowerDashName"          -> "behaviorsearch",
          "componentGuid"          -> winVariables("behaviorSearchExecutableId"),
          "desktopShortcutId"      -> winVariables("behaviorSearchDesktopShortcutId"),
          "startMenuShortcutId"    -> winVariables("behaviorSearchStartMenuShortcutId"),
          "hasFileAssociation"     -> Boolean.box(true),
          "launchArgs"             -> """"%1"""",
          "fileAssociationOld"     -> "bsearch",
          "fileIcon"               -> "BehaviorsearchExperimentIcon",
          "associationDescription" -> "Behaviorsearch Experiment"
        ) ++ baseComponentVariables
      ).map(_.asJava).asJava)

    Mustache(platformConfigDir / "NetLogo.wxs.mustache", msiBuildDir / "NetLogo.wxs", winVariables ++ componentConfig)

    Seq("NetLogoTranslation.wxl", "NetLogoUI.wxs", "ShortcutDialog.wxs").foreach { wixFile =>
      FileActions.copyFile(platformConfigDir / wixFile, msiBuildDir / wixFile)
    }

    val launcherExes = launchers.map( (launcher) => s"${launcher.name}.exe" ).toSet - "NetLogo_Console.exe"
    val generatedUUIDs =
      HarvestResources.harvest(appImageDir.toPath, "INSTALLDIR", "NetLogoApp",
        launcherExes, winVariables,
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
      "-arch", winVariables("platformArch"),
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
