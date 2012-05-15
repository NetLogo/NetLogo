// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.{ Component, Container }

class RowLayout(hGap: Int, hAlign: Float, vAlign: Float) extends java.awt.LayoutManager {

  // not implemented. it's not clear to me why we're getting away with that.
  // if we "throw new UnsupportedOperationException" it definitely blows up. - ST 4/21/11
  override def addLayoutComponent(name: String, comp: Component) { }
  override def removeLayoutComponent(comp: Component) { }

  override def layoutContainer(target: java.awt.Container) = {
    val size = target.getSize
    val insets = target.getInsets
    var x = insets.left
    if(hAlign == java.awt.Component.RIGHT_ALIGNMENT)
      x += size.width - minimumLayoutSize(target).width
    else if(hAlign == java.awt.Component.CENTER_ALIGNMENT)
      x += (size.width - minimumLayoutSize(target).width) / 2
    var y = 0
    for(i <- 0 until target.getComponentCount) {
      if(i > 0)
        x += hGap
      val comp = target.getComponent(i)
      val pref = comp.getPreferredSize
      if(vAlign == java.awt.Component.BOTTOM_ALIGNMENT)
        y = size.height - insets.top - insets.bottom - pref.height
      else if(vAlign == java.awt.Component.CENTER_ALIGNMENT)
        y = (size.height - insets.top - insets.bottom - pref.height) / 2
      else // top
        y = 0
      comp.setBounds(x, y + insets.top, pref.width, pref.height)
      x += pref.width
    }
  }

  override def minimumLayoutSize(target: java.awt.Container) = {
    val insets = target.getInsets
    var x = 0
    var y = 0
    for(i <- 0 until target.getComponentCount) {
      if(i > 0)
        x += hGap
      val comp = target.getComponent(i)
      val pref = comp.getPreferredSize
      x += pref.width
      y = y max pref.height
    }
    new java.awt.Dimension(insets.left + x + insets.right,
                           insets.top + y + insets.bottom)
  }

  override def preferredLayoutSize(target: Container) =
    minimumLayoutSize(target)

}
