// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import
  org.nlogo.xmllib.{ ElementFactory }

// NOTE: If you're looking for the ComponentSerialization used
// in NetLogo-GUI, you want org.nlogo.sdm.gui.NLogoXGuiSDMFormat.
// This is *only* used when loading the sdm section of the model
// headlessly. Why the difference? Headless doesn't know anything
// about the graphical-only components of the model, just sdm.Model
// GUI, meanwhile, knows about everything and deserializes an
// AggregateDrawing. - RG 9/19/17
class NLogoXSDMFormat(val factory: ElementFactory) extends NLogoXBaseSDMFormat[Model] {
  def stringsToObject(dt: Double, jhotdrawLines: String): Option[Model] =
    Loader.load(dt.toString + "\n" + jhotdrawLines)
  def objectToStrings(a: Model): String =
    a.serializedGUI
  def objectToDt(a: Model): Double =
    a.getDt
}
