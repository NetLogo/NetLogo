// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.{ File, PrintWriter, StringWriter, Writer }
import java.net.URI

import org.nlogo.api.{ AbstractModelLoader, AggregateDrawingInterface, FileIO, LabProtocol,
                       PreviewCommands, Version, XMLReader, XMLWriter }
import org.nlogo.core.{ Femto, LiteralParser, Model, Section, Widget, XMLElement }
import org.nlogo.core.model.{ ModelXMLLoader, NLogoXMLWriter, WidgetXMLLoader }

import scala.io.{ Codec, Source }
import scala.util.{ Failure, Success, Try }

class NLogoXMLLoader(headless: Boolean, literalParser: LiteralParser, editNames: Boolean) extends AbstractModelLoader {
  private implicit val codec: scala.io.Codec = Codec.UTF8

  private lazy val defaultInfo: String = FileIO.url2String("/system/empty-info.md")

  override def isCompatible(extension: String): Boolean =
    extension == "nlogox" || extension == "nlogox3d"

  override def isCompatible(uri: URI): Boolean =
    AbstractModelLoader.getURIExtension(uri).exists(isCompatible)

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

          val (model, unknownSections) = ModelXMLLoader.loadBasics(element, defaultInfo)

          element.children.foldLeft((model, Set[String]())) {

            case ((model, sections), XMLElement("previewCommands", _, commands, _)) =>
              val section = new Section("org.nlogo.modelsection.previewcommands", PreviewCommands(commands))
              (model.map((m) => m.copy(optionalSections = m.optionalSections :+ section)), sections)

            case ((model, sections), el @ XMLElement("systemDynamics", _, _, _)) =>
              val section = {
                if (headless) {
                  new Section("org.nlogo.modelsection.systemdynamics",
                    Femto.get[AggregateDrawingInterface]("org.nlogo.sdm.Model").read(el))
                } else {
                  new Section("org.nlogo.modelsection.systemdynamics.gui",
                    Femto.get[AggregateDrawingInterface]("org.nlogo.sdm.gui.AggregateDrawing").read(el))
                }
              }
              (model.map((m) => m.copy(optionalSections = m.optionalSections :+ section)), sections)

            case ((model, sections), XMLElement("experiments", _, _, children)) =>
              val (bspaceElems, _) = children.foldLeft((Seq[LabProtocol](), Set[String]())) {
                case ((elems, accNames), child) => {
                  val (elem, names) = LabXMLLoader.readExperiment(child, literalParser, editNames, accNames)
                  (elems :+ elem, accNames ++ names)
                }
              }
              val section = new Section("org.nlogo.modelsection.behaviorspace", bspaceElems)
              (model.map((m) => m.copy(optionalSections = m.optionalSections :+ section)), sections)

            case ((model, sections), XMLElement("hubNetClient", _, _, children)) =>
              val hnElems = children.map(WidgetXMLLoader.readWidget).flatten
              val section = new Section("org.nlogo.modelsection.hubnetclient", hnElems)
              (model.map((m) => m.copy(optionalSections = m.optionalSections :+ section)), sections)

            // ignore other sections for compatibility with other versions in the future (Isaac B 2/12/25)
            // but still keep track of them in case the user wanted them in there (Isaac B 7/6/25)
            case ((model, sections), XMLElement(name, _, _, _)) =>
              (model, sections + name)

          } match {
            case (model, unknownSectionNames) =>
              // unknownSections was constructed in ModelXMLLoader, which doesn't know about the above sections here,
              // so compute the intersection of the two sets to make sure valid sections don't get added to
              // unknownSections (Isaac B 7/6/25)
              model.map(_.copy(unknownSections = unknownSections.filter(el => unknownSectionNames.contains(el.name))))
          }

      }

    } else {
      Failure(new Exception(s"""Unable to open model with format "${extension}"."""))
    }

  }

  def writeExtras(writer: NLogoXMLWriter, model: Model): Unit = {
    for (section <- model.optionalSections) {
      section.key match {
        case "org.nlogo.modelsection.previewcommands" =>
          writer.startElement("previewCommands")
          writer.escapedText(section.get.get.asInstanceOf[PreviewCommands].source)
          writer.endElement("previewCommands")

        case "org.nlogo.modelsection.systemdynamics" | "org.nlogo.modelsection.systemdynamics.gui" =>
          Option(section.get.get.asInstanceOf[AggregateDrawingInterface].write()).foreach(writer.element)

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

    model.unknownSections.foreach(writer.element)
  }

  def saveToWriter(model: Model, destWriter: Writer): Unit = {
    val writer = new XMLWriter(destWriter)
    ModelXMLLoader.writeBasics(writer, model, writeExtras)
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
      ModelXMLLoader.emptyModel(Version.is3D, defaultInfo)
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
