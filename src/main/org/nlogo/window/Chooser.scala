// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Font, FontMetrics, Graphics }
import java.awt.event.{ ActionEvent, ActionListener,
  MouseAdapter, MouseEvent, MouseWheelEvent, MouseWheelListener }
import javax.swing.{ JComponent, JMenuItem, JPopupMenu }
import org.nlogo.agent.ChooserConstraint
import org.nlogo.api.{ Dump, LogoList, ParserServices }
import org.nlogo.awt.Fonts
import org.nlogo.swing.WrappingPopupMenu

object Chooser {
  // visual parameters
  private val MIN_WIDTH = 92
  private val MIN_PREFERRED_WIDTH = 120
  private val CHOOSER_HEIGHT = 45
  private val MARGIN = 4
  private val PADDING = 14
}

abstract class Chooser(parser: ParserServices) extends SingleErrorWidget with MouseWheelListener {
  import Chooser._

  // The constraint track the list of choices, and ensures the
  // global is always one of them.  We use it to track our current
  // index too (the selected value in the chooser). -- CLB
  val constraint = new ChooserConstraint(LogoList.Empty, 0)

  // sub-elements of Switch
  private val control = new ChooserClickControl

  /// setup and layout

  setOpaque(true)
  setBackground(InterfaceColors.SLIDER_BACKGROUND)
  setLayout(null)
  add(control)
  doLayout()
  Fonts.adjustDefaultFont(this)
  this.addMouseWheelListener(this)

  /// attributes

  private var _name = ""
  def name = _name
  def name_=(newName: String) = {
    _name = newName
    repaint()
  }
  protected def index = constraint.defaultIndex
  protected def index_=(newIndex: Int) = {
    constraint.defaultIndex = newIndex
    updateConstraints()
    repaint()
  }
  protected def choices(acceptedValues: LogoList) = constraint.acceptedValues(acceptedValues)
  def value = constraint.defaultValue
  def getMargin = MARGIN
  override def updateConstraints() = if(name.length > 0)
    new Events.AddChooserConstraintEvent(_name, constraint).raise(this)
  override def doLayout() = ChooserPainter.doLayout(this, control, MARGIN)

  /// size calculations

  override def getMinimumSize = new Dimension(MIN_WIDTH, CHOOSER_HEIGHT)
  override def getMaximumSize = new Dimension(10000, CHOOSER_HEIGHT)
  override def getPreferredSize(font: Font) = {
    var width = MIN_PREFERRED_WIDTH
    val metrics = getFontMetrics(font)
    width = StrictMath.max(width, metrics.stringWidth(_name) + 2 * MARGIN + PADDING)
    width = StrictMath.max(width,
      longestChoiceWidth(metrics) + triangleSize + 5 * MARGIN + PADDING + 2) // extra 2 for triangle shadow
    new Dimension(width, CHOOSER_HEIGHT)
  }
  private def longestChoiceWidth(metrics: FontMetrics) = {
    var result = 0
    constraint.acceptedValues.foreach { x =>
      val width = metrics.stringWidth(Dump.logoObject(x))
      result = StrictMath.max(result, width)
    }
    result
  }

  private def triangleSize = control.getBounds().height / 2 - MARGIN

  /// respond to user actions

  def popup() = {
    val menu = new WrappingPopupMenu
    populate(menu)
    // the 3 aligns us with the inside edge
    menu.show(this, control.getBounds().x + 3, control.getBounds().y + control.getBounds().height)
  }

  def populate(menu: JPopupMenu) =
    if(constraint.acceptedValues.isEmpty) {
      val nullItem = new JMenuItem("<No Choices>")
      nullItem.setEnabled(false)
      menu.add(nullItem)
    } else {
      constraint.acceptedValues.zipWithIndex.foreach { case (x, i) =>
        val item = new JMenuItem(Dump.logoObject(x))
        item.addActionListener(new ActionListener {
          def actionPerformed(e: ActionEvent) = index = i
        })
        menu.add(item)
      }
    }

  override def paintComponent(g: Graphics) = {
    super.paintComponent(g) // paint background
    ChooserPainter.paint(g, this, MARGIN, control.getBounds, _name, value)
  }

  private class ChooserClickControl extends JComponent {
    setBackground(InterfaceColors.SLIDER_BACKGROUND)
    setBorder(widgetBorder)
    setOpaque(false)
    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) = popup()
    })
  }

  def mouseWheelMoved(e: MouseWheelEvent) =
    index = if (e.getWheelRotation >= 1) {
      val max = constraint.acceptedValues.size - 1
      StrictMath.min(max, index + 1)
    } else {
      StrictMath.max(0, index - 1)
    }
}
