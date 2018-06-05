// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.util.concurrent.ExecutionException

abstract class SwingWorker[T,U] extends javax.swing.SwingWorker[T,U] {
  final override def done() = {
    try get() catch { // propagate any exception produced on `doInBackground`
      case ex: ExecutionException => throw ex.getCause
    }
    onComplete()
  }

  def onComplete(): Unit
}
