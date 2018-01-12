import java.io.File
import java.nio.file.Files
import sbt._
import Keys._
import NetLogoBuild.autogenRoot

import scala.xml.{ Attribute, Elem, Node, NodeSeq, Text, XML }
import org.nlogo.xmllib.plugin.{
  DataType => PluginDataType,
  XmlReaderGenerator => PluginReaderGenerator
}

object XmlReaderGenerator {
  val TargetNamespace = "http://ccl.northwestern.edu/netlogo/netlogox/1"

  lazy val experimentReader = taskKey[Seq[File]]("generate experiment reader")
  lazy val hubnetWidgetReader = taskKey[Seq[File]]("generate hubnet widget reader")
  lazy val linkShapeReader = taskKey[Seq[File]]("generate link shape reader")
  lazy val modelInfoReader = taskKey[Seq[File]]("generate model info reader")
  lazy val modelSettingsReader = taskKey[Seq[File]]("generate model settings reader")
  lazy val netLogoXsdSchema = settingKey[File]("model xsd schema")
  lazy val previewCommandsReader = taskKey[Seq[File]]("generate preview commands reader")
  lazy val vectorShapeReader = taskKey[Seq[File]]("generate vector shape reader")
  lazy val widgetReader = taskKey[Seq[File]]("generate widget reader")

  object ColorType extends PluginDataType {
    def attributeReaderName: Option[String] = Some("ColorReader.reader")
    def className: String = "RgbColor"
    override def writeValue(valueName: String): String =
      s"ColorReader.rgbColorToHex(${valueName})"
  }

  object DoubleColorType extends PluginDataType {
    def attributeReaderName: Option[String] = Some("ColorReader.doubleReader")
    def className: String = "Double"
    override def writeValue(valueName: String): String =
      s"ColorReader.colorDoubleToHex(${valueName})"
  }

  val additionalTypes =
    Map((TargetNamespace, "ColorType") -> ColorType,
      (TargetNamespace, "DoubleColor") -> DoubleColorType)

  lazy val parserSettings = Seq(
    netLogoXsdSchema := autogenRoot.value / "fileformat" / "model.xsd") ++
    generateTask("Widget", "org.nlogo.core.model", "org.nlogo.core", widgetReader, Some("WidgetXml.scala")) ++
    generateTask("ModelInfo", "org.nlogo.core.model", "org.nlogo.core", modelInfoReader, Some("ModelInfoXml.scala")) ++
    generateTask("HubNetWidget", "org.nlogo.core.model", "org.nlogo.core", hubnetWidgetReader, Some("HubNetWidgetXml.scala")) ++
    generateTask("VectorShape", "org.nlogo.core.model", "org.nlogo.core", vectorShapeReader, Some("VectorShapeXml.scala")) ++
    generateTask("LinkShape", "org.nlogo.core.model", "org.nlogo.core", linkShapeReader, Some("LinkShapeXml.scala"))


  lazy val additionalSectionsSettings =
    Seq(netLogoXsdSchema := autogenRoot.value / "fileformat" / "model.xsd") ++
      generateTask("ModelSettings", "org.nlogo.fileformat", "org.nlogo.api", modelSettingsReader, Some("ModelSettingsXml.scala")) ++
      generateTask("Experiment", "org.nlogo.fileformat", "org.nlogo.api", experimentReader, Some("ExperimentXml.scala")) ++
      generateTask("PreviewCommands", "org.nlogo.fileformat", "org.nlogo.api", previewCommandsReader, Some("PreviewCommandsXml.scala"))

  lazy val importSchemaSettings =
    Seq(
      resourceGenerators in Test += Def.task {
        val directory = (resourceManaged in Test).value
        Files.createDirectories((directory / "xfl").toPath)
        val typesXsd = directory / "xfl" / "types.xsd"
        val annotationsXsd = directory / "xfl" / "annotations.xsd"
        if (! Files.exists(typesXsd.toPath))
          Files.copy(XmlReaderGenerator.getClass.getResourceAsStream("types.xsd"), typesXsd.toPath)
        if (! Files.exists(annotationsXsd.toPath))
          Files.copy(XmlReaderGenerator.getClass.getResourceAsStream("annotations.xsd"), annotationsXsd.toPath)
        Seq(typesXsd, annotationsXsd)
      }
    )

  def generateTask(topLevel: String, packageName: String, importPackage: String, thisTask: TaskKey[Seq[File]], fileName: Option[String] = None): Seq[Def.Setting[_]] =
    Seq(
    thisTask := {
      FileFunction.cached(streams.value.cacheDirectory / ("cached" + fileName.getOrElse(topLevel)), inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
        (in: Set[File]) =>
          val contents = generateLibrary(netLogoXsdSchema.value, topLevel, packageName, importPackage)
          val generatedFile = (sourceManaged in Compile).value / packageName.replaceAllLiterally(".", "/") / fileName.getOrElse(topLevel + "Xml.scala")
          IO.write(generatedFile, contents.getBytes)
          Set(generatedFile)
      }(Set(netLogoXsdSchema.value)).toSeq
    },
    sourceGenerators in Compile += thisTask
  )

  def generateLibrary(schemaFile: File, topLevel: String, packageName: String, importPackage: String): String = {
    import scala.xml.XML
    val schema = XML.loadFile(schemaFile)
    PluginReaderGenerator.generateReaderWriter(schema, topLevel,
      packageName, importPackage, additionalTypes)
  }
}
