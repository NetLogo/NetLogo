import sbt._
import java.nio.file.FileSystems
import NetLogoPackaging.RunProcess
import AggregateMacBuild.copyAny

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

  val productIDs = Map[String, String](
    "5.2.2-RC2-32" -> "90393D49-CB1A-4CBC-A7A5-09E44B351649",
    "5.3-RC1-32"   -> "CF0A58E0-4CC9-44B7-B316-B4EE5A2CDBC4",
    "5.3-RC1-64"   -> "81E8BCF2-8BAE-40DD-8373-3ECD4CFF007B",
    "5.3-32"       -> "A6C64B81-FC8D-42E0-A55F-983705E52879",
    "5.3-64"       -> "BE6BDFA6-8DB5-47DB-94EC-B7504A3F7EBC",
    "5.3.1-RC1-32" -> "AB7522FA-0F90-43F7-AB78-CA14BBA3F0D4",
    "5.3.1-RC1-64" -> "A2B3AD1C-992F-4DEE-8C08-317B13BACBD2",
    "5.3.1-RC2-32" -> "D6D1E47D-40E3-4A8F-801C-2CD9926E88B1",
    "5.3.1-RC2-64" -> "F37E74D4-61D0-41EC-BEEE-7056D8CBFCC0",
    "5.3.1-RC3-32" -> "1F07A977-1323-428D-8708-C3FE6D6BC956",
    "5.3.1-RC3-64" -> "D754C720-1486-48FB-AAAB-40FD7CE9EE71",
    "5.3.1-32"     -> "EB5AB27B-F791-490D-9C46-FC3A9BA5270B",
    "5.3.1-64"     -> "54AB7C31-BB52-4579-BAE7-B75968DECAE0",
    "6.0-PREVIEW-12-15-32" -> "367B7025-7977-4DAA-8856-E9AAA3421707",
    "6.0-PREVIEW-12-15-64" -> "38EE8625-BFFF-430E-BFF3-D069B4F8F75C",
    "6.0-M1-32"            -> "97E5CCED-0294-44AC-8114-3AFFEAD974EB",
    "6.0-M1-64"            -> "0CD72DC9-2178-4846-8AAE-A2396A461EF7",
    "6.0-M2-32"            -> "2D720641-CF8D-4D65-A27E-D13AEAE8668C",
    "6.0-M2-64"            -> "D159116B-C8E9-47E1-BDF7-912A2AF513F8",
    "6.0-M3-32"            -> "1EC49718-2F90-4B23-9791-543B9AAE5D2E",
    "6.0-M3-64"            -> "301905ED-026D-413B-9DE5-9BA9F0220FB7",
    "6.0-M4-32"            -> "9508857F-56D4-4C76-94D8-138B5BF81DDD",
    "6.0-M4-64"            -> "500913FF-EDDC-4643-8153-58A81B129AD9",
    "6.0-M5-32"            -> "497EC77F-2B71-4B33-AB72-D0A4FDAFB135",
    "6.0-M5-64"            -> "921C948B-C4B5-4EB0-A6C5-45877717204F",
    "6.0-M6-32"            -> "1BF31DCC-9CBC-4E21-9B44-698EAF1D5CB7",
    "6.0-M6-64"            -> "3189AA6F-71B5-4CF4-858C-91EEF31B867D",
    "6.0-M7-32"            -> "D0946991-3E39-4274-967E-5B59CB1F1B99",
    "6.0-M7-64"            -> "E86ED203-D7EE-4A66-9105-A2B251281B96",
    "6.0-M8-32"            -> "9A963503-F5CC-44FD-A8A8-4C461E52BE07",
    "6.0-M8-64"            -> "C48828BC-FC67-4C40-A855-50916B4ED791"
  )

  val vars32 = Map[String, String](
    "upgradeCode"                     -> "7DEBD71E-5C9C-44C5-ABBB-B39A797CA851",
    "platformArch"                    -> "x86",
    "targetDirectory"                 -> "ProgramFilesFolder",
    "win64"                           -> "no",
    "NetLogoStartMenuShortcutId"      -> "AED4301A-C532-4E6D-B531-96289C472C74",
    "NetLogo3DStartMenuShortcutId"    -> "FF67AD21-B9A5-4B2D-928A-6C05FF0C18F6",
    "HubNetClientStartMenuShortcutId" -> "8197A493-D6AF-411C-B85A-B36F173DA276",
    "NetLogoDesktopShortcutId"        -> "EEC3DCC6-A22B-4A3D-A13E-048C07089A60",
    "NetLogo3DDesktopShortcutId"      -> "DD28ADDD-9C1D-46B3-89E8-48252B285B7F",
    "HubNetClientDesktopShortcutId"   -> "911212BF-519F-4852-BD4F-18A09EAB2AFB",
    "nlogoFileAssociationId"          -> "B58D5742-B3F6-481D-8656-8EF85BB87FA4",
    "nlogo3DFileAssociationId"        -> "D2F8CCF5-B6DB-4C32-BD4A-697EFFCA481B"
  )

  val vars64 = Map[String, String](
    "upgradeCode"                     -> "891140E9-912C-4E62-AC55-97129BD46DEF",
    "platformArch"                    -> "x64",
    "targetDirectory"                 -> "ProgramFiles64Folder",
    "win64"                           -> "yes",
    "NetLogoStartMenuShortcutId"      -> "E9DCFD73-B379-4850-90F1-8D0F44CF5B19",
    "NetLogo3DStartMenuShortcutId"    -> "315AC663-63B4-4D76-8E7F-405DB2ACE955",
    "HubNetClientStartMenuShortcutId" -> "8FE70E94-B58C-4571-BD70-72F51F39A225",
    "NetLogoDesktopShortcutId"        -> "EE92B07F-7419-4AF1-891C-4F038326DCCA",
    "NetLogo3DDesktopShortcutId"      -> "90D0D43D-B01B-41B6-A95E-7F4240020574",
    "HubNetClientDesktopShortcutId"   -> "2D027A72-6AE4-4606-ABFD-BE5ABFFB6D20",
    "nlogoFileAssociationId"          -> "1B046F14-55C9-484F-89CF-12024521A15C",
    "nlogo3DFileAssociationId"        -> "6A768245-4936-4940-B53F-BFDEAC291DB3"
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

  // TODO: Pass in configuration map
  def apply(
    aggregateTarget:        File,
    configurationDirectory: File,
    jdk:                    BuildJDK,
    buildsMap:              Map[SubApplication, File],
    variables:              Map[String, String],
    additionalFiles: Seq[File]): File = {
    val productIDMap =
      Map("productID" -> productIDs.get(s"${variables("version")}-${jdk.arch}")
        .getOrElse(sys.error("generate a new product ID for this version before packaging windows aggregate")))
    val winVariables: Map[String, String] =
      variables ++ (if (jdk.arch == "64") vars64 else vars32) ++ productIDMap
    val aggregateWindowsDir = aggregateTarget / s"windows-full-${jdk.arch}"
    val packageResourceLocation = aggregateWindowsDir / "resources"
    val msiName = s"NetLogo-${jdk.arch}.msi"
    IO.createDirectory(packageResourceLocation)
    val baseImage = buildsMap.head._2
    IO.copyDirectory(baseImage / "runtime", packageResourceLocation / "runtime")
    (baseImage * "*.dll").get.foreach(f => IO.copyFile(f, packageResourceLocation / f.getName))
    IO.createDirectory(packageResourceLocation / "app")
    buildsMap.foreach {
      case (app, image) =>
        import app.name
        IO.copyFile(image / (name + ".exe"), packageResourceLocation / (name + ".exe"))
        IO.copyFile(image / (name + ".ico"), packageResourceLocation / (name + ".ico"))
        val copies = Path.allSubpaths(image / "app").map {
          case (f, relPath) => (f, packageResourceLocation / "app" / relPath)
        }
        IO.copy(copies)
    }

    additionalFiles.foreach { f => copyAny(f, packageResourceLocation / f.getName) }

    Mustache.betweenDirectories(configurationDirectory, aggregateWindowsDir, winVariables)

    IO.copyFile(configurationDirectory / "model.ico", packageResourceLocation / "model.ico")

    val heatCommand =
      Seq(wixCommand("heat").getPath,
        "dir", packageResourceLocation.getPath,
        "-sfrag", "-srd", "-gg",
        "-template", "fragment",
        "-cg", "NetLogoApp",
        "-dr", "INSTALLDIR",
        "-out", "NetLogoApp.wxs",
        "-sw5150",
        "-t", (aggregateWindowsDir / "ElementNamer.xsl").getPath)

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

    Seq(heatCommand, candleCommand, lightCommand)
      .foreach(command => RunProcess(command, aggregateWindowsDir, command.head))

    aggregateWindowsDir / msiName
  }
}
