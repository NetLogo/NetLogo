// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait LinkRoot {
  protected val linkComponents = new collection.mutable.ListBuffer[AnyRef]()
  def addLinkComponent(c: AnyRef): Unit = { linkComponents += (c) }
  def removeLinkComponent(c: AnyRef): Unit = { linkComponents -= (c) }
  def getLinkChildren: Array[AnyRef] = linkComponents.toArray
}
