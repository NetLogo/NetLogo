// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Editable {
  def helpLink: Option[String]
  def propertySet: java.util.List[Property]
  def classDisplayName: String
  def editFinished(): Boolean

  def anyErrors: Boolean
  def error(key: Object, e: Exception): Unit
  // could be null
  def error(key: Object): Exception

  // it's kind of lame to put this here but it'll require a bunch of changes all over the properties
  // package otherwise it seems not worth the effort ev 6/10/08
  def sourceOffset: Int
}

trait DummyEditable extends Editable with DummyErrorHandler{
  def helpLink = None
  override def propertySet = java.util.Collections.emptyList[Property]
  override def classDisplayName = ""
  override def editFinished() = true
  override def sourceOffset = 0
}

trait DummyErrorHandler {
  def error(key: Object) = null
  def error(key: Object, e: Exception){}
  def anyErrors = false
}

trait MultiErrorHandler {
  private var errors = scala.collection.mutable.Map[Object, Exception]()
  def anyErrors = !errors.isEmpty
  def removeAllErrors() = errors.clear()
  def error(key: Object): Exception = errors.get(key).orNull
  def error(key: Object, e: Exception) {errors(key) = e}
}

trait SingleErrorHandler {
  private var _error: Option[Exception] = None
  def anyErrors = _error.isDefined
  def error(key: Object): Exception = _error.orNull
  def error(key: Object, e: Exception) { _error = Option(e) }
  def error() = _error.orNull
  def error(e: Exception){ _error = Option(e) }
}
