import sbt._

import java.nio.file.{ Files, Path => NioPath }
import scala.collection.JavaConverters._
import scala.sys.process.Process

class NetLogoDocs(
  docsSource: File, docsTarget: File, netLogoRoot: File, modelsDirectory: File,
  extensionDocs: ExtensionDocs, extensionRoot: File) {

  val dictTarget = docsTarget / "dict"

  // keys are file name (without extension), values are page title
  val markdownComponents = Map(
    "whatis"              -> "What is NetLogo?",
    "versions"            -> "What's new?",
    "requirements"        -> "System Requirements",
    "contact"             -> "Contacting Us",
    "sample"              -> "Sample Model: Party",
    "tutorial1"           -> "Tutorial #1: Models",
    "tutorial2"           -> "Tutorial #2: Commands",
    "tutorial3"           -> "Tutorial #3: Procedures",
    "interface"           -> "Interface Guide",
    "interfacetab"        -> "Interface Tab Guide",
    "codetab"             -> "Code Tab Guide",
    "programming"         -> "Programming Guide",
    "transition"          -> "Transition Guide",
    "resource-manager"    -> "Resource Manager Guide",
    "extension-authoring" -> "Extension Authoring Introduction",
    "extension-manager"   -> "Extension Manager Guide",
    "shapes"              -> "Shapes Editor Guide",
    "behaviorspace"       -> "BehaviorSpace Guide",
    "behaviorspace-spanish" -> "BehaviorSpace Guide en Español",
    "systemdynamics"      -> "System Dynamics Guide",
    "hubnet"              -> "HubNet Guide",
    "hubnet-authoring"    -> "HubNet Authoring Guide",
    "modelingcommons"     -> "Modeling Commons Guide",
    "logging"             -> "Logging",
    "controlling"         -> "Controlling Guide",
    "mathematica"         -> "Mathematica Link",
    "extensions"          -> "Extensions Guide",
    "faq"                 -> "FAQ (Frequently Asked Questions)",
    "colorpicker"         -> "Color Picker Guide",
    "netlogo7intro"       -> "NetLogo 7.0.0 Changes Overview",
    "netlogopreferences"  -> "NetLogo Preferences")

  def manualComponents(base: File, extensions: Seq[String]): Seq[File] = {
    val allComponents = Seq(
      "whatis", "copyright", "versions", "requirements", "contact",
      "sample", "tutorial1", "tutorial2", "tutorial3", "interface",
      "interfacetab", "infotab", "codetab", "programming", "transition",
      "extension-manager", "shapes", "behaviorspace", "systemdynamics",
      "hubnet", "hubnet-authoring", "modelingcommons", "logging", "controlling",
      "mathematica", "3d", "extensions", "extension-authoring",
      "colorpicker", "netlogo7intro", "netlogopreferences") ++
      extensions ++ Seq("faq", "dictionary")

    allComponents.map(n => (base / s"$n.html"))
  }

  private def generatePrimIndices(targetDir: File): Unit = {
    IO.createDirectory(targetDir)
    println(docsSource.getAbsolutePath)

    val vars = Map[String, Object](
      "dictHome" -> "dictionary.html",
      "dictTitle" -> "NetLogo Dictionary",
    )

    PrimIndex.generate(
      docsTarget / "dictionary.html",
      targetDir,
      docsSource / "dictTemplate.html.mustache",
      netLogoRoot / "resources" / "system" / "dict.txt",
      docsTarget / "header.html",
      vars
    )

    val vars3D = vars + (
      "dictHome" -> "3d.html",
      "dictTitle" -> "NetLogo 3D Dictionary",
    )
    PrimIndex.generate(
      docsTarget / "3d.html",
      targetDir,
      docsSource / "dictTemplate.html.mustache",
      netLogoRoot / "resources" / "system" / "dict3d.txt",
      docsTarget / "header.html",
      vars3D
    )
  }

  private def infoTabHTML: String = {
    InfoTabGenerator(modelsDirectory / "Code Examples" / "Info Tab Example.nlogox")
  }

  def generatePDF(buildVariables: Map[String, Object], autoDocumentedExtensions: Seq[(String, String)],
                  manuallyDocumentedExtensions: Seq[String]): File = {
    val mustacheVars =
      buildVariables + ("infoTabModelHTML" -> infoTabHTML)

    val tmp = IO.createTemporaryDirectory
    generateDocs(tmp, autoDocumentedExtensions, manuallyDocumentedExtensions, mustacheVars, perPageTOC = false)
    generateManualPDF(tmp, (autoDocumentedExtensions.map(_._1) ++ manuallyDocumentedExtensions).sorted)
  }

  def generateHTML(buildVariables: Map[String, Object], autoDocumentedExtensions: Seq[(String, String)],
                   manuallyDocumentedExtensions: Seq[String]): Seq[File] = {
    val mustacheVars =
      buildVariables + (
        "infoTabModelHTML" -> infoTabHTML,
        "documentedExtensions" -> autoDocumentedExtensions.asJava)

    // Render the header with the version, navlinks,
    // and other variables
    Mustache(
      docsSource / "header.mustache",
      docsTarget / "header.html",
      mustacheVars,
      Some(docsSource)
    )

    val supportFiles =
      Seq("dictTemplate.html", "title.html", "toc.xsl").map(n => docsTarget / n)
    generateDocs(docsTarget, autoDocumentedExtensions, manuallyDocumentedExtensions, mustacheVars, perPageTOC = true)
    generatePrimIndices(docsTarget / "dict")
    supportFiles.foreach(IO.delete)

    Path.allSubpaths(docsTarget).map(_._1).toSeq
  }

  private def generateDocs(
    targetDir:            File,
    autoDocumentedExtensions: Seq[(String, String)],
    manuallyDocumentedExtensions: Seq[String],
    variables:            Map[String, Object],
    perPageTOC:           Boolean): Unit = {

    IO.createDirectory(targetDir)
    Mustache.betweenDirectories(docsSource, targetDir, markdownComponents, variables)
    manuallyDocumentedExtensions.foreach { name =>
      Mustache(extensionRoot / name / "README.md.mustache", targetDir / (name + ".md"), variables)
    }
    (markdownComponents.keySet ++ manuallyDocumentedExtensions).foreach { name =>
      val md = Files.readAllLines((targetDir / (name + ".md")).toPath).asScala.mkString("\n")
      val html = Markdown(
        if (perPageTOC) md else md.replaceAll("""\n\n\[TOC[^]]*\]""",""),
        name, extension = false)
      IO.write(targetDir / (name + ".html"), "<!DOCTYPE html>\n" + html)
      IO.delete(targetDir / (name + ".md"))
    }
    extensionDocs.generateExtensionDocs(targetDir, autoDocumentedExtensions, variables)
    FileActions.copyFile(modelsDirectory / "Code Examples" / "Perspective Example.png", targetDir / "Perspective Example.png")
  }

  private def generateManualPDF(htmlFileRoot: File, extensions: Seq[String]): File = {
    val pdfFile = netLogoRoot / "NetLogo User Manual.pdf"

    val htmldocArgs =
      Seq("wkhtmltopdf", "--enable-local-file-access",
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
}
