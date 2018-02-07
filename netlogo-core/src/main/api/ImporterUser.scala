// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.util.{ List => JList }

trait ImporterUser extends WorldResizer {
  def setOutputAreaContents(text: String)
  def currentPlot(plot: String)  // for importing plots
  @deprecated("Use findPlot and check isEmpty / nonEmpty instead of null-checking getPlot", "6.1.0")
  def getPlot(plot: String): PlotInterface
  def findPlot(plot: String): Option[PlotInterface]
  def isExtensionName(name: String): Boolean  // for importing extensions
  @throws(classOf[ExtensionException])
  def importExtensionData(name: String, data: JList[Array[String]], handler: ImportErrorHandler)
}
