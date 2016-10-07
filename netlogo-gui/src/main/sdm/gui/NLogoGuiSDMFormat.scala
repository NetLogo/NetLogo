// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.io.{ ByteArrayOutputStream, ByteArrayInputStream, BufferedReader, StringReader }

import org.jhotdraw.util.{ StorableInput, StorableOutput }

import org.nlogo.sdm.Translator
import org.nlogo.core.{ LiteralParser, Model => CoreModel }
import org.nlogo.fileformat.NLogoFormat
import org.nlogo.api.ComponentSerialization

import scala.util.Try

class NLogoGuiSDMFormat extends ComponentSerialization[Array[String], NLogoFormat] {
  override def componentName = "org.nlogo.modelsection.systemdynamics"
  override def addDefault = identity
  override def serialize(m: CoreModel): Array[String] = {
    m.optionalSectionValue[AggregateDrawing](componentName)
      .map(drawingStrings)
      .getOrElse(Array[String]())
  }

  override def validationErrors(m: CoreModel): Option[String] =
    None

  override def deserialize(s: Array[String]): CoreModel => Try[CoreModel] = { (m: CoreModel) =>
    Try {
      stringsToDrawing(s)
        .map(sdm => m.withOptionalSection(componentName, Some(sdm), sdm))
        .getOrElse(m)
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
}
