// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Font, FontMetrics, Graphics, Graphics2D, Polygon, RenderingHints }
import java.awt.event.{ ActionEvent, ActionListener, MouseAdapter, MouseEvent, MouseWheelEvent, MouseWheelListener }
import javax.swing.{ JComponent, JPopupMenu, JMenuItem }

import org.nlogo.agent.ChooserConstraint
import org.nlogo.api.{ CompilerServices, Dump }
import org.nlogo.awt.{ Fonts => NLogoFonts }
import org.nlogo.core.LogoList
import org.nlogo.swing.WrappingPopupMenu

object Chooser {

  // visual parameters
  private val MinWidth = 92
  private val MinPreferredWidth = 120
  private val ChooserHeight = 45
  private val Margin = 4
  private val Padding = 14

  private def filledDownTriangle(g: Graphics, x: Int, y: Int, size: Int): Unit = {
    val shadowTriangle = new Polygon()
    shadowTriangle.addPoint(x + size / 2, y + size + 2)
    shadowTriangle.addPoint(x - 1, y - 1)
    shadowTriangle.addPoint(x + size + 2, y - 1)
    g.setColor(Color.DARK_GRAY)
    g.fillPolygon(shadowTriangle)

    val downTriangle = new Polygon()
    downTriangle.addPoint(x + size / 2, y + size)
    downTriangle.addPoint(x, y)
    downTriangle.addPoint(x + size, y)
    g.setColor(InterfaceColors.SLIDER_HANDLE)
    g.fillPolygon(downTriangle)
  }
}

import Chooser._

trait Chooser extends SingleErrorWidget with MouseWheelListener {
  def compiler: CompilerServices
  def name: String

  // The constraint track the list of choices, and ensures the
  // global is always one of them.  We use it to track our current
  // index too (the selected value in the chooser). -- CLB
  protected var constraint = new ChooserConstraint()

  // sub-elements of Switch
  private val control = new ChooserClickControl()

  locally {
    setOpaque(true);
    setBackground(InterfaceColors.SLIDER_BACKGROUND)
    setLayout(null)
    add(control)
    doLayout()
    NLogoFonts.adjustDefaultFont(this)
    addMouseWheelListener(this)
  }

  /// attributes

  protected def index: Int = constraint.defaultIndex

  protected def index(index: Int): Unit = {
    constraint.defaultIndex = index
    updateConstraints()
    repaint()
  }

  protected def choices(acceptedValues: LogoList): Unit = {
    constraint.acceptedValues(acceptedValues)
  }

  def value: AnyRef = {
    constraint.defaultValue
  }

  override def updateConstraints(): Unit = {
    if (name.length > 0) {
      new org.nlogo.window.Events.AddChooserConstraintEvent(name, constraint).raise(this)
    }
  }


  override def doLayout(): Unit = {
    val controlHeight = getHeight / 2
    control.setBounds(Margin, getHeight - Margin - controlHeight,
      getWidth - 2 * Margin, controlHeight)
  }

  /// size calculations

  override def getMinimumSize: Dimension =
    new Dimension(MinWidth, ChooserHeight)

  override def getMaximumSize: Dimension =
    new Dimension(10000, ChooserHeight)

  override def getPreferredSize(font: Font): Dimension = {
    var width = MinPreferredWidth
    val metrics = getFontMetrics(font)
    width = StrictMath.max(width, metrics.stringWidth(name) + 2 * Margin + Padding)
    // extra 2 for triangle shadow
    width = StrictMath.max(width,
      longestChoiceWidth(metrics) + triangleSize + 5 * Margin + Padding + 2)
    new Dimension(width, ChooserHeight)
  }

  private def longestChoiceWidth(metrics: FontMetrics): Int = {
    val acceptedValues = constraint.acceptedValues
    if (acceptedValues.isEmpty) 0
    else acceptedValues.map(value => metrics.stringWidth(Dump.logoObject(value))).max
  }

  private def triangleSize(): Int =
    control.getBounds().height / 2 - Margin


  /// respond to user actions

  def popup(): Unit = {
    val menu = new WrappingPopupMenu()
    populate(menu)
    menu.show(this,
      control.getBounds().x + 3,   // the 3 aligns us with the inside edge
      control.getBounds().y + control.getBounds().height);
  }

  def populate(menu: JPopupMenu): Unit = {
    if (constraint.acceptedValues.isEmpty) {
      val nullItem = new JMenuItem("<No Choices>")
      nullItem.setEnabled(false)
      menu.add(nullItem)
    } else {
      constraint.acceptedValues.zipWithIndex.foreach {
        case (constraintValue, i) =>
          val item = new JMenuItem(Dump.logoObject(constraintValue))
          val actionListener = new ActionListener {
            override def actionPerformed(e: ActionEvent): Unit = {
              index(i)
            }
          }
          item.addActionListener(actionListener)
          menu.add(item)
      }
    }
  }

  ///

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val size = getSize()
    val cb = control.getBounds()

    g.setColor(getForeground)
    val metrics = g.getFontMetrics
    val fontAscent = metrics.getMaxAscent
    val fontHeight = fontAscent + metrics.getMaxDescent

    val shortenedName =
        NLogoFonts.shortenStringToFit(name, size.width - 2 * Margin, metrics)
    g.drawString(shortenedName, Margin,
      Margin + (cb.y - Margin - fontHeight) / 2 + fontAscent)

    val shortenedValue =
        NLogoFonts.shortenStringToFit(
          Dump.logoObject(value),
                cb.width - Margin * 3 - triangleSize() - 2, // extra 2 for triangle shadow
                metrics)
    g.drawString(shortenedValue,
            cb.x + Margin,
            cb.y + (cb.height - fontHeight) / 2 + fontAscent)

    g.asInstanceOf[Graphics2D].setRenderingHint(
      RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    filledDownTriangle(g,
      cb.x + cb.width - Margin - triangleSize - 2,  // extra 2 for triangle shadow
      cb.y + (cb.height - triangleSize) / 2 + 1,
      triangleSize)

    setToolTipText(if (shortenedName != name) name else null)
    val toolTip = if (shortenedValue != Dump.logoObject(value)) Dump.logoObject(value) else null
    control.setToolTipText(toolTip)
  }

  class ChooserClickControl extends JComponent {
    setBackground(InterfaceColors.SLIDER_BACKGROUND)
    setBorder(widgetBorder)
    setOpaque(false)
    addMouseListener(
      new MouseAdapter() {
        override def mousePressed(e: MouseEvent): Unit = {
          popup()
        }
      })
  }

  def mouseWheelMoved(e: MouseWheelEvent): Unit = {
    if (e.getWheelRotation >= 1) {
      val max = constraint.acceptedValues.size - 1
      index(StrictMath.min(max, index + 1))
    } else {
      index(StrictMath.max(0, index - 1))
    }
  }
}
