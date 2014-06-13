import java.io.File
import sbt._
import Keys._

object Scaladoc {

  val docSmaller = TaskKey[File]("doc-smaller", "for docs/scaladoc/")
  val netlogoVersion = TaskKey[String]("netlogo-version", "from api.Version")

  val settings = Seq(
    netlogoVersion <<= (testLoader in Test) map {
      _.loadClass("org.nlogo.api.Version")
       .getMethod("version")
       .invoke(null).asInstanceOf[String]
       .replaceFirst("NetLogo ", "")
    },
    scalacOptions in (Compile, doc) <++= (baseDirectory, netlogoVersion) map {
      (base, version) =>
        Seq("-encoding", "us-ascii") ++
        Seq("-sourcepath", base.getAbsolutePath) ++
        Opts.doc.title("NetLogo") ++
        Opts.doc.version(version) ++
        Opts.doc.sourceUrl("https://github.com/NetLogo/NetLogo/blob/" +
                           version + "€{FILE_PATH}.scala")
    },
    doc in Compile ~= mungeScaladocSourceUrls,
    // The regular doc task includes doc for the entire main source tree.  But for the NetLogo
    // web site we want to document only select classes.  So I copy and pasted
    // the code for the main doc task and tweaked it. - ST 6/29/12, 7/18/12
    // sureiscute.com/images/cutepictures/I_Have_No_Idea_What_I_m_Doing.jpg
    docSmaller <<= (baseDirectory, cacheDirectory, scalacOptions in (Compile, doc), compileInputs in Compile, netlogoVersion, streams) map {
      (base, cache, options, inputs, version, s) =>
        val apiSources = Seq(
          "app/App.scala", "headless/HeadlessWorkspace.scala",
          "lite/InterfaceComponent.scala", "lite/Applet.scala", "lite/AppletPanel.scala",
          "api/", "agent/", "workspace/", "nvm/")
        val sourceFilter: File => Boolean = path =>
          apiSources.exists(ok => path.toString.containsSlice("src/main/org/nlogo/" + ok))
        val out = base / "docs" / "scaladoc"
        Doc(inputs.config.maxErrors, inputs.compilers.scalac)
          .cached(cache / "docSmaller", "NetLogo",
                  inputs.config.sources.filter(sourceFilter),
                  inputs.config.classpath, out, options, s.log)
        mungeScaladocSourceUrls(out)
      }
  )

  // compensate for issues.scala-lang.org/browse/SI-5388
  private def mungeScaladocSourceUrls(path: File): File = {
    for(file <- Process(Seq("find", path.toString, "-name", "*.html")).lines)
      IO.write(
        new File(file),
        IO.read(new File(file)).replaceAll("\\.java\\.scala", ".java"))
    path
  }

}
