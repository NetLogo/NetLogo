// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// it's very tempting to get rid of ride entirely but for the interface
// "riding turtle 0" I supposed we still need it. ev 4/29/05

// In the old days this was an integer instead of an enumeration, so in exported worlds it's still
// represented as an integer, hence the code here to convert back and forth to an integer at import
// or export time. - ST 3/18/08

// "class" not "trait" otherwise we won't get a static forwarder for Perspective.load() - ST 7/27/11

abstract sealed class Perspective(val export: Int)

object Perspective {
  case object Observe extends Perspective(0)
  case object Ride    extends Perspective(1)
  case object Follow  extends Perspective(2)
  case object Watch   extends Perspective(3)
  private val perspectives = List(Observe, Ride, Follow, Watch)
  def load(n: Int) = perspectives(n)
}

object PerspectiveJ {
  import Perspective._
  // (I don't think) Java can access the inner objects without reflection, so we provide these
  // convenience vals for use from the handful of Java clients we still have. - ST 7/11/11, 7/27/11
  val OBSERVE = Observe
  val RIDE = Ride
  val FOLLOW = Follow
  val WATCH = Watch
}
