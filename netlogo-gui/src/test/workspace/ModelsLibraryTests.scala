// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.File

import org.nlogo.api.TwoDVersion

import org.scalatest.FunSuite

class ModelsLibraryTests extends FunSuite {
  test("needsModelScan returns true when a scan needs to be performed") {
    ModelsLibrary.rootNodes = Map()
    assert(ModelsLibrary.needsModelScan(TwoDVersion))
    ModelsLibrary.getModelPaths(TwoDVersion)
    assert(! ModelsLibrary.needsModelScan(TwoDVersion))
  }
  test("getModelPathsAtRoot returns a list of all model paths in the given directory") {
    assert(ModelsLibrary.getModelPathsAtRoot(ExtensionManager.extensionPath, TwoDVersion).length > 10)
  }
  test("findModelsBySubstring returns a single model if it matches the name exactly") {
    assert(ModelsLibrary.findModelsBySubstring("ants", TwoDVersion).size == 1)
  }
  test("findModelsBySubstring returns a list of library models starting with that string if no model matches exactly") {
    assert(ModelsLibrary.findModelsBySubstring("gas", TwoDVersion).size > 10)
  }
  test("findModelsBySubstring returns a list of library models starting with that string") {
    assert(ModelsLibrary.findModelsBySubstring("ant", TwoDVersion).size > 10)
  }
  test("findModelsBySubstring excludes directories") {
    assert(! ModelsLibrary.findModelsBySubstring("sample", TwoDVersion).contains("Sample Models"))
  }
  test("findModelsBySubstring returns a list of library models with that string anywhere") {
    assert(ModelsLibrary.findModelsBySubstring("oper", TwoDVersion).size > 1)
  }
  test("getModelPath returns the full path to the model whose name matches exactly") {
    assert(ModelsLibrary.getModelPath("ants.nlogo", TwoDVersion).get.endsWith(s"Biology${File.separator}Ants.nlogo"))
  }
  test("getModelPath returns null when no model name matches exactly") {
    assert(ModelsLibrary.getModelPath("pong.nlogo", TwoDVersion) == None)
  }
  test("getModelPaths returns a list of all model paths in the models library") {
    assert(ModelsLibrary.getModelPaths(TwoDVersion, true).length > 450)
  }
  test("after indexing, rootNode contains a tree describing all the models") {
    ModelsLibrary.getModelPaths(TwoDVersion, true)
    assert(ModelsLibrary.rootNodes.get(TwoDVersion).nonEmpty)
    assert(ModelsLibrary.rootNodes(TwoDVersion).depthFirstIterable.size > 500)
  }
}
