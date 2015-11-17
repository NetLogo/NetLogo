import sbt._

object AggregateLinuxBuild {
  // each application maps to the root of the build product
  // in linux, the build product is a directory that looks like:
  // ProductDirectory
  // ├── ProductName (all spaces removed)
  // ├── app
  // │   ├── ProductMainJar
  // │   ├── Product.cfg
  // │   └── Other jars on the classpath
  // ├── libpackager.so
  // └── runtime
  //     └── JRE
  //
  //  The desire is to merge all of the app folders, copy the libpackager and runtime, and be done
  def apply(aggregateTarget: File, buildsMap: Map[SubApplication, File]): File = {
    val version = "5.2.2-RC1"
    val aggregateLinuxDir = aggregateTarget / "linux-full"
    // we build the image in this directory, and we later use tar.gz to package it
    val imageDir = aggregateLinuxDir / s"netlogo-$version"
    val archiveName = s"netlogo-$version.tar.gz"
    IO.delete(aggregateLinuxDir)
    IO.createDirectory(imageDir)
    val baseImage = buildsMap.head._2
    IO.copyDirectory(baseImage / "runtime", imageDir / "runtime")
    IO.copyFile(baseImage / "libpackager.so", imageDir / "libpackager.so")
    IO.createDirectory(imageDir / "app")

    buildsMap.foreach {
      case (app, image) =>
        val name = app.name.replaceAllLiterally(" ", "")
        IO.copyFile(image / name, imageDir / name)
        val copies = Path.allSubpaths(image / "app").map {
          case (f, relPath) => (f, imageDir / "app" / relPath)
        }
        IO.copy(copies)
    }

    buildsMap.map(_._1.name.replaceAllLiterally(" ", "")).foreach { executableName =>
      (imageDir / executableName).setExecutable(true)
    }

    val res = Process(Seq("tar", "-zcf", archiveName, imageDir.getName), aggregateLinuxDir).!
    if (res != 0)
      sys.error("failed to build tarball")

    aggregateLinuxDir / archiveName
  }
}
