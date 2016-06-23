import sbt._

class NetLogoDocs(docsSource: File, docsTarget: File, netLogoRoot: File, modelsDirectory: File, extensionsDirectory: File) {
  val dictTarget = docsTarget / "dict"

  def manualComponents(base: File): Seq[File] = Seq(
    "whatis", "copyright", "versions", "requirements", "contact",
    "sample", "tutorial1", "tutorial2", "tutorial3", "interface",
    "infotab", "programming", "transition", "shapes",
    "behaviorspace", "systemdynamics", "hubnet", "hubnet-authoring",
    "modelingcommons", "logging", "controlling", "mathematica", "3d",
    "extensions", "arraystables", "matrix", "sound",
    "netlogolab", "profiler", "gis", "nw", "cf", "csv", "palette",
    "faq", "dictionary").map(n => (base / s"$n.html"))

  private def pandoc(input: File, targetFile: File, title: String): Unit = {
    val args = Seq("pandoc", input.getAbsolutePath,
      "-o", targetFile.getAbsolutePath,
      "-t", "html",
      "-T", title,
      "-c", "netlogo.css")
    val res = Process(args, docsTarget).!
    if (res != 0)
      sys.error(s"failed to generate document: $title")
  }

  private def generatePrimIndices(targetDir: File): Unit = {
    IO.createDirectory(targetDir)
    println(docsSource.getAbsolutePath)
    PrimIndex.generate(
      docsTarget / "dictionary.html",
      targetDir,
      docsSource / "dictTemplate.html.mustache",
      netLogoRoot / "resources" / "system" / "dict.txt")
    PrimIndex.generate(
      docsTarget / "3d.html",
      targetDir,
      docsSource / "dictTemplate.html.mustache",
      netLogoRoot / "resources" / "system" / "dict3d.txt")
  }

  private def generateExtensionDocs(htmlFileRoot: File): Unit = {
    Map(
      "nw"      -> "Networks Extension",
      "cf"      -> "Control Flow Extension",
      "csv"     -> "CSV Extension",
      "palette" -> "Palette Extension",
      "vid"     -> "Vid Extension").foreach {
        case (ext, title) =>
          pandoc(extensionsDirectory / ext / "README.md",
            htmlFileRoot / (ext + ".html"),
            s"NetLogo User Manual: $title")
      }
  }

  private def infoTabHTML: String = {
    InfoTabGenerator(modelsDirectory / "Code Examples" / "Info Tab Example.nlogo")
  }

  private def generateManualPDF(htmlFileRoot: File): File = {
    val pdfFile = netLogoRoot / "NetLogo User Manual.pdf"

    val htmldocArgs =
      Seq("wkhtmltopdf",
        "cover", (htmlFileRoot / "title.html").getAbsolutePath,
        "toc", "--xsl-style-sheet", (htmlFileRoot / "toc.xsl").getAbsolutePath) ++
        manualComponents(htmlFileRoot).map(_.getAbsolutePath) ++
        Seq(pdfFile.getAbsolutePath)

    println(htmldocArgs.mkString(" "))

    val res = Process(htmldocArgs, docsTarget).!

    if (res != 0)
      sys.error("could not generate htmldoc!")

    pdfFile
  }

  private def generateDocs(targetDir: File, variables: Map[String, Object]): Unit = {
    IO.createDirectory(targetDir)
    Mustache.betweenDirectories(docsSource, targetDir, variables)
    generateExtensionDocs(targetDir)
    IO.copyFile(modelsDirectory / "Code Examples" / "Perspective Example.png", targetDir / "Perspective Example.png")
  }

  def generatePDF(buildVariables: Map[String, Object]): File = {
    val mustacheVars =
      buildVariables + ("infoTabModelHTML" -> infoTabHTML)

    val tmp = IO.createTemporaryDirectory
    generateDocs(tmp, mustacheVars)
    generateManualPDF(tmp)
  }

  def generateHTML(buildVariables: Map[String, Object]): Seq[File] = {
    import scala.collection.JavaConverters._

    IO.delete(docsTarget)

    val mustacheVars =
      buildVariables + ("infoTabModelHTML" -> infoTabHTML)

    val standaloneVars =
      mustacheVars + ("sectionHeader" ->
        Seq[Object](Map("version" -> buildVariables("version")).asJava).asJava)

    val supportFiles =
      Seq("dictTemplate.html", "title.html", "toc.xsl").map(n => docsTarget / n)
    generateDocs(docsTarget, standaloneVars)
    generatePrimIndices(docsTarget / "dict")
    supportFiles.foreach(IO.delete)

    Path.allSubpaths(docsTarget).map(_._1).toSeq
  }
}
