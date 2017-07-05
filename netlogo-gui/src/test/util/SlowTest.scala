// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import org.scalatest.{ Tag => STTag }

trait SlowTest

object SlowTest {
  object Tag extends STTag("org.nlogo.util.SlowTestTag")
  object ExtensionTag extends STTag("org.nlogo.util.ExtensionTestTag")
}
