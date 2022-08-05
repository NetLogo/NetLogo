import sbt._
import sbt.io.Using

import java.io.File
import java.nio.file.{ Files, Path => JPath, Paths }
import java.io.IOException
import java.util.jar.Manifest

import NetLogoPackaging.RunProcess

object PackageMacAggregate {
  // we're given a dummy package with a directory structure that looks like:
  // Product Name.app (spaces intact)
  // └── Contents
  //     ├── Info.plist
  //     ├── MacOS
  //     │   └── dummy
  //     ├── PkgInfo
  //     ├── Resources
  //     │   └── dummy.icns
  //     ├── app
  //     │   ├── dummy.cfg
  //     │   └── netlogo-6.2.2.jar
  //     └── runtime
  //         └── Contents
  //
  // A main jar, common configuration, and several sub-applications.
  //
  // Our goal is to get a directory
  // mac-full
  // ├── Product 1.app
  // |   └── Contents
  // |      ├── Info.plist
  // |      │   ├── Product 1.cfg (adjusted for new classpath)
  // |      │   ├── natives       (duplicated in each package)
  // |      │   └── lib
  // |      ├── PkgInfo
  // |      ├── MacOS
  // |      │   ├── Product Name (must match name in Info.plist
  // |      │   └── libpackager.dylib
  // |      └── Resources
  // |           └── NetLogo.icns
  // ├── Product 2.app (same as product 1)
  // ├── Java
  // |   └── All dependency jars
  // └── Java.runtime (contains JRE)
  val libraryDirs = Seq("natives")

  val CodesigningIdentity = "Developer ID Application: Northwestern University (E74ZKF37E6)"

  // assumes subApplicationDir is {<app.name>.app as copied by JavaPackager.copyMacStubApplication
  private def configureSubApplication(subApplicationDir: File, app: SubApplication, common: CommonConfiguration, variables: Map[String, AnyRef]): Unit = {
    // add runtimeFiles "natives"
    common.runtimeFiles.foreach { d =>
      d.fileMappings.foreach {
        case (f, p) =>
          val newPath = subApplicationDir / "Contents" / "Java" / p
          if (! newPath.getParentFile.isDirectory)
            FileActions.createDirectories(newPath.getParentFile)
          FileActions.copyAny(f, newPath)
      }
    }

    // add icon, remove dummy icon
    common.icons
      .filter(f => app.allIcons.exists(iconName => f.getName.startsWith(iconName)))
      .foreach { f => FileActions.copyAny(f, subApplicationDir / "Contents" / "Resources" / f.getName) }
    FileActions.remove(subApplicationDir / "Contents" / "Resources" / "dummy.icns")

    val allVariables =
      variables ++ app.configurationVariables("macosx") +
      ("mainClass" -> app.mainClass) +
      ("classpathJars" ->
        common.classpath
          .sortBy(_.getName)
          .map(f => s"$$APPDIR/../../Java/${f.getName}")
          .mkString(File.pathSeparator))

    app.additionalArtifacts(common.configRoot).foreach { f =>
      FileActions.copyFile(f, subApplicationDir / "Contents" / "Java" / f.getName)
    }

    // rewrite configuration file
    Mustache(common.configRoot / "shared" / "macosx" / "NetLogo.cfg.mustache",
      subApplicationDir / "Contents" / "Java" / (app.name + ".cfg"), allVariables)

    // rewrite Info.plist
    Mustache(common.configRoot / "shared" / "macosx" / "Info.plist.mustache",
      subApplicationDir / "Contents" / "Info.plist", allVariables)

    // add PkgInfo
    Mustache(common.configRoot / "shared" / "macosx" / "image" / "Contents" / "PkgInfo.mustache",
      subApplicationDir / "Contents" / "Resources" / "PkgInfo", allVariables)

  }

