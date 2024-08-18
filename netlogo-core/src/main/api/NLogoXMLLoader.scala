// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.{ File, PrintWriter, StringReader, StringWriter, Writer }
import java.net.URI
import javax.xml.stream.{ XMLInputFactory, XMLOutputFactory, XMLStreamConstants }

import org.nlogo.core.{ Model, OptionalSection, ShapeXMLLoader, UpdateMode, View, Widget, WidgetXMLLoader,
                        WorldDimensions, XMLElement }
import org.nlogo.core.Shape.{ LinkShape, VectorShape }
import org.nlogo.sdm.gui.SDMXMLLoader

import scala.io.Source
import scala.util.{ Failure, Success, Try }

class NLogoXMLLoader(editNames: Boolean) extends GenericModelLoader {
  lazy private val defaultInfo: String = FileIO.url2String("/system/empty-info.md")

  private def isCompatible(extension: String): Boolean =
    extension == "nlogo" || extension == "nlogo3d"

  private def isCompatible(uri: URI): Boolean = {
    val extension = GenericModelLoader.getURIExtension(uri)

    extension.isDefined && isCompatible(extension.get)
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

      def readXMLElement(): XMLElement = {
        val name = reader.getLocalName
        val attributes = (for (i <- 0 until reader.getAttributeCount) yield
                            ((reader.getAttributeLocalName(i), reader.getAttributeValue(i)))).toMap
        var text = ""
        var children = List[XMLElement]()

        var end = false

        while (reader.hasNext && !end) {
          reader.next match {
            case XMLStreamConstants.START_ELEMENT =>
              children = children :+ readXMLElement

            case XMLStreamConstants.END_ELEMENT =>
              end = true
            
            case XMLStreamConstants.CHARACTERS =>
              text = reader.getText
            
            case _ =>
          }
        }

        XMLElement(name, attributes, text, children)
      }

      var code: Option[String] = None
      var widgets = List[Widget]()
      var info: Option[String] = None
      var version = ""
      var turtleShapes: Option[List[VectorShape]] = None
      var linkShapes: Option[List[LinkShape]] = None
      var optionalSections = List[OptionalSection[_]]()

      while (reader.hasNext && reader.next != XMLStreamConstants.START_ELEMENT) {}

      val element = readXMLElement

      element.name match {
        case "model" =>
          version = element.attributes("version")

          for (element <- element.children) {
            element.name match {
              case "widgets" =>
                widgets = element.children.map(WidgetXMLLoader.readWidget)
              
              case "info" =>
                info = Some(element.text)

              case "code" =>
                code = Some(element.text)
              
              case "turtleShapes" =>
                turtleShapes = Some(for (element <- element.children if element.name == "shape")
                                      yield ShapeXMLLoader.readShape(element))
              
              case "linkShapes" =>
                linkShapes = Some(for (element <- element.children if element.name == "shape")
                                    yield ShapeXMLLoader.readLinkShape(element))
              
              case "previewCommands" =>
                optionalSections = optionalSections :+
                  new OptionalSection("org.nlogo.modelsection.previewcommands",
                                      Some(PreviewCommands.Custom(element.text)), PreviewCommands.Default)

              case "systemDynamics" =>
                optionalSections = optionalSections :+
                  new OptionalSection("org.nlogo.modelsection.systemdynamics.gui",
                                       Some(SDMXMLLoader.readDrawing(element)), SDMXMLLoader.defaultDrawing)

              case "experiments" =>
                optionalSections = optionalSections :+
                  new OptionalSection("org.nlogo.modelsection.behaviorspace",
                                      Some(for (element <- element.children if element.name == "experiment") yield
                                             LabXMLLoader.readExperiment(element)), Seq[LabProtocol]())

              case "hubNetClient" =>
                optionalSections = optionalSections :+
                  new OptionalSection("org.nlogo.modelsection.hubnetclient",
                                      Some(element.children.map(WidgetXMLLoader.readWidget)), Seq[Widget]())

              case "settings" =>
                optionalSections = optionalSections :+
                  new OptionalSection("org.nlogo.modelsection.modelsettings",
                                      Some(ModelSettings(element.attributes("snapToGrid").toBoolean)),
                                      ModelSettings(false))

              case "deltaTick" =>
                // not sure what this is

            }
          }

      }

      reader.close

      Success(Model(code.getOrElse(Model.defaultCode), widgets, info.getOrElse(defaultInfo), version,
                    turtleShapes.getOrElse(Model.defaultShapes), linkShapes.getOrElse(Model.defaultLinkShapes),
                    optionalSections))
    }

