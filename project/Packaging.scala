import sbt._
import Keys._
import java.io.File

object Packaging {

  lazy val moreJars = TaskKey[Set[File]]("more-jars", "build NetLogoLite.jar and HubNet.jar")

  val settings = Seq(
    artifactName := { (_, _, _) => "NetLogo.jar" },
    packageOptions <+= dependencyClasspath in Runtime map {
      classpath =>
        Package.ManifestAttributes((
          "Class-Path", classpath.files
            .map(f => "lib/" + f.getName)
            .filter(_.endsWith(".jar"))
            .mkString(" ")))},
    moreJars <<= (packageBin in Compile, scalaInstance, target, cacheDirectory, streams) map {
      (jar, instance, target, cacheDir, s) =>
        val cache =
          FileFunction.cached(cacheDir / "jars", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              IO.delete(target / "NetLogoLite.jar")
              IO.delete(target / "HubNet.jar")
              val scalaLibrary = instance.libraryJar.getAbsolutePath
              runProGuard(scalaLibrary, "lite", s.log)
              runProGuard(scalaLibrary, "hubnet", s.log)
              addManifest("HubNet", "manifesthubnet")
              Set(target / "NetLogoLite.jar",
                  target / "HubNet.jar")
          }
        cache(Set(jar))
      }
  )

  private def runProGuard(scalaLibraryPath: String, config: String, log: Logger) {
    log.info("building " + config + " jar")
    val javaLibraryPath = System.getProperty("java.home") +
      (if (System.getProperty("os.name").startsWith("Mac"))
         "/../Classes/classes.jar"
       else
         "/lib/rt.jar")
    def doIt() {
      System.setProperty("org.nlogo.java-library", javaLibraryPath)
      System.setProperty("org.nlogo.scala-library", scalaLibraryPath)
      proguard.ProGuard.main(Array("@project/proguard/" + config + ".txt"))
    }
    assert(TrapExit(doIt(), log) == 0)
  }

  private def addManifest(name: String, manifest: String) {
    ("jar umf project/proguard/" + manifest + ".txt target/" + name + ".jar").!
  }


}
