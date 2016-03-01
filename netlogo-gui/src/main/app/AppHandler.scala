// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

trait AppHandler {
  def init(): Unit
  def ready(a: App): Unit
}

object NullAppHandler extends AppHandler {
  def init(): Unit = {}
  def ready(a: App): Unit = {}
}