    else
      Failure(new Exception("Unable to open model with format \"" + extension + "\"."))
  }


  def saveToWriter(model: Model, destWriter: Writer) {
    val writer = XMLOutputFactory.newFactory.createXMLStreamWriter(destWriter)

    def writeXMLElement(element: XMLElement) {
      writer.writeStartElement(element.name)

      for ((key, value) <- element.attributes)
        writer.writeAttribute(key, value)
      
      if (element.text.isEmpty)
        element.children.foreach(writeXMLElement)
      else
        writer.writeCData(element.text)

      writer.writeEndElement()
    }

    writer.writeStartDocument("utf-8", "1.0")

    writer.writeStartElement("model")

    writer.writeAttribute("version", model.version)

    writeXMLElement(XMLElement("widgets", Map(), "", model.widgets.map(WidgetXMLLoader.writeWidget).toList))

    writer.writeStartElement("info")
    writer.writeCData(model.info)
    writer.writeEndElement

    writer.writeStartElement("code")
    writer.writeCData(model.code)
    writer.writeEndElement

    writeXMLElement(XMLElement("turtleShapes", Map(), "", model.turtleShapes.map(ShapeXMLLoader.writeShape).toList))
    writeXMLElement(XMLElement("linkShapes", Map(), "", model.linkShapes.map(ShapeXMLLoader.writeLinkShape).toList))

    for (section <- model.optionalSections) {
      section.key match {
        case "org.nlogo.modelsection.previewcommands" =>
          writer.writeStartElement("previewCommands")
          writer.writeCData(section.get.get.asInstanceOf[PreviewCommands].source)
          writer.writeEndElement

        case "org.nlogo.modelsection.systemdynamics.gui" =>
          writeXMLElement(SDMXMLLoader.writeDrawing(section.get.get.asInstanceOf[AnyRef]))

        case "org.nlogo.modelsection.systemdynamics" =>
          // ignore, duplicate of previous case

        case "org.nlogo.modelsection.behaviorspace" =>
          val experiments = section.get.get.asInstanceOf[Seq[LabProtocol]]

          if (experiments.nonEmpty)
            writeXMLElement(XMLElement("experiments", Map(), "", experiments.map(LabXMLLoader.writeExperiment).toList))

        case "org.nlogo.modelsection.hubnetclient" =>
          val widgets = section.get.get.asInstanceOf[Seq[Widget]]

          if (widgets.nonEmpty)
            writeXMLElement(XMLElement("hubNetClient", Map(), "", widgets.map(WidgetXMLLoader.writeWidget).toList))

        case "org.nlogo.modelsection.modelsettings" =>
          val settings = section.get.get.asInstanceOf[ModelSettings]

          if (settings.snapToGrid) {
            writer.writeStartElement("settings")
            writer.writeAttribute("snapToGrid", settings.snapToGrid.toString)
            writer.writeEndElement
          }

        case "org.nlogo.modelsection.deltatick" =>
          // not sure what this is

      }
    }

    writer.writeEndElement

    writer.writeEndDocument

    writer.close
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
      Model(Model.defaultCode, List(View(left = 210, top = 10, right = 649, bottom = 470,
                                   dimensions = WorldDimensions(-16, 16, -16, 16, 13.0), fontSize = 10,
                                   updateMode = UpdateMode.Continuous, showTickCounter = true, frameRate = 30)),
            defaultInfo, "NetLogo 6.4.0", Model.defaultShapes, Model.defaultLinkShapes)
    }

    else
      throw new Exception("Unable to create empty model with format \"" + extension + "\".")
  }
}
