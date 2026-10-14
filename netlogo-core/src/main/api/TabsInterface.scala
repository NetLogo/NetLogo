// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.awt.Component

trait TabsInterface {
  val interfaceTab: AbstractInterfaceTab
  val mainCodeTab: Component

  def smartTabbingEnabled: Boolean
  def smartTabbingEnabled_=(enabled: Boolean): Unit
  def newExternalFile(): Unit
  def openExternalFile(filename: String, focus: Boolean = true): Unit
  def getExternalFileTabs: Seq[Component]
  def addTab(tab: Component, name: String): Unit
  def replaceTab(oldTab: Component, newTab: Component): Unit
  def setSelectedTab(tab: Component): Unit
  def removeTab(tab: Component): Unit
  def switchWindow(separate: Boolean, preserveSelected: Boolean): Unit
  def showCodeTabPreferences(): Unit
}
