// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api

trait ReviewTabInterface extends java.awt.Component {
  def startRecording(): Unit
  def currentRun: Option[api.ModelRun]
  def loadedRuns: Seq[api.ModelRun]
  def loadRun(inputStream: java.io.InputStream): Unit
}
