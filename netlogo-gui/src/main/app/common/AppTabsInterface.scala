// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.{ Component, Font }

import org.nlogo.api.TabsInterface

object AppTabsInterface {
  /**
    * Right indicates a real path, and Left means a name for a never-saved file
    * (analogous to ModelType.Normal & ModelType.New, respectively)
    */
  type Filename = Either[String, String]
}

trait AppTabsInterface extends TabsInterface {
  val infoTab: Component

  def lineNumbersVisible: Boolean
  def lineNumbersVisible_=(b: Boolean): Unit

  def smartTabbingEnabled: Boolean
  def smartTabbingEnabled_=(b: Boolean): Unit

  def getCompleteOnType: Boolean
  def setCompleteOnType(enabled: Boolean): Unit

  def setIncludedFilesShown(visible: Boolean): Unit

  def remoteCommandsEnabled: Boolean
  def remoteCommandsEnabled_=(b: Boolean): Unit

  def watchingFiles: Boolean
  def watchingFiles_=(b: Boolean): Unit

  def stopWatcherThread(): Unit

  def externalFileSaved(path: String): Unit
  def closeExternalFile(filename: AppTabsInterface.Filename): Unit

  def setJumpOnClick(value: Boolean): Unit

  def setCodeFont(font: Option[Font]): Unit
}
