// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.{ ModelType, NetLogoLegacyDialect }
import org.nlogo.core.Femto
import org.scalatest.funsuite.AnyFunSuite

class ModelTrackerTest extends AnyFunSuite {
  class Subject(modelType: ModelType, modelName: String) extends ModelTracker {
    _modelFileName = modelName
    setModelType(modelType)
    def compiler = Femto.get("org.nlogo.compile.Compiler", NetLogoLegacyDialect)
    def getExtensionManager() = null
  }

  def makeModelNameForDisplay(modelType: ModelType = ModelType.Normal, name: String = "foo.nlogox"): String = {
    new Subject(modelType, name).modelNameForDisplay
  }

  test("if the model is of model type new, modelName for Display is Untitled") {
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.New, null))
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.New, ""))
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.New, "empty.nlogox"))
  }
  test("if the model name is null, modelNameForDisplay is Untitled") {
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.New, null))
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.Library, null))
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.Normal, null))
  }
  test("MakeModelNameForDisplay") {
    assertResult("there")(makeModelNameForDisplay(name = "there.nlogox"))
  }
  test("MakeModelNameForDisplay2") {
    assertResult("there")(makeModelNameForDisplay(name = "there"))
  }
  test("MakeModelNameForDisplay3") {
    assertResult("th.ere")(makeModelNameForDisplay(name = "th.ere"))
  }
  test("MakeModelNameForDisplay4") {
    assertResult("th.ere")(makeModelNameForDisplay(name = "th.ere.nlogox"))
  }
  test("MakeModelNameForDisplay5") {
    assertResult("foo.nlogo2")(makeModelNameForDisplay(name = "foo.nlogo2"))
  }
}
