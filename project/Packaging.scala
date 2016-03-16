import sbt._
import Keys._
import java.io.File

object Packaging {

  val settings = Seq(
    publishArtifact in Test := true,
    packageOptions <+= dependencyClasspath in Runtime map {
      classpath =>
        Package.ManifestAttributes((
          "Class-Path", classpath.files
            .map(f => f.getName)
            .filter(_.endsWith(".jar"))
            .mkString(" ")))}
  )
}
