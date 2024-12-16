// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.{ File, PrintWriter, StringReader, StringWriter, Writer }
import java.net.URI
import java.util.Base64
import javax.xml.stream.{ XMLInputFactory, XMLOutputFactory, XMLStreamConstants, XMLStreamException, XMLStreamReader,
                          XMLStreamWriter }

import org.nlogo.api.{ AbstractModelLoader, AggregateDrawingInterface, FileIO, LabProtocol, LabXMLLoader,
                       ModelSettings, PreviewCommands, Version }
import org.nlogo.core.{ ExternalResource, Femto, LiteralParser, Model, Section, ShapeXMLLoader, UpdateMode,
                        View, Widget, WidgetXMLLoader, WorldDimensions, WorldDimensions3D, XMLElement }

import scala.io.Source
import scala.util.{ Failure, Success, Try }
import scala.util.matching.Regex

class NLogoXMLLoader(literalParser: LiteralParser, editNames: Boolean) extends AbstractModelLoader {

  private lazy val defaultInfo: String = FileIO.url2String("/system/empty-info.md")

  private def readXMLElement(reader: XMLStreamReader): XMLElement = {

    def parseElement(reader: XMLStreamReader, acc: XMLElement): XMLElement = {
      if (reader.hasNext)
        reader.next match {
          case XMLStreamConstants.START_ELEMENT =>
            parseElement(reader, acc.copy(children = acc.children :+ readXMLElement(reader)))
          case XMLStreamConstants.END_ELEMENT =>
            acc
          case XMLStreamConstants.CHARACTERS =>
            val newAcc = acc.copy(text = new Regex(s"]]${XMLElement.CDATA_ESCAPE}>").replaceAllIn(reader.getText, "]]>"))
            parseElement(reader, newAcc)
          case x =>
            throw new Exception(s"Unexpected value found while parsing XML: ${x}")
        }
      else
        acc
    }

    val attributes =
      (0 until reader.getAttributeCount).
        map((i) => reader.getAttributeLocalName(i) -> reader.getAttributeValue(i)).
        toMap

    parseElement(reader, XMLElement(reader.getLocalName, attributes,  "", Seq[XMLElement]()))

  }

  private def writeXMLElement(writer: XMLStreamWriter, element: XMLElement): Unit = {

    writer.writeStartElement(element.name)

    for ((key, value) <- element.attributes)
      writer.writeAttribute(key, value)

    if (element.text.isEmpty)
      element.children.foreach(writeXMLElement(writer, _))
    else
      writeCDataEscaped(writer, element.text)

    writer.writeEndElement()

  }

  private def writeCDataEscaped(writer: XMLStreamWriter, text: String): Unit = {
    writer.writeCData(new Regex("]]>").replaceAllIn(text, "]]" + XMLElement.CDATA_ESCAPE + ">"))
  }

  private def isCompatible(extension: String): Boolean =
    (Version.is3D && extension == "nlogo3d") || (!Version.is3D && extension == "nlogo")

  private def isCompatible(uri: URI): Boolean = {
    val extension = AbstractModelLoader.getURIExtension(uri)
    extension.isDefined && isCompatible(extension.get)
  }

  def readModel(uri: URI): Try[Model] = {
    val text =
      if (uri.getScheme == "jar")
        Source.fromInputStream(uri.toURL.openStream).mkString
      else
        Source.fromURI(uri).mkString
    readModel(text, AbstractModelLoader.getURIExtension(uri).getOrElse(""))
  }

  def readModel(source: String, extension: String): Try[Model] = {

    if (isCompatible(extension)) {

      val reader = XMLInputFactory.newFactory.createXMLStreamReader(new StringReader(source))

      try {
        while (reader.hasNext && reader.next != XMLStreamConstants.START_ELEMENT) {}
      }

      catch {
        case e: XMLStreamException => return Failure(new Exception(e))
      }

      val element = readXMLElement(reader)

      reader.close()

      element.name match {
        case "model" =>

          import Model.{ defaultCode, defaultShapes, defaultLinkShapes }

          val version = element("version")
          val snapToGrid = element("snapToGrid", "false").toBoolean

          val settings  = new Section("org.nlogo.modelsection.modelsettings", ModelSettings(snapToGrid))

          val model = Model(defaultCode, List(View()), defaultInfo, version, defaultShapes, defaultLinkShapes, List(settings), Seq(), Seq())

          element.children.foldLeft(Try(model)) {
            case (model, XMLElement("widgets", _, _, children)) =>
              model.map(_.copy(widgets = children.map(WidgetXMLLoader.readWidget)))
            case (model, XMLElement("info", _, text, _)) =>
              model.map(_.copy(info = text))
            case (model, XMLElement("code", _, text, _)) =>
              model.map(_.copy(code = text))
            case (model, el @ XMLElement("turtleShapes", _, _, _)) =>
              model.map(_.copy(turtleShapes = el.getChildren("shape").map(ShapeXMLLoader.readShape)))
            case (model, el @ XMLElement("linkShapes", _, _, _)) =>
              model.map(_.copy(linkShapes = el.getChildren("shape").map(ShapeXMLLoader.readLinkShape)))
            case (model, XMLElement("previewCommands", _, commands, _)) =>
              val section = new Section("org.nlogo.modelsection.previewcommands", PreviewCommands.Custom(commands))
              model.map((m) => m.copy(optionalSections = m.optionalSections :+ section))
            case (model, el @ XMLElement("systemDynamics", _, _, _)) =>
              val section = new Section("org.nlogo.modelsection.systemdynamics.gui",
                Femto.get[AggregateDrawingInterface]("org.nlogo.sdm.gui.AggregateDrawing").read(el))
              model.map((m) => m.copy(optionalSections = m.optionalSections :+ section))
            case (model, XMLElement("experiments", _, _, children)) =>
              val bspaceElems = children.map(LabXMLLoader.readExperiment(_, literalParser, editNames, Set()))
              val section     = new Section("org.nlogo.modelsection.behaviorspace", bspaceElems)
              model.map((m) => m.copy(optionalSections = m.optionalSections :+ section))
            case (model, XMLElement("hubNetClient", _, _, children)) =>
              val hnElems = children.map(WidgetXMLLoader.readWidget)
              val section = new Section("org.nlogo.modelsection.hubnetclient", hnElems)
              model.map((m) => m.copy(optionalSections = m.optionalSections :+ section))
            case (model, el @ XMLElement("openTempFiles", _, _, _)) =>
              model.map(_.copy(openTempFiles = el.getChildren("file").map(file => file("path"))))
            case (model, el @ XMLElement("resources", _, _, _)) =>
              model.map(_.copy(
                resources = el.getChildren("resource").map(
                  resource => ExternalResource(resource("name"), Base64.getDecoder.decode(resource.text), resource("extension"))
                )
              ))
            case (    _, XMLElement(name, _, _, _)) =>
              Failure(new Exception(s"Unexpected file section: ${name}"))

          }

        case x =>
          Failure(new Exception(s"Expect 'model' element, but got: ${x}"))

      }

    } else {
      Failure(new Exception(s"""Unable to open model with format "${extension}"."""))
    }

  }

