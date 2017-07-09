import sbt._
import Keys._
import java.io.File

object Packaging {

  val settings = Seq(
    publishArtifact in Test := true,
    packageOptions += {
      Package.ManifestAttributes((
        "Class-Path", (dependencyClasspath in Runtime).value.files
          .map(f => f.getName)
          .filter(_.endsWith(".jar"))
          .mkString(" ")))
    }
  )
}
