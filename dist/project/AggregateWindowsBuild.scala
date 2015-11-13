import sbt._

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
  def apply(aggregateTarget: File, buildsMap: Map[SubApplication, File]): File = {
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

    // TODO: Tag with version
    // TODO: Bunch of wix crap
    aggregateTarget / "windows-full"
  }
}
