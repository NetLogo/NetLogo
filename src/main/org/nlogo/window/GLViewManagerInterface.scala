// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait GLViewManagerInterface extends LocalViewInterface {
  @throws[JOGLLoadingException]
  def open(): Unit
  def isFullscreen: Boolean
  var displayOn: Boolean
  var antiAliasingOn: Boolean
  var wireframeOn: Boolean
  def editFinished(): Unit
  def close(): Unit
  @throws[java.io.IOException]
  @throws[org.nlogo.shape.InvalidShapeDescriptionException]
  def addCustomShapes(filename: String): Unit
}
