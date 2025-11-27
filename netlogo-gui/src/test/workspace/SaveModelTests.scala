// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.URI
import java.nio.file.Paths

import org.nlogo.core.Model
import org.nlogo.api.{ ConfigurableModelLoader, ModelType, Version }

import org.scalatest.funsuite.AnyFunSuite

import org.nlogo.util.PathUtils

import scala.util.{ Failure, Try }

class SaveModelTests extends AnyFunSuite {
  val model = Model()

  def testSave(withModel: Model => Model = identity,
    controller: MockController = new MockController,
    modelType: ModelType = ModelType.New,
    error: Option[Exception] = None,
    existingPath: Option[String] = None,
    forcePathSelect: Boolean = false)(
    assertion: (Option[Try[String]], MockController) => Unit): Unit = {
      val model = withModel(Model(version = "NetLogo 6.0.4"))
      val model3d = withModel(Model(version = "NetLogo 3D 6.0.4"))
      val format = new MockFormat(model, error)
      val format3d = new MockFormat(model3d, error)
      val loader = new ConfigurableModelLoader()
        .addFormat[String, MockFormat](format)
        .addFormat[String, MockFormat](format3d)
      val modelTracker = new ModelTracker {
        override def getModelType = modelType
        override def getModelPath: String = existingPath.orNull
        override def compiler = null
        override def getExtensionManager() = null
      }
      val res =
        if (forcePathSelect)
          SaveModelAs(model, loader, controller, modelTracker, Version)
            .map(_.apply().map(u => "[A-Z]:".r.replaceFirstIn(PathUtils.standardize(Paths.get(u).toString), "")))
        else
          SaveModel(model, loader, controller, modelTracker, Version)
            .map(_.apply().map(u => "[A-Z]:".r.replaceFirstIn(PathUtils.standardize(Paths.get(u).toString), "")))
      assertion(res, controller)
  }

  def testSave3d(withModel: Model => Model = identity,
    controller: MockController = new MockController,
    modelType: ModelType = ModelType.New,
    error: Option[Exception] = None,
    existingPath: Option[String] = None,
    forcePathSelect: Boolean = false)(
    assertion: (Option[Try[String]], MockController) => Unit): Unit = {
      val model = withModel(Model(version = "NetLogo 6.0.4"))
      val model3d = withModel(Model(version = "NetLogo 3D 6.0.4"))
      val format = new MockFormat(model, error)
      val format3d = new MockFormat(model3d, error)
      val loader = new ConfigurableModelLoader()
        .addFormat[String, MockFormat](format)
        .addFormat[String, MockFormat](format3d)
      val modelTracker = new ModelTracker {
        override def getModelType = modelType
        override def getModelPath: String = existingPath.orNull
        override def compiler = null
        override def getExtensionManager() = null
      }
      val res =
        if (forcePathSelect)
          SaveModelAs(model, loader, controller, modelTracker, Version)
            .map(_.apply().map(u => "[A-Z]:".r.replaceFirstIn(PathUtils.standardize(Paths.get(u).toString), "")))
        else
          SaveModel(model, loader, controller, modelTracker, Version)
            .map(_.apply().map(u => "[A-Z]:".r.replaceFirstIn(PathUtils.standardize(Paths.get(u).toString), "")))
      assertion(res, controller)
  }

  /* Tests that are NetLogo version agnostic */

  test("if workspace fileMode is Normal, but the user is forced to select a path, use the chosen path with 3D model") {
    testSave3d(modelType = ModelType.Normal,
      existingPath = Some("/existing/save.test"),
      forcePathSelect = true) { (result, _) =>
      assert(Some(Try("/tmp/nlogo.test")) == result)
    }
  }

  test("if workspace fileMode is Normal, saves at the workspace model path with 3D model") {
    testSave3d(modelType = ModelType.Normal, existingPath = Some("/existing/save.test")) { (result, _) =>
      assert(Some(Try("/existing/save.test")) == result)
    }
  }

  test("if ModelType is New, prompts for path and saves as specified path with 3D model") {
    testSave3d() { (result, _) =>
      assert(Some(Try("/tmp/nlogo.test")) == result)
    }
  }

  test("if user doesn't select a path when prompted, returns None with 3D model") {
    testSave3d(controller = new MockController(chosenFilePaths = Seq())) { (result, _) =>
      assert(result == None)
    }
  }

  test("if user elects not to save when warned of differing version, returns None with 3D model") {
    testSave3d(withModel = _.copy(version = "NetLogo 2.0"), controller = new MockController()) { (result, _) =>
      assert(result == None)
    }
  }

