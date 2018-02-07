// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.ModelType
import org.scalatest.FunSuite

class ModelTrackerTest extends FunSuite {
  def subject(modelType: ModelType, modelName: String) = {
    val helper = Helper.twoD
    helper.modelTracker._modelFileName = modelName
    helper.modelTracker.setModelType(modelType)
    helper.modelTracker
  }

  trait Helper {
    val messageCenter = new WorkspaceMessageCenter()
    val subject = new ModelTrackerImpl(messageCenter)
    def setModelPath(s: String) = subject.setModelPath(s)
  }

  def makeModelNameForDisplay(modelType: ModelType = ModelType.Normal, name: String = "foo.nlogo"): String = {
    subject(modelType, name).modelNameForDisplay
  }

  test("if the model is of model type new, modelName for Display is Untitled") {
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.New, null))
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.New, ""))
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.New, "empty.nlogo"))
  }
  test("if the model name is null, modelNameForDisplay is Untitled") {
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.New, null))
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.Library, null))
    assertResult("Untitled")(makeModelNameForDisplay(ModelType.Normal, null))
  }
  test("MakeModelNameForDisplay") {
    assertResult("there")(makeModelNameForDisplay(name = "there.nlogo"))
  }
  test("MakeModelNameForDisplay2") {
    assertResult("there")(makeModelNameForDisplay(name = "there"))
  }
  test("MakeModelNameForDisplay3") {
    assertResult("th.ere")(makeModelNameForDisplay(name = "th.ere"))
  }
  test("MakeModelNameForDisplay4") {
    assertResult("th.ere")(makeModelNameForDisplay(name = "th.ere.nlogo"))
  }
  test("MakeModelNameForDisplay5") {
    assertResult("foo.nlogo2")(makeModelNameForDisplay(name = "foo.nlogo2"))
  }


  test("GuessExportName1") { new Helper {
    setModelPath("foo.nlogo")
    assertResult("foo graphics.png")(subject.guessExportName("graphics.png"))
  } }
  test("GuessExportName2") { new Helper {
    setModelPath("fo.o.nlogo")
    assertResult("fo.o graph.ics.png")(subject.guessExportName("graph.ics.png"))
  } }
  test("GuessExportName3") { new Helper {
    setModelPath("foo.nlogo.nlogo")
    assertResult("foo.nlogo graphics.png")(subject.guessExportName("graphics.png"))
  } }
  test("GuessExportName4") { new Helper {
    setModelPath(null)
    assertResult("graphics.png")(subject.guessExportName("graphics.png"))
  } }
  test("GuessExportName5") { new Helper {
    setModelPath("foo")
    assertResult("foo graphics.png")(subject.guessExportName("graphics.png"))
  } }
  test("AttachModelDir1") { new Helper {
    setModelPath("/tmp/foo.nlogo")
    assertResult("/tmp/abc.txt")(subject.attachModelDir("abc.txt"))
  } }
  test("AttachModelDir2") { new Helper {
    setModelPath("/tmp/foo.nlogo")
    assertResult("/usr/abc.txt")(subject.attachModelDir("/usr/abc.txt"))
  } }
  test("AttachModelDir3") { new Helper {
    val home = System.getProperty("user.home")
    assertResult(s"$home/abc.txt")(subject.attachModelDir("abc.txt"))
  } }
}
