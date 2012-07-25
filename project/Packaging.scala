import sbt._
import Keys._
import java.io.File

object Packaging {

  val settings = Seq(
    artifactName := { (_, _, _) => "NetLogo.jar" },
    packageOptions <+= dependencyClasspath in Runtime map {
      classpath =>
        Package.ManifestAttributes((
          "Class-Path", classpath.files
            .map(f => "lib/" + f.getName)
            .filter(_.endsWith(".jar"))
            .mkString(" ")))}
  )

}
