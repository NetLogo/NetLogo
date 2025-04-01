// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait Editable {
  def helpLink: Option[String]
  def liveUpdate: Boolean = true
  def classDisplayName: String
  def editFinished(): Boolean

  def editPanel: EditPanel

  def anyErrors: Boolean
  def error(key: Object): Option[Exception]
  def error(key: Object, e: Exception): Unit
  def errorString: Option[String] = None

  // it's kind of lame to put this here but it'll require a bunch of changes all over the properties
  // package otherwise it seems not worth the effort ev 6/10/08
  def sourceOffset: Int
}

class DummyEditable extends Editable with DummyErrorHandler {
  def helpLink = None
  override def classDisplayName = ""
  override def editFinished() = true
  override def editPanel: EditPanel = null
  override def sourceOffset = 0
}

trait DummyErrorHandler {
  def error(key: Object): Option[Exception] = None
  def error(key: Object, e: Exception): Unit = {}
  def anyErrors = false
}

trait MultiErrorHandler {
  private val errors = scala.collection.mutable.Map[Object, Exception]()
  def anyErrors: Boolean = errors.nonEmpty
  def removeAllErrors(): Unit = { errors.clear() }
  def error(key: Object): Option[Exception] = Option(errors.get(key)).flatten
  def error(key: Object, e: Exception): Unit = { errors(key) = e }
}

trait SingleErrorHandler {
  private var _error: Option[Exception] = None
  def anyErrors: Boolean = _error.isDefined
  def error(key: Object): Option[Exception] = _error
  def error(key: Object, e: Exception): Unit = { _error = Option(e) }
  def error(): Option[Exception] = _error
  def error(e: Exception): Unit = { _error = Option(e) }
}