  test("if workspace fileMode is LIBRARY, prompts for path and saves as specified path with 3D model") {
    testSave3d(modelType = ModelType.Library) { (result, _) =>
      assert(Some(Try("/tmp/nlogo.test")) == result)
    }
  }

  test("if the original version of the model doesn't match the current version of NetLogo, checks before saving with 3D model") {
    val versionString = if(Version.version.contains("3D")) "NetLogo 3D 100" else "NetLogo 100"
    testSave3d(withModel = _.copy(version = versionString),
      controller = new MockController(continueSavingModel = true)) { (_, controller) =>
        assert(controller.warnedOfDifferingVersion == versionString)
      }
  }

  test("if user tries to save the file in a non-default NetLogo file format, accept the file with 3D model") {
    val filePaths = Seq(new URI("file:///invalid.invalid"), new URI("file:///valid.test"))
    testSave3d(controller = new MockController(chosenFilePaths = filePaths)) { (result, controller) =>
      assert(result == Some(Try("/invalid.invalid")))
    }
  }

  test("if the file fails to save, reports an error with 3D model") {
    val error = new Exception("couldn't save file")
    testSave3d(error = Some(error)) { (result, controller) =>
      assert(result.get == Failure(error))
    }
  }

  test("if workspace fileMode is Normal, saves at the workspace model path") {
    testSave(modelType = ModelType.Normal, existingPath = Some("/existing/save.test")) { (result, _) =>
      assert(Some(Try("/existing/save.test")) == result)
    }
  }

  test("if workspace fileMode is Normal, but the user is forced to select a path, use the chosen path") {
    testSave(modelType = ModelType.Normal,
      existingPath = Some("/existing/save.test"),
      forcePathSelect = true) { (result, _) =>
      assert(Some(Try("/tmp/nlogo.test")) == result)
    }
  }

  test("if ModelType is New, prompts for path and saves as specified path") {
    testSave() { (result, _) =>
      assert(Some(Try("/tmp/nlogo.test")) == result)
    }
  }

  test("if user doesn't select a path when prompted, returns None") {
    testSave(controller = new MockController(chosenFilePaths = Seq())) { (result, _) =>
      assert(result == None)
    }
  }

  test("if user elects not to save when warned of differing version, returns None") {
    testSave(withModel = _.copy(version = "NetLogo 2.0"), controller = new MockController()) { (result, _) =>
      assert(result == None)
    }
  }

  test("if workspace fileMode is LIBRARY, prompts for path and saves as specified path") {
    testSave(modelType = ModelType.Library) { (result, _) =>
      assert(Some(Try("/tmp/nlogo.test")) == result)
    }
  }

  test("if the original version of the model doesn't match the current version of NetLogo, checks before saving") {
    val versionString = if(Version.version.contains("3D")) "NetLogo 3D 100" else "NetLogo 100"
    testSave(withModel = _.copy(version = versionString),
      controller = new MockController(continueSavingModel = true)) { (_, controller) =>
        assert(controller.warnedOfDifferingVersion == versionString)
      }
  }

  test("if user tries to save the file in a non-default NetLogo file format, accept the file") {
    val filePaths = Seq(new URI("file:///invalid.invalid"), new URI("file:///valid.test"))
    testSave(controller = new MockController(chosenFilePaths = filePaths)) { (result, controller) =>
      assert(result == Some(Try("/invalid.invalid")))
    }
  }

  test("if the file fails to save, reports an error") {
    val error = new Exception("couldn't save file")
    testSave(error = Some(error)) { (result, controller) =>
      assert(result.get == Failure(error))
    }
  }

  class MockController(
    var chosenFilePaths: Seq[URI] = Seq(new URI("file:///tmp/nlogo.test")),
    continueSavingModel: Boolean = false)
    extends SaveModel.Controller {
      var warnedOfDifferingVersion = ""
      var warnedOfInvalidFileFormat = ""

      def chooseFilePath(modelType: ModelType): Option[URI] = {
        val r = chosenFilePaths.headOption
        if (chosenFilePaths.nonEmpty)
          chosenFilePaths = chosenFilePaths.tail
        r
      }

      def warnInvalidFileFormat(extension: String): Unit = {
        warnedOfInvalidFileFormat = extension
      }

      def shouldSaveModelOfDifferingVersion(version: String): Boolean = {
        warnedOfDifferingVersion = version
        continueSavingModel
      }
    }

}
