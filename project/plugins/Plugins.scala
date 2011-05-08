import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val jflex = "de.jflex" % "jflex" % "1.4.3"
  val lessRepo = "lessis repo" at "http://repo.lessis.me"
  val n2a = "eu.getintheloop" % "sbt-native2ascii-plugin" % "0.1.0"
  val proguard = "net.sf.proguard" % "proguard" % "4.6" from
    "http://ccl.northwestern.edu/devel/proguard-4.6.jar"
  val pmd = "pmd" % "pmd" % "4.2rc1" from
    "http://ccl.northwestern.edu/devel/pmd-4.2rc1.jar"
  val classycle = "classycle" % "classycle" % "1.3.2" from
    "http://ccl.northwestern.edu/devel/classycle-1.3.2.jar"
  val perfanal = "perfanal" % "perfanal" % "1.0" from
    "http://ccl.northwestern.edu/devel/PerfAnal.jar"
}
