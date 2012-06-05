import sbt._

// some notes:
//
// - DefaultMavenRepository and ScalaToolsReleases are included by DefaultProject, so
//   if we don't specify a repo it's coming from one of those two
//
// - some dependencies we pull from the CCL server because their authors don't make just the jar
//   available separately
//
// - the ModuleConfiguration trick, referencing a repo that isn't in scope at the top level, lets
//   you pull a library from a particular repo without causing that repo to be searched when
//   looking for other libraries (cause that might slow down "sbt update").

trait Libraries extends DefaultProject {

  /// regular dependencies
  val asm = "asm" % "asm-all" % "3.3.1"
  val picocontainer = "org.picocontainer" % "picocontainer" % "2.13.6"
  val mrjadapter = "steveroy" % "mrjadapter" % "1.2" from
    "http://ccl.northwestern.edu/devel/mrjadapter-1.2.jar"
  val quaqua = "ch.randelshofer" % "quaqua" % "7.3.4" from
    "http://ccl.northwestern.edu/devel/quaqua-7.3.4.jar"
  // 7.3.4 isn't the real version number, it's just "the version that comes
  // with Quaqua 7.3.4" - ST 9/2/11
  val swingLayout = "ch.randelshofer" % "swing-layout" % "7.3.4" from
    "http://ccl.northwestern.edu/devel/swing-layout-7.3.4.jar"
    
  /// test dependencies
  val jmock = "org.jmock" % "jmock" % "2.5.1" % "test"
  val jmockLegacy = "org.jmock" % "jmock-legacy" % "2.5.1" % "test"
  val jmockJUnit = "org.jmock" % "jmock-junit4" % "2.5.1" % "test"
  val scalacheck = "org.scalacheck" %% "scalacheck" % "1.9" % "test"
  val scalatest = "org.scalatest" %% "scalatest" % "1.8" % "test"

}
