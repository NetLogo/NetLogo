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
  //     ├── JRE
  //     └── JRE
  //
  //  The desire is to merge all of the app folders, copy the libpackager and runtime, and be done
  def apply(aggregateTarget: File, buildsMap: Map[SubApplication, File]): File = {
    val aggregateLinuxDir = aggregateTarget / "linux-full"
    IO.delete(aggregateLinuxDir)
    IO.createDirectory(aggregateLinuxDir)
    val baseImage = buildsMap.head._2
    IO.copyDirectory(baseImage / "runtime", aggregateLinuxDir / "runtime")
    IO.copyFile(baseImage / "libpackager.so", aggregateLinuxDir / "libpackager.so")
    IO.createDirectory(aggregateLinuxDir / "app")
    buildsMap.foreach {
      case (app, image) =>
        val name = app.name.replaceAllLiterally(" ", "")
        IO.copyFile(image / name, aggregateLinuxDir / name)
        val copies = Path.allSubpaths(image / "app").map {
          case (f, relPath) => (f, aggregateLinuxDir / "app" / relPath)
        }
        IO.copy(copies)
    }
    // TODO: Tag with version
    IO.zip(Path.allSubpaths(aggregateLinuxDir), aggregateTarget / "netlogo-linux.zip")
    aggregateTarget / "netlogo-linux.zip"
  }
}
