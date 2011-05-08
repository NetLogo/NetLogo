package org.nlogo.hubnet.client
trait ErrorHandler {
  def handleLoginFailure(errorMessage: String)
  def handleDisconnect(activityName: String, connected: Boolean, reason: String)
  def completeLogin()
}
