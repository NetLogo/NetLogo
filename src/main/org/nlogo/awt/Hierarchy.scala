// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.{ Component, Frame, Window }

object Hierarchy {

  /**
   * Returns the frame containing a component.
   */
  def getFrame(comp: Component): Frame =
    getTopAncestor(comp) match {
      case top: Frame => top
      case top: Window if top.getParent != null =>
        getFrame(top.getParent)
      case _ => null
    }

  /**
   * Returns the window containing a component.
   */
  def getWindow(comp: Component): Window =
    getTopAncestor(comp) match {
      case top: Window => top
      case _ => null
    }

  def getTopAncestor(comp: Component): Component =
    Iterator.iterate(comp)(_.getParent)
      .takeWhile(_ != null)
      .collectFirst{case w: Window => w}
      .getOrElse(null)

  def hasAncestorOfClass(comp: Component, clazz: Class[_]) =
    findAncestorOfClass(comp, clazz).isDefined

  def findAncestorOfClass(comp: Component, clazz: Class[_]): Option[Component] =
    Iterator.iterate(comp)(_.getParent)
      .takeWhile(_ != null)
      .find(clazz.isInstance)

}
