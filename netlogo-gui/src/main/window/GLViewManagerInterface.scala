// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.io.IOException
import java.awt.event.KeyListener
import java.beans.PropertyChangeListener

import org.nlogo.shape.InvalidShapeDescriptionException

trait GLViewManagerInterface extends LocalViewInterface with PropertyChangeListener {
  @throws(classOf[JOGLLoadingException])
  def open(for3D: Boolean)
  def isFullscreen: Boolean
  def antiAliasingOn(on: Boolean): Unit
  def antiAliasingOn: Boolean
  def wireframeOn_=(on: Boolean): Unit
  def wireframeOn: Boolean
  def editFinished(): Unit
  def close(): Unit
  @throws(classOf[IOException])
  @throws(classOf[InvalidShapeDescriptionException])
  def addCustomShapes(filename: String): Unit
  def addKeyListener(listener: KeyListener): Unit
}
