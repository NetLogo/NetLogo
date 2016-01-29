import sbt._
import Keys._

object NetLogoBuild extends Build {

  lazy val all = TaskKey[Unit]("all", "build everything!!!")

  lazy val root =
    Project(id = "NetLogo", base = file("."))
      .configs(Testing.configs: _*)
      .settings(Defaults.coreDefaultSettings ++
                Compiling.settings ++
                Testing.settings ++
                Packaging.settings ++
                Running.settings ++
                Dump.settings ++
                Scaladoc.settings ++
                ChecksumsAndPreviews.settings: _*)

  // surely there's some better way to do these - ST 5/30/12
  lazy val threed = TaskKey[Unit]("threed", "enable NetLogo 3D")
  lazy val nogen = TaskKey[Unit]("nogen", "disable bytecode generator")

}
