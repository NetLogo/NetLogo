// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait InterfaceGlobalWidget {
  def name: String
  def classDisplayName: String
  def valueObject(): AnyRef
  def valueObject(value: AnyRef): Unit
  def updateConstraints(): Unit
}
