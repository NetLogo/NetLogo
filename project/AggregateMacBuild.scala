import sbt._

import java.nio.file.Files
import java.nio.file.Paths

import NetLogoPackaging.RunProcess

object AggregateMacBuild extends PackageAction.AggregateBuild {
  // each application maps to the root of the build product
  // in mac, the build product is a that looks like:
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
  // │   ├── All dependency jars
  // |   └── links to extensions, models, etc.
  // └── Java.runtime (contains JRE)
  val contentDirs = Seq("extensions", "models", "docs")
  val libraryDirs = Seq("lib", "natives")

  private def postProcessSubApplication(aggregateMacDir: File)(app: SubApplication, image: File): Unit = {
    val aggregatedAppDir = aggregateMacDir / image.getName
    Process(Seq("bash",
      (image.getParentFile.getParentFile.getParentFile / "package" / "macosx" / (app.name + "-post-image.sh")).getAbsolutePath, image.getAbsolutePath)).!!

    def copyToNewPath(fileName: String) = {
      val sourceFile = image / "Contents" / fileName
      val destFile = aggregatedAppDir / "Contents" / fileName
      sourceFile match {
        case f if f.isDirectory => IO.copyDirectory(sourceFile, destFile)
        case f                  => IO.copyFile(sourceFile, destFile)
      }
    }

    def createRelativeSymlink(linkLocation: File, linkTarget: File): Unit = {
      val linkPath = linkLocation.toPath
      val linkTargetPath = linkTarget.toPath
      Files.createSymbolicLink(linkPath, linkPath.getParent.relativize(linkTargetPath))
    }

    def alterCfgContents(cfgFile: File, app: SubApplication): Seq[String] = {
      val sharedRootPath = "$APPDIR/../.."
      IO.readLines(cfgFile).flatMap { l =>
        if (l.startsWith("app.classpath")) {
          val classpathJars = l.split("=")(1).split(":").toSet + s"${app.jarName}.jar"
          val newClasspath  = classpathJars.map(jar => s"$sharedRootPath/Java/$jar").mkString(":")
          Seq(s"app.classpath=$newClasspath")
        } else if (l.startsWith("app.runtime"))
          Seq(s"app.runtime=$sharedRootPath/JRE")
        else if (l == "[JVMOptions]")
          "[JVMOptions]" +: contentDirs.map(d => s"-Dnetlogo.$d.dir=$sharedRootPath/$d")
        else
          Seq(l)
      }
    }

    val javaDir = aggregatedAppDir / "Contents" / "Java"
    IO.createDirectory(javaDir)

    Seq("MacOS", "Resources", "Info.plist", "PkgInfo").map(copyToNewPath)
    (aggregatedAppDir / "Contents" / "MacOS" / app.name).setExecutable(true)

    libraryDirs.foreach(d => copyToNewPath(s"Java/$d"))

    IO.delete(image / "Contents" / "Java" / "NetLogo User Manual.pdf")
    (image / "Contents" / "Java" * (- ("*.jar" || DirectoryFilter))).get.foreach(f => IO.copyFile(f, javaDir / f.getName))
    val cfgFile = image / "Contents" / "Java" / (app.name + ".cfg")
    IO.writeLines(javaDir / (app.name + ".cfg"), alterCfgContents(cfgFile, app))
  }

  def apply(
    aggregateTarget: File,
    configurationDirectory: File,
    buildJDK: BuildJDK,
    buildsMap: Map[SubApplication, File],
    variables: Map[String, String]): File = {

    val version = variables("version")
    val aggregateMacDir = aggregateTarget / "NetLogo Bundle" / s"NetLogo $version"
    IO.delete(aggregateMacDir)
    val baseImage = buildsMap.head._2
    val sharedJars = aggregateMacDir / "Java"
    val buildName = s"NetLogo-$version"
    IO.createDirectory(sharedJars)
    IO.copyDirectory(baseImage / "Contents" / "PlugIns" / "Java.runtime", aggregateMacDir / "JRE" )
    IO.copyFile(baseImage / "Contents" / "Java" / "NetLogo User Manual.pdf", aggregateMacDir / "NetLogo User Manual.pdf" )

    contentDirs.foreach { subdir =>
      IO.copyDirectory(baseImage / "Contents" / "Java" / subdir, aggregateMacDir / subdir)
    }

    buildsMap.foreach {
      case (app, image) =>
        (image / "Contents" / "Java" * "*.jar").get.foreach(f => IO.copyFile(f, sharedJars / f.getName))
    }

    buildsMap.foreach {
      case (app, image) => postProcessSubApplication(aggregateMacDir)(app, image)
    }

    val apps = buildsMap.map(_._2).map(f => (aggregateMacDir / f.getName).getAbsolutePath)

    RunProcess(Seq("codesign", "--deep", "-s", "Developer ID Application") ++ apps, "codesigning")

    val dmgArgs = Seq("hdiutil", "create",
        "-quiet", s"$buildName.dmg",
        "-srcfolder", (aggregateTarget / "NetLogo Bundle").getAbsolutePath,
        "-size", "375m",
        "-volname", buildName, "-ov")
    RunProcess(dmgArgs, aggregateTarget, "dmg packaging")

    aggregateTarget / (buildName + ".dmg")
  }
}
