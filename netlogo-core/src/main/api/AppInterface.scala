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
  def fileManager: FileManagerInterface
  def monitorManager: MonitorManagerInterface
  def modelCoordinator: ModelCoordinator
  def dirtyMonitor: DirtyMonitorInterface
  def addSyncFunction(f: () => Unit): Long
  def removeSyncFunction(id: Long): Unit
  def magicOpen(name: String): Unit
  def reload(): Unit
  def restart(): Unit
}
