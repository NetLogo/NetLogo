// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.Component

object TabsInterface {
  /**
    * Right indicates a real path, and Left means a name for a never-saved file
    * (analogous to ModelType.Normal & ModelType.New, respectively)
    */
  type Filename = Either[String, String]
}

trait TabsInterface {
  import TabsInterface.Filename

  val interfaceTab: Component
  val infoTab: Component
  val mainCodeTab: Component

  def lineNumbersVisible: Boolean
  def lineNumbersVisible_=(b: Boolean): Unit

  def newExternalFile(): Unit
  def openExternalFile(filename: String): Unit
  def closeExternalFile(filename: Filename): Unit
}
