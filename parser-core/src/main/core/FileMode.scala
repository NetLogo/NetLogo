// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object FileMode {
  case object None extends FileMode
  case object Read extends FileMode
  case object Write extends FileMode
  case object Append extends FileMode
}

sealed trait FileMode

/**
 * Java can't (I don't think) access Scala inner objects without reflection, so we provide these
 * convenience vals for use from Java.
 */
object FileModeJ {
  import FileMode._
  val NONE = None
  val READ = Read
  val WRITE = Write
  val APPEND = Append
}
