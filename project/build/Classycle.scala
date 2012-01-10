import sbt._
import classycle.Analyser

trait Classycle extends DefaultProject {

  lazy val classycle = task { analyze(); None } dependsOn(compile)

  private def analyze() {
    "cp -f project/build/classycle/reportXMLtoHTML.xsl tmp" ! log
    "rm -rf tmp/images" ! log
    "cp -rp project/build/classycle/images tmp/images" ! log
    Analyser.main(Array("-xmlFile=tmp/classycle.xml", "-mergeInnerClasses",
                        mainCompileConfiguration.outputDirectory.toString))
    "open -a Safari tmp/classycle.xml" !
  }

}
