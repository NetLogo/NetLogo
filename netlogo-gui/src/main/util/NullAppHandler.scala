// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

// this is a dummy object used in App and hubnet client App as
// a stub alternative to the OS-Specific application handler
// passed in by the mac launcher
class AppHandler {
  def init(): Unit = {}
  def ready(app: AnyRef): Unit = {}
}
