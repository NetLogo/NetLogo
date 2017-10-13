// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo


package org.nlogo.util

import org.scalactic.source.Position
import org.scalatest.{ FunSuite, Tag }

class TaggedFunSuite(tags: Tag*) extends FunSuite {
  override def test(testName: String, testTags: Tag*)(testFun: => Any)(implicit pos: Position): Unit = {
    super.test(testName, (testTags ++ tags): _*)(testFun)
  }
}
