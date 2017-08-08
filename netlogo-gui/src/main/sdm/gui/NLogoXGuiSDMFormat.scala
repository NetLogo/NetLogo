// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.io.{ ByteArrayOutputStream, ByteArrayInputStream, BufferedReader, StringReader }

import
  cats.data.Validated.{ Invalid, Valid }

import
  org.jhotdraw.util.{ StorableInput, StorableOutput }

import
  org.nlogo.core.{ model, Model => CoreModel },
    model.{ ElementFactory, MissingElement, XmlReader }

import
  org.nlogo.fileformat.{ NLogoXFormat, NLogoXFormatException }

import org.nlogo.api.{ AddableLoader, ComponentSerialization, ConfigurableModelLoader }

import
  scala.util.{ Failure, Success, Try }

class NLogoXGuiSDMFormat(factory: ElementFactory)
  extends AddableLoader
  with ComponentSerialization[NLogoXFormat.Section, NLogoXFormat] {
  override def componentName = "org.nlogo.modelsection.systemdynamics"
  override def addDefault = identity
  override def serialize(m: CoreModel): NLogoXFormat.Section = {
    m.optionalSectionValue[AggregateDrawing](componentName)
      .map(drawing => (drawingStrings(drawing), drawing.getModel.getDt))
      .map {
        case (strings, dt) =>
          factory.newElement("systemDynamics")
            .withAttribute("dt", dt.toString)
            .withElement(
              factory.newElement("jhotdraw6").withText(strings.mkString("\n")).build
            )
            .build
      }
      .getOrElse(factory.newElement("systemDynamics").build)
  }

  override def validationErrors(m: CoreModel): Option[String] =
    None

  override def deserialize(e: NLogoXFormat.Section): CoreModel => Try[CoreModel] = { (m: CoreModel) =>
    XmlReader.allElementReader("jhotdraw6").read(e)
      .map(XmlReader.childText _) match {
        case Valid(sdm) =>
          Try(stringsToDrawing(sdm.lines.toArray[String])
            .map(drawing => m.withOptionalSection(componentName, Some(drawing), drawing))
            .getOrElse(m))
          case Invalid(_: MissingElement) => Success(m)
          case Invalid(err) => Failure(new NLogoXFormatException(err.message))
      }
  }

  private def drawingStrings(drawing: AggregateDrawing): Array[String] = {
    if (drawing.getModel.elements.isEmpty)
      Array()
    else {
      val s = new ByteArrayOutputStream
      val output = new StorableOutput(s)
      output.writeDouble(drawing.getModel.getDt)
      output.writeStorable(drawing)
      output.close()
      s.flush()

      // JHotDraw has an annoying habit of including spaces at the end of lines.  we have stripped
      // those out of the models in version control, so to prevent spurious diffs, we need to keep
      // them from coming back - ST 3/10/09
      s.toString.lines.map(_.replaceAll("\\s*$", "")).toArray
    }
  }

  private def stringsToDrawing(s: Array[String]): Option[AggregateDrawing] = {
    val text = s.mkString("\n")
    if (text.trim.nonEmpty) {
      var text2 = mungeClassNames(text)
      // first parse out dt on our own as jhotdraw does not deal with scientific notation
      // properly. ev 10/11/05
      val br = new BufferedReader(new StringReader(text2))
      val dt = br.readLine().toDouble
      val str = br.readLine()
      text2 = text2.substring(text2.indexOf(str))
      val s = new ByteArrayInputStream(text2.getBytes())
      val input = new StorableInput(s)
      val drawing = input.readStorable.asInstanceOf[AggregateDrawing]
      drawing.synchronizeModel()
      drawing.getModel.setDt(dt)
      Some(drawing)
    } else
      None
  }

  private def mungeClassNames(input: String) =
    input.replaceAll(" *org.nlogo.sdm.Stock ",
                     "org.nlogo.sdm.gui.WrappedStock ")
         .replaceAll(" *org.nlogo.sdm.Rate ",
                     "org.nlogo.sdm.gui.WrappedRate ")
         .replaceAll(" *org.nlogo.sdm.Reservoir ",
                     "org.nlogo.sdm.gui.WrappedReservoir")
         .replaceAll(" *org.nlogo.sdm.Converter ",
                     "org.nlogo.sdm.gui.WrappedConverter")
         // also translate pre-4.1 save format
         .replaceAll("org.nlogo.aggregate.gui",
                     "org.nlogo.sdm.gui")

  def addToLoader(loader: ConfigurableModelLoader): ConfigurableModelLoader =
    loader.addSerializer[NLogoXFormat.Section, NLogoXFormat](this)
}
