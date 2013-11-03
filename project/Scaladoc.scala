import java.io.File
import sbt._
import Keys._

object Scaladoc {

  val netlogoVersion = TaskKey[String]("netlogo-version", "from api.Version")

  val settings = Seq(
    netlogoVersion := {
      (testLoader in Test).value
        .loadClass("org.nlogo.api.Version")
        .getMethod("version")
        .invoke(null).asInstanceOf[String]
        .replaceFirst("NetLogo ", "")
    },
    scalacOptions in (Compile, doc) ++= {
      val version = netlogoVersion.value
      Seq("-encoding", "us-ascii") ++
        Opts.doc.title("NetLogo") ++
        Opts.doc.version(version) ++
        Opts.doc.sourceUrl("https://github.com/NetLogo/NetLogo/blob/" +
                           version + "/src/main€{FILE_PATH}.scala")
    },
    doc in Compile :=
      mungeScaladocSourceUrls((doc in Compile).value)
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
