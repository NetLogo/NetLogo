// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.FunSuite
import ModelsLibrary._

class TestLibrary extends FunSuite {
  test("every model has a unique name") {
    val names = new collection.mutable.HashSet[String]
    for(path <- getModelPaths) {
      val name = new java.io.File(path).getName
      assert(!names.contains(name.toUpperCase), name)
      names += name.toUpperCase
    }
  }
}
