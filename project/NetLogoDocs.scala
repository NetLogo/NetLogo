import sbt._

import java.nio.file.{ Files, Path => NioPath }
import scala.collection.JavaConverters._

class NetLogoDocs(
  genRoot: File, docsTarget: File, netLogoRoot: File, modelsDirectory: File,
  extensionDocs: ExtensionDocs) {
    val docsSource = genRoot / "docs"

  val dictTarget = docsTarget / "dict"

  // keys are file name (without extension), values are page title
  val markdownComponents = Map(
    "whatis"           -> "What is NetLogo?",
    "versions"         -> "What's new?",
    "requirements"     -> "System Requirements",
    "contact"          -> "Contacting Us",
    "sample"           -> "Sample Model: Party",
    "tutorial1"        -> "Tutorial #1: Models",
    "tutorial2"        -> "Tutorial #2: Commands",
    "tutorial3"        -> "Tutorial #3: Procedures",
    "interface"        -> "Interface Guide",
    "interfacetab"     -> "Interface Tab Guide",
    "codetab"          -> "Code Tab Guide",
    "programming"      -> "Programming Guide",
    "transition"       -> "Transition Guide",
    "shapes"           -> "Shapes Editor Guide",
    "behaviorspace"    -> "BehaviorSpace Guide",
    "systemdynamics"   -> "System Dynamics Guide",
    "hubnet"           -> "HubNet Guide",
    "hubnet-authoring" -> "HubNet Authoring Guide",
    "modelingcommons"  -> "Modeling Commons Guide",
    "logging"          -> "Logging",
    "controlling"      -> "Controlling Guide",
    "mathematica"      -> "Mathematica Link",
    "extensions"       -> "Extensions Guide",
    "faq"              -> "FAQ (Frequently Asked Questions)")

  def manualComponents(base: File, extensions: Seq[String]): Seq[File] = {
    val allComponents = Seq(
      "whatis", "copyright", "versions", "requirements", "contact",
      "sample", "tutorial1", "tutorial2", "tutorial3", "interface",
      "interfacetab", "infotab", "codetab", "programming", "transition", "shapes",
      "behaviorspace", "systemdynamics", "hubnet", "hubnet-authoring",
      "modelingcommons", "logging", "nlogox", "controlling", "mathematica", "3d",
      "extensions") ++ extensions ++ Seq("faq", "dictionary")

    allComponents.map(n => (base / s"$n.html"))
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

  private def infoTabHTML: String = {
    InfoTabGenerator(modelsDirectory / "Code Examples" / "Info Tab Example.nlogo")
  }

  def generatePDF(buildVariables: Map[String, Object], documentedExtensions: Seq[(String, String)]): File = {
    val mustacheVars =
      buildVariables + ("infoTabModelHTML" -> infoTabHTML)

    val tmp = IO.createTemporaryDirectory
    generateDocs(tmp, documentedExtensions, mustacheVars, perPageTOC = false)
    generateManualPDF(tmp, documentedExtensions.map(_._1))
  }

  def generateHTML(buildVariables: Map[String, Object], documentedExtensions: Seq[(String, String)]): Seq[File] = {
    val mustacheVars =
      buildVariables + (
        "infoTabModelHTML" -> infoTabHTML,
        "documentedExtensions" -> documentedExtensions.asJava)

    val supportFiles =
      Seq("dictTemplate.html", "title.html", "toc.xsl").map(n => docsTarget / n)
    generateDocs(docsTarget, documentedExtensions, mustacheVars, perPageTOC = true)
    generatePrimIndices(docsTarget / "dict")
    supportFiles.foreach(IO.delete)

    Path.allSubpaths(docsTarget).map(_._1).toSeq
  }

  private def generateDocs(
    targetDir:            File,
    documentedExtensions: Seq[(String, String)],
    variables:            Map[String, Object],
    perPageTOC:           Boolean): Unit = {

    IO.createDirectory(targetDir)
    Mustache.betweenDirectories(docsSource, targetDir, markdownComponents, variables)
    markdownComponents.keySet foreach { name =>
      val md = Files.readAllLines((targetDir / (name + ".md")).toPath).asScala.mkString("\n")
      val html = Markdown(
        if (perPageTOC) md else md.replaceAll("""\n\n\[TOC[^]]*\]""",""),
        name, extension = false)
      IO.write(targetDir / (name + ".html"), "<!DOCTYPE html>\n" + html)
      IO.delete(targetDir / (name + ".md"))
    }
    extensionDocs.generateExtensionDocs(targetDir, documentedExtensions, variables)
    generateFileFormatDocumentation(targetDir)
    FileActions.copyFile(modelsDirectory / "Code Examples" / "Perspective Example.png", targetDir / "Perspective Example.png")
  }

  private def generateManualPDF(htmlFileRoot: File, extensions: Seq[String]): File = {
    val pdfFile = netLogoRoot / "NetLogo User Manual.pdf"

    val htmldocArgs =
      Seq("wkhtmltopdf",
        "cover", (htmlFileRoot / "title.html").getAbsolutePath,
        "toc", "--xsl-style-sheet", (htmlFileRoot / "toc.xsl").getAbsolutePath) ++
        manualComponents(htmlFileRoot, extensions).map(_.getAbsolutePath) ++
        Seq(pdfFile.getAbsolutePath)

    println(htmldocArgs.mkString(" "))

    val res = Process(htmldocArgs, docsTarget).!

    if (res != 0)
      sys.error("could not generate htmldoc!")

    pdfFile
  }

  private def generateFileFormatDocumentation(targetDir: File): Seq[File] = {
    import java.io.StringWriter
    import java.nio.file.Files
    import javax.xml.transform.{ OutputKeys, Source, TransformerFactory }
    import javax.xml.transform.stream.{ StreamResult, StreamSource }
    def transformTo(sourceFile: File, templateFile: File, destFile: File): Unit = {
      val transformerFactory = TransformerFactory.newInstance
      val docStylesheet = templateFile
      val transformer = transformerFactory.newTransformer(new StreamSource(Files.newBufferedReader(docStylesheet.toPath)))
      val modelXsd = sourceFile
      val (result, writer) = {
        val w = new StringWriter()
        val r = new StreamResult(w)
        (r, w)
      }
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
      transformer.setOutputProperty(OutputKeys.INDENT, "yes")
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        transformer.transform(new StreamSource(Files.newBufferedReader(modelXsd.toPath)), result)
        Files.write(destFile.toPath, writer.toString.getBytes("UTF-8"))
    }
    val transformerFactory = TransformerFactory.newInstance
    transformTo(
      genRoot / "fileformat" / "model.xsd",
      genRoot / "docs" / "netlogo-display.xsl",
      targetDir / "nlogox.html")
    FileActions.copyFile(genRoot / "docs" / "nlogox.css", targetDir / "nlogox.css")
    FileActions.copyFile(genRoot / "fileformat" / "model.xsd", targetDir / "model.xsd")
    Seq(targetDir / "nlogox.html", targetDir / "model.xsd", targetDir / "nlogox.css")
  }
}
