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
    packageBin in Compile <<= (packageBin in Compile, scalaInstance, baseDirectory, streams) map {
      (jar, instance, base, s) =>
        IO.delete(base / "NetLogoLite.jar")
        IO.delete(base / "HubNet.jar")
        IO.copyFile(jar, base / "NetLogo.jar")
        val scalaLibrary = instance.libraryJar.getAbsolutePath
        runProGuard(scalaLibrary, "lite", s.log)
        runProGuard(scalaLibrary, "hubnet", s.log)
        addManifest("HubNet", "manifesthubnet")
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
