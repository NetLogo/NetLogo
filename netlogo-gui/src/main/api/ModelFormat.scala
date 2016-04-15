// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.util.Utils
import org.nlogo.core.{ ComponentProvider, Femto, LiteralParser, Model, Shape, ShapeParser, View, Widget }, Shape.{ LinkShape, VectorShape }
import org.nlogo.core.model.WidgetReader

import scala.io.Source

trait ComponentSerialization[A, B, C <: ModelFormat[B, _]] {
  def componentName: String
  def default: A
  def serialize(c: A): B
  def validationErrors(t: A): Option[String]
  def deserialize(s: B): A
}

trait ModelFormat[Section, Format <: ModelFormat[Section, _]] {
  def name: String
  def sections(location: java.net.URI): Seq[(String, Section)]
  def codeComponent:       ComponentSerialization[String, Section, Format]
  def infoComponent:       ComponentSerialization[String, Section, Format]
  def interfaceComponent:  ComponentSerialization[Seq[Widget], Section, Format]
  def shapesComponent:     ComponentSerialization[Seq[VectorShape], Section, Format]
  def linkShapesComponent: ComponentSerialization[Seq[LinkShape], Section, Format]
  def version:             ComponentSerialization[String, Section, Format]
  def load(location: java.net.URI, optionalComponents: Seq[ComponentSerialization[ComponentProvider, Section, Format]]): Model
}

class NLogoFormat extends ModelFormat[Array[String], NLogoFormat] {
  def name: String = "nlogo"
  val SEPARATOR = "@#\\$#@#\\$#@"

  def sections(location: java.net.URI) = {
    val fileAsString = Source.fromURI(location).mkString
    val sections = fileAsString.split(SEPARATOR)
    val sectionNames =
      ModelSection.allSections.map(s => "org.nlogo.modelsection." +
        s.getClass.getSimpleName.replaceAll("\\$", "").toLowerCase)
    (sectionNames zip sections.map(_.lines.toArray))
  }

  object CodeComponent extends ComponentSerialization[String, Array[String], NLogoFormat] {
    val componentName = "org.nlogo.modelsection.code"
    def default: String = ""
    def serialize(c: String): Array[String] = c.lines.toArray
    def validationErrors(t: String): Option[String] = None
    def deserialize(s: Array[String]): String = s.mkString("\n")
  }

  object InfoComponent extends ComponentSerialization[String, Array[String], NLogoFormat] {
    val componentName = "org.nlogo.modelsection.info"
    val EmptyInfoPath = "/system/empty-info.md"
    def default: String = Utils.url2String(EmptyInfoPath)
    def serialize(c: String): Array[String] = c.lines.toArray
    def validationErrors(t: String): Option[String] = None
    def deserialize(s: Array[String]): String = s.mkString("\n")
  }

  object VersionComponent extends ComponentSerialization[String, Array[String], NLogoFormat] {
    val componentName = "org.nlogo.modelsection.version"
    def default: String = Version.version
    def serialize(c: String): Array[String] = Array(c)
    def validationErrors(t: String): Option[String] = None
    def deserialize(s: Array[String]): String = s.mkString.trim
  }

  object InterfaceComponent extends ComponentSerialization[Seq[Widget], Array[String], NLogoFormat] {
    import org.nlogo.fileformat
    val componentName = "org.nlogo.modelsection.interface"
    private val additionalReaders = fileformat.nlogoReaders(Version.is3D)
    private val literalParser = Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")
    def default = Seq(View())

    def serialize(ws: Seq[Widget]): Array[String] =
      ws.flatMap((w: Widget) => (WidgetReader.format(w, additionalReaders).lines.toSeq :+ "")).toArray

    def validationErrors(t: Seq[Widget]): Option[String] = None

    private def parseWidgets(lines: Array[String]): List[List[String]] = {
      val widgets = new collection.mutable.ListBuffer[List[String]]
      val widget = new collection.mutable.ListBuffer[String]
      for(line <- lines)
        if(line.nonEmpty)
          widget += line
        else {
          if(!widget.forall(_.isEmpty))
            widgets += widget.toList
          widget.clear()
        }
        if(!widget.isEmpty)
          widgets += widget.toList
        widgets.toList
    }

    def deserialize(s: Array[String]): Seq[Widget] = {
      val widgets = parseWidgets(s)
      widgets.map(w => WidgetReader.read(w.toList, literalParser, additionalReaders))
    }
  }

  object VectorShapesComponent extends ComponentSerialization[Seq[VectorShape], Array[String], NLogoFormat] {
    val componentName = "org.nlogo.modelsection.turtleshapes"
    def default: Seq[VectorShape] = Model.defaultShapes.toSeq
    def serialize(c: Seq[VectorShape]): Array[String] = ShapeParser.formatVectorShapes(c).lines.toArray
    def validationErrors(t: Seq[VectorShape]): Option[String] = None
    def deserialize(s: Array[String]): Seq[VectorShape] = ShapeParser.parseVectorShapes(s)
  }

  object LinkShapesComponent extends ComponentSerialization[Seq[LinkShape], Array[String], NLogoFormat] {
    val componentName = "org.nlogo.modelsection.linkshapes"
    def default: Seq[LinkShape] = Model.defaultLinkShapes.toSeq
    def serialize(c: Seq[LinkShape]): Array[String] = ShapeParser.formatLinkShapes(c).lines.toArray
    def validationErrors(t: Seq[LinkShape]): Option[String] = None
    def deserialize(s: Array[String]): Seq[LinkShape] = ShapeParser.parseLinkShapes(s)
  }

  def codeComponent       = CodeComponent
  def infoComponent       = InfoComponent
  def interfaceComponent  = InterfaceComponent
  def version             = VersionComponent
  def shapesComponent     = VectorShapesComponent
  def linkShapesComponent = LinkShapesComponent

  def load(location: java.net.URI, optionalComponents: Seq[ComponentSerialization[ComponentProvider, Array[String], NLogoFormat]]): Model = {
    val loadedSections = sections(location).toMap
    val code = codeComponent.deserialize(loadedSections(codeComponent.componentName))
    val info = infoComponent.deserialize(loadedSections(infoComponent.componentName))
    val modelVersion = version.deserialize(loadedSections(version.componentName))
    val interface = interfaceComponent.deserialize(loadedSections(interfaceComponent.componentName))
    Model(code = code, info = info, version = modelVersion, widgets = interface.toList)
  }
}
