package org.nlogo.gl.render

private class JOGLException(message: String, val t: Throwable)
extends Exception(message) {
  def this(message: String) = this(message, null)
  val throwImmediately = t != null
}
