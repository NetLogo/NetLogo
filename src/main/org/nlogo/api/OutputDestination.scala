package org.nlogo.api

sealed trait OutputDestination
object OutputDestination {
  case object Normal extends OutputDestination
  case object OutputArea extends OutputDestination
  case object File extends OutputDestination
}

/**
 * Java can't (I don't think) access Scala inner objects without reflection, so we provide these
 * convenience vals for use from Java.
 */
object OutputDestinationJ {
  import OutputDestination._
  val NORMAL = Normal
  val OUTPUT_AREA = OutputArea
  val FILE = File
}
