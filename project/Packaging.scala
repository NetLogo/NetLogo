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
            .mkString(" ")))},
    packageBin in Compile <<= (packageBin in Compile, baseDirectory, cacheDirectory) map {
      (jar, base, cacheDir) =>
        val cache =
          FileFunction.cached(cacheDir / "NetLogo-jar", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              IO.copyFile(jar, base / "NetLogo.jar")
              Set(base / "NetLogo.jar")
          }
        cache(Set(jar))
        jar
      }
  )

}
