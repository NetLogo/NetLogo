import sbt._

import java.nio.file.Files
import java.nio.file.Paths

object AggregateMacBuild {
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
  // |      │   └── links to extensions, models, etc.
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
  val contentDirs = Seq("extensions", "models")
  val libraryDirs = Seq("lib", "natives")

  def apply(aggregateTarget: File, buildsMap: Map[SubApplication, File]): File = {
    val aggregateMacDir = aggregateTarget / "NetLogo Bundle" // TODO: Add version
    IO.delete(aggregateMacDir)
    val baseImage = buildsMap.head._2
    IO.copyDirectory(baseImage / "Contents" / "PlugIns" / "Java.runtime", aggregateMacDir / "JRE" )
    contentDirs.foreach { subdir =>
      IO.copyDirectory(baseImage / "Contents" / "Java" / subdir, aggregateMacDir / subdir)
    }
    IO.createDirectory(aggregateMacDir / "Java")
    libraryDirs.foreach { subdir =>
      IO.copyDirectory(
        baseImage / "Contents" / "Java" / subdir,
        aggregateMacDir / "Java" / subdir)
    }
    buildsMap.foreach {
      case (app, image) =>
        // post-process image
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

        IO.createDirectory(aggregatedAppDir / "Contents")
        Seq("MacOS", "Resources", "Info.plist", "PkgInfo")
          .map(copyToNewPath)

        (aggregatedAppDir / "Contents" / "MacOS" / app.name).setExecutable(true)

        val javaDir = aggregatedAppDir / "Contents" / "Java"
        IO.createDirectory(javaDir)

        def createRelativeSymlink(linkLocation: File, linkTarget: File): Unit = {
          val linkPath = linkLocation.toPath
          val linkTargetPath = linkTarget.toPath
          Files.createSymbolicLink(linkPath, linkPath.getParent.relativize(linkTargetPath))
        }

        contentDirs.foreach(contentdir =>
          createRelativeSymlink(javaDir / contentdir, aggregateMacDir / contentdir))
        libraryDirs.foreach(libdir =>
          createRelativeSymlink(javaDir / libdir, aggregateMacDir / "Java" / libdir))

        (image / "Contents" / "Java" * "*.jar").get.foreach(f =>
          IO.copyFile(f, aggregateMacDir / "Java" / f.getName))
        (image / "Contents" / "Java" * (- ("*.jar" || DirectoryFilter))).get.foreach(f =>
          IO.copyFile(f, aggregatedAppDir / "Contents" / "Java" / f.getName))
        val cfgFile = image / "Contents" / "Java" / (app.name + ".cfg")
        val alteredCfgContents = IO.readLines(cfgFile).map { l =>
          if (l.startsWith("app.classpath")) {
            val classpathJars = l.split("=")(1).split(":")
            "app.classpath=" + classpathJars.map(s => "$APPDIR/../../Java/" + s).mkString(":")
          } else if (l.startsWith("app.runtime")) {
            "app.runtime=$APPDIR/../../JRE"
          } else
            l
        }
        IO.writeLines(aggregatedAppDir / "Contents" / "Java" / (app.name + ".cfg"), alteredCfgContents)
    }

    val buildName = "NetLogo 5.2.2-RC1"
    val dmgArgs = Seq("hdiutil", "create",
        "-quiet", buildName + ".dmg",
        "-srcfolder", (aggregateTarget / "NetLogo Bundle").getAbsolutePath,
        "-size", "300m",
        "-volname", buildName, "-ov")
    val ret = Process(dmgArgs, aggregateTarget).!
    if (ret != 0)
      sys.error("dmg packaging failed!\ncommand: " + dmgArgs.mkString(" "))

    val iEnable = Process(
      Seq("hdiutil", "internet-enable", "-quiet", "-yes", buildName + ".dmg"), aggregateTarget).!

    if (iEnable != 0)
      sys.error("Internet enabling failed")

    aggregateTarget / (buildName + ".dmg")
  }
}
