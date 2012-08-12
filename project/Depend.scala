import sbt._
import Keys._

// check whether the code is secretly engaging in forbidden dependencies!
// and rat on it if it is!

object Depend {

  val depend = TaskKey[Unit](
    "depend", "use Classycle to ferret out forbidden dependencies")

  val settings = Seq(dependTask)

  private lazy val dependTask =
    depend <<= (fullClasspath in Test, baseDirectory, classDirectory in Compile, classDirectory in Test, streams).map{
      (cp, base, classes, testClasses, s) =>
        IO.write(base / "tmp" / "depend.ddf", ddfContents)
        import classycle.dependency.DependencyChecker
        def main() = TrapExit(
          DependencyChecker.main(Array("-dependencies=@tmp/depend.ddf",
                                       classes.toString)),
          s.log)
        def test() = TrapExit(
          DependencyChecker.main(Array("-dependencies=@tmp/depend.ddf",
                                       testClasses.toString)),
          s.log)
        s.log.info("depend: " + classes.toString)
        main() match {
          case 0 =>
            s.log.info("depend: " + testClasses.toString)
            test() match { case 0 => ; case fail => sys.error(fail.toString) }
          case fail => sys.error(fail.toString)
        }
      }.dependsOn(compile in Test)

  private def ddfContents: String = {
    val buf = new StringBuilder
    def println(s: String) { buf ++= s + "\n" }

    // this needs to be manually kept in sync with dist/depend.graffle
    val packageDefs = Map(
      "" -> Nil,
      "agent" -> List("api"),
      "api" -> List("util"),
      "compiler" -> List("prim"),
      "generator" -> List("prim"),
      "headless" -> List("shape","workspace"),
      "job" -> List("nvm"),
      "lab" -> List("nvm"),
      "lex" -> List("api"),
      "nvm" -> List("agent"),
      "plot" -> List("api"),
      "prim" -> List("nvm"),
      "prim/etc" -> List("nvm"),
      "prim/file" -> List("nvm"),
      "prim/plot" -> List("nvm","plot"),
      "render" -> List("shape"),
      "shape" -> List("api"),
      "util" -> Nil,
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
      println("")
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
# just to put ShapesManagerFactory in - ST 2/27/09
[bad-AWT] = java.awt.* excluding [headless-AWT] java.awt.Frame

check [util+] independentOf [Sun-AWT]
check [headless+] independentOf [Sun-Swing] [bad-AWT]

### checks on external libraries

[ASM-free-zone] = org.nlogo.* excluding [generator]
check [ASM-free-zone] independentOf org.objectweb.*

check org.nlogo.* independentOf com.wolfram.*

[PicoContainer-free-zone] = org.nlogo.* excluding org.nlogo.util.Pico [headless]
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
