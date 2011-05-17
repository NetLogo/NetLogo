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
    // ProGuard prints stuff straight to stdout, so we do the same
    println("building " + config + " jar")
    TrapExit(
      proguard.ProGuard.main(Array("@project/build/proguard/" + config + ".txt")),
      log)
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
      build("main")
      addManifest("NetLogo", "manifest")
      None
    }.dependsOn(compile)

  lazy val hubnetJar =
    fileTask(Seq(path("HubNet.jar")) from Set(java5Path) ++ configs) {
      path("HubNet.jar").asFile.delete()
      build("hubnet")
      addManifest("HubNet", "manifesthubnet")
      None
    }.dependsOn(compile)

  lazy val liteJar =
    fileTask(Seq(path("NetLogoLite.jar")) from Set(java5Path) ++ configs) {
      path("NetLogoLite.jar").asFile.delete()
      build("lite")
      None
    }.dependsOn(compile)

  lazy val labJar =
    fileTask(Seq(path("BehaviorSpace.jar")) from Set(java5Path) ++ configs) {
      path("BehaviorSpace.jar").asFile.delete()
      build("lab")
      None
    }.dependsOn(mainJar)

  lazy val alljars =
    task { None }.dependsOn(mainJar, hubnetJar, liteJar, labJar)

}
