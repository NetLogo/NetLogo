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
    val additionalConfig = Map(
      "extensionName"         -> extFullName.capitalize,
      "prePrimitiveSections"  -> prePrimFiles,
      "postPrimitiveSections" -> postPrimFiles,
      "emptyTableOfContents"  -> Boolean.box(emptyToC),
      "header"                -> headerTemplate
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
    documentedExtensions: Seq[(String, String)],
    buildVariables: Map[String, Object]): Seq[NioPath] = documentedExtensions map {
      case (extShortName, extFullName) =>
        val targetPath = (htmlFileRoot / (extShortName + ".html").toLowerCase).toPath
        generateHTMLPageForExtension(extShortName, extFullName, targetPath, buildVariables)
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
