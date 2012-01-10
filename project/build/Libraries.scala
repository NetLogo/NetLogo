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

  /// additional repos
  object Repos {
    val jmfRepo = "java.net" at "http://download.java.net/maven/2"
  }
  val jmfConfig = ModuleConfiguration("javax.media", Repos.jmfRepo)

  /// regular dependencies
  val asm = "asm" % "asm-all" % "3.3.1"
  val picocontainer = "org.picocontainer" % "picocontainer" % "2.13.6"
  val log4j = "log4j" % "log4j" % "1.2.16"
  val jmf = "javax.media" % "jmf" % "2.1.1e"
  val pegdown = "org.pegdown" % "pegdown" % "1.1.0"
  val parboiled = "org.parboiled" % "parboiled-java" % "1.0.2"
  val mrjadapter = "steveroy" % "mrjadapter" % "1.2" from
    "http://ccl.northwestern.edu/devel/mrjadapter-1.2.jar"
  val jhotdraw = "org.jhotdraw" % "jhotdraw" % "6.0b1" from
    "http://ccl.northwestern.edu/devel/jhotdraw-6.0b1.jar"
  val quaqua = "ch.randelshofer" % "quaqua" % "7.3.4" from
    "http://ccl.northwestern.edu/devel/quaqua-7.3.4.jar"
  val jogl = "org.jogl" % "jogl" % "1.1.1" from
    "http://ccl.northwestern.edu/devel/jogl-1.1.1.jar"
  val gluegen = "org.gluegen-rt" % "gluegen-rt" % "1.1.1" from
    "http://ccl.northwestern.edu/devel/gluegen-rt-1.1.1.jar"
  // 7.3.4 isn't the real version number, it's just "the version that comes
  // with Quaqua 7.3.4" - ST 9/2/11
  val swingLayout = "ch.randelshofer" % "swing-layout" % "7.3.4" from
    "http://ccl.northwestern.edu/devel/swing-layout-7.3.4.jar"
    
  /// native libraries for JOGL and Quaqua
  private val libs_mac = Seq("lib" / "Mac OS X" / "libjogl.jnilib",
                             "lib" / "Mac OS X" / "libjogl_awt.jnilib",
                             "lib" / "Mac OS X" / "libgluegen-rt.jnilib",
                             "lib" / "Mac OS X" / "libquaqua.jnilib",
                             "lib" / "Mac OS X" / "libquaqua64.jnilib")
  private val libs_win = Seq("lib" / "Windows" / "jogl.dll",
                             "lib" / "Windows" / "jogl_awt.dll",
                             "lib" / "Windows" / "gluegen-rt.dll")
  private val libs_x86 = Seq("lib" / "Linux-x86" / "libjogl.so",
                             "lib" / "Linux-x86" / "libjogl_awt.so",
                             "lib" / "Linux-x86" / "libgluegen-rt.so")
  private val libs_amd64 = Seq("lib" / "Linux-amd64" / "libjogl.so",
                               "lib" / "Linux-amd64" / "libjogl_awt.so",
                               "lib" / "Linux-amd64" / "libgluegen-rt.so")
  private val libs_all = libs_mac ++ libs_win ++ libs_x86 ++ libs_amd64
  lazy val nativeJoglLibs = fileTask(libs_all) {
    sbt.FileUtilities.createDirectory("lib" / "Mac OS X", log)
    sbt.FileUtilities.createDirectory("lib" / "Windows", log)
    sbt.FileUtilities.createDirectory("lib" / "Linux-amd64", log)
    sbt.FileUtilities.createDirectory("lib" / "Linux-x86", log)
    for(path <- libs_all) {
      val pathString = path.asFile.toString
      val filename =
        pathString.reverse.takeWhile(_ != '/').mkString
          .replaceFirst("\\.", (if(pathString.containsSlice("quaqua")) "-7.3.4."
                                else "-1.1.1.").reverse)
          .reverse
      val url = "http://ccl.northwestern.edu/devel/" + filename
      import Process._
      List("curl", "-s", "-o", pathString, url).!
    }
    None
  }

  /// test dependencies
  val jmock = "org.jmock" % "jmock" % "2.5.1" % "test"
  val jmockLegacy = "org.jmock" % "jmock-legacy" % "2.5.1" % "test"
  val jmockJUnit = "org.jmock" % "jmock-junit4" % "2.5.1" % "test"
  val scalacheck = "org.scala-tools.testing" % "scalacheck_2.9.1" % "1.9" % "test"
  val scalatest = "org.scalatest" % "scalatest_2.9.1" % "1.6.1" % "test"

}
