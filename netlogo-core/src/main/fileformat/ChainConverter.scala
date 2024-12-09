// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.nio.file.Path

import org.nlogo.core.Model

import FileFormat.ModelConversion

class ChainConverter(converters: Seq[ModelConversion]) extends ModelConversion {
  def apply(model: Model, modelPath: Path): ConversionResult = {
    converters.foldLeft[ConversionResult](SuccessfulConversion(model, model)) {
      case (result, converter) => result.mergeResult(converter(result.model, modelPath))
    }
  }
}
