package org.nlogo.api

trait ImportErrorHandler {
  def showError(title: String, message: String, defaultAction: String)
}
