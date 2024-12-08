// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.{ File, PrintWriter, StringReader, StringWriter, Writer }
import java.net.URI
import javax.xml.stream.{ XMLInputFactory, XMLOutputFactory, XMLStreamConstants, XMLStreamReader, XMLStreamWriter }

import org.nlogo.api.{ FileIO, GenericModelLoader, LabProtocol, LabXMLLoader, ModelSettings, PreviewCommands, Version,
                       WorldDimensions3D }
import org.nlogo.core.{ ExternalResource, LiteralParser, Model, OptionalSection, Section, ShapeXMLLoader, UpdateMode,
                        View, Widget, WidgetXMLLoader, WorldDimensions, XMLElement }
import org.nlogo.core.Shape.{ LinkShape, VectorShape }
// import org.nlogo.sdm.gui.SDMXMLLoader

import scala.collection.mutable.Set
import scala.io.Source
import scala.util.{ Failure, Success, Try }
import scala.util.matching.Regex

class NLogoXMLLoader(literalParser: LiteralParser, editNames: Boolean) extends GenericModelLoader {
  lazy private val defaultInfo: String = FileIO.url2String("/system/empty-info.md")

  private def readXMLElement(reader: XMLStreamReader): XMLElement = {
    val name = reader.getLocalName
    val attributes = (for (i <- 0 until reader.getAttributeCount) yield
                        ((reader.getAttributeLocalName(i), reader.getAttributeValue(i)))).toMap
    var text = ""
    var children = List[XMLElement]()

    var end = false

    while (reader.hasNext && !end) {
      reader.next match {
        case XMLStreamConstants.START_ELEMENT =>
          children = children :+ readXMLElement(reader)

        case XMLStreamConstants.END_ELEMENT =>
          end = true

        case XMLStreamConstants.CHARACTERS =>
          text = new Regex("]]" + XMLElement.CDATA_ESCAPE + ">").replaceAllIn(reader.getText, "]]>")

        case _ =>
      }
    }

    XMLElement(name, attributes, text, children)
  }

  private def writeXMLElement(writer: XMLStreamWriter, element: XMLElement) {
    writer.writeStartElement(element.name)

    for ((key, value) <- element.attributes)
      writer.writeAttribute(key, value)

    if (element.text.isEmpty)
      element.children.foreach(writeXMLElement(writer, _))
    else
      writeCDataEscaped(writer, element.text)

    writer.writeEndElement()
  }

  private def writeCDataEscaped(writer: XMLStreamWriter, text: String) {
    writer.writeCData(new Regex("]]>").replaceAllIn(text, "]]" + XMLElement.CDATA_ESCAPE + ">"))
  }

  private def isCompatible(extension: String): Boolean =
    (Version.is3D && extension == "nlogo3d") || (!Version.is3D && extension == "nlogo")

  private def isCompatible(uri: URI): Boolean = {
    val extension = GenericModelLoader.getURIExtension(uri)

    extension.isDefined && isCompatible(extension.get)
  }

  private def makeDimensions3D(dimensions: WorldDimensions, minPzcor: Int, maxPzcor: Int,
                               wrappingAllowedInZ: Boolean): WorldDimensions = {
    new WorldDimensions3D(dimensions.minPxcor, dimensions.maxPxcor, dimensions.minPycor, dimensions.maxPycor,
                          minPzcor, maxPzcor, dimensions.patchSize, dimensions.wrappingAllowedInX,
                          dimensions.wrappingAllowedInY, wrappingAllowedInZ)
  }

  def readModel(uri: URI): Try[Model] = {
    readModel(if (uri.getScheme == "jar") {
                Source.fromInputStream(uri.toURL.openStream).mkString
              } else {
                Source.fromURI(uri).mkString
              }, GenericModelLoader.getURIExtension(uri).getOrElse(""))
  }

