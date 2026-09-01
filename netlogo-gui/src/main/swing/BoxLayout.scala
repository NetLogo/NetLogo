// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Component
import javax.swing.{ Box, BoxLayout, JPanel }

sealed abstract trait BoxAlign(val extraComponents: Int)

object BoxAlign {
  case object None extends BoxAlign(0)
  case object Start extends BoxAlign(1)
  case object Center extends BoxAlign(2)
  case object End extends BoxAlign(1)
}

abstract class AbstractBoxLayout(axis: Int, components: Seq[Component], gap: Int, align: BoxAlign)
  extends JPanel with Transparent {

  setLayout(new BoxLayout(this, axis))

  if (align != BoxAlign.End && align != BoxAlign.None)
    add(createGlue)

  if (align != BoxAlign.Start && align != BoxAlign.None)
    add(createGlue)

  components.foreach(add)

  protected def createStrut: Component
  protected def createGlue: Component

  override def addImpl(component: Component, constraints: AnyRef, index: Int): Unit = {
    val adjustedIndex: Int = {
      if (index == -1) {
        if (align == BoxAlign.None) {
          getComponentCount
        } else if (align != BoxAlign.End) {
          getComponentCount - 1
        } else {
          index
        }
      } else {
        index
      }
    }

    val componentCount: Int = getComponentCount - align.extraComponents

    super.addImpl(component, constraints, adjustedIndex)

    if (gap > 0 && componentCount > 0)
      super.addImpl(createStrut, constraints, adjustedIndex)
  }

  override def removeAll(): Unit = {
    align match {
      case BoxAlign.None =>
        super.removeAll()

      case BoxAlign.Start =>
        while (getComponentCount > 1)
          remove(0)

      case BoxAlign.Center =>
        while (getComponentCount > 2)
          remove(1)

      case BoxAlign.End =>
        while (getComponentCount > 1)
          remove(1)
    }
  }
}

class BoxRow(components: Seq[Component] = Seq(), gap: Int = 0, align: BoxAlign = BoxAlign.None)
  extends AbstractBoxLayout(BoxLayout.X_AXIS, components, gap, align) {

  def this(component: Component, align: BoxAlign) = this(Seq(component), 0, align)
  def this(gap: Int, align: BoxAlign) = this(Seq(), gap, align)
  def this(gap: Int) = this(Seq(), gap, BoxAlign.None)
  def this(align: BoxAlign) = this(Seq(), 0, align)

  override def createStrut: Component =
    new HorizontalStrut(gap)

  override def createGlue: Component =
    Box.createHorizontalGlue
}

class BoxColumn(components: Seq[Component] = Seq(), gap: Int = 0, align: BoxAlign = BoxAlign.None)
  extends AbstractBoxLayout(BoxLayout.Y_AXIS, components, gap, align) {

  def this(component: Component, align: BoxAlign) = this(Seq(component), 0, align)
  def this(gap: Int, align: BoxAlign) = this(Seq(), gap, align)
  def this(gap: Int) = this(Seq(), gap, BoxAlign.None)
  def this(align: BoxAlign) = this(Seq(), 0, align)

  override def createStrut: Component =
    new VerticalStrut(gap)

  override def createGlue: Component =
    Box.createVerticalGlue
}