  def saveToWriter(model: Model, destWriter: Writer) {

    val writer = XMLOutputFactory.newFactory.createXMLStreamWriter(destWriter)

    writer.writeStartDocument("utf-8", "1.0")
    writer.writeStartElement("model")
    writer.writeAttribute("version", model.version)

    model.optionalSections.find(_.key == "org.nlogo.modelsection.modelsettings").foreach(section =>
      writer.writeAttribute("snapToGrid", section.get.get.asInstanceOf[ModelSettings].snapToGrid.toString)
    )

    writer.writeStartElement("code")
    writeCDataEscaped(writer, model.code)
    writer.writeEndElement()

    writer.writeStartElement("widgets")
    model.widgets.foreach(widget => writeXMLElement(writer, WidgetXMLLoader.writeWidget(widget)))
    writer.writeEndElement()

    writer.writeStartElement("info")
    writeCDataEscaped(writer, model.info)
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

        case "org.nlogo.modelsection.systemdynamics.gui" =>
          writeXMLElement(writer, section.get.get.asInstanceOf[AggregateDrawingInterface].write())

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
          // handled in model attributes

        case x =>
          throw new Error(s"Unhandlable file section type: ${x}")

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
        writer.writeAttribute("extension", resource.extension)

        writeCDataEscaped(writer, Base64.getEncoder.encodeToString(resource.data))

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
    } else {
      Failure(new Exception(s"""Unable to save model with format "${AbstractModelLoader.getURIExtension(uri)}"."""))
    }
  }

  def sourceString(model: Model, extension: String): Try[String] = {
    if (isCompatible(extension)) {
      val writer = new StringWriter
      saveToWriter(model, writer)
      Success(writer.toString)
    } else {
      Failure(new Exception(s"""Unable to create source string for model with format "${extension}"."""))
    }
  }

  def emptyModel(extension: String): Model = {
    if (isCompatible(extension)) {

      val (name, dims) =
        if (Version.is3D)
          ("NetLogo 3D 6.4.0", new WorldDimensions3D(-16, 16, -16, 16, -16, 16, 13.0))
        else
          ("NetLogo 6.4.0", WorldDimensions(-16, 16, -16, 16, 13.0))

      val widgets =
        List(View( x = 210, y = 10, width = 439, height = 460, dimensions = dims, fontSize = 10
                 , updateMode = UpdateMode.Continuous, showTickCounter = true, frameRate = 30))

      Model(Model.defaultCode, widgets, defaultInfo, name, Model.defaultShapes, Model.defaultLinkShapes)

    } else {
      throw new Exception(s"""Unable to create empty model with format "${extension}".""")
    }
  }

  def readExperiments(source: String, editNames: Boolean, existingNames: Set[String]): Try[(Seq[LabProtocol], Set[String])] = {
    val reader = XMLInputFactory.newFactory.createXMLStreamReader(new StringReader(source))
    Try(
      readXMLElement(reader).children.foldLeft((Seq[LabProtocol](), existingNames)) {
        case ((acc, names), elem) =>
          val (proto, newNames) = LabXMLLoader.readExperiment(elem, literalParser, editNames, names)
          (acc :+ proto, newNames)
      }
    )
  }

  def writeExperiments(experiments: Seq[LabProtocol], writer: Writer): Try[Unit] = {
    val xmlWriter     = XMLOutputFactory.newFactory.createXMLStreamWriter(writer)
    val writeStatuses = experiments.map(LabXMLLoader.writeExperiment).toList
    Try(writeXMLElement(xmlWriter, XMLElement("experiments", Map(), "", writeStatuses)))
  }

}