  def signJarLibs(jarFile: File, options: Seq[String], libsToSign: Seq[String]): Unit = {
    val tmpDir = IO.createTemporaryDirectory
    println(tmpDir)
    IO.unzip(jarFile, tmpDir)

    val libPaths = libsToSign.map( (libToSign) => (tmpDir / libToSign).toString )
    runCodeSign(options, libPaths, "jar libraries")

    val manifest = Using.fileInputStream(tmpDir / "META-INF" / "MANIFEST.MF") { is =>
      new Manifest(is)
    }
    IO.delete(tmpDir / "META-INF")
    IO.jar(Path.allSubpaths(tmpDir), jarFile, manifest)

    IO.delete(tmpDir)
  }

  def runCodeSign(options: Seq[String], paths: Seq[String], taskName: String, workingDirectory: Option[File] = None): Unit = {
    RunProcess(Seq("codesign", "-v", "--force", "--sign", CodesigningIdentity) ++ options ++ paths, workingDirectory, s"codesign of $taskName")
  }

  def apply(
    aggregateTarget:        File,
    commonConfig:           CommonConfiguration,
    appSpecificConfig:      Map[SubApplication, Map[String, AnyRef]],
    stubApplicationAndName: (File, String),
    subApplications:        Seq[SubApplication],
    variables:              Map[String, String]): File = {
      import commonConfig.webDirectory

    val version = variables("version")
    val buildName = s"NetLogo-$version"
    // create aggregate directory for this build
    val aggregateMacDir = aggregateTarget / "NetLogo Bundle" / s"NetLogo $version"
    IO.delete(aggregateMacDir)
    IO.createDirectory(aggregateMacDir)
    IO.createDirectory(aggregateMacDir / "JRE")

    // copy in JRE
    FileActions.copyDirectory(stubApplicationAndName._1 / (stubApplicationAndName._2 + ".app") / "Contents" / "runtime", aggregateMacDir / "JRE" )

    // add java jars
    // this is wrong, may need further adjustment
    val sharedJars = aggregateMacDir / "Java"
    JavaPackager.repackageJar("netlogo-mac-app.jar", Some(commonConfig.launcherClass), commonConfig.mainJar,  sharedJars)
    commonConfig.classpath.foreach { jar =>
      FileActions.copyFile(jar, sharedJars / jar.getName)
    }

    // add bundled directories
    commonConfig.bundledDirs.filterNot(d => libraryDirs.contains(d.directoryName)).foreach { d =>
      d.fileMappings.foreach {
        case (f, p) =>
          val targetFile = aggregateMacDir / p
          if (! targetFile.getParentFile.isDirectory) {
            FileActions.createDirectories(targetFile.getParentFile)
          }
          FileActions.copyFile(f, aggregateMacDir / p)
      }
    }

    commonConfig.rootFiles.foreach { f =>
      FileActions.copyAny(f, aggregateMacDir / f.getName)
    }

    // copy stub for each application and post-process
    subApplications.foreach { app =>
      val appName = s"${app.name} $version"
      JavaPackager.copyMacStubApplication(stubApplicationAndName._1, stubApplicationAndName._2,
        aggregateMacDir, appName, app.name)
      configureSubApplication(aggregateMacDir / (appName + ".app"), app, commonConfig, variables ++ appSpecificConfig(app))
    }

    val headlessClasspath =
      ("classpathJars" ->
        commonConfig.classpath
          .map(jar => "Java/" + jar.getName)
          .sorted
          .mkString(File.pathSeparator))

    val headlessFile = aggregateMacDir / "netlogo-headless.sh"
    Mustache(commonConfig.configRoot / "shared" / "macosx" / "netlogo-headless.sh.mustache",
      headlessFile, variables + headlessClasspath + ("mainClass" -> "org.nlogo.headless.Main"))
    headlessFile.setExecutable(true)

    val guiFile = aggregateMacDir / "netlogo-gui.sh"
    Mustache(commonConfig.configRoot / "shared" / "macosx" / "netlogo-headless.sh.mustache",
      guiFile, variables + headlessClasspath + ("mainClass" -> "org.nlogo.app.App"))
    guiFile.setExecutable(true)

    // build and sign
    (aggregateMacDir / "JRE" / "Contents" / "Home" / "jre" / "lib" / "jspawnhelper").setExecutable(true)

    val apps = subApplications.map(a => s"${a.name} $version.app")
      .map(n => aggregateMacDir / n)

    val filesToBeSigned =
      apps
        .flatMap(a => FileActions.enumeratePaths(a.toPath).filterNot(p => Files.isDirectory(p)))

    val filesToMakeExecutable =
      filesToBeSigned.filter(p => p.getFileName.toString.endsWith(".dylib") || p.getFileName.toString.endsWith(".jnilib"))

    filesToMakeExecutable.foreach(_.toFile.setExecutable(true))

    // ensure applications are signed *after* their libraries and resources
    val orderedFilesToBeSigned =
      filesToBeSigned.sortBy {
        case p if subApplications.map(_.name).contains(p.getFileName.toString) => 2
        case _ => 1
      }

    val dmgName = buildName + ".dmg"

    // Apple requires a "hardened" runtime for notarization -Jeremy B July 2020
    val appSigningOptions = Seq("--options", "runtime", "--entitlements", (commonConfig.configRoot / "shared" / "macosx" / "entitlements.xml").toString)

    // In theory instead of hardcoding these we could search all jars for any libs that
    // aren't signed or are signed incorrectly.  But Apple will do that search for us
    // when we submit for notarization and these libraries don't change that often.
    // -Jeremy B July 2020
    val jarLibsToSign = Map(
      ("extensions/.bundled/gogo/hid4java-0.7.0.jar", Seq("darwin/libhidapi.dylib")),
      ("extensions/.bundled/nw/gephi-toolkit-0.9.7.jar", Seq("org/sqlite/native/Mac/aarch64/libsqlitejdbc.jnilib", "org/sqlite/native/Mac/x86_64/libsqlitejdbc.jnilib")),
      ("extensions/.bundled/vid/core-video-capture-1.4-20220209.101851-153.jar", Seq("org/openimaj/video/capture/nativelib/darwin_universal/libOpenIMAJGrabber.dylib")),
      ("Java/java-objc-bridge-1.0.0.jar", Seq("libjcocoa.dylib"))
    )
    jarLibsToSign.foreach { case (jarPath: String, libsToSign: Seq[String]) => signJarLibs(aggregateMacDir / jarPath, appSigningOptions, libsToSign) }

    // It's odd that we have to do this, but it works so I'm not going to worry about it.
    // We should try to remove it once we're on a more modern JDK version and the package
    // and notarization tools have better adapted to Apple's requirements.
    // More info:  https://github.com/AdoptOpenJDK/openjdk-support/issues/97
    // -Jeremy B July 2020
    val extraLibsToSign = Seq(
      "JRE/Contents/MacOS/libjli.dylib"
    ).map((p) => (aggregateMacDir / p).toString)

    runCodeSign(appSigningOptions, orderedFilesToBeSigned.map(_.toString) ++ extraLibsToSign, "app bundles")

    // Remove .dmg file if it exists
    val dmgPath = Paths.get(s"$buildName.dmg")
    Files.deleteIfExists(dmgPath)

    val dmgArgs = Seq("hdiutil", "create",
        s"$buildName.dmg",
        "-srcfolder", (aggregateTarget / "NetLogo Bundle").getAbsolutePath,
        "-size", "1200m",
        "-fs", "HFS+",
        "-volname", buildName, "-ov")
    RunProcess(dmgArgs, aggregateTarget, "disk image (dmg) packaging")

    runCodeSign(Seq(), Seq(dmgName), "disk image (dmg)", Some(aggregateTarget))

    IO.delete(webDirectory)
    FileActions.createDirectory(webDirectory)
    FileActions.moveFile(aggregateTarget / dmgName, webDirectory / dmgName)

    println("\n**Note**: The NetLogo macOS packaging and signing is complete, but you must **notarize** the .dmg file if you intend to distribute it.\n")

    webDirectory / dmgName
  }
}
