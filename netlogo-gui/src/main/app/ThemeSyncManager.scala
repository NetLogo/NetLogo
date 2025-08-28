// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.app

import org.nlogo.theme.ThemeSync

class ThemeSyncManager {

  private var syncComponentCounter = 0L
  private var syncComponents       = Set[ThemeSync]()
  private var syncFunctionMap      = Map[Long, () => Unit]()

  def syncAll(): Unit = {
    syncComponents.foreach(_.syncTheme())
    syncFunctionMap.values.foreach(_())
  }

  def addSyncComponent(ts: ThemeSync): Unit = {
    syncComponents = syncComponents + ts
  }

  def addSyncFunction(f: () => Unit): Long = {
    val id = syncComponentCounter
    syncComponentCounter += 1
    syncFunctionMap = syncFunctionMap + (id -> f)
    id
  }

  def removeSyncComponent(ts: ThemeSync): Unit = {
    syncComponents = syncComponents - ts
  }

  def removeSyncFunction(id: Long): Unit = {
    syncFunctionMap = syncFunctionMap - id
  }

}
