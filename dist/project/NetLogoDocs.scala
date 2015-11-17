import sbt._

class NetLogoDocs(docsSource: File, docsTarget: File, netLogoRoot: File) {
  val dictTarget = docsTarget / "dict"

  def manualComponents(base: File): Seq[File] = Seq(
    "whatis", "copyright", "versions", "requirements", "contact",
    "sample", "tutorial1", "tutorial2", "tutorial3", "interface",
    "programming", "transition", "applet", "shapes",
    "behaviorspace", "systemdynamics", "hubnet", "hubnet-authoring",
    "modelingcommons", "logging", "controlling", "mathematica",
    "3d", "extensions", "arraystables", "matrix", "sound",
    "netlogolab", "profiler", "gis", "nw", "csv", "palette",
    "faq", "dictionary").map(n => (base / s"$n.html"))

  private def pandoc(input: File, targetFile: File, title: String): Unit = {
    val args = Seq("pandoc", input.getAbsolutePath,
      "-o", targetFile.getAbsolutePath,
      "-t", "html",
      "-T", title,
      "-c", (docsSource / "netlogo.css").getAbsolutePath)
    val res = Process(args, docsTarget).!
    if (res != 0)
      sys.error(s"failed to generate document: $title")
  }

  private def generatePrimIndices(): Unit = {
    PrimIndex.generate(
      docsTarget / "dictionary.html",
      dictTarget,
      docsSource / "dictTemplate.html.mustache",
      netLogoRoot / "resources" / "system" / "dict.txt")
    PrimIndex.generate(
      docsTarget / "3d.html",
      dictTarget,
      docsSource / "dictTemplate.html.mustache",
      netLogoRoot / "resources" / "system" / "dict3d.txt")
  }

  private def generateExtensionDocs(htmlFileRoot: File): Unit = {
    Map(
      "nw"      -> "Networks Extension",
      "csv"     -> "CSV Extension",
      "palette" -> "Palette Extension").foreach {
        case (ext, title) =>
          pandoc(netLogoRoot / "extensions" / ext / "README.md",
            htmlFileRoot / (ext + ".html"),
            s"NetLogo User Manual: $title")
      }
  }

  private def generateManualPDF(htmlFileRoot: File): Unit = {
    val htmldocArgs = Seq("htmldoc", "--strict", "--duplex",
      "--titleimage", (htmlFileRoot / "images" / "title.jpg").getAbsolutePath,
      "--bodyfont", "helvetica",
      "--footer", "c.1",
      "--color", "--book",
      "-f", (netLogoRoot / "NetLogo User Manual.pdf").getAbsolutePath) ++
    manualComponents(htmlFileRoot).map(_.getAbsolutePath)

    println(htmldocArgs.mkString(" "))

    val res = Process(htmldocArgs, docsTarget).!

    if (res != 0)
      sys.error("could not generate htmldoc!")
  }

  def generate(buildVariables: Map[String, Object]): Seq[File] = {
    import scala.collection.JavaConverters._

    IO.createDirectory(dictTarget)

    val sectionHeaderList =
      Seq[Object](Map("version" -> buildVariables("version")).asJava).asJava
    // variables now through next comment required only by docs
    val standaloneVars =
      buildVariables + ("sectionHeader" -> sectionHeaderList)

    val files = Mustache.betweenDirectories(docsSource, docsTarget, standaloneVars)
    generatePrimIndices()
    generateExtensionDocs(docsTarget)

    val tmp = IO.createTemporaryDirectory

    Mustache.betweenDirectories(docsSource, tmp, buildVariables)
    generateExtensionDocs(tmp)
    generateManualPDF(tmp)

    files
  }
}
