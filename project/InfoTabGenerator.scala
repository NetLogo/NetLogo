import sbt._

object InfoTabGenerator {
  def apply(model: File): String =
    Markdown(Preprocessor.convert(InfoExtractor(IO.read(model))), "", extension = false)

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
