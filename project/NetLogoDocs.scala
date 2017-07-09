import sbt._

import java.nio.file.{ Files, Path => NioPath }
import com.github.mustachejava.TemplateFunction
import org.nlogo.build.{ DocumentationConfig, Documenter, HoconParser }
import scala.collection.JavaConverters._
import scala.util.matching.Regex

class NetLogoDocs(docsSource: File, docsTarget: File, netLogoRoot: File, modelsDirectory: File, extensionsDirectory: File, extensionDocConfigFile: File) {
  val dictTarget = docsTarget / "dict"

  def manualComponents(base: File, extensions: Seq[String]): Seq[File] = {
    val allComponents = Seq(
    "whatis", "copyright", "versions", "requirements", "contact",
    "sample", "tutorial1", "tutorial2", "tutorial3", "interface",
    "interfacetab", "infotab", "codetab", "programming", "transition", "shapes",
    "behaviorspace", "systemdynamics", "hubnet", "hubnet-authoring",
    "modelingcommons", "logging", "controlling", "mathematica", "3d",
    "extensions") ++
      extensions ++ Seq("faq", "dictionary")

    allComponents.map(n => (base / s"$n.html"))
  }

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

  private def infoTabHTML: String = {
    InfoTabGenerator(modelsDirectory / "Code Examples" / "Info Tab Example.nlogo")
  }

  def generatePDF(buildVariables: Map[String, Object], documentedExtensions: Seq[(String, String)]): File = {
    val mustacheVars =
      buildVariables + ("infoTabModelHTML" -> infoTabHTML)

    val tmp = IO.createTemporaryDirectory
    generateDocs(tmp, documentedExtensions, mustacheVars)
    generateManualPDF(tmp, documentedExtensions.map(_._1))
  }

  def generateHTML(buildVariables: Map[String, Object], documentedExtensions: Seq[(String, String)]): Seq[File] = {
    import scala.collection.JavaConverters._

    val mustacheVars =
      buildVariables + (
        "infoTabModelHTML" -> infoTabHTML,
        "documentedExtensions" -> documentedExtensions.asJava)

    val standaloneVars =
      mustacheVars + ("sectionHeader" ->
        Seq[Object](Map("version" -> buildVariables("version")).asJava).asJava)

    val supportFiles =
      Seq("dictTemplate.html", "title.html", "toc.xsl").map(n => docsTarget / n)
    generateDocs(docsTarget, documentedExtensions, standaloneVars)
    generatePrimIndices(docsTarget / "dict")
    supportFiles.foreach(IO.delete)

    Path.allSubpaths(docsTarget).map(_._1).toSeq
  }

  def generateHTMLPageForExtension(
    extensionName:  String,
    targetFile:     NioPath,
    buildVariables: Map[String, Object]): NioPath = {

    val documentationConf = (extensionsDirectory / extensionName / "documentation.conf").toPath
    val netLogoConfFile = extensionDocConfigFile.toPath
    val configDocument = HoconParser.parseConfigFile(documentationConf.toFile)
    val netLogoConfig = HoconParser.parseConfig(HoconParser.parseConfigFile(netLogoConfFile.toFile))
    val docConfig = HoconParser.parseConfig(configDocument)
    val primitives = HoconParser.parsePrimitives(configDocument).primitives
    val filesToIncludeInManual: Seq[String] = configDocument.getStringList("filesToIncludeInManual").asScala
    val prePrimFiles =
      getIncludes(filesToIncludeInManual.takeWhile(_ != "primitives"), documentationConf.getParent)
    val postPrimFiles =
      getIncludes(filesToIncludeInManual.dropWhile(_ != "primitives").tail, documentationConf.getParent)
    val emptyToC =
      docConfig.tableOfContents.isEmpty ||
        (docConfig.tableOfContents.size == 1 && docConfig.tableOfContents.isDefinedAt(""))
    val additionalConfig = Map(
      "extensionName"         -> extensionName.capitalize,
      "prePrimitiveSections"  -> prePrimFiles,
      "postPrimitiveSections" -> postPrimFiles,
      "emptyTableOfContents"  -> Boolean.box(emptyToC)
    )
    val finalConfig = DocumentationConfig(
      markdownTemplate = netLogoConfig.markdownTemplate,
      primTemplate     = netLogoConfig.primTemplate,
      tableOfContents  = docConfig.tableOfContents,
      additionalConfig = docConfig.additionalConfig ++ additionalConfig ++ buildVariables
    )

    val renderedPage =
      renderMarkdown(extensionName)(Documenter.documentAll(finalConfig, primitives, documentationConf.getParent))
    Files.write(targetFile, renderedPage.getBytes("UTF-8"))
  }

  private def generateDocs(targetDir: File, documentedExtensions: Seq[(String, String)], variables: Map[String, Object]): Unit = {
    IO.createDirectory(targetDir)
    Mustache.betweenDirectories(docsSource, targetDir, variables)
    generateExtensionDocs(targetDir, documentedExtensions, variables)
    FileActions.copyFile(modelsDirectory / "Code Examples" / "Perspective Example.png", targetDir / "Perspective Example.png")
  }

  private def generateExtensionDocs(
    htmlFileRoot: File,
    documentedExtensions: Seq[(String, String)],
    buildVariables: Map[String, Object]): Seq[NioPath] = {
    documentedExtensions.map {
      case (extName, extTitle) =>
        val targetPath = (htmlFileRoot / (extName + ".html").toLowerCase).toPath
        generateHTMLPageForExtension(extName, targetPath, buildVariables)
    }
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

  private def renderMarkdown(extensionName: String)(str: String): String =
    Markdown(str, addTableOfContents = false, manualizeLinks = true, extName = Some(extensionName))

  private def getIncludes(filenames: Seq[String], basePath: NioPath): TemplateFunction =
    new TemplateFunction {
      override def apply(s: String): String = filenames
        .map(name => basePath.resolve(name))
        .map(path => Files.readAllLines(path).asScala.mkString("\n"))
        .mkString("\n\n")
    }
}
