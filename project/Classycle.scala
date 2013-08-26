import sbt._
import Keys._
import classycle.Analyser

object Classycle {

  val classycle = taskKey[Unit](
    "run Classycle and display a dependency report")

  val settings = Seq(
    classycle := {
      val _ = (compile in Compile).value  // run it, ignore result
      runClassycle((classDirectory in Compile).value)
    }
  )

  private def runClassycle(classes: java.io.File) {
    "mkdir -p target/classycle".!
    "cp -f project/classycle/reportXMLtoHTML.xsl target/classycle".!
    "rm -rf target/classycle/images".!
    "cp -rp project/classycle/images target/classycle/images".!
    Analyser.main(
      Array(
        "-xmlFile=target/classycle/classycle.xml",
        "-mergeInnerClasses",
        classes.getAbsolutePath.toString))
    "open -a Safari target/classycle/classycle.xml".!
  }

}
