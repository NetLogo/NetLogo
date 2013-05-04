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
    "mkdir -p tmp".!
    "cp -f project/classycle/reportXMLtoHTML.xsl tmp".!
    "rm -rf tmp/images".!
    "cp -rp project/classycle/images tmp/images".!
    Analyser.main(
      Array(
        "-xmlFile=tmp/classycle.xml",
        "-mergeInnerClasses",
        classes.getAbsolutePath.toString))
    "open -a Safari tmp/classycle.xml".!
  }

}
