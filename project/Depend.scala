import sbt._
import Keys._

// check whether the code is secretly engaging in forbidden dependencies!
// and rat on it if it is!

object Depend {

  val depend = taskKey[Unit](
    "use Classycle to ferret out forbidden dependencies")

  lazy val dependTask =
    depend := {
      val _ = (compile in Test).value
      val s = streams.value
      val classes = (classDirectory in Compile).value.toString
      val testClasses = (classDirectory in Test).value.toString
      val ddfFile = baseDirectory.value / "tmp" / "depend.ddf"
      IO.write(ddfFile, ddfContents)
      import classycle.dependency.DependencyChecker
      def main() = TrapExit(
        DependencyChecker.main(Array("-dependencies=@" + ddfFile.getPath,
                                     classes)),
        s.log)
      def test() = TrapExit(
        DependencyChecker.main(Array("-dependencies=@" + ddfFile.getPath,
                                     testClasses)),
        s.log)
      main() match {
        case 0 => test() match { case 0 => ; case fail => sys.error(fail.toString) }
        case fail => sys.error(fail.toString)
      }
    }

  private def ddfContents: String = {
    val buf = new StringBuilder
    def println(s: String) { buf ++= s + "\n" }

    // this needs to be manually kept in sync with dist/depend.graffle
    val packageDefs = Map(
      "" -> Nil,
      "agent" -> List("api"),
      "api" -> List("core", "core/model", "util"),
      "app" -> List("ide", "window"),
      "awt" -> Nil,
      "compiler" -> List("core/prim","prim","prim/dead","prim/threed"),
      "core" -> Nil,
      "core/prim" -> List("core"),
      "core/model" -> List("core"),
      "editor" -> List("core"),
      "generate" -> List("prim","prim/dead","prim/threed"),
      "generate" -> List("prim"), // for headless
      "gl/render" -> List("shape"),
      "gl/view" -> List("gl/render","window"),
      "headless" -> List("core/model", "fileformat", "shape", "workspace", "headless/test"),
      "headless/hubnet" -> List("headless", "hubnet/protocol"),
      "headless/test" -> List("core"),
      "hubnet/client" -> List("hubnet/connection","hubnet/mirroring","hubnet/protocol","render","fileformat","widget"),
      "hubnet/connection" -> List("api"),
      "hubnet/mirroring" -> List("api"),
      "hubnet/protocol" -> List("api"),
      "hubnet/server" -> List("workspace","hubnet/connection","hubnet/mirroring","hubnet/protocol","fileformat"),
      "hubnet/server/gui" -> List("hubnet/server","window"),
      "ide" -> List("editor", "api", "window"),
      "job" -> List("nvm"),
      "lab" -> List("nvm"),
      "lab/gui" -> List("lab","window"),
      "lex" -> List("api"),
      "lite" -> List("window"),
      "log" -> List("api"),
      "mc" -> List("workspace", "swing"),
      "nvm" -> List("agent"),
      "parse" -> List("core", "core/prim"),
      "plot" -> List("api"),
      "prim" -> List("nvm"),
      "prim/dead" -> List("nvm"),
      "prim/etc" -> List("nvm"),
      "prim/file" -> List("nvm"),
      "prim/gui" -> List("window"),
      "prim/hubnet" -> List("nvm"),
      "prim/plot" -> List("nvm","plot"),
      "prim/threed" -> List("nvm"),
      "properties" -> List("window"),
      "render" -> List("shape"),
      "fileformat" -> List("api", "core", "core/model"),
      "sdm" -> List("api", "fileformat"),
      "sdm/gui" -> List("sdm","window"),
      "shape" -> List("api"),
      "shape/editor" -> List("shape","swing"),
      "swing" -> List("awt"),
      "util" -> Nil,
      "widget" -> List("window"),
      "window" -> List("core/model","editor","log","fileformat","shape","swing","workspace"),
      "workspace" -> List("fileformat", "nvm", "plot"))
    case class Package(val dir: String, var depends: Set[Package]) {
      def ancestors:Set[Package] = depends ++ depends.flatMap(_.ancestors)
    }
    val allPackages: Set[Package] = Set() ++ packageDefs.keys.map(Package(_,Set()))
    for(p <- allPackages)
      p.depends = allPackages.filter(p2 => packageDefs(p.dir).contains(p2.dir))
    def generate(p: Package) {
      val name = p.dir.replaceAll("/",".")
      println(s"[$name] = org.nlogo.$name.* excluding org.nlogo.$name.*.*")
      println(s"[$name+] = [$name]" + p.depends.map(p2 => "[" + p2.dir.replaceAll("/",".") + "+]").mkString(" "," ",""))
      println(s"[$name-] = org.nlogo.* excluding [$name+]")
      println(s"check [$name] independentOf [$name-]")
      println("")
    }
    def generateFooter() {
      println("""
### HubNet client dependencies

[HubNet-client] = [hubnet.client] [hubnet.connection] [hubnet.mirroring] [hubnet.protocol] excluding org.nlogo.hubnet.client.App org.nlogo.hubnet.client.App$ org.nlogo.hubnet.client.ClientApp
check [HubNet-client] independentOf [workspace]
# Someday this should be completely independent, not just directly independent - ST 12/4/08
check [HubNet-client] directlyIndependentOf [nvm]

### checks for packages with only one direct parent

[not-job-not-workspace] = org.nlogo.* excluding [job] [workspace]
check [not-job-not-workspace] directlyIndependentOf [job]

### checks on AWT, Swing, JOGL

[Sun-Swing] = javax.swing.* excluding javax.swing.tree.MutableTreeNode javax.swing.tree.DefaultMutableTreeNode javax.swing.tree.TreeNode
[Sun-AWT] = java.awt.*
[headless-AWT] = java.awt.geom.* java.awt.image.* java.awt.Color java.awt.Image java.awt.Shape java.awt.Graphics2D java.awt.Graphics java.awt.Stroke java.awt.Composite java.awt.BasicStroke java.awt.Point java.awt.Font java.awt.AlphaComposite java.awt.RenderingHints java.awt.Rectangle java.awt.FontMetrics java.awt.color.ColorSpace java.awt.Polygon java.awt.RenderingHints$Key
# as a special case, we allow referring to java.awt.Frame, because ShapesManagerFactory
# mentions it in its constructor, and I don't want to have to make a whole new package
# just to put ShapesManagerFactory in - ST 2/27/09
[bad-AWT] = java.awt.* excluding [headless-AWT] java.awt.Frame

check [util+] independentOf [Sun-AWT]
check [awt+] independentOf [Sun-Swing]
check [headless+] independentOf [Sun-Swing] [bad-AWT]
check [gl.render] independentOf [Sun-Swing] [bad-AWT]

### checks on external libraries

[JOGL-free-zone] = org.nlogo.* excluding [gl.render] [gl.view]
[JOGL] = net.java.games.* com.jogamp.opengl.*
check [JOGL-free-zone] independentOf [JOGL]

[ASM-free-zone] = org.nlogo.* excluding [generate]
check [ASM-free-zone] independentOf org.objectweb.*

check org.nlogo.* independentOf com.wolfram.*

[JHotDraw-free-zone] = org.nlogo.* excluding [sdm.gui]
check [JHotDraw-free-zone] independentOf org.jhotdraw.*

[Log4J-free-zone] = org.nlogo.* excluding [log] org.nlogo.app.App org.nlogo.lite.InterfaceComponent
check [Log4J-free-zone] directlyIndependentOf org.apache.log4j.*

[PicoContainer-free-zone] = org.nlogo.* excluding org.nlogo.util.Pico [app] [headless]
check [PicoContainer-free-zone] independentOf org.picocontainer.*

"""
              )
    }

    var done = List(allPackages.find(_.dir == "").get)
    def eligible(p:Package) = !done.contains(p) && p.ancestors.forall(done.contains(_))
    while(true) {
      allPackages.filter(!_.dir.isEmpty).find(eligible) match {
        case None =>
          generateFooter()
          return buf.toString
        case Some(p) =>
          generate(p)
          done = p :: done
      }
    }
    throw new IllegalStateException  // unreachable
  }
}
