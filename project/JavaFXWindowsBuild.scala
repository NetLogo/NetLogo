import sbt._

import java.io.{ IOException, File }
import java.nio.charset.Charset
import java.nio.file.{ Files, FileSystems, Path }
import java.util.Properties
import java.util.jar.JarFile

import scala.collection.JavaConverters._

import NetLogoPackaging.RunProcess

object JavaFXWindowsBuild extends PackageWinAggregate {

  override val vars32 = Map[String, String](
    "upgradeCode"                     -> "B1FB3802-4F0C-4B54-B411-6B7E64AAE325",
    "platformArch"                    -> "x86",
    "targetDirectory"                 -> "ProgramFilesFolder",
    "win64"                           -> "no"
  )

  override val vars64 = Map[String, String](
    "upgradeCode"                     -> "B7A04F78-AC10-4697-8364-73E0F95D0C2D",
    "platformArch"                    -> "x64",
    "targetDirectory"                 -> "ProgramFiles64Folder",
    "win64"                           -> "yes"
  )

  override def generateUUIDs: Map[String, String] = {
    Seq("product",
      "NetLogoMTStartMenuShortcutId",
      "NetLogoMTDesktopShortcutId",
      "nlogoMTExecutableId",
      "startMenuFolderId"
      ).flatMap(k => Seq(k + ".32", k + ".64")).map(k => k -> java.util.UUID.randomUUID.toString.toUpperCase).toMap
  }

  override def apply(
    aggregateTarget:        File,
    commonConfig:           CommonConfiguration,
    stubApplicationAndName: (File, String),
    subApplications:        Seq[SubApplication],
    variables:              Map[String, String]): File = {
      import commonConfig.{ jdk, webDirectory }
      val version = variables("version")

      val buildName = s"NetLogo-fxDemo-$version"

      val aggregateWinDir = aggregateTarget / buildName

      val msiName = s"NetLogo-fxDemo-${version}-${jdk.arch}.msi"

      IO.delete(aggregateWinDir)
      IO.createDirectory(aggregateWinDir)

      JavaPackager.copyWinStubApplications(
        stubApplicationAndName._1, stubApplicationAndName._2,
        aggregateWinDir, subApplications.map(_.name))

      val sharedJars = aggregateWinDir / "app"


      commonConfig.bundledDirs.foreach { d =>
        d.fileMappings.foreach {
          case (f, p) =>
            val targetFile = sharedJars / p
            if (! targetFile.getParentFile.isDirectory)
              FileActions.createDirectories(targetFile.getParentFile)
            FileActions.copyFile(f, sharedJars / p)
        }
      }

      commonConfig.classpath.foreach { jar =>
        FileActions.copyFile(jar, sharedJars / jar.getName)
      }

      commonConfig.icons.foreach { icon => FileActions.copyFile(icon, aggregateWinDir / icon.getName) }
      commonConfig.rootFiles.foreach { f =>
        FileActions.copyAny(f, aggregateWinDir / f.getName)
      }

      extractBuildTools(aggregateTarget, jdk)

      // configure each sub application
      subApplications.foreach { app =>
        configureSubApplication(aggregateWinDir, app, commonConfig, variables, aggregateTarget)
      }

      val uuidArchiveFileName =
        variables("version").replaceAllLiterally("-", "").replaceAllLiterally(".", "") + "-mt.properties"
      val uuidArchiveFile = commonConfig.configRoot / "aggregate" / "win" /  "archive" / uuidArchiveFileName
      val uuids =
        if (! uuidArchiveFile.exists) {
          val ids = generateUUIDs
          archive(uuidArchiveFile, ids)
          ids
        } else {
          println("loading UUIDs from: " + uuidArchiveFile.toString)
          loadUUIDs(uuidArchiveFile)
        }

      val archUUIDs = uuids.filter {
        case (k, _) => (jdk.arch == "64" && k.endsWith("64")) || (jdk.arch != "64" && k.endsWith("32"))
      }.map {
        case (k, v) => k.stripSuffix(".64").stripSuffix(".32") -> v
      }

      val winVariables: Map[String, String] =
        variables ++ (if (jdk.arch == "64") vars64 else vars32) ++ archUUIDs

      val msiBuildDir = aggregateWinDir.getParentFile

      val aggregateConfigDir = commonConfig.configRoot / "aggregate" / "win"

      val baseComponentVariables =
        Map[String, AnyRef](
          "win64"                 -> winVariables("win64"),
          "version"               -> winVariables("version"),
          "processorArchitecture" -> winVariables("platformArch"))

      val componentConfig = Map[String, AnyRef](
        "components"            -> Seq(
          Map[String, AnyRef](
            "componentFriendlyName"  -> "NetLogo Multitouch",
            "noSpaceName"            -> "NetLogoMultitouch",
            "componentId"            -> "NetLogo_Multitouch.exe",
            "componentFileName"      -> "NetLogo Multitouch.exe",
            "lowerDashName"          -> "netlogo-multitouch",
            "componentGuid"          -> winVariables("nlogoMTExecutableId"),
            "desktopShortcutId"      -> winVariables("NetLogoMTDesktopShortcutId"),
            "startMenuShortcutId"    -> winVariables("NetLogoMTStartMenuShortcutId"),
            "hasFileAssociation"     -> Boolean.box(true),
            "fileAssociation"        -> "nlogo",
            "fileIcon"               -> "ModelIcon",
            "launchArgs"             -> """--launch "%1"""",
            "associationDescription" -> "NetLogo Model"
          ) ++ baseComponentVariables).map(_.asJava).asJava)

    Mustache(aggregateConfigDir / "NetLogoMT.wxs.mustache",
      msiBuildDir / "NetLogo.wxs", winVariables ++ componentConfig)

    Seq("NetLogoTranslation.wxl", "NetLogoUI.wxs", "ShortcutDialog.wxs").foreach { wixFile =>
      FileActions.copyFile(aggregateConfigDir / wixFile, msiBuildDir / wixFile)
    }

    val generatedUUIDs =
      HarvestResources.harvest(aggregateWinDir.toPath, "INSTALLDIR", "NetLogoApp",
        Seq("NetLogo.exe", "NetLogo 3D.exe", "HubNet Client.exe", "Behaviorsearch.exe", "NetLogo Multitouch.exe"), winVariables,
        (msiBuildDir / "NetLogoApp.wxs").toPath)

    val candleCommand =
      Seq[String](wixCommand("candle").getPath, "NetLogo.wxs", "NetLogoApp.wxs", "NetLogoUI.wxs", "ShortcutDialog.wxs", "-sw1026")

    val lightCommand =
      Seq[String](wixCommand("light").getPath,
        "NetLogo.wixobj", "NetLogoUI.wixobj", "NetLogoApp.wixobj", "ShortcutDialog.wixobj",
        "-cultures:en-us", "-loc", "NetLogoTranslation.wxl",
        "-ext", "WixUIExtension",
        "-sw69", "-sw1076",
        "-o", msiName,
        "-b", aggregateWinDir.toString)

    Seq(candleCommand, lightCommand)
      .foreach(command => RunProcess(command, msiBuildDir, command.head))

    FileActions.createDirectory(webDirectory)
    FileActions.moveFile(msiBuildDir / msiName, webDirectory / msiName)

    webDirectory / msiName
  }
}
