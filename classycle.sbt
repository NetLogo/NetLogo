import scala.sys.process.Process

val classycle = taskKey[File](
  "run Classycle and display a dependency report")

classycle := {
  val _ = (compile in Compile).value  // run it, ignore result
  Process("mkdir -p target/classycle").!
  Process("cp -f project/classycle/reportXMLtoHTML.xsl target/classycle").!
  Process("rm -rf target/classycle/images").!
  Process("cp -rp project/classycle/images target/classycle/images").!
  _root_.classycle.Analyser.main(
    Array(
      "-xmlFile=target/classycle/classycle.xml",
      "-mergeInnerClasses",
      (classDirectory in Compile).value.getAbsolutePath.toString))
  Process("open -a Safari target/classycle/classycle.xml").!
  baseDirectory.value / "target" / "classycle" / "classycle.xml"
}
