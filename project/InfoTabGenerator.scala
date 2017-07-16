import sbt._

object InfoTabGenerator {
  def apply(model: File): String = {
    val modelText = IO.read(model)
    val info = modelText.split("\\@\\#\\$\\#\\@\\#\\$\\#\\@(\r)?\n")(2)
    Markdown(Preprocessor.convert(info), "", false)
  }

  // runs on the wiki text, before it gets converted to HTML
  object Preprocessor {
    lazy val convert: String => String = List(dropWhatIsItSection, addToc).reduceLeft(_ andThen _)

    private val addToc = (s: String) => {
      "[TOC]\n" + s
    }

    private val dropWhatIsItSection = (s:String) => {
      s.split("\n").drop(1). // drop the ## WHAT IS IT? line
        dropWhile( line => ! line.startsWith("##") ). // drop the rest of the section
        mkString("\n")
    }
  }
}
