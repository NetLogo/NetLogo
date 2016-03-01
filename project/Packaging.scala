import sbt._
import Keys._
import java.io.File

object Packaging {

  val settings = Seq(
    publishArtifact in Test := true,
    artifactName in Compile := { (_, _, _) => "NetLogo.jar" },
    artifactName in Test := { (_, _, _) => "NetLogo-tests.jar" },
    packageOptions <+= dependencyClasspath in Runtime map {
      classpath =>
        Package.ManifestAttributes((
          "Class-Path", classpath.files
            .map(f => f.getName)
            .filter(_.endsWith(".jar"))
            .mkString(" ")))}
  )
}
