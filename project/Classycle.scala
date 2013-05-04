import sbt._
import Keys._
import classycle.Analyser

object Classycle {

  val classycle = TaskKey[Unit](
    "classycle", "run Classycle and display a dependency report")

  val settings = Seq(classycleTask)

  private lazy val classycleTask =
    classycle <<= (classDirectory in Compile).map{
      (classes) =>
        runClassycle(classes)
    }.dependsOn(compile in Compile)

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
