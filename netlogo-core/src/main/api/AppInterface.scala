// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.awt.Frame

import org.nlogo.core.CompilerUtilitiesInterface

trait AppInterface {
  def frame: Frame & ZoomRoot
  def workspace: Workspace
  def utilities: CompilerUtilitiesInterface
  def updateManager: UpdateManagerInterface
  def tabManager: TabsInterface
  def addSyncFunction(f: () => Unit): Long
  def removeSyncFunction(id: Long): Unit
}
