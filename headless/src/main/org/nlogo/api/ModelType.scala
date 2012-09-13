// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * how a model was loaded. NEW is type for new models, NORMAL is a
 * model opened normally (e.g. via Open on the File menu),
 * LIBRARY a models library model.
 */

abstract sealed trait ModelType

object ModelType {
  case object New extends ModelType
  case object Normal extends ModelType
  case object Library extends ModelType
}

object ModelTypeJ {
  import ModelType._
  // (I don't think) Java can access the inner objects without reflection, so we provide these
  // convenience vals for use from the handful of Java clients we still have. - ST 7/11/11
  val NEW = New
  val NORMAL = Normal
  val LIBRARY = Library
}
