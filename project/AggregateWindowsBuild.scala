import sbt._

import java.io.{ IOException, File }
import java.nio.charset.Charset
import java.nio.file.{ Files, FileSystems, Path }
import java.util.Properties
import java.util.jar.JarFile

import scala.collection.JavaConverters._

import NetLogoPackaging.RunProcess

object PackageWinAggregate {
  // we're given a dummy package with a directory structure that look like:
  // dummy
  //  ├── dummy.exe (spaces intact)
  //  ├── dummy.ico (spaces intact)
  //  ├── app
  //  │   ├── dummy.cfg (spaces intact)
  //  │   └── NetLogo.jar
  //  ├── msvcp120.dll
  //  ├── msvcr100.dll
  //  ├── msvcr120.dll
  //  ├── packager.dll
  //  └── runtime
  //      └── JRE, etc.
  //
  // The desire is to add and configure all sub applications from the dummy application.
  // JavaPackager.copyWinStubApplications gives us the raw exe, ico, cfg files, but we will
  // need to overwrite those with the correct ones.
  //
  // Once we've generated a functioning suite of applications, we'll process the applications
  // into a windows package using WiX packager.

  val vars32 = Map[String, String](
    "upgradeCode"                     -> "7DEBD71E-5C9C-44C5-ABBB-B39A797CA851",
    "platformArch"                    -> "x86",
    "targetDirectory"                 -> "ProgramFilesFolder",
    "win64"                           -> "no"
  )

  val vars64 = Map[String, String](
    "upgradeCode"                     -> "891140E9-912C-4E62-AC55-97129BD46DEF",
    "platformArch"                    -> "x64",
    "targetDirectory"                 -> "ProgramFiles64Folder",
    "win64"                           -> "yes"
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

  private def configureSubApplication(sharedAppRoot: File, app: SubApplication, common: CommonConfiguration, variables: Map[String, AnyRef], helperBinDirectory: File): Unit = {
    val allVariables =
      variables ++ app.configurationVariables("windows") +
      ("mainClass"      -> app.mainClass) +
      ("mainClassSlash" -> app.mainClass.replaceAllLiterally(".", "/").replaceAllLiterally("$", "")) +
      ("appIdentifier"  -> app.mainClass.split("\\.").init.mkString(".")) +
      ("classpathJars"  ->
        common.classpath
          .map(_.getName)
          .sorted
          .mkString(File.pathSeparator))

    Mustache(common.configRoot / "shared" / "windows" / "NetLogo.cfg.mustache",
      sharedAppRoot / "app" / (app.name + ".cfg"), allVariables)

    app.additionalArtifacts(common.configRoot).foreach { f =>
      FileActions.copyFile(f, sharedAppRoot / "app" / f.getName)
    }

    (sharedAppRoot / (app.name + ".exe")).setWritable(true)
    RunProcess(Seq((helperBinDirectory / "IconSwap.exe").toString,
      (sharedAppRoot / (app.iconName + ".ico")).toString, (sharedAppRoot / (app.name + ".exe")).toString),
      "swapping exe icon")
    RunProcess(
      Seq((helperBinDirectory / "verpatch.exe").toString, "/va", (app.name + ".exe"), "/s", "FileDescription", app.name + " " + variables("version")),
      sharedAppRoot,
      "Tagging application with versioned description")
    (sharedAppRoot / (app.name + ".exe")).setWritable(false)
  }

  def apply(
    aggregateTarget:        File,
    commonConfig:           CommonConfiguration,
    stubApplicationAndName: (File, String),
    subApplications:        Seq[SubApplication],
    variables:              Map[String, String]): File = {
    import commonConfig.{ jdk, webDirectory }

    val version = variables("version")

    val buildName = s"NetLogo-$version"

    val aggregateWinDir = aggregateTarget / s"NetLogo $version"
    val msiName = s"NetLogo-${version}-${jdk.arch}.msi"

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

    // extract IconSwap and download verpatch, used when customizing the executables
    if (! (aggregateTarget / "IconSwap.exe").exists) {
      val jfxJar = new java.util.jar.JarFile(file(jdk.javaHome.get) / "lib" / "ant-javafx.jar")
      val iconSwapEntry = jfxJar.getEntry("com/oracle/tools/packager/windows/IconSwap.exe")
      val iconSwapStream = jfxJar.getInputStream(iconSwapEntry)
      IO.transfer(iconSwapStream, aggregateTarget / "IconSwap.exe")
      iconSwapStream.close()
      jfxJar.close()
    }

    if (! (aggregateTarget / "verpatch.exe").exists) {
      FileActions.download(url("https://s3.amazonaws.com/ccl-artifacts/verpatch.exe"), aggregateTarget / "verpatch.exe")
    }

    // configure each sub application
    subApplications.foreach { app =>
      configureSubApplication(aggregateWinDir, app, commonConfig, variables, aggregateTarget)
    }

    val headlessClasspath =
      ("netlogoJar" ->
        commonConfig.classpath
          .filter(_.getName.startsWith("netlogo"))
          .map(jar => "app\\" + jar.getName)
          .take(1)
          .mkString(""))

    val targetFile = aggregateWinDir / "netlogo-headless.bat"
    Mustache(commonConfig.configRoot / "shared" / "windows" / "netlogo-headless.bat.mustache",
      targetFile, variables + headlessClasspath)
    targetFile.setExecutable(true)

    val uuidArchiveFileName =
      variables("version").replaceAllLiterally("-", "").replaceAllLiterally(".", "") + ".properties"
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
          "fileAssociation"        -> "nlogo",
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
          "fileAssociation"        -> "nlogo3d",
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
          "fileAssociation"        -> "bsearch",
          "fileIcon"               -> "BehaviorsearchExperimentIcon",
          "associationDescription" -> "Behaviorsearch Experiment"
        ) ++ baseComponentVariables
      ).map(_.asJava).asJava)
    Mustache(aggregateConfigDir / "NetLogo.wxs.mustache",
      msiBuildDir / "NetLogo.wxs", winVariables ++ componentConfig)

    Seq("NetLogoTranslation.wxl", "NetLogoUI.wxs", "ShortcutDialog.wxs").foreach { wixFile =>
      FileActions.copyFile(aggregateConfigDir / wixFile, msiBuildDir / wixFile)
    }

    val generatedUUIDs =
      HarvestResources.harvest(aggregateWinDir.toPath, "INSTALLDIR", "NetLogoApp",
        Seq("NetLogo.exe", "NetLogo 3D.exe", "HubNet Client.exe", "Behaviorsearch.exe"), winVariables,
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
