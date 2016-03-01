import java.io.File
import java.util.regex.Pattern
import sbt._
import Keys._

object ModelsLibrary {

  val modelsDirectory = settingKey[File]("models directory")

  val modelIndex = TaskKey[Seq[File]](
    "modelIndex", "builds models/index.txt for use in Models Library dialog")

  lazy val settings = Seq(
    modelIndex := {
      streams.value.log.info("creating models/index.txt")
      val path = modelsDirectory.value / "index.txt"
      IO.write(path, generateIndex(modelsDirectory.value))
      Seq(path)
    },
    javaOptions += "-Dnetlogo.models.dir=" + modelsDirectory.value.getAbsolutePath.toString
  )

  private def generateIndex(modelsPath: File): String = {
    val buf = new StringBuilder
    def println(s: String) { buf ++= s + "\n" }
    // -H tells find to follow symbolic links.  we need that because
    // bin/release.sh uses a symbolic link to fool this task into
    // generating the index.txt file for a release - ST 6/18/12
    val command =
      Seq("find", "-H", modelsPath.toString,
        "-name", "test", "-prune", "-o", "-name", "*.nlogo", "-print",
        "-o", "-name", "*.nlogo3d", "-print")
    val paths = command.lines_!
    def infoTab(path: String) =
      IO.read(new File(path)).split("\\@\\#\\$\\#\\@\\#\\$\\#\\@\n")(2)
    for(path <- paths) {
      val info = infoTab(path)
      // The (?s) part allows . to match line endings
      val whatIsItPattern = "(?s).*## WHAT IS IT\\?\\s*\\n"
      val modelPathPattern = Pattern.quote(modelsPath.toString)
      if(info.matches(whatIsItPattern + ".*") ) {
        val firstParagraph = info.replaceFirst(whatIsItPattern, "").split('\n').head
        println("models" + path.replaceFirst(modelPathPattern, ""))
        println(firstParagraph)
      } else {
        System.err.println("WHAT IS IT not found: " + path)
      }
    }
    buf.toString
  }

}
