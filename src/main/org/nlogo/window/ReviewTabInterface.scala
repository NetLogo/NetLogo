// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api

trait ReviewTabInterface extends java.awt.Component {
  def recordingEnabled: Boolean
  def recordingEnabled_=(enabled: Boolean): Unit
  def currentRun: Option[api.ModelRun]
  def loadedRuns: Seq[api.ModelRun]
  def loadRun(inputStream: java.io.InputStream): Unit
  def addNote(note: String): Unit
}
