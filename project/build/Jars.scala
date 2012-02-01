import sbt._
import Process._
import java.net.URL

trait Jars extends DefaultProject {

  private val java5Path = "dist" / "java5" / "classes.jar"
  lazy val java5 = fileTask(Seq(java5Path)) {
    // we ought to use sbt's internal fetcher rather than invoking curl. we can clean it up when
    // we move to sbt 0.11 - ST 2/1/12
    val exitCode = List("curl", "-f", "-s", "-o", java5Path.asFile.toString, "http://ccl.northwestern.edu/devel/java5-classes.jar").!
    require(exitCode == 0, "exitCode = " + exitCode)
    None
  }

  private val jarPaths: List[Path] =
    List("NetLogo.jar", "NetLogoLite.jar", "HubNet.jar").map(path)

  private def build(config: String): Option[String] = {
    // ProGuard prints stuff straight to stdout, so we do the same
    println("building " + config + " jar")
    def doIt() {
      proguard.ProGuard.main(Array("@project/build/proguard/" + config + ".txt"))
    }
    TrapExit(doIt(), log) match {
      case 0 =>
        None
      case exitCode =>
        Some("exit code: " + exitCode.toString)
    }
  }

  private def addManifest(name: String, manifest: String) {
    ("jar umf project/build/proguard/" + manifest + ".txt " + name + ".jar").!
  }

  private val configs = ("project" / "build" / "proguard" * "*.txt").get

  // ProGuard will update an existing jar, but let's not risk that,
  // let's always use delete() first - ST 5/17/11

  lazy val mainJar =
    fileTask(Seq(path("NetLogo.jar")) from Set(java5Path) ++ configs) {
      path("NetLogo.jar").asFile.delete()
      build("main") orElse {
        addManifest("NetLogo", "manifest")
        None
      }
    }.dependsOn(compile)

  lazy val hubnetJar =
    fileTask(Seq(path("HubNet.jar")) from Set(java5Path) ++ configs) {
      path("HubNet.jar").asFile.delete()
      build("hubnet") orElse {
        addManifest("HubNet", "manifesthubnet")
        None
      }
    }.dependsOn(compile)

  lazy val liteJar =
    fileTask(Seq(path("NetLogoLite.jar")) from Set(java5Path) ++ configs) {
      path("NetLogoLite.jar").asFile.delete()
      build("lite") orElse { None }
    }.dependsOn(compile)

  lazy val alljars =
    task { None }.dependsOn(mainJar, hubnetJar, liteJar)

}
