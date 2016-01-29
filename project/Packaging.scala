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
        val cacheDir = s.cacheDirectory
        val cache =
          FileFunction.cached(cacheDir / "jars", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              IO.delete(base / "NetLogoLite.jar")
              IO.delete(base / "HubNet.jar")
              IO.copyFile(jar, base / "NetLogo.jar")
              val scalaLibrary = instance.libraryJar.getAbsolutePath
              runProGuard(scalaLibrary, "lite", s.log)
              runProGuard(scalaLibrary, "hubnet", s.log)
              addManifest("HubNet", "manifesthubnet")
              Set(base / "NetLogo.jar",
                  base / "NetLogoLite.jar",
                  base / "HubNet.jar")
          }
        cache(Set(jar))
        jar
      }
  )

  private def runProGuard(scalaLibraryPath: String, config: String, log: Logger) {
    log.info("building " + config + " jar")
    def doIt() {
      System.setProperty("org.nlogo.scala-library", scalaLibraryPath)
      proguard.ProGuard.main(Array("@project/proguard/" + config + ".txt"))
    }
    assert(TrapExit(doIt(), log) == 0)
  }

  private def addManifest(name: String, manifest: String) {
    ("jar umf project/proguard/" + manifest + ".txt " + name + ".jar").!
  }


}
