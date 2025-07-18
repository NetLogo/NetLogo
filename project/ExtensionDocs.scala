import sbt._

import java.nio.file.{ Files, Path => NioPath }
import scala.collection.JavaConverters._

import com.github.mustachejava.TemplateFunction
import org.nlogo.build.{ DocumentationConfig, Documenter, HoconParser }
import Docs.docsRoot

class ExtensionDocs(extensionsDirectory: File, extensionDocConfigFile: File, headerFile: File) {
  def generateHTMLPageForExtension(
    extShortName:   String,
    extFullName:    String,
    targetFile:     NioPath,
    buildVariables: Map[String, Object]): NioPath = {

    val documentationConf = (extensionsDirectory / extShortName / "documentation.conf").toPath
    val headerTemplate = Files.readString(headerFile.toPath())
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



    // Find </title> in the header template and insert the extension name before it.
    val titleInsertIndex = headerTemplate.indexOf("</title>")
    val titledHeader = if (titleInsertIndex != -1) {
      headerTemplate.substring(0, titleInsertIndex) + extFullName.capitalize + headerTemplate.substring(titleInsertIndex)
    } else { headerTemplate }


    val additionalConfig = Map(
      "extensionName"         -> extFullName.capitalize,
      "extensionShortName"    -> extShortName.toLowerCase(),
      "prePrimitiveSections"  -> prePrimFiles,
      "postPrimitiveSections" -> postPrimFiles,
      "emptyTableOfContents"  -> Boolean.box(emptyToC),
      "header"                -> titledHeader
    )
    val finalConfig = DocumentationConfig(
      markdownTemplate = netLogoConfig.markdownTemplate,
      primTemplate     = netLogoConfig.primTemplate,
      tableOfContents  = docConfig.tableOfContents,
      additionalConfig = docConfig.additionalConfig ++ additionalConfig ++ buildVariables
    )

    val renderedPage =
      renderMarkdown(extFullName)(Documenter.documentAll(finalConfig, primitives, documentationConf.getParent))
    val res = Files.write(targetFile, renderedPage.getBytes("UTF-8"))

    val imagesCopy = Path.allSubpaths(extensionsDirectory / extShortName / "images").map {
      case (f, p) => (f, targetFile.getParent.toFile / "images" / p)
    }
    FileActions.copyAll(imagesCopy)

    res
  }

  def generateExtensionDocs(
    htmlFileRoot: File,
    docsSource: File,
    documentedExtensions: Seq[(String, String)],
    buildVariables: Map[String, Object]): Seq[NioPath] = documentedExtensions map {
      case (extShortName, extFullName) =>
        val primRoot = extShortName.toLowerCase()                 // Need for links in entries
        val fileName = primRoot + ".html"                         // Need for links in entries
        val targetFile = htmlFileRoot / fileName                  // Top-level path for prims
        val targetPath = targetFile.toPath                        // Extension README in docs

        // Generate the HTML page for the extension
        generateHTMLPageForExtension(extShortName, extFullName, targetPath, buildVariables + ("primRoot" -> primRoot))

        // Generate the dictionary reference for
        val targetDir = htmlFileRoot / extShortName
        IO.createDirectory(targetDir)

        val dictVars = Map[String, Object](
          "dictHome" -> fileName,
          "dictTitle" -> s"$extFullName Extension Dictionary",
          "primRoot" -> primRoot, // /<version>/<primRoot>/<prim>
        )
        PrimIndex.generate(
          targetFile,
          targetDir,
          docsSource / "dictTemplate.html.mustache",
          targetDir / "index.txt",
          headerFile,
          dictVars,
          generateHTMLIndexPage = true
        )

        targetPath
    }

  private def wrapString(
    str: String,
    pre: String = "",
    post: String = ""
  ): String = {
    val wrapped = str.linesIterator.map(line => pre + line + post).mkString("\n")
    if (wrapped.nonEmpty) wrapped + "\n" else wrapped
  }

  private def renderMarkdown(extensionName: String)(str: String): String = {
    Markdown(str, extensionName, extension = true)
  }

  private def getIncludes(filenames: Seq[String], basePath: NioPath): TemplateFunction =
    new TemplateFunction {
      override def apply(s: String): String = filenames
        .map(name => basePath.resolve(name))
        .map(path => Files.readAllLines(path).asScala.mkString("\n"))
        .mkString("\n\n")
    }
}
