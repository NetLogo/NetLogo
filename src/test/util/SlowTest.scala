// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

// marker trait.  FastMediumSlow.scala in our sbt build looks for the
// presence of this to classify tests, so we can run just the fast ones
// when we're in a hurry.

trait SlowTest extends org.scalatest.Suite
