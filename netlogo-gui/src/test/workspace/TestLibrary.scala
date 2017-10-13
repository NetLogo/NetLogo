// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.FunSuite

import org.nlogo.api.{ TwoDVersion, ThreeDVersion, Version }
import ModelsLibrary._

class TestLibrary extends FunSuite {
  def testArity(version: Version): Unit = {
    test(s"there are no empty leaf folders in $version") {
      val exclusions = Set("project", "src", "target")
        .map("/models/" + _ + "/")
        val emptyLeafFolders = scanForModelsAtRoot("models", version, false)
          .getOrElse(fail)
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

  testArity(TwoDVersion)

  testArity(ThreeDVersion)
}
