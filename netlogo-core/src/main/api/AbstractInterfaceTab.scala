// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.awt.Component
import java.io.PrintWriter

trait AbstractInterfaceTab extends Component {
  def enable2D(enabled: Boolean): Unit
  def exportOutputArea(writer: PrintWriter): Unit
}
