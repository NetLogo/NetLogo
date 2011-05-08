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
  def ignoring(cs: Class[_]*)(body: =>Unit) {
    util.control.Exception.catching(cs: _*).withApply(ignore) { body }
  }
  def handling(cs: Class[_]*)(body: =>Unit) {
    util.control.Exception.catching(cs: _*).withApply(handle) { body }
  }
  def warning(cs: Class[_]*)(body: =>Unit) {
    util.control.Exception.catching(cs: _*).withApply(warn) { body }
  }
}
