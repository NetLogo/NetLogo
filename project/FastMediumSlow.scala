import sbt._
import Keys._

object FastMediumSlow {

  val FastTest = config("fast") extend(Test)
  val MediumTest = config("medium") extend(Test)
  val SlowTest = config("slow") extend(Test)

  val configs = Seq(FastTest, MediumTest, SlowTest)

  lazy val settings =
    inConfig(FastTest)(Defaults.testTasks) ++
    inConfig(MediumTest)(Defaults.testTasks) ++
    inConfig(SlowTest)(Defaults.testTasks) ++
    Seq(
      testOptions in FastTest :=
        Seq(Tests.Filter(fastFilter((fullClasspath in Test).value, _))),
      testOptions in MediumTest :=
        Seq(Tests.Filter(mediumFilter((fullClasspath in Test).value, _))),
      testOptions in SlowTest :=
        Seq(Tests.Filter(slowFilter((fullClasspath in Test).value, _)))
    )

  private def fastFilter(path: Classpath, name: String): Boolean = !slowFilter(path, name)
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
