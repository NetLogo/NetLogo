import sbt._

import java.nio.file.{ Files, Path => NioPath }
import org.nlogo.build.{ DocumentationConfig, Documenter, HoconParser }
import org.pegdown.{ ast, LinkRenderer, PegDownProcessor, Extensions => PegdownExtensions }, LinkRenderer.Rendering, ast.{ AutoLinkNode, ExpLinkNode, MailLinkNode, RefLinkNode, WikiLinkNode }
import scala.collection.JavaConverters._
import scala.util.matching.Regex

class NetLogoDocs(docsSource: File, docsTarget: File, netLogoRoot: File, modelsDirectory: File, extensionsDirectory: File, extensionDocConfigFile: File) {
  val dictTarget = docsTarget / "dict"

  def manualComponents(base: File, extensions: Seq[String]): Seq[File] = {
    val allComponents = Seq(
    "whatis", "copyright", "versions", "requirements", "contact",
    "sample", "tutorial1", "tutorial2", "tutorial3", "interface",
    "infotab", "programming", "transition", "shapes",
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
    val primResult = HoconParser.parsePrimitives(configDocument)
    val renderedPrimitives = primResult.primitives.map(p => p.copy(description = renderMarkdown(extensionName)(p.description)))
    val filesToIncludeInManual: Seq[String] = configDocument.getStringList("filesToIncludeInManual").asScala
    val prePrimFiles =
      filesToIncludeInManual.takeWhile(_ != "primitives").map(documentationConf.resolveSibling).map(renderMarkdownPath(extensionName))
    val postPrimFiles =
      filesToIncludeInManual.dropWhile(_ != "primitives").tail.map(documentationConf.resolveSibling).map(renderMarkdownPath(extensionName))
    val emptyToC =
      docConfig.tableOfContents.isEmpty ||
        (docConfig.tableOfContents.size == 1 && docConfig.tableOfContents.isDefinedAt(""))
    val additionalConfig = Map(
      "extensionName"         -> renderedPrimitives.head.extensionName.capitalize,
      "prePrimitiveSections"  -> prePrimFiles.asJava,
      "postPrimitiveSections" -> postPrimFiles.asJava,
      "emptyTableOfContents"  -> Boolean.box(emptyToC)
    )
    val finalConfig = DocumentationConfig(
      markdownTemplate = netLogoConfig.markdownTemplate,
      primTemplate     = netLogoConfig.primTemplate,
      tableOfContents  = docConfig.tableOfContents,
      additionalConfig = docConfig.additionalConfig ++ additionalConfig ++ buildVariables
    )

    val renderedPage = Documenter.documentAll(finalConfig, renderedPrimitives, documentationConf.getParent)
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

  private def renderMarkdown(extensionName: String)(str: String): String = {
    val defaultLinkRenderer = new LinkRenderer()
    val customRenderer = new FakeGitHubLinkRenderer(extensionName, new ManualLinkRenderer(defaultLinkRenderer))
    new PegDownProcessor(PegdownExtensions.SMARTYPANTS |       // beautifies quotes, dashes, etc.
                         PegdownExtensions.AUTOLINKS |         // angle brackets around URLs and email addresses not needed
                         PegdownExtensions.FENCED_CODE_BLOCKS) // delimit code blocks with ```
      .markdownToHtml(str, customRenderer)
  }

  private def renderMarkdownPath(extensionName: String)(p: NioPath): String =
    renderMarkdown(extensionName)(new String(Files.readAllBytes(p), "UTF-8"))
}

class FakeGitHubLinkRenderer(extensionName: String, parent: LinkRenderer) extends LinkRenderer {
  val hashPrefix = s"#$extensionName"

  override def render(node: AutoLinkNode): Rendering = parent.render(node)
  override def render(node: ExpLinkNode, text: String): Rendering = {
    if (node.url.startsWith(hashPrefix)) {
      val newNode = new ExpLinkNode(node.title, s"${hashPrefix}:" + node.url.stripPrefix(hashPrefix), node.getChildren.get(0))
      parent.render(newNode, text)
    } else
      parent.render(node, text)
  }
  override def render(node: MailLinkNode): Rendering = parent.render(node)
  override def render(node: RefLinkNode, url: String, title: String, text: String): Rendering = {
    if (url.startsWith(hashPrefix))
      parent.render(node, s"${hashPrefix}:" + url.stripPrefix(hashPrefix), title, text)
    else
      parent.render(node, url, title, text)
  }
  override def render(node: WikiLinkNode): Rendering = parent.render(node)
}

class ManualLinkRenderer(parent: LinkRenderer) extends LinkRenderer {
  val nlDocURL = new Regex("(?:http://)ccl.northwestern.edu/netlogo/docs/")
  override def render(node: AutoLinkNode): Rendering = parent.render(node)
  override def render(node: ExpLinkNode, text: String): Rendering = {
    if (nlDocURL.findPrefixMatchOf(node.url).nonEmpty) {
      val newNode = new ExpLinkNode(node.title, nlDocURL.replaceFirstIn(node.url, ""), node.getChildren.get(0))
      parent.render(newNode, text)
    } else if (! node.url.startsWith("#"))
      parent.render(node, text).withAttribute(new LinkRenderer.Attribute("target", "_blank"))
    else
      parent.render(node, text)
  }
  override def render(node: MailLinkNode): Rendering = parent.render(node)
  override def render(node: RefLinkNode, url: String, title: String, text: String): Rendering = {
    if (nlDocURL.findPrefixMatchOf(url).nonEmpty)
      parent.render(node, nlDocURL.replaceFirstIn(url, ""), title, text)
    else if (! url.startsWith("#"))
      parent.render(node, url, title, text).withAttribute(new LinkRenderer.Attribute("target", "_blank"))
    else
      parent.render(node, url, title, text)
  }
  override def render(node: WikiLinkNode): Rendering = parent.render(node)
}
