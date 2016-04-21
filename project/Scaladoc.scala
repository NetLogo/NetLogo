import java.io.File
import sbt._
import Keys._
import NetLogoBuild.netlogoVersion

object Scaladoc {

  val apiScaladoc = TaskKey[File]("apiScaladoc", "for docs/scaladoc/")

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
    apiScaladoc := {
      val classpath = (compileInputs in compile in Compile).value.config.classpath
      val out = baseDirectory.value / "docs" / "scaladoc"
      IO.createDirectory(out)
      val excludedePackages = Seq("org.nlogo.app.previewcommands", "org.nlogo.awt",
        "org.nlogo.compiler", "org.nlogo.core.prim",
        "org.nlogo.gl", "org.nlogo.hubnet", "org.nlogo.job",
        "org.nlogo.lex", "org.nlogo.log", "org.nlogo.mc",
        "org.nlogo.parse", "org.nlogo.plot", "org.nlogo.properties",
        "org.nlogo.sdm", "org.nlogo.shape", "org.nlogo.widget",
        "org.nlogo.window", "org.nlogo.generator", "org.nlogo.lab",
        "org.nlogo.prim", "org.nlogo.swing")
      val opts = (scalacOptions in Compile in doc).value ++
        Seq("-skip-packages", excludedePackages.mkString(":"))
      Doc.scaladoc("NetLogo", streams.value.cacheDirectory / "apiScaladoc",
        (compileInputs in compile in Compile).value.compilers.scalac, opts)(
          (sources in Compile).value, classpath, out, opts,
          (compileInputs in compile in Compile).value.config.maxErrors, streams.value.log)
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
