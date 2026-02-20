import sbt._
import sbt.io.Using
import sbt.util.Logger

import java.io.File
import java.nio.file.{ Files, Path => JPath, Paths }
import java.io.IOException
import java.util.jar.Manifest

import scala.collection.JavaConverters.{ asScalaIteratorConverter, mapAsJavaMapConverter }
import scala.sys.process.Process

import NetLogoPackaging.RunProcess

object PackageMacAggregate {
  val CodesigningIdentity = "Developer ID Application: Northwestern University (E74ZKF37E6)"

  private def needToSign(path: JPath): Boolean = {
    val name = path.getFileName.toString

    name.endsWith(".dylib") || name.endsWith(".jnilib") || name.endsWith(".so") ||
      (!name.contains(".") && Process(Seq("file", path.toString)).!!.contains("executable"))
  }

  private def signJarLibs(jarFile: File, options: Seq[String]): Unit = {
    val tmpDir = IO.createTemporaryDirectory

    IO.unzip(jarFile, tmpDir)

    val libPaths = Files.walk(tmpDir.toPath).iterator.asScala.collect {
      case path if path.toFile.isFile && needToSign(path) => path.toString
    }.toSeq

    if (libPaths.nonEmpty) {
      runCodeSign(options, libPaths, "jar libraries")

      val manifest = Using.fileInputStream(tmpDir / "META-INF" / "MANIFEST.MF") { is =>
        new Manifest(is)
      }

      IO.delete(tmpDir / "META-INF")
      IO.jar(Path.allSubpaths(tmpDir), jarFile, manifest, None)
    }

    IO.delete(tmpDir)
  }

  def runCodeSign(options: Seq[String], paths: Seq[String], taskName: String, workingDirectory: Option[File] = None): Unit = {
    RunProcess(Seq("codesign", "-v", "--force", "--sign", CodesigningIdentity) ++ options ++ paths, workingDirectory, s"codesign of $taskName")
  }

  def createBundleDir(log: Logger, version: String, destDir: File, configDir: File, launchers: Seq[Launcher]): File = {
    val buildName = s"NetLogo-$version"

    val bundleDir     = destDir / s"NetLogo $version"
    val bundleLibsDir = bundleDir / "app"
    IO.createDirectory(bundleDir)
    IO.createDirectory(bundleLibsDir)

    val plistConfig = Map(
      "NetLogo" -> Map(
        "appName"             -> s"NetLogo $version"
      , "bundleIdentifier"    -> "org.nlogo.NetLogo"
      , "bundleName"          -> "NetLogo"
      , "bundleSignature"     -> "nLo1"
      , "fileAssociations"    -> Array(
        Map(
          "extension" -> "nlogo"
        , "icon"      -> "ModelOld.icns"
        , "type"      -> "NetLogo Model"
        ).asJava
      , Map(
          "extension" -> "nlogox"
        , "icon"      -> "Model.icns"
        , "type"      -> "NetLogo Model"
        ).asJava
      )
      , "iconFile"            -> s"NetLogo $version.icns"
      , "packageID"           -> "APPLnLo1"
      , "version"             -> version
     )
    , "NetLogo 3D" -> Map(
        "appName"             -> s"NetLogo 3D $version"
      , "bundleIdentifier"    -> "org.nlogo.NetLogo3D"
      , "bundleName"          -> "NetLogo"
      , "bundleSignature"     -> "nLo1"
      , "fileAssociations"    -> Array(
        Map(
          "extension" -> "nlogo3d"
        , "icon"      -> "ModelOld.icns"
        , "type"      -> "NetLogo 3D Model"
        ).asJava
      , Map(
          "extension" -> "nlogox3d"
        , "icon"      -> "Model.icns"
        , "type"      -> "NetLogo 3D Model"
        ).asJava
      )
      , "iconFile"            -> s"NetLogo 3D $version.icns"
      , "packageID"           -> "APPLnLo1"
      , "version"             -> version
     )
    , "HubNet Client" -> Map(
        "appName"             -> s"HubNet Client $version"
      , "bundleIdentifier" -> "org.nlogo.HubNetClient"
      , "bundleName"       -> "HubNet Client"
      , "bundleSignature"  -> "????"
      , "fileAssociations" -> Array()
      , "iconFile"         -> s"HubNet Client $version.icns"
      , "packageID"        -> "APPL????"
      , "version"          -> version
     )
    , "BehaviorSearch" -> Map(
        "appName"             -> s"BehaviorSearch $version"
      , "bundleIdentifier"    -> "org.nlogo.BehaviorSearch"
      , "bundleName"          -> "BehaviorSearch"
      , "bundleSignature"     -> "????"
      , "fileAssociations"    -> Array(
        Map(
          "extension" -> "bsearch"
        , "icon"      -> "BehaviorSearchModel.icns"
        , "type"      -> "BehaviorSearch Model"
        ).asJava
      )
      , "iconFile"            -> s"BehaviorSearch $version.icns"
      , "packageID"           -> "APPL????"
      , "version"             -> version
      )
    )

    var first = true
    launchers.foreach( (launcher) => {
      log.info(s"Cleaning up ${launcher.name}.app")
      val appDir     = destDir / s"${launcher.name}.app"
      val runtimeDir = appDir / "Contents" / "runtime"
      val appLibsDir = appDir / "Contents" / "app"
      val jars       = appLibsDir.listFiles.filter( (f) => f.getName().endsWith(".jar") )
      if (first) {
        log.info("  First app we're cleaning, copying Java runtime and application jars.")
        FileActions.copyDirectory(runtimeDir, bundleDir / runtimeDir.getName)
        jars.foreach( (jar) => FileActions.copyFile(jar, bundleLibsDir / jar.getName) )
        first = false
      }
      FileActions.remove(runtimeDir)
      jars.foreach(FileActions.remove)

      // Each app has its own `$APPDIR` that we have to replace with the relative path to get to the root of the NetLogo
      // installation folder do they can share the needed jars.  But we also want to be able to specify the plain old
      // "root directory" without `app/` appended on for the extra bundled directories, so that's why we also use the
      // `{{{ROOTDIR}}}` placeholder.
      // -Jeremy B September 2022
      log.info("  Reworking the config file with the new paths.")
      val configFile = appLibsDir / s"${launcher.name}.cfg"
      val config1 = Files.readString(configFile.toPath)
      val config2 = config1.replace("$APPDIR/", "$APPDIR/../../../app/")
      val config3 = config2.replace("{{{ROOTDIR}}}", "$APPDIR/../../..")
      val splitPoint = "[Application]\n".length
      val config4 = s"${config3.substring(0, splitPoint)}app.runtime=$$APPDIR/../../../runtime/\n${config3.substring(splitPoint)}"
      Files.writeString(configFile.toPath, config4)

      log.info("  Generating Info.plist and PkgInfo files.")

      Mustache(configDir / "macosx" / "Info.plist.mustache", appDir / "Contents" / "Info.plist", plistConfig(launcher.id))
      Mustache(configDir / "macosx" / "PkgInfo.mustache",    appDir / "Contents" / "PkgInfo", plistConfig(launcher.id))

      log.info("  Move to the bundle directory.")
      FileActions.copyDirectory(appDir, bundleDir / appDir.getName)
      FileActions.remove(appDir)
    })

    bundleDir
  }

