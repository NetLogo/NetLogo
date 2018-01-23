// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

// This class is used by ClientPanel to notify ClientApp that a disconnect has happened.
// With such a simple run method, this doesn't really seem necessary, but the problem is that
// creating a closure or an inner class inside ClientPanel retains a reference *to* ClientPanel
// (`$outer`). This screws up delivery of future messages through some means
// that I'm not 100% confident about but seems to involve having the first listener thread
// (the one that would be retained by a closure created inside it) stick around
// until a second login is processed.
class RunDisconnection(errorHandler: ErrorHandler, activityName: String, connected: Boolean, reason: String) extends Runnable {
  override def run(): Unit = {
    errorHandler.handleDisconnect(activityName, connected, reason)
  }
}
