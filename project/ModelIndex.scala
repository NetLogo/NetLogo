import java.io.File
import sbt._
import Keys._

object ModelIndex {

  val modelIndex = TaskKey[Seq[File]](
    "model-index", "builds models/index.txt for use in Models Library dialog")

  val modelIndexTask =
    modelIndex <<= (baseDirectory, streams) map {
      (base, s) =>
        s.log.info("creating models/index.txt")
        val path = base / "models" / "index.txt"
        IO.write(path, generateIndex(base / "models"))
        Seq(path)
    }

  private def generateIndex(modelsPath: File): String = {
    val buf = new StringBuilder
    def println(s: String) { buf ++= s + "\n" }
    val command =
      "find " + modelsPath.toString +
      " -name test -prune -o -name *.nlogo -print -o -name *.nlogo3d -print"
    val paths = (command).lines_!
    def infoTab(path: String) =
      IO.read(new File(path)).split("\\@\\#\\$\\#\\@\\#\\$\\#\\@\n")(2)
    for(path <- paths) {
      val info = infoTab(path)
      // The (?s) part allows . to match line endings
      val pattern = "(?s)## WHAT IS IT\\?\\s*\\n"
      if(info.matches(pattern + ".*") ) {
        val firstParagraph = info.replaceFirst(pattern, "").split('\n').head
        println("models" + path.replaceFirst(modelsPath.toString, ""))
        println(firstParagraph)
      }
      else
        System.err.println("WHAT IS IT not found: " + path)
    }
    buf.toString
  }

}
