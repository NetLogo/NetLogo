// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

sealed abstract class FileMode(val modeFlag: Int)

object FileMode {
  case object None   extends FileMode(0)
  case object Read   extends FileMode(1)
  case object Write  extends FileMode(2)
  case object Append extends FileMode(3)
}

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
