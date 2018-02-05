// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import
  java.io.{ ByteArrayOutputStream, ByteArrayInputStream, BufferedReader, StringReader }

import
  org.jhotdraw.util.{ StorableInput, StorableOutput }

import
  org.nlogo.api.{ AddableLoader, ConfigurableModelLoader }

import
  org.nlogo.xmllib.ElementFactory

import
  org.nlogo.fileformat.NLogoXFormat

import
  org.nlogo.sdm.NLogoXBaseSDMFormat

class NLogoXGuiSDMFormat(val factory: ElementFactory)
  extends AddableLoader
  with NLogoXBaseSDMFormat[AggregateDrawing] {

  def objectToDt(a: AggregateDrawing): Double = a.getModel.getDt

  def objectToStrings(drawing: AggregateDrawing): String = {
    if (drawing.getModel.elements.isEmpty)
      ""
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
      // We drop the first item because we save dt as an attribute - RG 2/5/18
      s.toString.lines.drop(1).map(_.replaceAll("\\s*$", "")).toArray.mkString("\n")
    }
  }

  def stringsToObject(dt: Double, jhotdrawLines: String): Option[AggregateDrawing] = {
    val text = dt.toString + "\n" + jhotdrawLines
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
