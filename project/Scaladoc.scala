import java.io.File
import sbt._
import Keys._
import NetLogoBuild.netlogoVersion

object Scaladoc {

  val docSmaller = TaskKey[File]("doc-smaller", "for docs/scaladoc/")
  val settings = NetLogoBuild.settings ++ Seq(
    apiMappings += (
      file(System.getenv("JAVA_HOME") + "/jre/lib/rt.jar") ->
      url("http://docs.oracle.com/javase/8/docs/api")),
    // automagically link scaladoc with appropriate library docs
    autoAPIMappings := true,
    scalacOptions in (Compile, doc) ++= {
      Seq("-no-link-warnings", "-encoding", "us-ascii") ++
      Seq("-sourcepath", baseDirectory.value.getParentFile.getAbsolutePath) ++
      Opts.doc.title("NetLogo") ++
      Opts.doc.version(netlogoVersion.value) ++
      Opts.doc.sourceUrl("https://github.com/NetLogo/NetLogo/blob/" +
        netlogoVersion.value + "€{FILE_PATH}.scala")
    },
    doc in Compile ~= mungeScaladocSourceUrls,
    // The regular doc task includes doc for the entire main source tree.  But for the NetLogo
    // web site we want to document only select classes.  So I copy and pasted
    // the code for the main doc task and tweaked it. - ST 6/29/12, 7/18/12
    // sureiscute.com/images/cutepictures/I_Have_No_Idea_What_I_m_Doing.jpg
    docSmaller <<= (baseDirectory, scalacOptions in (Compile, doc), compileInputs in compile in Compile, netlogoVersion, streams) map {
      (base, options, inputs, version, s) =>
        val apiSources = Seq(
          "app/App.scala", "headless/HeadlessWorkspace.scala",
          "lite/InterfaceComponent.scala", "lite/Applet.scala", "lite/AppletPanel.scala",
          "api/", "agent/", "workspace/", "nvm/")
        val sourceFilter: File => Boolean = path =>
          apiSources.exists(ok => path.toString.containsSlice("src/main/" + ok))
        // not sure these are being accounted for
        val classpath = inputs.config.classpath
        val out = base / "docs" / "scaladoc"
        IO.createDirectory(out)
        val sources = inputs.config.sources.filter(sourceFilter)
        Doc.scaladoc("NetLogo", s.cacheDirectory / "docSmaller",
          inputs.compilers.scalac, options)(
            sources, classpath, out, options,
            inputs.config.maxErrors, s.log)
        mungeScaladocSourceUrls(out)
      }
  )

  // compensate for issues.scala-lang.org/browse/SI-5388
  private def mungeScaladocSourceUrls(path: File): File = {
    for (file <- (path ** "*.html").get)
      IO.write(file,
        IO.read(file).replaceAll("\\.java\\.scala", ".java"))
    path
  }

}
