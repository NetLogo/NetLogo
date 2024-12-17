// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.{ File, PrintWriter, StringWriter, Writer }
import java.net.URI

import org.nlogo.api.{ AbstractModelLoader, AggregateDrawingInterface, FileIO, LabProtocol, LabXMLLoader,
                       ModelSettings, PreviewCommands, Version }
import org.nlogo.core.{ ExternalResource, Femto, LiteralParser, Model, Section, ShapeXMLLoader, UpdateMode,
                        View, Widget, WidgetXMLLoader, WorldDimensions, WorldDimensions3D, XMLElement, XMLReader,
                        XMLWriter }

import scala.io.Source
import scala.util.{ Failure, Success, Try }

class NLogoXMLLoader(literalParser: LiteralParser, editNames: Boolean) extends AbstractModelLoader {

  private lazy val defaultInfo: String = FileIO.url2String("/system/empty-info.md")

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

      XMLReader.read(source).flatMap {
        element =>

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
                      resource => ExternalResource(resource("name"), resource("extension"), resource.text)
                    )
                  ))
                case (    _, XMLElement(name, _, _, _)) =>
                  Failure(new Exception(s"Unexpected file section: ${name}"))

              }

            case x =>
              Failure(new Exception(s"Expect 'model' element, but got: ${x}"))

          }

      }

    } else {
      Failure(new Exception(s"""Unable to open model with format "${extension}"."""))
    }

  }

  def saveToWriter(model: Model, destWriter: Writer) {

    val writer = new XMLWriter(destWriter)

    writer.startDocument()
    writer.startElement("model")
    writer.attribute("version", model.version)

    model.optionalSections.find(_.key == "org.nlogo.modelsection.modelsettings").foreach(section =>
      writer.attribute("snapToGrid", section.get.get.asInstanceOf[ModelSettings].snapToGrid.toString)
    )

    writer.startElement("code")
    writer.cData(model.code)
    writer.endElement("code")

    writer.startElement("widgets")
    model.widgets.foreach(widget => writer.element(WidgetXMLLoader.writeWidget(widget)))
    writer.endElement("widgets")

    writer.startElement("info")
    writer.cData(model.info)
    writer.endElement("info")

    writer.element(XMLElement("turtleShapes", Map(), "", model.turtleShapes.map(ShapeXMLLoader.writeShape).toList))
    writer.element(XMLElement("linkShapes", Map(), "", model.linkShapes.map(ShapeXMLLoader.writeLinkShape).toList))

    for (section <- model.optionalSections) {
      section.key match {
        case "org.nlogo.modelsection.previewcommands" =>
          writer.startElement("previewCommands")
          writer.cData(section.get.get.asInstanceOf[PreviewCommands].source)
          writer.endElement("previewCommands")

        case "org.nlogo.modelsection.systemdynamics.gui" =>
          writer.element(section.get.get.asInstanceOf[AggregateDrawingInterface].write())

        case "org.nlogo.modelsection.behaviorspace" =>
          val experiments = section.get.get.asInstanceOf[Seq[LabProtocol]]
          if (experiments.nonEmpty)
            writer.element(XMLElement("experiments", Map(), "", experiments.map(LabXMLLoader.writeExperiment).toList))

        case "org.nlogo.modelsection.hubnetclient" =>
          val widgets = section.get.get.asInstanceOf[Seq[Widget]]
          if (widgets.nonEmpty)
            writer.element(XMLElement("hubNetClient", Map(), "", widgets.map(WidgetXMLLoader.writeWidget).toList))

        case "org.nlogo.modelsection.modelsettings" =>
          // handled in model attributes

        case x =>
          throw new Error(s"Unhandlable file section type: ${x}")

      }
    }

    if (model.openTempFiles.nonEmpty) {

      writer.startElement("openTempFiles")

      for (file <- model.openTempFiles) {
        writer.startElement("file")
        writer.attribute("path", file)
        writer.endElement("file")
      }

      writer.endElement("openTempFiles")

    }

    if (model.resources.nonEmpty) {

      writer.startElement("resources")

      for (resource <- model.resources) {

        writer.startElement("resource")

        writer.attribute("name", resource.name)
        writer.attribute("extension", resource.extension)
        writer.cData(resource.data)

        writer.endElement("resource")

      }

      writer.endElement("resources")

    }

    writer.endElement("model")
    writer.endDocument()
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
    XMLReader.read(source).map(_.children.foldLeft((Seq[LabProtocol](), existingNames)) {
      case ((acc, names), elem) =>
        val (proto, newNames) = LabXMLLoader.readExperiment(elem, literalParser, editNames, names)
        (acc :+ proto, newNames)
    })
  }

  def writeExperiments(experiments: Seq[LabProtocol], writer: Writer): Try[Unit] = {
    val xmlWriter     = new XMLWriter(writer)
    val writeStatuses = experiments.map(LabXMLLoader.writeExperiment).toList
    Try(xmlWriter.element(XMLElement("experiments", Map(), "", writeStatuses)))
  }

}
