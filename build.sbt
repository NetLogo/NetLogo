scalaVersion := "2.9.2"

name := "NetLogo"

onLoadMessage := ""

resourceDirectory in Compile <<= baseDirectory(_ / "resources")

scalacOptions ++=
  "-deprecation -unchecked -Xfatal-warnings -Xcheckinit -encoding us-ascii"
  .split(" ").toSeq

javacOptions ++=
  "-bootclasspath dist/java5/classes.jar:dist/java5/ui.jar -g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.5 -target 1.5"
  .split(" ").toSeq

// this makes jar-building and script-writing easier
retrieveManaged := true

scalaSource in Compile <<= baseDirectory(_ / "src" / "main")

scalaSource in Test <<= baseDirectory(_ / "src" / "test")

javaSource in Compile <<= baseDirectory(_ / "src" / "main")

javaSource in Test <<= baseDirectory(_ / "src" / "test")

unmanagedSourceDirectories in Test <+= baseDirectory(_ / "src" / "tools")

unmanagedResourceDirectories in Compile <+= baseDirectory { _ / "resources" }

mainClass in (Compile, run) := Some("org.nlogo.app.App")

mainClass in (Compile, packageBin) := Some("org.nlogo.app.App")

sourceGenerators in Compile <+= Autogen.sourceGeneratorTask

resourceGenerators in Compile <+= I18n.resourceGeneratorTask
 
Extensions.extensionsTask

InfoTab.infoTabTask

ModelIndex.modelIndexTask

NativeLibs.nativeLibsTask

Depend.dependTask

threed := { System.setProperty("org.nlogo.is3d", "true") }

nogen  := { System.setProperty("org.nlogo.noGenerator", "true") }

moduleConfigurations += ModuleConfiguration("javax.media", JavaNet2Repository)

// The standard doc task doesn't handle mixed Scala/Java projects the way we would like.  Instead of
// passing all the sources to Scaladoc, it divided them up and called both Scaladoc and Javadoc.
// So I copy and pasted the code for the task and tweaked it. - ST 6/29/12
// sureiscute.com/images/cutepictures/I_Have_No_Idea_What_I_m_Doing.jpg
doc <<= (baseDirectory, cacheDirectory, compileInputs in Compile, testLoader in Test, streams) map {
  (base, cache, in, loader, s) =>
    val version =
      loader.loadClass("org.nlogo.api.Version")
        .getMethod("version")
        .invoke(null)
        .asInstanceOf[String]
        .replaceAll("NetLogo ", "")
    def generate(out: File, sourceFilter: File => Boolean, includeClassesOnClassPath: Boolean = false) {
      IO.delete(out)
      val options = Seq(
        "-doc-title", "NetLogo",
        "-doc-version", version,
        "-encoding", "us-ascii",
        "-sourcepath", (base / "src/main").toString,
        "-doc-source-url", "https://github.com/NetLogo/NetLogo/blob/" + version + "/src/main€{FILE_PATH}.scala")
      val cp = in.config.classpath.toList.filterNot(x => !includeClassesOnClassPath &&
                                                         x == in.config.classesDirectory)
      Doc(in.config.maxErrors, in.compilers.scalac)
        .cached(cache / "doc", "NetLogo",
                in.config.sources.filter(sourceFilter), cp, out, options, s.log)
      // compensate for issues.scala-lang.org/browse/SI-5388
      for(file <- Process("find " + out + " -name *.html").lines)
        IO.write(new File(file), IO.read(new File(file)).replaceAll("\\.java\\.scala", ".java"))
    }
    IO.createDirectory(base / "tmp")
    val out = base / "tmp" / "scaladoc"
    // these are the docs with everything
    generate(out, _ => true)
    // these are the docs we include with the User Manual
    val apiSources = Seq(
      "src/main/org/nlogo/app/App.scala",
      "src/main/org/nlogo/lite/InterfaceComponent.scala",
      "src/main/org/nlogo/lite/Applet.scala", 
      "src/main/org/nlogo/lite/AppletPanel.scala",
      "src/main/org/nlogo/headless/HeadlessWorkspace.scala",
      "src/main/org/nlogo/api/",
      "src/main/org/nlogo/agent/",
      "src/main/org/nlogo/workspace/",
      "src/main/org/nlogo/nvm/")
    generate(base / "docs" / "scaladoc",
             path => apiSources.exists(ok => path.toString.containsSlice(ok)),
             includeClassesOnClassPath = true) // since the set of sources isn't complete
    out
  }

libraryDependencies ++= Seq(
  "asm" % "asm-all" % "3.3.1",
  "org.picocontainer" % "picocontainer" % "2.13.6",
  "log4j" % "log4j" % "1.2.16",
  "javax.media" % "jmf" % "2.1.1e",
  "org.pegdown" % "pegdown" % "1.1.0",
  "org.parboiled" % "parboiled-java" % "1.0.2",
  "steveroy" % "mrjadapter" % "1.2" from "http://ccl.northwestern.edu/devel/mrjadapter-1.2.jar",
  "org.jhotdraw" % "jhotdraw" % "6.0b1" from "http://ccl.northwestern.edu/devel/jhotdraw-6.0b1.jar",
  "ch.randelshofer" % "quaqua" % "7.3.4" from "http://ccl.northwestern.edu/devel/quaqua-7.3.4.jar",
  "ch.randelshofer" % "swing-layout" % "7.3.4" from "http://ccl.northwestern.edu/devel/swing-layout-7.3.4.jar",
  "org.jogl" % "jogl" % "1.1.1" from "http://ccl.northwestern.edu/devel/jogl-1.1.1.jar",
  "org.gluegen-rt" % "gluegen-rt" % "1.1.1" from "http://ccl.northwestern.edu/devel/gluegen-rt-1.1.1.jar",
  "org.jmock" % "jmock" % "2.5.1" % "test",
  "org.jmock" % "jmock-legacy" % "2.5.1" % "test",
  "org.jmock" % "jmock-junit4" % "2.5.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.9" % "test",
  "org.scalatest" %% "scalatest" % "1.8" % "test"
)

all <<= (baseDirectory, streams) map { (base, s) =>
  s.log.info("making resources/system/dict.txt and docs/dict folder")
  IO.delete(base / "docs" / "dict")
  Process("python bin/dictsplit.py").!!
}

all <<= all.dependsOn(
  Extensions.extensions,
  NativeLibs.nativeLibs,
  ModelIndex.modelIndex,
  InfoTab.infoTab)
