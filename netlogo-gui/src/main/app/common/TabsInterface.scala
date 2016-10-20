// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.Component

trait TabsInterface {
  val interfaceTab: Component
  val infoTab: Component
  val codeTab: Component

  def lineNumbersVisible: Boolean
  def lineNumbersVisible_=(b: Boolean): Unit

  def newTemporaryFile(): Unit
  def openTemporaryFile(filename: String, fileMustExist: Boolean): Unit
  def saveTemporaryFile(filename: String): Unit
  def closeTemporaryFile(filename: String): Unit
}
