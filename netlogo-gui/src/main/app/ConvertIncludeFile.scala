// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.nio.file.Path

import org.nlogo.core.{ DeclaresGlobal, Model, Slider, View }
import org.nlogo.fileformat.{ ConversionResult, ModelConversion }

class ConvertIncludeFile(modelConverter: ModelConversion, baseVersion: String) {
  /* We filter out widgets in such a way that we avoid two types of errors:
   * 1) Widgets which have code referring to procedures in the main code tab,
   *    which we don't have access to.
   * 2) Code in the includes file which references an interface global
   *    and requires that global be defined by some widget
   *  - RG 3/1/18
   */
  def apply(path: Path, currentModel: Model, source: String): ConversionResult = {
    val conversionWidgets = currentModel.widgets.collect {
      case s: Slider => s.copy(min = "0", max = "100", step = "1")
      case dg: DeclaresGlobal => dg
      case v: View => v
    }
    val tempModel = Model(code = source,
      widgets = conversionWidgets,
      version = baseVersion)
    modelConverter(tempModel, path)
  }
}
