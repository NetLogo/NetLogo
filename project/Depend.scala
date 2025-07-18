import sbt._
import Keys._

// check whether the code is secretly engaging in forbidden dependencies!
// and rat on it if it is!

object Depend {

  val depend = taskKey[Unit](
    "use Classycle to ferret out forbidden dependencies")

  lazy val dependTask =
    depend := {
      val _ = (Test / compile).value
      val s = streams.value
      val classes = (Compile / classDirectory).value.toString
      val testClasses = (Test / classDirectory).value.toString
      val ddfFile = baseDirectory.value / "tmp" / "depend.ddf"
      IO.write(ddfFile, ddfContents)
      import classycle.Analyser
      import classycle.dependency.{ DefaultResultRenderer, DependencyChecker }
      import java.io.{ PrintWriter, StringWriter }
      import java.lang.Object
      import java.util.HashMap
      import scala.io.Source
      // if the output goes straight to System.out, it sometimes doesn't print everything,
      // so you can't tell what went wrong (Isaac B 2/25/25)
      val writer = new StringWriter
      def main(): Boolean = {
        new DependencyChecker(new Analyser(Array(classes)), Source.fromFile(ddfFile).getLines.mkString("\n"),
                              new HashMap[Object, Object],
                              new DefaultResultRenderer()).check(new PrintWriter(writer))
      }
      def test(): Boolean = {
        new DependencyChecker(new Analyser(Array(testClasses)), Source.fromFile(ddfFile).getLines.mkString("\n"),
                              new HashMap[Object, Object],
                              new DefaultResultRenderer()).check(new PrintWriter(writer))
      }
      val result = main()
      if (writer.toString.nonEmpty)
        println(writer.toString)
      assert(result)
    }

  private def ddfContents: String = {
    val buf = new StringBuilder
    def println(s: String): Unit = { buf ++= s + "\n" }

    // this needs to be manually kept in sync with dist/depend.graffle
    val packageDefs = Map(
      "" -> Nil,
      "agent" -> List("api", "log"),
      "analytics" -> List("core"),
      "api" -> List("core", "core/model", "core/prim", "util"),
      "app" -> List("app/codetab", "app/common", "app/infotab", "app/interfacetab", "app/tools", "headless", "log"),
      "app/codetab" -> List("app/common"),
      "app/infotab" -> List("app/common"),
      "app/interfacetab" -> List("app/common", "app/tools", "log"),
      "app/tools" -> List("app/common", "app/infotab"),
      "app/common" -> List("ide", "window"),
      "awt" -> Nil,
      "compile" -> List("compile/api", "compile/prim", "prim", "prim/dead", "prim/threed"),
      "compile/api" -> List("core", "nvm", "core/prim"),
      "compile/optimize" -> List("compile/middle"),
      "compile/middle"   -> List("compile"),
      "compile/middle/optimize" -> List("api", "compile/api", "nvm", "prim"),
      "compile/back" -> List("compile"),
      "compile/prim" -> List("core"),
      "core" -> Nil,
      "core/prim" -> List("core"),
      "core/prim/etc" -> List("core"),
      "core/prim/hubnet" -> List("core"),
      "core/model" -> List("core"),
      "drawing" -> List("api"),
      "editor" -> List("core", "swing"),
      "generate" -> List("prim", "prim/dead", "prim/threed"),
      "generate" -> List("prim"), // for headless
      "gl/render" -> List("shape"),
      "gl/view" -> List("gl/render","window"),
      "headless" -> List("core/model", "drawing", "fileformat", "shape", "workspace", "headless/test"),
      "headless/hubnet" -> List("headless", "hubnet/protocol"),
      "headless/test" -> List("api", "core"),
      "hubnet/client" -> List("hubnet/connection", "hubnet/mirroring", "hubnet/protocol", "render", "fileformat", "window"),
      "hubnet/connection" -> List("api"),
      "hubnet/mirroring" -> List("api"),
      "hubnet/protocol" -> List("api"),
      "hubnet/server" -> List("workspace", "hubnet/connection", "hubnet/mirroring", "hubnet/protocol", "fileformat"),
      "hubnet/server/gui" -> List("hubnet/server", "window"),
      "ide" -> List("api", "window"),
      "job" -> List("nvm"),
      "lab" -> List("nvm"),
      "lab/gui" -> List("lab","window"),
      "lex" -> List("api"),
      "lite" -> List("window"),
      "log" -> List("api"),
      "nvm" -> List("agent"),
      "parse" -> List("core", "core/prim", "core/prim/etc", "core/prim/hubnet", "util"),
      "plot" -> List("api"),
      "prim" -> List("nvm"),
      "prim/dead" -> List("nvm"),
      "prim/etc" -> List("nvm"),
      "prim/file" -> List("nvm"),
      "prim/gui" -> List("window"),
      "prim/hubnet" -> List("nvm"),
      "prim/plot" -> List("nvm", "plot"),
      "prim/threed" -> List("nvm"),
      "render" -> List("shape"),
      "fileformat" -> List("api", "core", "core/model"),
      "sdm" -> List("api", "fileformat"),
      "sdm/gui" -> List("sdm", "window"),
      "shape" -> List("api", "theme"),
      "shape/editor" -> List("analytics", "shape", "swing"),
      "swing" -> List("awt", "core", "theme"),
      "util" -> Nil,
      "theme" -> List("api"),
      "window" -> List("analytics", "core/model", "editor", "fileformat", "log", "shape", "swing", "workspace"),
      "workspace" -> List("analytics", "fileformat", "nvm", "plot"))
    case class Package(val dir: String, var depends: Set[Package]) {
      def ancestors:Set[Package] = depends ++ depends.flatMap(_.ancestors)
    }
    val allPackages: Set[Package] = Set() ++ packageDefs.keys.map(Package(_,Set()))
    for(p <- allPackages)
      p.depends = allPackages.filter(p2 => packageDefs(p.dir).contains(p2.dir))
    def generate(p: Package): Unit = {
      val name = p.dir.replaceAll("/",".")
      println(s"[$name] = org.nlogo.$name.* excluding org.nlogo.$name.*.*")
      println(s"[$name+] = [$name]" + p.depends.map(p2 => "[" + p2.dir.replaceAll("/",".") + "+]").mkString(" "," ",""))
      println(s"[$name-] = org.nlogo.* excluding [$name+]")
      println(s"check [$name] independentOf [$name-]")
      println("")
    }
    def generateFooter(): Unit = {
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
