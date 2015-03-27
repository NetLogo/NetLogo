// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.FunSuite
import ModelsLibrary._
import scala.collection.JavaConverters._

class TestLibrary extends FunSuite {
  test("every model has a unique name") {
    val names = new collection.mutable.HashSet[String]
    for(path <- getModelPaths) {
      val name = new java.io.File(path).getName
      assert(!names.contains(name.toUpperCase), name)
      names += name.toUpperCase
    }
  }
  test("there are no empty leaf folders") {
    val emptyLeafFolders = scanForModelsAtRoot("models", false)
      .breadthFirstEnumeration.asScala
      .collect { case node: ModelsLibrary.Node => node }
      .filter { node => node.isFolder && !node.children.hasMoreElements }
    assert(emptyLeafFolders.isEmpty, emptyLeafFolders.map(_.getPath.mkString("/")).toList)
  }
}
