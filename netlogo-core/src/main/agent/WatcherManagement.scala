// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import java.util.{ HashMap => JHashMap, List => JList, Map => JMap }
import java.util.concurrent.CopyOnWriteArrayList

import World.VariableWatcher

trait WatcherManagement {
  // Variable watching *must* be done on variable name, not number. Numbers
  // can change in the middle of runs if, for instance, the user rearranges
  // the order of declarations in turtles-own and then keeps running.
  //
  // I didn't use SimpleChangeEvent here since I wanted the observers to know
  // what the change actually was.
  // -- BCH (4/1/2014)
  private var variableWatchers: JMap[String, JList[VariableWatcher]] =
    new JHashMap[String, JList[VariableWatcher]]()
  // this boolean is micro-optimization to make notifying watchers as fast as possible
  private var hasWatchers: Boolean = false

  /**
   * A watcher to be notified every time the given variable changes for any agent.
   * @param variableName The variable name to watch as an upper case string; e.g. "XCOR"
   * @param watcher The watcher to notify when the variable changes
   */
  def addWatcher(variableName: String, watcher: VariableWatcher): Unit = {
    if (! variableWatchers.containsKey(variableName)) {
      variableWatchers.put(variableName, new CopyOnWriteArrayList[VariableWatcher]())
    }
    variableWatchers.get(variableName).add(watcher)
    hasWatchers = true
  }

  /**
   * Deletes a variable watcher.
   * @param variableName The watched variable name as an upper case string; e.g. "XCOR"
   * @param watcher The watcher to delete
   */
  def deleteWatcher(variableName: String, watcher: VariableWatcher): Unit = {
    if (variableWatchers.containsKey(variableName)) {
      val watchers = variableWatchers.get(variableName)
      watchers.remove(watcher)
      if (watchers.isEmpty) {
        variableWatchers.remove(variableName)
      }
      if (variableWatchers.isEmpty) {
        hasWatchers = false
      }
    }
  }

  def notifyWatchers(agent: Agent, vn: Int, value: Object): Unit = {
    // This needs to be crazy fast if there are no watchers. Thus, hasWatchers. -- BCH (3/31/2014)
    if (hasWatchers) {
      val variableName = agent.variableName(vn)
      val watchers = variableWatchers.get(variableName)
      if (watchers != null) {
        val iter = watchers.iterator
        while (iter.hasNext) {
          iter.next().asInstanceOf[VariableWatcher].update(agent, variableName, value)
        }
      }
    }
  }
}
