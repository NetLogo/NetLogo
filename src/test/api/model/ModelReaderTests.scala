// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model

import org.scalatest.FunSuite
import org.nlogo.util.Utils.getResourceAsString

class ModelReaderTests extends FunSuite {

  /// parseModel

  test("parseModel: empty model has correct version string") {
    val emptyModel = getResourceAsString("/system/empty.nlogo")
    val model = ModelReader.parseModel(emptyModel)
    assert("NetLogo (no version)" === model.version)
  }
}
