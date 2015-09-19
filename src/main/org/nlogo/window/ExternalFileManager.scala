// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait ExternalFileManager {
  def getSource(filename: String): String
}
