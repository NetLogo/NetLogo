import sbt._
import Keys._

object GUISettings {
  lazy val settings = Seq(
    run / javaOptions ++= (
      if (System.getProperty("os.name").contains("Mac"))
        Seq(
          "-Dapple.awt.showGrowBox=true",
          "-Dapple.laf.useScreenMenuBar=true")
      else
        Seq())
  )
}
