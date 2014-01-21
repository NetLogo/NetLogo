import sbt._
import Keys._

// At the time we first made this, the state of sbt/ScalaTest integration was
// more primitive and this was the best way we could figure out how to do it.
// Now we could maybe instead use a Tag or something. But the marker trait
// approach works, so, leaving it alone for now.  If it ever becomes difficult
// to maintain, consider replacing it. - ST 12/5/13

object FastMediumSlow {

  val configs =
    Seq("fast", "medium", "slow")
      .map(config(_).extend(Test))

  private val filters =
    Seq(fastFilter _, mediumFilter _, slowFilter _)

  val settings =
    (configs zip filters).flatMap{
      case (config, filter) =>
        inConfig(config)(Defaults.testTasks) :+
        (testOptions in config :=
          Seq(Tests.Filter(filter((fullClasspath in Test).value, _))))
    }

  private def fastFilter(path: Classpath, name: String): Boolean =
    !slowFilter(path, name)

  private def mediumFilter(path: Classpath, name: String): Boolean =
    fastFilter(path, name) ||
    name == "org.nlogo.headless.lang.TestReporters" ||
    name == "org.nlogo.headless.lang.TestCommands"

  private def slowFilter(path: Classpath, name: String): Boolean = {
    val jars = path.files.map(_.asURL).toArray[java.net.URL]
    val loader = new java.net.URLClassLoader(jars, getClass.getClassLoader)
    def clazz(name: String) = Class.forName(name, false, loader)
    clazz("org.nlogo.util.SlowTest").isAssignableFrom(clazz(name))
  }

}
