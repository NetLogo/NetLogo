// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import scala.collection.mutable.ArrayBuffer
import org.nlogo.api.{ Shape, ViewInterface }

class ViewManager {
  private val views = new ArrayBuffer[ViewInterface]

  // note that primary views *must* be local views aka not hubnet
  def getPrimary = views(0).asInstanceOf[LocalViewInterface]
  def setPrimary(view: LocalViewInterface) = {
    views -= view
    view +=: views
  }
  def setSecondary(view: ViewInterface) = {
    views -= view
    views.insert(1, view)
  }

  def add(v: ViewInterface) = views += v
  def remove(v: ViewInterface) = views -= v

  def paintImmediately(force: Boolean) =
    for(view <- views)
      view.paintImmediately(force)

  def framesSkipped() =
    for(v <- views)
      v.framesSkipped()

  def incrementalUpdateFromEventThread() =
    for(v <- views)
      if(!v.isDead && v.viewIsVisible)
        v.incrementalUpdateFromEventThread()
  private val updateRunnable = new Runnable {
      def run() = {
        incrementalUpdateFromEventThread()
      }
    }
  def incrementalUpdateFromJobThread() =
    try {
      org.nlogo.awt.EventQueue.invokeAndWait(updateRunnable)
    } catch {
      case _: InterruptedException => getPrimary.repaint()
    }
  
  def applyNewFontSize(newFontSize: Int) =
    for(v <- views)
      v.applyNewFontSize(newFontSize, 0)

  def shapeChanged(shape: Shape) =
    for(view <- views)
      view.shapeChanged(shape)
  
  def mouseDown: Boolean = {
    for(view <- views)
      if (view.mouseDown)
        return true
    false
  }
  def mouseInside: Boolean = {
    for(view <- views)
      if (view.mouseInside)
        return true
    false
  }
  def mouseXCor: Double = {
    for (view <- views)
      if (view.mouseInside)
        return view.mouseXCor
    getPrimary.mouseXCor
  }
  def mouseYCor: Double = {
    for(view <- views)
      if(view.mouseInside)
        return view.mouseYCor
    getPrimary.mouseYCor
  }
  def resetMouseCors() =
    for(view <- views)
      view.resetMouseCors()
}
