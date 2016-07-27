import sbt._
import java.nio.charset.Charset
import java.nio.file.{ Files, FileSystems }
import java.util.Properties
import NetLogoPackaging.RunProcess
import scala.collection.JavaConversions._

object AggregateWindowsBuild extends PackageAction.AggregateBuild {
  // each application maps to the root of the build product
  // in linux, the build product is a directory that looks like:
  //  Product Name (spaces intact)
  //  ├── Product Name.exe (spaces intact)
  //  ├── Product Name.ico (spaces intact)
  //  ├── app
  //  │   ├── Product.cfg
  //  │   ├── Product 2.cfg (spaces intact)
  //  │   ├── NetLogo.jar
  //  │   └── Other jars on the classpath
  //  ├── msvcp120.dll
  //  ├── msvcr100.dll
  //  ├── msvcr120.dll
  //  ├── packager.dll
  //  └── runtime
  //      └── JRE, etc.
  //
  //  The desire is to merge all of the app folders,
  //  copy the dlls and runtime, and the icons and exe files.
  //  Should be pretty much the same as the linux build, with the
  //  exception that we'll do a bunch of WiX stuff at the end instead
  //  of just zipping it up

  val productIDs = Map[String, String]()

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
      "nlogoExecutableId", "nlogo3DExecutableId", "hubNetClientExecutableId"
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
    properties.stringPropertyNames.map {
      case name => (name -> properties.getProperty(name))
    }.toMap
  }

  // TODO: Pass in configuration map
  def apply(
    aggregateTarget:        File,
    configurationDirectory: File,
    jdk:                    BuildJDK,
    buildsMap:              Map[SubApplication, File],
    variables:              Map[String, String],
    additionalFiles:        Seq[File]): File = {
    val uuidArchiveFileName =
      variables("version").replaceAllLiterally("-", "").replaceAllLiterally(".", "") + ".properties"
    val uuidArchiveFile = configurationDirectory / "archive" / uuidArchiveFileName
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
    val aggregateWindowsDir = aggregateTarget / s"windows-full-${jdk.arch}"
    val packageResourceLocation = aggregateWindowsDir / "resources"
    val msiName = s"NetLogo-${jdk.arch}.msi"
    FileActions.createDirectories(packageResourceLocation)
    val baseImage = buildsMap.head._2
    FileActions.copyDirectory(baseImage / "runtime", packageResourceLocation / "runtime")
    (baseImage * "*.dll").get.foreach(f => FileActions.copyFile(f, packageResourceLocation / f.getName))
    FileActions.createDirectory(packageResourceLocation / "app")

    buildsMap.foreach {
      case (app, image) =>
        import app.name
        FileActions.copyFile(image / (name + ".exe"), packageResourceLocation / (name + ".exe"))
        FileActions.copyFile(image / (name + ".ico"), packageResourceLocation / (name + ".ico"))
        val copies = Path.allSubpaths(image / "app").map {
          case (f, relPath) => (f, packageResourceLocation / "app" / relPath)
        }
        FileActions.copyAll(copies)
    }

    additionalFiles.foreach { f => FileActions.copyAny(f, packageResourceLocation / f.getName) }

    Mustache.betweenDirectories(configurationDirectory, aggregateWindowsDir, winVariables)

    FileActions.copyFile(configurationDirectory / "model.ico", packageResourceLocation / "model.ico")

    val generatedUUIDs =
      HarvestResources.harvest(packageResourceLocation.toPath, "INSTALLDIR", "NetLogoApp",
        Seq("NetLogo.exe", "NetLogo 3D.exe", "HubNet Client.exe"), winVariables,
        (aggregateWindowsDir / "NetLogoApp.wxs").toPath)

    val candleCommand =
      Seq(wixCommand("candle").getPath, "NetLogo.wxs", "NetLogoApp.wxs", "NetLogoUI.wxs", "ShortcutDialog.wxs", "-sw1026")

    val lightCommand =
      Seq(wixCommand("light").getPath,
        "NetLogo.wixobj", "NetLogoUI.wixobj", "NetLogoApp.wixobj", "ShortcutDialog.wixobj",
        "-cultures:en-us", "-loc", "NetLogoTranslation.wxl",
        "-ext", "WixUIExtension",
        "-sw69", "-sw1076",
        "-o", msiName,
        "-b", packageResourceLocation.getPath)

    Seq(candleCommand, lightCommand)
      .foreach(command => RunProcess(command, aggregateWindowsDir, command.head))

    aggregateWindowsDir / msiName
  }
}
