// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.{ DummyView, ExternalResource, Model, ModelSettings, Section, WorldDimensions, WorldDimensions3D }

import scala.util.{ Failure, Try }

// This class exists to provide basic loading to/from XML that Tortoise can consume, while sharing the same code with
// desktop.  -Jeremy B June 2025

object ModelXMLLoader {

  def emptyModel(is3D: Boolean, defaultInfo: String): Model = {
    val (name, dims) =
      if (is3D)
        ("NetLogo 3D 7.0.0-beta2", new WorldDimensions3D(-16, 16, -16, 16, -16, 16, 13.0))
      else
        ("NetLogo 7.0.0-beta2", WorldDimensions(-16, 16, -16, 16, 13.0))

    val widgets =
      List(Model.defaultView.copy(dimensions = dims))

    Model(Model.defaultCode, widgets, defaultInfo, name, Model.defaultTurtleShapes, Model.defaultLinkShapes)
  }

  def loadBasics(root: XMLElement, defaultInfo: String): Try[Model] = {
    root.name match {
      case "model" =>

        import Model.{ defaultCode, defaultTurtleShapes, defaultLinkShapes }

        val version = root("version")
        val snapToGrid = root("snapToGrid", "false").toBoolean

        val settings = new Section("org.nlogo.modelsection.modelsettings", ModelSettings(snapToGrid))

        val model = Model(defaultCode, List(DummyView), defaultInfo, version, defaultTurtleShapes,
                          defaultLinkShapes, List(settings), Seq())

        root.children.foldLeft(Try(model)) {

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

          case (model, el @ XMLElement("resources", _, _, _)) =>
            model.map(_.copy(
              resources = el.getChildren("resource").map(
                resource => ExternalResource(resource("name"), resource("extension"), resource.text)
              )
            ))

          case (model, _) => model // ignore other sections for compatibility with other versions in the future (Isaac B 2/12/25)
        }.map(model => model.copy(widgets = model.widgets.filter(_ != DummyView)))

      case x =>
        Failure(new Exception(s"Expect 'model' element, but got: ${x}"))

    }
  }

  def writeBasics(writer: NLogoXMLWriter, model: Model, writeExtras: (w: NLogoXMLWriter, m: Model) => Unit): Unit = {
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

    writeExtras(writer, model)

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
}
