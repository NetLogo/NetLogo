import sbt._
import Keys._

object NetLogoBuild extends Build {

  lazy val root =
    Project(id = "NetLogo", base = file("."))
      .configs(Testing.configs: _*)

  lazy val all = TaskKey[Unit]("all", "build all the things!!!")

  // surely there's some better way to do this - ST 5/30/12
  lazy val nogen = TaskKey[Unit]("nogen", "disable bytecode generator")

}
