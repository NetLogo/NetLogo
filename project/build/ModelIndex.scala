import sbt._
import Process._

//  creates an index of the models in the Models Library and the first paragraph of the info
//  tabs. we read this when we open the Models Library dialog instead of sifting through all the
//  files at that time cause that's super slow. ev 3/26/09

trait ModelIndex extends DefaultProject {
  lazy val modelIndex = task { args =>
    val modelsPath =
      if(args.isEmpty) path("models")
      else Path.fromString(path("."), args(0))
    val indexPath = modelsPath / "index.txt"
    fileTask(Seq(indexPath)) {
      FileUtilities.writeStream(indexPath.asFile, log) {
        stream: java.io.OutputStream =>
          val writer = new java.io.PrintWriter(stream)
        writeIndex(modelsPath, writer)
        writer.flush()
        None
      }
    }
  }
  private def writeIndex(modelsPath: Path, w: java.io.PrintWriter) {
    import w.println
    val command = "find " + modelsPath.toString + " -name test -prune -o -name *.nlogo -print -o -name *.nlogo3d -print"
    val paths = (command).lines_!
    def infoTab(path: String) =
      io.Source.fromFile(path).mkString.split("\\@\\#\\$\\#\\@\\#\\$\\#\\@\n")(2)
    for(path <- paths) {
      val info = infoTab(path)
      // The (?s) part allows . to match line endings
      val pattern = "(?s)## WHAT IS IT\\?\\s*\\n"
      if(info.matches(pattern + ".*") ) {
        val firstParagraph = info.replaceFirst(pattern, "").split('\n').first
        println("models" + path.replaceFirst(modelsPath.toString, ""))
        println(firstParagraph)
      }
      else
        System.err.println("WHAT IS IT not found: " + path)
    }
  }
}
