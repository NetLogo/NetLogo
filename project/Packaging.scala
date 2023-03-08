import sbt._
import Keys._
import java.io.File

object Packaging {

  val settings = Seq(
    Test / publishArtifact := true,
    packageOptions += {
      Package.ManifestAttributes((
        "Class-Path", (Runtime / dependencyClasspath).value.files
          .map(f => f.getName)
          .filter(_.endsWith(".jar"))
          .mkString(" ")))
    }
  )
}
