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
    "hpc"                 -> "High-Performance Cluster Guide",
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
      "primRoot" -> "dict", // /<version>/<primRoot>/<prim>
    )

    PrimIndex.generate(
      docsTarget / "dictionary.html",
      targetDir,
      docsSource / "dictTemplate.html.mustache",
      netLogoRoot / "resources" / "system" / "dict.txt",
      docsTarget / "header.html",
      vars
    )

    // Having to use <primRoot> = "dict" here is an
    // unfortunate artifact of the way things were done
    // before. (Omar I 07/15/25)
    val vars3D = vars + (
      "dictHome" -> "3d.html",
      "dictTitle" -> "NetLogo 3D Dictionary",
      "primRoot" -> "dict" // /<version>/<primRoot>/<prim>
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
      buildVariables + ("infoTabModelHTML" -> infoTabHTML, "pdf" -> java.lang.Boolean.TRUE)

    // Render the header with the version, navlinks,
    // and other variables
    Mustache(
      docsSource / "header.mustache",
      docsTarget / "header.html",
      mustacheVars,
      Some(docsSource)
    )

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
    extensionDocs.generateExtensionDocs(targetDir, docsSource, autoDocumentedExtensions, variables)
    FileActions.copyFile(modelsDirectory / "Code Examples" / "Perspective Example.png", targetDir / "Perspective Example.png")
  }

  /* Check if whoever is running this has:
   *  1. npm installed
   *  2. the generate-manual script in the right place
   *  3. run `npm install` in the right directory
   *  If not, throw an error with instructions.
   * */
  private def checkPDFRenderingDependencies(): Unit = {
    if (!(docsSource.getParentFile / "generate-manual" / "index.js").exists)
      sys.error(s"index.js not found in generate-manual directory.")

    if (Process(Seq("bash", "npm-install.sh"), docsSource.getParentFile / "generate-manual").! != 0)
      sys.error("npm install failed. Please ensure you have npm installed and try again.")

    println("npm install completed successfully.")
  }

  private def generateManualPDF(htmlFileRoot: File, extensions: Seq[String]): File = {
    checkPDFRenderingDependencies()

    val pdfFile = netLogoRoot / "NetLogo User Manual.pdf"
    val scriptPath = docsSource.getParentFile / "generate-manual" / "index.js"
    if (!Files.exists(scriptPath.toPath)) {
      sys.error(s"Script not found: $scriptPath")
    }

    val htmlDocArgs = // node generate-manual.js <output> <...html files>
      Seq("node", scriptPath.getAbsolutePath, (htmlFileRoot / "title.html").getAbsolutePath) ++
      manualComponents(htmlFileRoot, extensions).map(_.getAbsolutePath) ++
      Seq(pdfFile.getAbsolutePath)

    println(htmlDocArgs.mkString(" "))

    val res = Process(htmlDocArgs, docsTarget).!

    if (res != 0)
      sys.error("could not generate htmldoc!")

    pdfFile
  }
}