  def apply(
    log: sbt.util.Logger
  , version: String
  , arch: String
  , destDir: File
  , bundleDir: File
  , configDir: File
  , webDir: File
  , launchers: Seq[Launcher]
  ): File = {
    val buildName = s"NetLogo $version"
    (bundleDir / "runtime" / "Contents" / "Home" / "lib" / "jspawnhelper").setExecutable(true)

    log.info("Creating NetLogo_Console sym link")
    FileActions.createRelativeSoftLink(bundleDir / "NetLogo_Console", bundleDir / s"$buildName.app" / "Contents" / "MacOS" / buildName)

    log.info("Gathering files to sign")
    val appNames = launchers.map(_.id) :+ s"NetLogo Launcher $version.app"
    val apps     = launchers.map( (l) => (bundleDir / s"${l.name}.app") ) :+
                     (bundleDir / s"NetLogo Launcher $version.app")

    val filesToBeSigned =
      (apps :+ (bundleDir / "natives") :+ (bundleDir / "runtime" / "Contents" / "Home" / "lib")).flatMap( a => FileActions.enumeratePaths(a.toPath).filterNot( p => Files.isDirectory(p) ) )

    val filesToMakeExecutable =
      filesToBeSigned.filter( p => p.getFileName.toString.endsWith(".dylib") || p.getFileName.toString.endsWith(".jnilib") )

    filesToMakeExecutable.foreach(_.toFile.setExecutable(true))

    // ensure applications are signed *after* their libraries and resources
    val orderedFilesToBeSigned =
      filesToBeSigned.sortBy {
        case p if appNames.contains(p.getFileName.toString) => 2
        case _ => 1
      }

    val dmgName = s"NetLogo-$version-$arch.dmg"

    // Apple requires a "hardened" runtime for notarization -Jeremy B July 2020
    val appSigningOptions = Seq("--options", "runtime", "--entitlements", (configDir / "macosx" / "entitlements.xml").toString)

    log.info("Signing libs inside jars.")

    Files.walk(bundleDir.toPath).iterator.asScala.foreach { path =>
      if (path.toString.endsWith(".jar"))
        signJarLibs(path.toFile, appSigningOptions)
    }

    // It's odd that we have to sign `libjli.dylib`, but it works so I'm not going to
    // worry about it.  We should try to remove it once we're on a more modern JDK version
    // and the package and notarization tools have better adapted to Apple's requirements.
    // More info:  https://github.com/AdoptOpenJDK/openjdk-support/issues/97 -Jeremy B
    // July 2020
    val extraLibsToSign = Seq(
      "runtime/Contents/MacOS/libjli.dylib"
    ).map( (p) => (bundleDir / p).toString )

    log.info("Signing standalone libs in bundles and natives.")
    runCodeSign(appSigningOptions, orderedFilesToBeSigned.map(_.toString) ++ extraLibsToSign, "app bundles")

    log.info(s"Creating $dmgName")
    val dmgPath = Paths.get(dmgName)
    Files.deleteIfExists(dmgPath)

    val dmgArgs = Seq("hdiutil", "create",
      dmgName,
      "-srcfolder", destDir.getAbsolutePath,
      "-size", "1200m",
      "-fs", "HFS+",
      "-volname", buildName, "-ov"
    )
    RunProcess(dmgArgs, destDir, "disk image (dmg) packaging")

    log.info(s"Signing dmg.")
    runCodeSign(Seq(), Seq(dmgName), "disk image (dmg)", Some(destDir))

    log.info(s"Moving dmg file to final location.")
    val archiveFile = webDir / dmgName
    Files.deleteIfExists(archiveFile.toPath)
    FileActions.createDirectory(webDir)
    FileActions.moveFile(destDir / dmgName, archiveFile)

    log.info("\n**Note**: The NetLogo macOS packaging and signing are complete, but you must **notarize** the .dmg file if you intend to distribute it.\n")

    archiveFile
  }
}
