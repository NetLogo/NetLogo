// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client
trait ErrorHandler {
  def handleLoginFailure(errorMessage: String): Unit
  def handleDisconnect(activityName: String, connected: Boolean, reason: String): Unit
  def completeLogin(): Unit
}
