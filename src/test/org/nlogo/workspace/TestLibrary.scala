// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.FunSuite
import ModelsLibrary._
import scala.collection.JavaConverters._

class TestLibrary extends FunSuite {
  test("there are no empty leaf folders") {
    val exclusions = Set("project", "src", "target")
      .map("/models/" + _ + "/")
    val emptyLeafFolders = scanForModelsAtRoot("models", false)
      .breadthFirstEnumeration.asScala
      .collect { case node: ModelsLibrary.Node => node }
      .filterNot { node => exclusions.exists(node.getFilePath.contains) }
      .filter { node => node.isFolder && !node.children.hasMoreElements }
    assert(emptyLeafFolders.isEmpty, emptyLeafFolders.map(_.getPath.mkString("/")).toList)
  }
}
