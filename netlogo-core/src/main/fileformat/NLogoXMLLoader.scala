// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.{ File, PrintWriter, StringWriter, Writer }
import java.net.URI

import org.nlogo.api.{ AbstractModelLoader, AggregateDrawingInterface, FileIO, LabProtocol, ModelSettings,
                       PreviewCommands, Version, XMLElement, XMLReader, XMLWriter }
import org.nlogo.core.{ DummyView, ExternalResource, Femto, LiteralParser, Model, Section, Widget, WorldDimensions,
                        WorldDimensions3D }

import scala.io.{ Codec, Source }
import scala.util.{ Failure, Success, Try }

class NLogoXMLLoader(headless: Boolean, literalParser: LiteralParser, editNames: Boolean) extends AbstractModelLoader {

  private implicit val codec = Codec.UTF8

  private lazy val defaultInfo: String = FileIO.url2String("/system/empty-info.md")

  private def isCompatible(extension: String): Boolean =
    extension == "nlogox" || extension == "nlogox3d"

  private def isCompatible(uri: URI): Boolean = {
    val extension = AbstractModelLoader.getURIExtension(uri)
    extension.isDefined && isCompatible(extension.get)
  }

  def readModel(uri: URI): Try[Model] = {
    val source = {
      if (uri.getScheme == "jar")
        Source.fromInputStream(uri.toURL.openStream)
      else
        Source.fromURI(uri)
    }

    val text = Try(source.mkString)

    source.close()

    text match {
      case Success(str) =>
        readModel(str, AbstractModelLoader.getURIExtension(uri).getOrElse(""))

      case Failure(e) =>
        Failure(e)
    }
  }

