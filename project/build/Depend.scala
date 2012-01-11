import sbt._

// check whether the code is secretly engaging in forbidden dependencies!
// and rat on it if it is!

trait Depend extends DefaultProject {

  val ddfPath = "devel" / "depend.ddf"

  lazy val ddf = fileTask(Seq(ddfPath)) {
    FileUtilities.writeStream(ddfPath.asFile, log) {
      stream: java.io.OutputStream =>
        val writer = new java.io.PrintWriter(stream)
        writeDdf(writer)
        writer.flush()
        None
    }
  }

  lazy val depend = task {
    import classycle.dependency.DependencyChecker
    def main() = TrapExit(
      DependencyChecker.main(Array("-dependencies=@devel/depend.ddf",
                                   mainCompileConfiguration.outputDirectory.toString)),
      log)
    def test() = TrapExit(
      DependencyChecker.main(Array("-dependencies=@devel/depend.ddf",
                                   testCompileConfiguration.outputDirectory.toString)),
      log)
    main() match {
      case 0 => test() match { case 0 => None ; case fail => Some(fail.toString) }
      case fail => Some(fail.toString)
    }
  }.dependsOn(testCompile, ddf)

  private def writeDdf(w: java.io.PrintWriter) {
    import w.println

    // this needs to be manually kept in sync with devel/depend.graffle
    val packageDefs = Map(
      "" -> Nil,
      "agent" -> List("api"),
      "api" -> List("util"),
      "app" -> List("window"),
      "awt" -> Nil,
      "compiler" -> List("prim","prim/threed"),
      "editor" -> Nil,
      "generator" -> List("prim","prim/threed"),
      "headless" -> List("shape","workspace"),
      "job" -> List("nvm"),
      "lab" -> List("nvm"),
      "lab/gui" -> List("lab","window"),
      "lex" -> List("api"),
      "lite" -> List("window"),
      "nvm" -> List("agent"),
      "plot" -> List("api"),
      "prim" -> List("nvm"),
      "prim/etc" -> List("nvm"),
      "prim/file" -> List("nvm"),
      "prim/gui" -> List("window"),
      "prim/plot" -> List("nvm","plot"),
      "prim/threed" -> List("nvm"),
      "properties" -> List("window"),
      "render" -> List("shape"),
      "shape" -> List("api"),
      "shape/editor" -> List("shape","swing"),
      "swing" -> List("awt"),
      "util" -> Nil,
      "widget" -> List("window"),
      "window" -> List("editor","shape","swing","workspace"),
      "workspace" -> List("nvm", "plot"))
    case class Package(val dir: String, var depends: Set[Package]) {
      def ancestors:Set[Package] = depends ++ depends.flatMap(_.ancestors)
    }
    val allPackages: Set[Package] = Set() ++ packageDefs.keys.map(Package(_,Set()))
    for(p <- allPackages)
      p.depends = allPackages.filter(p2 => packageDefs(p.dir).contains(p2.dir))
    def generate(p: Package) {
      val name = p.dir.replaceAll("/",".")
      println("[" + name + "] = org.nlogo." + name + ".* excluding org.nlogo." + name + ".*.*")
      println("[" + name + "+] = [" + name + "]" + p.depends.map(p2 => "[" + p2.dir.replaceAll("/",".") + "+]").mkString(" "," ",""))
      println("[" + name + "-] = org.nlogo.* excluding [" + name + "+]")
      println("check [" + name + "] independentOf [" + name + "-]")
      println
    }
    def generateFooter() {
      println("""
### checks for packages with only one direct parent

[not-job-not-workspace] = org.nlogo.* excluding [job] [workspace]
check [not-job-not-workspace] directlyIndependentOf [job]

### checks on AWT, Swing

[Sun-Swing] = javax.swing.* excluding javax.swing.tree.MutableTreeNode javax.swing.tree.DefaultMutableTreeNode
[Sun-AWT] = java.awt.*
[headless-AWT] = java.awt.geom.* java.awt.image.* java.awt.Color java.awt.Image java.awt.Shape java.awt.Graphics2D java.awt.Graphics java.awt.Stroke java.awt.Composite java.awt.BasicStroke java.awt.Point java.awt.Font java.awt.AlphaComposite java.awt.RenderingHints java.awt.Rectangle java.awt.FontMetrics java.awt.color.ColorSpace java.awt.Polygon java.awt.RenderingHints$Key
# as a special case, we allow referring to java.awt.Frame, because ShapesManagerFactory
# mentions it in its constructor, and I don't want to have to make a whole new package
# just to put ShapesManagaerFactory in - ST 2/27/09
[bad-AWT] = java.awt.* excluding [headless-AWT] java.awt.Frame

check [util+] independentOf [Sun-AWT]
check [awt+] independentOf [Sun-Swing]
check [headless+] independentOf [Sun-Swing] [bad-AWT]

### checks on external libraries

[ASM-free-zone] = org.nlogo.* excluding [generator]
check [ASM-free-zone] independentOf org.objectweb.*

check org.nlogo.* independentOf com.wolfram.*

[MRJAdapter-free-zone] = org.nlogo.* excluding [app] [swing]
check [MRJAdapter-free-zone] directlyIndependentOf net.roydesign.*

[Quaqua-free-zone] = org.nlogo.* excluding org.nlogo.swing.Utils
check [Quaqua-free-zone] directlyIndependentOf ch.randelshofer.*

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
          return
        case Some(p) =>
          generate(p)
          done = p :: done
      }
    }
  }
}
