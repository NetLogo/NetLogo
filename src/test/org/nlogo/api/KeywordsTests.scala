// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class KeywordsTests extends FunSuite {
  import Keywords.isKeyword
  test("OneKeyword1") { assert(isKeyword("to")) }
  test("OneKeyword2") { assert(isKeyword("TO")) }
  test("BreedOwn") { assert(isKeyword("mice-own")) }
}
