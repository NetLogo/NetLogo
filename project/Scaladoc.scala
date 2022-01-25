import java.io.{ File, PrintWriter }
import sbt._, internal.util.Attributed.data
import Keys._
import NetLogoBuild.netlogoVersion

object Scaladoc {

  val apiScaladoc = TaskKey[File]("apiScaladoc", "for docs/scaladoc/")

  val settings = NetLogoBuild.settings ++ Seq(
    apiMappings += (
      file(System.getenv("JAVA_HOME") + "/jre/lib/rt.jar") ->
      url("https://docs.oracle.com/javase/8/docs/api")),
    // automagically link scaladoc with appropriate library docs
    autoAPIMappings := true,
    scalacOptions in (Compile, doc) ++= {
      Seq("-no-link-warnings", "-encoding", "us-ascii") ++
      Seq("-sourcepath", baseDirectory.value.getParentFile.getAbsolutePath) ++
      Opts.doc.title("NetLogo") ++
      Opts.doc.version(netlogoVersion.value) ++
      Opts.doc.sourceUrl("https://github.com/NetLogo/NetLogo/blob/" +
        netlogoVersion.value + "€{FILE_PATH}.scala")
    }) ++
    // The regular doc task includes doc for the entire main source tree.  But for
    // the NetLogo web site we want to document only select classes
    inConfig(Compile)(Defaults.docTaskSettings(apiScaladoc)) :+ {
      val excludedPackages = Seq("org.nlogo.app.previewcommands", "org.nlogo.awt",
        "org.nlogo.compile", "org.nlogo.core.prim",
        "org.nlogo.gl", "org.nlogo.hubnet", "org.nlogo.job",
        "org.nlogo.lex", "org.nlogo.log", "org.nlogo.mc",
        "org.nlogo.parse", "org.nlogo.plot", "org.nlogo.properties",
        "org.nlogo.sdm", "org.nlogo.shape", "org.nlogo.widget",
        "org.nlogo.window", "org.nlogo.generate", "org.nlogo.lab",
        "org.nlogo.prim", "org.nlogo.swing")
      Compile / apiScaladoc / scalacOptions ++=
        Seq("-skip-packages", excludedPackages.mkString(":"))
    } :+ (Compile / apiScaladoc / target := baseDirectory.value / "docs" / "scaladoc")

}
