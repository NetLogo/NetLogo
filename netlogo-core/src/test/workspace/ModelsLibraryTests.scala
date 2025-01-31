// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.File

import org.nlogo.api.{ ExtensionManager => APIEM, Version }

import org.scalatest.funsuite.AnyFunSuite

class ModelsLibraryTests extends AnyFunSuite {
  test("needsModelScan returns true when a scan needs to be performed") {
    ModelsLibrary.rootNode = None
    assert(ModelsLibrary.needsModelScan)
    ModelsLibrary.getModelPaths
    assert(! ModelsLibrary.needsModelScan)
  }
  test("getModelPathsAtRoot returns a list of all model paths in the given directory") {
    assert(ModelsLibrary.getModelPathsAtRoot(APIEM.extensionsPath.toString).length > 10)
  }
  test("findModelsBySubstring returns a single model if it matches the name exactly") {
    assert(ModelsLibrary.findModelsBySubstring("ants").size == 1)
  }
  test("findModelsBySubstring returns a list of library models starting with that string if no model matches exactly") {
    assert(ModelsLibrary.findModelsBySubstring("gas").size > 10)
  }
  test("findModelsBySubstring returns a list of library models starting with that string") {
    assert(ModelsLibrary.findModelsBySubstring("ant").size > 10)
  }
  test("findModelsBySubstring excludes directories") {
    assert(! ModelsLibrary.findModelsBySubstring("sample").contains("Sample Models"))
  }
  test("findModelsBySubstring returns a list of library models with that string anywhere") {
    assert(ModelsLibrary.findModelsBySubstring("oper").size > 1)
  }
  test("getModelPath returns the full path to the model whose name matches exactly") {
    assert(ModelsLibrary.getModelPath("ants.nlogox").get.endsWith(s"Biology${File.separator}Ants.nlogox"))
  }
  test("getModelPath returns null when no model name matches exactly") {
    assert(ModelsLibrary.getModelPath("pong.nlogox") == None)
  }
  if (! Version.is3D) {
    test("getModelPaths returns a list of all model paths in the models library") {
      assert(ModelsLibrary.getModelPaths(true).length > 450)
    }
    test("after indexing, rootNode contains a tree describing all the models") {
      ModelsLibrary.getModelPaths(true)
      assert(ModelsLibrary.rootNode.nonEmpty)
      assert(ModelsLibrary.rootNode.get.depthFirstIterable.size > 500)
    }
  }
}