  def readModel(source: String, extension: String): Try[Model] = {
    if (isCompatible(extension)) {
      val reader = XMLInputFactory.newFactory.createXMLStreamReader(new StringReader(source))

      while (reader.hasNext && reader.next != XMLStreamConstants.START_ELEMENT) {}

      val element = readXMLElement(reader)

      reader.close

      var code: Option[String] = None
      var widgets = List[Widget]()
      var info: Option[String] = None
      var version = ""
      var turtleShapes: Option[List[VectorShape]] = None
      var linkShapes: Option[List[LinkShape]] = None
      var optionalSections = List[OptionalSection[_]]()
      var openTempFiles = Seq[String]()
      var resources = Seq[ExternalResource]()

      element.name match {
        case "model" =>
          version = element("version")

          for (element <- element.children) {
            element.name match {
              case "widgets" =>
                widgets = element.children.map(WidgetXMLLoader.readWidget(_, makeDimensions3D))

              case "info" =>
                info = Some(element.text)

              case "code" =>
                code = Some(element.text)

              case "turtleShapes" =>
                turtleShapes = Some(element.getChildren("shape").map(ShapeXMLLoader.readShape))

              case "linkShapes" =>
                linkShapes = Some(element.getChildren("shape").map(ShapeXMLLoader.readLinkShape))

              case "previewCommands" =>
                optionalSections = optionalSections :+
                  new Section("org.nlogo.modelsection.previewcommands", PreviewCommands.Custom(element.text))

              // case "systemDynamics" =>
              //   optionalSections = optionalSections :+
              //     new Section("org.nlogo.modelsection.systemdynamics.gui", SDMXMLLoader.readDrawing(element))

              case "experiments" =>
                optionalSections = optionalSections :+
                  new Section("org.nlogo.modelsection.behaviorspace",
                              element.children.map(LabXMLLoader.readExperiment(_, literalParser, editNames, Set())))

              case "hubNetClient" =>
                optionalSections = optionalSections :+
                  new Section("org.nlogo.modelsection.hubnetclient",
                              element.children.map(WidgetXMLLoader.readWidget(_, makeDimensions3D)))

              case "settings" =>
                optionalSections = optionalSections :+
                  new Section("org.nlogo.modelsection.modelsettings", ModelSettings(element("snapToGrid").toBoolean))

              case "openTempFiles" =>
                openTempFiles = element.getChildren("file").map(element => element("path"))

              case "resources" =>
                resources = element.getChildren("resource").map(element =>
                  new ExternalResource(element("name"), element("type"), element.text))

              case _ =>
            }
          }

      }

      Success(Model(code.getOrElse(Model.defaultCode), widgets, info.getOrElse(defaultInfo), version,
                    turtleShapes.getOrElse(Model.defaultShapes), linkShapes.getOrElse(Model.defaultLinkShapes),
                    optionalSections, openTempFiles, resources))
    }

    else
      Failure(new Exception("Unable to open model with format \"" + extension + "\"."))
  }

