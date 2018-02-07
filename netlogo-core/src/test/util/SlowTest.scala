// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import org.scalatest.Tag

trait SlowTest

object SlowTestTag extends Tag("org.nlogo.util.SlowTestTag")

object SlowTest {
  val Tag = SlowTestTag
  object ExtensionTag extends Tag("org.nlogo.util.ExtensionTestTag")
}
