import sbt._
import Keys._

object GUISettings {
  lazy val settings = Seq(
    javaOptions in run ++= (
      if (System.getProperty("os.name").contains("Mac"))
        Seq(
          "-Dapple.awt.graphics.UseQuartz=true",
          "-Dapple.awt.showGrowBox=true",
          "-Dapple.laf.useScreenMenuBar=true")
      else
        Seq())
  )
}
