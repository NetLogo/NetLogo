// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import org.scalatest.Tag

// indicates that test can be run once in 2D or 3D
object ArityIndependent extends Tag("org.nlogo.util.ArityIndependent")

// instead of running the tests in 2D/3D mode, once we can run tests in a unified
// mode, it will sometimes be useful to differentiate between tests which test 2D
// and tests which test 3D.
object ThreeDTag extends Tag("org.nlogo.util.ThreeDTag")
object TwoDTag extends Tag("org.nlogo.util.TwoDTag")
