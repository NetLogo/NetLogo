package org.nlogo.api

trait ModelRun {
  self: ModelRun =>
  @throws(classOf[java.io.IOException])
  def save(outputStream: java.io.OutputStream): Unit
}
