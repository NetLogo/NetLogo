import java.io.File
import sbt._
import Keys._
import NetLogoBuild.autogenRoot

import scala.xml.{ Attribute, Elem, Node, NodeSeq, Text, XML }
import org.nlogo.xmllib.plugin.{
  DataType => PluginDataType,
  XmlReaderGenerator => PluginReaderGenerator
}

object XmlReaderGenerator {
  lazy val experimentReader = taskKey[Seq[File]]("generate experiment reader")
  lazy val hubnetWidgetReader = taskKey[Seq[File]]("generate hubnet widget reader")
  lazy val linkShapeReader = taskKey[Seq[File]]("generate link shape reader")
  lazy val modelInfoReader = taskKey[Seq[File]]("generate model info reader")
  lazy val modelSettingsReader = taskKey[Seq[File]]("generate model settings reader")
  lazy val netLogoXsdSchema = settingKey[File]("netlogo xsd schema")
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

  object PointsType extends PluginDataType {
    val className = "Seq[(Int, Int)]"
    val attributeReaderName = Some("XmlReader.pointsReader")
    override def writeValue(valueName: String): String =
      s"""${valueName}.map(t => t._1 + "," + t._2).mkString(" ")"""
  }

  object DashArrayType extends PluginDataType {
    val attributeReaderName = Some("XmlReader.dashArrayReader")
    val className = "Seq[Float]"
    override def writeValue(valueName: String): String =
      s"XmlReader.dashArrayToString(${valueName})"
  }

  val additionalTypes =
    Map("svg:ColorType" -> ColorType,
      "svg:PointsType" -> PointsType,
      "svg:StrokeDashArrayValueType" -> DashArrayType,
      "nlx:DoubleColor" -> DoubleColorType)

  lazy val parserSettings = Seq(
    netLogoXsdSchema := autogenRoot.value / "fileformat" / "netlogo.xsd") ++
    generateTask("Widget", "org.nlogo.core.model", "org.nlogo.core", widgetReader, Some("WidgetXml.scala")) ++
    generateTask("modelInfo", "org.nlogo.core.model", "org.nlogo.core", modelInfoReader, Some("ModelInfoXml.scala")) ++
    generateTask("HubNetWidget", "org.nlogo.core.model", "org.nlogo.core", hubnetWidgetReader, Some("HubNetWidgetXml.scala")) ++
    generateTask("vectorShape", "org.nlogo.core.model", "org.nlogo.core", vectorShapeReader, Some("VectorShapeXml.scala")) ++
    generateTask("linkShape", "org.nlogo.core.model", "org.nlogo.core", linkShapeReader, Some("LinkShapeXml.scala"))


  lazy val additionalSectionsSettings =
    Seq(netLogoXsdSchema := autogenRoot.value / "fileformat" / "netlogo.xsd") ++
      generateTask("modelSettings", "org.nlogo.fileformat", "org.nlogo.api", modelSettingsReader, Some("ModelSettingsXml.scala")) ++
      generateTask("experiment", "org.nlogo.fileformat", "org.nlogo.api", experimentReader, Some("ExperimentXml.scala")) ++
      generateTask("previewCommands", "org.nlogo.fileformat", "org.nlogo.api", previewCommandsReader, Some("PreviewCommandsXml.scala"))

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
