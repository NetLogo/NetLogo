import sbt._
import Keys._

object NetLogoBuild extends Build {

  lazy val root =
    Project(id = "NetLogo", base = file("."))
      .configs(Testing.configs: _*)
      .settings(Defaults.defaultSettings ++
                Testing.settings ++
                Packaging.settings ++
                Running.settings: _*)

  // surely there's some better way to do this - ST 5/30/12
  lazy val nogen = TaskKey[Unit]("nogen", "disable bytecode generator")

}
