import sbt._
import Keys._

object NetLogoBuild extends Build {

  lazy val root =
    Project(id = "NetLogo", base = file("."))
      .configs(Testing.configs: _*)
      .settings(Defaults.defaultSettings ++
                Testing.settings ++
                Packaging.settings: _*)

  lazy val nogen = TaskKey[Unit]("nogen", "disable bytecode generator")

}
