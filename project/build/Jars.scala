import sbt._
import Process._
import java.net.URL

trait Jars extends DefaultProject {

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
    fileTask(Seq(path("NetLogo.jar")) from configs) {
      path("NetLogo.jar").asFile.delete()
      build("main") orElse {
        addManifest("NetLogo", "manifest")
        None
      }
    }.dependsOn(compile)

  lazy val hubnetJar =
    fileTask(Seq(path("HubNet.jar")) from configs) {
      path("HubNet.jar").asFile.delete()
      build("hubnet") orElse {
        addManifest("HubNet", "manifesthubnet")
        None
      }
    }.dependsOn(compile)

  lazy val liteJar =
    fileTask(Seq(path("NetLogoLite.jar")) from configs) {
      path("NetLogoLite.jar").asFile.delete()
      build("lite") orElse { None }
    }.dependsOn(compile)

  lazy val alljars =
    task { None }.dependsOn(mainJar, hubnetJar, liteJar)

}