  def readModel(source: String, extension: String): Try[Model] = {

    if (isCompatible(extension)) {

      XMLReader.read(source).flatMap {
        element =>

          element.name match {
            case "model" =>

              import Model.{ defaultCode, defaultTurtleShapes, defaultLinkShapes }

              val version = element("version")
              val snapToGrid = element("snapToGrid", "false").toBoolean

              val settings  = new Section("org.nlogo.modelsection.modelsettings", ModelSettings(snapToGrid))

              val model = Model(defaultCode, List(DummyView), defaultInfo, version, defaultTurtleShapes,
                                defaultLinkShapes, List(settings), Seq())

              element.children.foldLeft(Try(model)) {
                case (model, XMLElement("widgets", _, _, children)) =>
                  model.map(_.copy(widgets = children.map(WidgetXMLLoader.readWidget).flatten))
                case (model, XMLElement("info", _, text, _)) =>
                  model.map(_.copy(info = text))
                case (model, XMLElement("code", _, text, _)) =>
                  model.map(_.copy(code = text))
                case (model, el @ XMLElement("turtleShapes", _, _, _)) =>
                  model.map(_.copy(turtleShapes = el.getChildren("shape").map(ShapeXMLLoader.readShape)))
                case (model, el @ XMLElement("linkShapes", _, _, _)) =>
                  model.map(_.copy(linkShapes = el.getChildren("shape").map(ShapeXMLLoader.readLinkShape)))
                case (model, XMLElement("previewCommands", _, commands, _)) =>
                  val section = new Section("org.nlogo.modelsection.previewcommands", PreviewCommands(commands))
                  model.map((m) => m.copy(optionalSections = m.optionalSections :+ section))
                case (model, el @ XMLElement("systemDynamics", _, _, _)) =>
                  val section = {
                    if (headless) {
                      new Section("org.nlogo.modelsection.systemdynamics",
                        Femto.get[AggregateDrawingInterface]("org.nlogo.sdm.Model").read(el))
                    } else {
                      new Section("org.nlogo.modelsection.systemdynamics.gui",
                        Femto.get[AggregateDrawingInterface]("org.nlogo.sdm.gui.AggregateDrawing").read(el))
                    }
                  }
                  model.map((m) => m.copy(optionalSections = m.optionalSections :+ section))
                case (model, XMLElement("experiments", _, _, children)) =>
                  val (bspaceElems, _) = children.foldLeft((Seq[LabProtocol](), Set[String]())) {
                    case ((elems, accNames), child) => {
                      val (elem, names) = LabXMLLoader.readExperiment(child, literalParser, editNames, accNames)
                      (elems :+ elem, accNames ++ names)
                    }
                  }
                  val section = new Section("org.nlogo.modelsection.behaviorspace", bspaceElems)
                  model.map((m) => m.copy(optionalSections = m.optionalSections :+ section))
                case (model, XMLElement("hubNetClient", _, _, children)) =>
                  val hnElems = children.map(WidgetXMLLoader.readWidget).flatten
                  val section = new Section("org.nlogo.modelsection.hubnetclient", hnElems)
                  model.map((m) => m.copy(optionalSections = m.optionalSections :+ section))
                case (model, el @ XMLElement("resources", _, _, _)) =>
                  model.map(_.copy(
                    resources = el.getChildren("resource").map(
                      resource => ExternalResource(resource("name"), resource("extension"), resource.text)
                    )
                  ))
                case (model, _) => model // ignore other sections for compatibility with other versions in the future (Isaac B 2/12/25)
              }

            case x =>
              Failure(new Exception(s"Expect 'model' element, but got: ${x}"))

          }

      }.map(model => model.copy(widgets = model.widgets.filter(_ != DummyView)))

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
    writer.escapedText(model.code)
    writer.endElement("code")

    writer.startElement("widgets")
    model.widgets.foreach(widget => writer.element(WidgetXMLLoader.writeWidget(widget)))
    writer.endElement("widgets")

    writer.startElement("info")
    writer.escapedText(model.info)
    writer.endElement("info")

    writer.element(XMLElement("turtleShapes", Map(), "", model.turtleShapes.map(ShapeXMLLoader.writeShape).toList))
    writer.element(XMLElement("linkShapes", Map(), "", model.linkShapes.map(ShapeXMLLoader.writeLinkShape).toList))

    for (section <- model.optionalSections) {
      section.key match {
        case "org.nlogo.modelsection.previewcommands" =>
          writer.startElement("previewCommands")
          writer.escapedText(section.get.get.asInstanceOf[PreviewCommands].source)
          writer.endElement("previewCommands")

        case "org.nlogo.modelsection.systemdynamics" | "org.nlogo.modelsection.systemdynamics.gui" =>
          writer.element(section.get.get.asInstanceOf[AggregateDrawingInterface].write())

        case "org.nlogo.modelsection.behaviorspace" =>
          section.get.map(section => {
            val experiments = section.asInstanceOf[Seq[LabProtocol]]

            if (experiments.nonEmpty) {
              writer.element(XMLElement("experiments", Map(), "",
                                        experiments.map(LabXMLLoader.writeExperiment).toList))
            }
          })

        case "org.nlogo.modelsection.hubnetclient" =>
          val widgets = section.get.get.asInstanceOf[Seq[Widget]]
          if (widgets.nonEmpty)
            writer.element(XMLElement("hubNetClient", Map(), "", widgets.map(WidgetXMLLoader.writeWidget)))

        case "org.nlogo.modelsection.modelsettings" =>
          // handled in model attributes

        case x =>
          throw new Error(s"Unhandlable file section type: ${x}")

      }
    }

    if (model.resources.nonEmpty) {

      writer.startElement("resources")

      for (resource <- model.resources) {

        writer.startElement("resource")

        writer.attribute("name", resource.name)
        writer.attribute("extension", resource.extension)
        writer.escapedText(resource.data)

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
      val writer = new PrintWriter(new File(uri), "UTF-8")
      saveToWriter(model, writer)
      writer.close()
      Success(uri)
    } else {
      Failure(new Exception(s"""Unable to save model with format "${AbstractModelLoader.getURIExtension(uri)}"."""))
    }
  }

  def sourceString(model: Model, extension: String): Try[String] = {
    if (isCompatible(extension)) {
      val writer = new StringWriter
      saveToWriter(model, writer)
      val result = writer.toString
      writer.close()
      Success(result)
    } else {
      Failure(new Exception(s"""Unable to create source string for model with format "${extension}"."""))
    }
  }

  def emptyModel(extension: String): Model = {
    if (isCompatible(extension)) {

      val (name, dims) =
        if (Version.is3D)
          ("NetLogo 3D 7.0.0", new WorldDimensions3D(-16, 16, -16, 16, -16, 16, 13.0))
        else
          ("NetLogo 7.0.0", WorldDimensions(-16, 16, -16, 16, 13.0))

      val widgets =
        List(Model.defaultView.copy(dimensions = dims))

      Model(Model.defaultCode, widgets, defaultInfo, name, Model.defaultTurtleShapes, Model.defaultLinkShapes)

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
    val result = Try(xmlWriter.element(XMLElement("experiments", Map(), "", writeStatuses)))
    xmlWriter.close()
    result
  }

}
