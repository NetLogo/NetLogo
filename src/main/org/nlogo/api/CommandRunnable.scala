package org.nlogo.api

trait CommandRunnable {
  @throws(classOf[LogoException])
  def run()
}
