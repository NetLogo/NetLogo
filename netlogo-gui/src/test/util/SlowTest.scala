// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import org.scalatest.{ FunSuiteLike, Tag => STTag }

trait SlowTest

object SlowTest {
  object Tag extends STTag("org.nlogo.util.SlowTestTag")
}
