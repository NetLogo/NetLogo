import sbt._
import Keys._

object NetLogoBuild extends Build {

  lazy val root =
    Project(id = "NetLogo", base = file("."))
      .configs(Testing.configs: _*)
      .aggregate(headless)
      .dependsOn(headless % "test->test;compile->compile")

  lazy val headless =
    Project(id = "headless",
            base = file("headless"))
      .configs(Testing.configs: _*)

  lazy val all = TaskKey[Unit]("all", "build all the things!!!")

  // surely there's some better way to do these - ST 5/30/12
  lazy val threed = TaskKey[Unit]("threed", "enable NetLogo 3D")
  lazy val nogen = TaskKey[Unit]("nogen", "disable bytecode generator")

}
