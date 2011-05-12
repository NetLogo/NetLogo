import sbt._
import Process._
import java.net.URL

trait Jars extends DefaultProject {

  private val java5Path = "dist" / "java5" / "classes.jar"
  lazy val java5 = fileTask(Seq(java5Path)) {
    ("curl -s -o " + java5Path.asFile.toString + " http://ccl.northwestern.edu/devel/java5-classes.jar").!
    None
  }

  private val jarPaths: List[Path] =
    List("NetLogo.jar", "NetLogoLite.jar", "HubNet.jar", "BehaviorSpace.jar").map(path)

  private def build(config: String) {
    TrapExit(
      proguard.ProGuard.main(Array("@project/build/proguard/" + config + ".txt")),
      log)
  }

  private def addManifest(name: String, manifest: String) {
    ("jar umf project/build/proguard/" + manifest + ".txt " + name + ".jar").!
  }

  // it would be nicer if these were separate tasks - ST 3/29/11
  lazy val alljars =
    fileTask(jarPaths from (Set(java5Path) ++ ("project" / "build" / "proguard" * "*.txt").get)) {
      jarPaths.map(_.asFile.delete()) // don't risk updating existing, just start fresh
      build("main"); addManifest("NetLogo", "manifest")
      build("hubnet"); addManifest("HubNet", "manifesthubnet")
      build("lite")
      build("lab")
      None
    }.dependsOn(compile)

}
