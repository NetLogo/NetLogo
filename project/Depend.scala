import sbt._
import Keys._

// check whether the code is secretly engaging in forbidden dependencies!
// and rat on it if it is!

object Depend {

  val depend = TaskKey[Unit](
    "depend", "use Classycle to ferret out forbidden dependencies")

  val settings = Seq(dependTask)

  // perhaps this voodoo will make intermittent failures-for-no-apparent-
  // reason stop. sigh - ST 8/12/12
  private val lock = new AnyRef

  private lazy val dependTask =
    depend <<= (fullClasspath in Test, classDirectory in Compile, classDirectory in Test, streams, thisProject).map{
      (cp, classes, testClasses, s, project) => lock.synchronized {
        s.log.info("begin depend: " + project.id)
        IO.write(file(".") / "tmp" / "depend.ddf", ddfContents)
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
            test() match {
              case 0 =>
              case fail =>
                s.log.info("depend failed: " + testClasses.toString)
                sys.error(fail.toString) }
          case fail =>
            s.log.info("depend failed: " + classes.toString)
            sys.error(fail.toString)
        }
        s.log.info("end depend: " + project.id)
      }}.dependsOn(compile in Test)

  private def ddfContents: String = {
    val buf = new StringBuilder
    def println(s: String) { buf ++= s + "\n" }

    // this needs to be manually kept in sync with dist/depend.graffle
    val packageDefs = Map(
      "" -> Nil,
      "agent" -> List("api"),
      "api" -> List("util"),
      "compile" -> List("parse"),
      "drawing" -> List("api"),
      "generate" -> List("prim"),
      "headless" -> List("mirror","workspace"),
      "job" -> List("nvm"),
      "lab" -> List("nvm"),
      "lex" -> List("api"),
      "mirror" -> List("drawing", "plot", "shape"),
      "nvm" -> List("agent"),
      "parse" -> List("parse0", "prim","prim/dead","prim/threed"),
      "parse0" -> List("api"),
      "plot" -> List("api"),
      "prim" -> List("nvm"),
      "prim/etc" -> List("nvm"),
      "prim/file" -> List("nvm"),
      "render" -> List("shape"),
      "review" -> List("mirror", "window"),
      "shape" -> List("api"),
      "tortoise" -> List("headless", "compiler", "prim/etc"),
      "util" -> Nil,
      "workspace" -> List("nvm", "plot", "drawing"))
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
### checks on AWT, Swing

[Sun-Swing] = javax.swing.* excluding javax.swing.tree.MutableTreeNode javax.swing.tree.DefaultMutableTreeNode
[Sun-AWT] = java.awt.*
[headless-AWT] = java.awt.geom.* java.awt.image.* java.awt.Color java.awt.Image java.awt.Shape java.awt.Graphics2D java.awt.Graphics java.awt.Stroke java.awt.Composite java.awt.BasicStroke java.awt.Point java.awt.Font java.awt.AlphaComposite java.awt.RenderingHints java.awt.Rectangle java.awt.FontMetrics java.awt.color.ColorSpace java.awt.Polygon java.awt.RenderingHints$Key
[bad-AWT] = java.awt.* excluding [headless-AWT]

check [util+] independentOf [Sun-AWT]
check [headless+] independentOf [Sun-Swing] [bad-AWT]

### checks on external libraries

[ASM-free-zone] = org.nlogo.* excluding [generate]
check [ASM-free-zone] independentOf org.objectweb.*

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
