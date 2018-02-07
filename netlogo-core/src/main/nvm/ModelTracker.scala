// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

trait ModelTracker {
  @throws(classOf[java.net.MalformedURLException])
  def attachModelDir(filePath: String): String
  def modelFileName: String
  def guessExportName(defaultName: String): String
}
