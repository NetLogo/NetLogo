// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

object Exceptions
{
  trait Handler {
    def handle(t: Throwable)
  }
  val defaultHandler = new Handler() {
    def handle(t: Throwable) {
      t.printStackTrace(System.err)
    }
  }
  private var handler: Handler = defaultHandler
  def setHandler(handler: Handler) {
    assert(handler != null)
    this.handler = handler
  }
  def handle(t: Throwable) {
    handler.handle(t)
  }
  def ignore(t: Throwable) {
    // do nothing, but you could put debugging output here, or a debugger breakpoint - ST 5/14/03
  }
  def warn(t: Throwable) {
    System.err.println("Warning -- Ignoring exception: " + t)
    t.printStackTrace(System.err)
  }

  // "catchingPromiscuously" rather than "catching" because the latter won't let us catch
  // InterruptedException, it wants to rethrow it because ignoring it is considered bad practice.
  // However there are a ton of places in NetLogo where we ignore it and only noticed this issue
  // very late in the game for NetLogo 5.0, so it seems safer not to rock the boat on this
  // at the moment. - ST 8/5/11

  def ignoring(cs: Class[_]*)(body: =>Unit) {
    util.control.Exception.catchingPromiscuously(cs: _*).withApply(ignore) { body }
  }
  def handling(cs: Class[_]*)(body: =>Unit) {
    util.control.Exception.catchingPromiscuously(cs: _*).withApply(handle) { body }
  }
  def warning(cs: Class[_]*)(body: =>Unit) {
    util.control.Exception.catchingPromiscuously(cs: _*).withApply(warn) { body }
  }

}
