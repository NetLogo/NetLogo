// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.EventQueue.isDispatchThread

object EventQueue {

  /** At the moment this one is useless, but historically we sometimes had extra stuff attached here,
    * and we might want to add some again in the future. */
  def invokeLater(r: Runnable) {
    java.awt.EventQueue.invokeLater(r)
  }

  /** Isn't declared as throwing InvocationTargetException, so this is slightly more convenient
    * to call from Java than the java.awt.EventQueue version is. */
  @throws(classOf[InterruptedException])
  def invokeAndWait(r: Runnable) {
    java.awt.EventQueue.invokeAndWait(r)
  }

  def mustBeEventDispatchThread() {
    require(isDispatchThread, "not event thread: " + Thread.currentThread)
  }

  def cantBeEventDispatchThread() {
    require(!isDispatchThread)
  }

}
