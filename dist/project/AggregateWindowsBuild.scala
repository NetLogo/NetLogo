import sbt._
import java.nio.file.FileSystems

object AggregateWindowsBuild {
  // each application maps to the root of the build product
  // in linux, the build product is a directory that looks like:
  //  Product Name (spaces intact)
  //  ├── Product Name.exe (spaces intact)
  //  ├── Product Name.ico (spaces intact)
  //  ├── app
  //  │   ├── NetLogo 3D.cfg (spaces intact)
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

  // TODO: Pass in configuration map
  def apply(aggregateTarget: File, buildsMap: Map[SubApplication, File], configurationDirectory: File): File = {
    val aggregateWindowsDir = aggregateTarget / "windows-full"
    IO.createDirectory(aggregateWindowsDir)
    val baseImage = buildsMap.head._2
    IO.copyDirectory(baseImage / "runtime", aggregateWindowsDir / "runtime")
    (baseImage * "*.dll").get.foreach(f => IO.copyFile(f, aggregateWindowsDir / f.getName))
    IO.createDirectory(aggregateWindowsDir / "app")
    buildsMap.foreach {
      case (app, image) =>
        import app.name
        IO.copyFile(image / (name + ".exe"), aggregateWindowsDir / (name + ".exe"))
        IO.copyFile(image / (name + ".ico"), aggregateWindowsDir / (name + ".ico"))
        val copies = Path.allSubpaths(image / "app").map {
          case (f, relPath) => (f, aggregateWindowsDir / "app" / relPath)
        }
        IO.copy(copies)
    }

    Mustache(
      configurationDirectory / "NetLogo.wxs.mustache",
      aggregateTarget / "NetLogo.wxs",
      Map("version" -> "5.2.2"))

    IO.copyFile(configurationDirectory / "model.ico", aggregateWindowsDir / "model.ico")

    val heatCommand =
      Seq(wixCommand("heat").getPath,
        "dir", aggregateWindowsDir.getPath,
        "-sfrag", "-srd", "-gg",
        "-template", "fragment",
        "-cg", "NetLogoApp",
        "-dr", "INSTALLDIR",
        "-out", "NetLogoApp.wxs",
        "-sw5150",
        "-t", (configurationDirectory / "ElementNamer.xsl").getPath)

    val candleCommand =
      Seq(wixCommand("candle").getPath, "NetLogo.wxs", "NetLogoApp.wxs", "-sw1026")

    val lightCommand =
      Seq(wixCommand("light").getPath,
        "NetLogo.wixobj", "NetLogoApp.wixobj",
        "-sw69", "-sw1076",
        "-o", "NetLogo.msi",
        "-b", aggregateWindowsDir.getPath)

    Seq(
      heatCommand,
      candleCommand,
      lightCommand).foreach { command =>
        val res = Process(command, aggregateTarget).!
        if (res != 0)
          sys.error("Command failed: " + command.mkString(" "))
      }

    aggregateTarget / "windows-full"
  }
}