  def saveToWriter(model: Model, destWriter: Writer) {
    val writer = XMLOutputFactory.newFactory.createXMLStreamWriter(destWriter)

    writer.writeStartDocument("utf-8", "1.0")

    writer.writeStartElement("model")

    writer.writeAttribute("version", model.version)

    writer.writeStartElement("widgets")

    model.widgets.foreach(widget => writeXMLElement(writer, WidgetXMLLoader.writeWidget(widget)))

    writer.writeEndElement()

    writer.writeStartElement("info")
    writeCDataEscaped(writer, model.info)
    writer.writeEndElement()

    writer.writeStartElement("code")
    writeCDataEscaped(writer, model.code)
    writer.writeEndElement()

    writeXMLElement(writer, XMLElement("turtleShapes", Map(), "",
                                       model.turtleShapes.map(ShapeXMLLoader.writeShape).toList))
    writeXMLElement(writer, XMLElement("linkShapes", Map(), "",
                                       model.linkShapes.map(ShapeXMLLoader.writeLinkShape).toList))

    for (section <- model.optionalSections) {
      section.key match {
        case "org.nlogo.modelsection.previewcommands" =>
          writer.writeStartElement("previewCommands")
          writeCDataEscaped(writer, section.get.get.asInstanceOf[PreviewCommands].source)
          writer.writeEndElement()

        // case "org.nlogo.modelsection.systemdynamics.gui" =>
        //   writeXMLElement(writer, SDMXMLLoader.writeDrawing(section.get.get.asInstanceOf[AnyRef]))

        case "org.nlogo.modelsection.behaviorspace" =>
          val experiments = section.get.get.asInstanceOf[Seq[LabProtocol]]

          if (experiments.nonEmpty)
            writeXMLElement(writer, XMLElement("experiments", Map(), "",
                                               experiments.map(LabXMLLoader.writeExperiment).toList))

        case "org.nlogo.modelsection.hubnetclient" =>
          val widgets = section.get.get.asInstanceOf[Seq[Widget]]

          if (widgets.nonEmpty)
            writeXMLElement(writer, XMLElement("hubNetClient", Map(), "",
                                               widgets.map(WidgetXMLLoader.writeWidget).toList))

        case "org.nlogo.modelsection.modelsettings" =>
          val settings = section.get.get.asInstanceOf[ModelSettings]

          if (settings.snapToGrid) {
            writer.writeStartElement("settings")
            writer.writeAttribute("snapToGrid", settings.snapToGrid.toString)
            writer.writeEndElement()
          }

        case _ =>
      }
    }

    if (model.openTempFiles.nonEmpty) {
      writer.writeStartElement("openTempFiles")

      for (file <- model.openTempFiles) {
        writer.writeStartElement("file")

        writer.writeAttribute("path", file)

        writer.writeEndElement()
      }

      writer.writeEndElement()
    }

    if (model.resources.nonEmpty) {
      writer.writeStartElement("resources")

      for (resource <- model.resources) {
        writer.writeStartElement("resource")

        writer.writeAttribute("name", resource.name)
        writer.writeAttribute("type", resource.resourceType)

        writeCDataEscaped(writer, resource.data)

        writer.writeEndElement()
      }

      writer.writeEndElement()
    }

    writer.writeEndElement()

    writer.writeEndDocument()

    writer.close()
  }

  def save(model: Model, uri: URI): Try[URI] = {
    if (isCompatible(uri)) {
      saveToWriter(model, new PrintWriter(new File(uri)))

      Success(uri)
    }

    else
      Failure(new Exception("Unable to save model with format \"" + GenericModelLoader.getURIExtension(uri) + "\"."))
  }

  def sourceString(model: Model, extension: String): Try[String] = {
    if (isCompatible(extension)) {
      val writer = new StringWriter

      saveToWriter(model, writer)

      Success(writer.toString)
    }

    else
      Failure(new Exception("Unable to create source string for model with format \"" + extension + "\"."))
  }

  def emptyModel(extension: String): Model = {
    if (isCompatible(extension)) {
      if (Version.is3D) {
        Model(Model.defaultCode, List(View(left = 210, top = 10, right = 649, bottom = 470,
                                           dimensions = new WorldDimensions3D(-16, 16, -16, 16, -16, 16, 13.0),
                                           fontSize = 10, updateMode = UpdateMode.Continuous,
                                           showTickCounter = true, frameRate = 30)),
              defaultInfo, "NetLogo 3D 6.4.0", Model.defaultShapes, Model.defaultLinkShapes)
      }

      else {
        Model(Model.defaultCode, List(View(left = 210, top = 10, right = 649, bottom = 470,
                                           dimensions = WorldDimensions(-16, 16, -16, 16, 13.0), fontSize = 10,
                                           updateMode = UpdateMode.Continuous, showTickCounter = true,
                                           frameRate = 30)),
              defaultInfo, "NetLogo 6.4.0", Model.defaultShapes, Model.defaultLinkShapes)
      }
    }

    else
      throw new Exception("Unable to create empty model with format \"" + extension + "\".")
  }

  def readExperiments(source: String, editNames: Boolean, existingNames: Set[String]): Try[Seq[LabProtocol]] = {
    val reader = XMLInputFactory.newFactory.createXMLStreamReader(new StringReader(source))

    Try(readXMLElement(reader).children.map(LabXMLLoader.readExperiment(_, literalParser, editNames, existingNames)))
  }

  def writeExperiments(experiments: Seq[LabProtocol], writer: Writer): Try[Unit] = {
    val xmlWriter = XMLOutputFactory.newFactory.createXMLStreamWriter(writer)

    Try(writeXMLElement(xmlWriter, XMLElement("experiments", Map(), "",
                                              experiments.map(LabXMLLoader.writeExperiment).toList)))
  }
}
