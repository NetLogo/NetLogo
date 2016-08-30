import sbt._

import java.nio.file.{ Files, Path => NioPath }
import org.nlogo.build.{ DocumentationConfig, Documenter, HoconParser }
import org.pegdown.{ PegDownProcessor, Extensions => PegdownExtensions }
import scala.collection.JavaConverters._

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

  private def generateManualPDF(htmlFileRoot: File, documentedExtensions: Seq[(String, String)]): File = {
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
    FileActions.copyFile(modelsDirectory / "Code Examples" / "Perspective Example.png", targetDir / "Perspective Example.png")
  }

  def generatePDF(buildVariables: Map[String, Object]): File = {
    val mustacheVars =
      buildVariables + ("infoTabModelHTML" -> infoTabHTML)

    val tmp = IO.createTemporaryDirectory
    generateDocs(tmp, mustacheVars)
    generateManualPDF(tmp)
  }

  def generateHTML(buildVariables: Map[String, Object], documentedExtensions: Seq[(String, String)]): Seq[File] = {
    import scala.collection.JavaConverters._

    IO.delete(docsTarget)

    val mustacheVars =
      buildVariables + (
        "infoTabModelHTML" -> infoTabHTML,
        "documentedExtensions" -> documentedExtensions.asJava)

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

  def renderMarkdown(str: String): String = {
    new PegDownProcessor(PegdownExtensions.SMARTYPANTS |       // beautifies quotes, dashes, etc.
                         PegdownExtensions.AUTOLINKS |         // angle brackets around URLs and email addresses not needed
                         PegdownExtensions.FENCED_CODE_BLOCKS) // delimit code blocks with ```
      .markdownToHtml(str)
  }

  def renderMarkdown(p: NioPath): String =
    renderMarkdown(new String(Files.readAllBytes(p), "UTF-8"))

  def generateHTMLPageForExtension(extensionName: String, netLogoConfFile: NioPath, buildVariables: Map[String, Object]): NioPath = {
    val targetFile = (docsTarget / (extensionName + ".html").toLowerCase).toPath
    val documentationConf = (extensionsDirectory / extensionName / "documentation.conf").toPath
    val configDocument = HoconParser.parseConfigFile(documentationConf.toFile)
    val netLogoConfig = HoconParser.parseConfig(HoconParser.parseConfigFile(netLogoConfFile.toFile))
    val docConfig = HoconParser.parseConfig(configDocument)
    val primResult = HoconParser.parsePrimitives(configDocument)
    val renderedPrimitives = primResult.primitives.map(p => p.copy(description = renderMarkdown(p.description)))
    val filesToIncludeInManual: Seq[String] = configDocument.getStringList("filesToIncludeInManual").asScala
    val prePrimFiles =
      filesToIncludeInManual.takeWhile(_ != "primitives").map(documentationConf.resolveSibling).map(renderMarkdown)
    val postPrimFiles =
      filesToIncludeInManual.dropWhile(_ != "primitives").tail.map(documentationConf.resolveSibling).map(renderMarkdown)
    val additionalConfig = Map(
      "extensionName"         -> renderedPrimitives.head.extensionName.capitalize,
      "prePrimitiveSections"  -> prePrimFiles.asJava,
      "postPrimitiveSections" -> postPrimFiles.asJava
    )
    val newTableOfContents =
      if (docConfig.tableOfContents.keys == Seq("")) Map("" -> "Primitives")
      else docConfig.tableOfContents
    val finalConfig = DocumentationConfig(
      markdownTemplate = netLogoConfig.markdownTemplate,
      primTemplate     = netLogoConfig.primTemplate,
      tableOfContents  = newTableOfContents,
      additionalConfig = docConfig.additionalConfig ++ additionalConfig ++ buildVariables
    )

    val renderedPage = Documenter.documentAll(finalConfig, renderedPrimitives, documentationConf.getParent)
    Files.write(targetFile, renderedPage.getBytes("UTF-8"))
  }
}
