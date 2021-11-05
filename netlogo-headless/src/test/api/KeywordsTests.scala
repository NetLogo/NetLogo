// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Keywords
import org.scalatest.funsuite.AnyFunSuite

class KeywordsTests extends AnyFunSuite {
  import Keywords.isKeyword
  test("OneKeyword1") { assert(isKeyword("to")) }
  test("OneKeyword2") { assert(isKeyword("TO")) }
  test("BreedOwn") { assert(isKeyword("mice-own")) }
}
