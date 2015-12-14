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
            .mkString(" ")))},

    packageBin in Compile <<= (packageBin in Compile, scalaInstance, baseDirectory, streams) map {
      (jar, instance, base, s) =>
        val cache =
          FileFunction.cached(s.cacheDirectory / "jars", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              IO.copyFile(jar, base / "NetLogo.jar")
              Set(base / "NetLogo.jar")
          }
        cache(Set(jar))
        jar
      }
  )
}
