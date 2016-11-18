import sbt._

import java.io.File
import java.nio.file.{ Files, Path }
import java.io.IOException

import NetLogoPackaging.RunProcess

object PackageMacAggregate {
  // we're given a dummy package with a directory structure that looks like:
  // Product Name.app (spaces intact)
  // └── Contents
  //     ├── Info.plist
  //     ├── Java
  //     │   ├── Product Name.cfg (spaces intact)
  //     │   ├── NetLogo.jar
  //     │   └── Other jars on the classpath
  //     ├── MacOS
  //     │   ├── Product Name (must match name in Info.plist
  //     │   └── libpackager.dylib
  //     ├── PkgInfo
  //     ├── PlugIns
  //     │   └── Java.runtime
  //     └── Resources
  //         └── NetLogo.icns
  //
  // A main jar, common configuration, and several sub-applications.
  //
  // Our goal is to get a directory
  // mac-full
  // ├── Product 1.app
  // |   └── Contents
  // |      ├── Info.plist
  // |      ├── Java
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
  val contentDirs = Seq("extensions", "models", "docs")
  val libraryDirs = Seq("natives")

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

    // add icons
    common.icons.foreach { f => FileActions.copyAny(f, subApplicationDir / "Contents" / "Resources" / f.getName) }

    val allVariables =
      variables ++ app.configurationVariables +
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
    FileActions.copyDirectory(stubApplicationAndName._1 / "bundles" / (stubApplicationAndName._2 + ".app") / "Contents" / "PlugIns" / "Java.runtime", aggregateMacDir / "JRE" )

    // add java jars
    // this is wrong, may need further adjustment
    val sharedJars = aggregateMacDir / "Java"
    JavaPackager.repackageJar("netlogo-mac-app.jar", Some(commonConfig.launcherClass), commonConfig.mainJar,  sharedJars)
    commonConfig.classpath.foreach { jar =>
      FileActions.copyFile(jar, sharedJars / jar.getName)
    }

    // add bundled directories
    commonConfig.bundledDirs.filter(d => contentDirs.contains(d.directoryName)).foreach { d =>
      d.fileMappings.foreach {
        case (f, p) =>
          val targetFile = aggregateMacDir / p
          if (! targetFile.getParentFile.isDirectory)
            FileActions.createDirectories(targetFile.getParentFile)
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

    // build and sign
    (aggregateMacDir / "JRE" / "Contents" / "Home" / "jre" / "lib" / "jspawnhelper").setExecutable(true)

    val apps = subApplications.map(a => s"${a.name} $version.app")
      .map(n => aggregateMacDir / n)
      .map(_.getAbsolutePath)

    RunProcess(Seq("codesign", "--deep", "-s", "Developer ID Application") ++ apps, "codesigning")

    val dmgArgs = Seq("hdiutil", "create",
        "-quiet", s"$buildName.dmg",
        "-srcfolder", (aggregateTarget / "NetLogo Bundle").getAbsolutePath,
        "-size", "380m",
        "-volname", buildName, "-ov")
    RunProcess(dmgArgs, aggregateTarget, "dmg packaging")

    val dmgName = buildName + ".dmg"
    RunProcess(Seq("codesign", "-s", "Developer ID Application") :+ dmgName, aggregateTarget, "codesigning dmg")

    FileActions.createDirectory(webDirectory)
    FileActions.moveFile(aggregateTarget / dmgName, webDirectory / dmgName)

    webDirectory / dmgName
  }
}
