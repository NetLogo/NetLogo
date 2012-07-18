import sbt._
import Keys._

object NetLogoBuild extends Build {

  lazy val all = TaskKey[Unit]("all", "build everything!!!")

  lazy val root =
    Project(id = "NetLogo", base = file("."))
      .configs(Testing.configs: _*)
      .settings(Defaults.defaultSettings ++
                Compiling.settings ++
                Testing.settings ++
                Packaging.settings ++
                Running.settings ++
                Dump.settings ++
                ChecksumsAndPreviews.settings: _*)

  lazy val netlogoVersion = TaskKey[String]("netlogo-version", "from api.Version")
  lazy val docSmaller = TaskKey[File]("doc-smaller", "for docs/scaladoc/")

  // surely there's some better way to do these - ST 5/30/12
  lazy val threed = TaskKey[Unit]("threed", "enable NetLogo 3D")
  lazy val nogen = TaskKey[Unit]("nogen", "disable bytecode generator")

  def mungeScaladocSourceUrls(path: File): File = {
    for(file <- Process("find " + path + " -name *.html").lines)
      IO.write(
        new File(file),
        IO.read(new File(file)).replaceAll("\\.java\\.scala", ".java"))
    path
  }

}
