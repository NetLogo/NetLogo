// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.funsuite.AnyFunSuite
import ModelsLibrary._

class TestLibrary extends AnyFunSuite {
  test("there are no empty leaf folders") {
    val exclusions = Set("project", "src", "target")
      .map("/models/" + _ + "/")
    val emptyLeafFolders = scanForModelsAtRoot("models", false)
      .getOrElse(fail())
      .breadthFirstIterable
      .collect { case node: ModelsLibrary.Node => node }
      .filterNot { node => exclusions.exists(node.path.contains) }
      .filter {
        case ModelsLibrary.Tree(_, _, children) => children.isEmpty
        case _ => false
      }
    assert(emptyLeafFolders.isEmpty, emptyLeafFolders.map(_.path).toList)
  }
}
