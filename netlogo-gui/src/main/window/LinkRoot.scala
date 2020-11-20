// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait LinkRoot {
  protected val linkComponents = new collection.mutable.ListBuffer[AnyRef]()
  def addLinkComponent(c: AnyRef) { linkComponents += (c) }
  def removeLinkComponent(c: AnyRef) { linkComponents -= (c) }
  def getLinkChildren: Array[AnyRef] = linkComponents.toArray
}
