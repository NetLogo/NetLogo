// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

import TestEnvironment.projectFilePath

class ModelReaderTests extends FunSuite {

  /// parseModel

  test("parseModel: empty model has correct version string") {
    val emptyModel = FileIO.file2String(projectFilePath("resources/system/empty.nlogo"))
    val map = ModelReader.parseModel(emptyModel)
    assert("NetLogo (no version)" === ModelReader.parseVersion(map))
  }
  test("parseModel: trailing section with no separator isn't ignored") {
    val map = ModelReader.parseModel("foo\n" + ModelReader.SEPARATOR + "\nbar\n")
    assert(map.get(ModelSection.Code).toList === List("foo"))
    assert(map.get(ModelSection.Interface).toList === List("bar"))
  }
  test("parseModel: missing sections result in empty arrays") {
    val map = ModelReader.parseModel("")
    assert(map.get(ModelSection.Interface).toList === Nil)
  }
}
