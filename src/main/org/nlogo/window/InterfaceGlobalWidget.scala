// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait InterfaceGlobalWidget {
  def name: String
  def classDisplayName: String
  var valueObject: AnyRef
  def updateConstraints(): Unit
}
