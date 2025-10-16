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

    PrimIndex.generate(
      docsTarget / "dictionary.html",
      targetDir,
      docsSource / "dictTemplate.html.mustache",
      netLogoRoot / "resources" / "system" / "dict.txt",
      docsTarget / "header.html",
      "dictionary.html",
      "NetLogo Dictionary",
      "dict" // /<version>/<primRoot>/<prim>
    )

    // Having to use <primRoot> = "dict" here is an
    // unfortunate artifact of the way things were done
    // before. (Omar I 07/15/25)
    PrimIndex.generate(
      docsTarget / "3d.html",
      targetDir,
      docsSource / "dictTemplate.html.mustache",
      netLogoRoot / "resources" / "system" / "dict3d.txt",
      docsTarget / "header.html",
      "3d.html",
      "NetLogo 3D Dictionary",
      "dict" // /<version>/<primRoot>/<prim>
    )
  }

  private def infoTabHTML: String = {
    InfoTabGenerator(modelsDirectory / "Code Examples" / "Info Tab Example.nlogox")
  }

  def generatePDF(version: String, year: String, autoDocumentedExtensions: Seq[(String, String)]): File = {
    // Render the header with the version, navlinks,
    // and other variables
    Mustache(
      docsSource / "header.mustache",
      docsTarget / "header.html",
      Map("version" -> version, "title" -> "", "pdf" -> Boolean.box(true)),
      Some(docsSource)
    )

    val tmp = IO.createTemporaryDirectory
    generateDocs(tmp, autoDocumentedExtensions, version, year, true)
    generateManualPDF(tmp, autoDocumentedExtensions.map(_._1).sorted)
  }

  def generateHTML(version: String, year: String, autoDocumentedExtensions: Seq[(String, String)]): Seq[File] = {
    // Render the header with the version, navlinks,
    // and other variables
    Mustache(docsSource / "header.mustache", docsTarget / "header.html", Map(
      "version" -> version,
      "title" -> "",
      "pdf" -> Boolean.box(false),
      "documentedExtensions" -> autoDocumentedExtensions.asJava
    ), Some(docsSource))

    val supportFiles =
      Seq("dictTemplate.html", "title.html", "toc.xsl").map(n => docsTarget / n)
    generateDocs(docsTarget, autoDocumentedExtensions, version, year, false)
    generatePrimIndices(docsTarget / "dict")
    supportFiles.foreach(IO.delete)
    Path.allSubpaths(docsTarget).map(_._1).toSeq
  }

  private def generateDocs(targetDir: File, autoDocumentedExtensions: Seq[(String, String)], version: String,
                           year: String, pdf: Boolean): Unit = {

    IO.createDirectory(targetDir)
    Mustache.betweenDirectories(docsSource, targetDir, markdownComponents, Map(
      "version" -> version,
      "year" -> year,
      "pdf" -> Boolean.box(pdf),
      "documentedExtensions" -> autoDocumentedExtensions.asJava,
      "infoTabModelHTML" -> infoTabHTML
    ), Set("dictTemplate.html.mustache", "headings.html.mustache"))
    markdownComponents.keySet.foreach { name =>
      val md = Files.readAllLines((targetDir / (name + ".md")).toPath).asScala.mkString("\n")
      val html = Markdown(
        if (!pdf) md else md.replaceAll("""\n\n\[TOC[^]]*\]""",""),
        name, extension = false)
      IO.write(targetDir / (name + ".html"), "<!DOCTYPE html>\n" + html)
      IO.delete(targetDir / (name + ".md"))
    }
    extensionDocs.generateExtensionDocs(targetDir, docsSource, autoDocumentedExtensions, version)
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
