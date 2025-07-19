// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.util.{ List => JList }

trait ImporterUser extends WorldResizer {
  def setOutputAreaContents(text: String): Unit
  def currentPlot(plot: String): Unit  // for importing plots
  def maybeGetPlot(plot: String): Option[PlotInterface]
  def isExtensionName(name: String): Boolean  // for importing extensions
  @throws(classOf[ExtensionException])
  def importExtensionData(name: String, data: JList[Array[String]], handler: ImportErrorHandler): Unit
}
