// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.{ Component, Container, Dimension, LayoutManager }

class ColumnLayout(vGap: Int, hAlign: Float, vAlign: Float) extends LayoutManager {

  def this(vGap: Int) =
    this(vGap, Component.LEFT_ALIGNMENT, Component.TOP_ALIGNMENT)

  // not implemented. it's not clear to me why we're getting away with that.
  // if we "throw new UnsupportedOperationException" it definitely blows up. - ST 4/21/11
  override def addLayoutComponent(name: String, comp: Component) { }
  override def removeLayoutComponent(comp: Component) { }

  override def layoutContainer(target: Container) {
    val size = target.getSize
    val insets = target.getInsets
    var x = 0
    var y = insets.top
    if(vAlign == Component.CENTER_ALIGNMENT)
      y += (size.height - minimumLayoutSize(target).height) / 2
    else if(vAlign == Component.BOTTOM_ALIGNMENT)
      y += size.height - minimumLayoutSize(target).height
    for(i <- 0 until target.getComponentCount) {
      if(i > 0)
        y += vGap
      val comp = target.getComponent(i)
      val pref = comp.getPreferredSize
      if(hAlign == Component.RIGHT_ALIGNMENT)
        x = size.width - insets.left - insets.right - pref.width
      else if(hAlign == Component.CENTER_ALIGNMENT)
        x = (size.width - insets.left - insets.right - pref.width) / 2
      else // left
        x = 0
      comp.setBounds(x + insets.left, y, pref.width, pref.height)
      y += pref.height
    }
  }

  override def minimumLayoutSize(target: Container) = {
    val insets = target.getInsets
    var x = 0
    var y = 0
    for(i <- 0 until target.getComponentCount) {
      if(i > 0)
        y += vGap
      val comp = target.getComponent(i)
      val pref = comp.getPreferredSize
      y += pref.height
      x = x max pref.width
    }
    new Dimension(insets.left + x + insets.right,
                  insets.top + y + insets.bottom)
  }

  override def preferredLayoutSize(target: Container) =
    minimumLayoutSize(target)

}
