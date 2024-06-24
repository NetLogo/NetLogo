// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Font, Graphics }
import java.awt.event.{ MouseWheelEvent, MouseWheelListener }

import org.nlogo.agent.ChooserConstraint
import org.nlogo.api.{ CompilerServices, Dump }
import org.nlogo.awt.{ Fonts => NLogoFonts }
import org.nlogo.core.LogoList

object Chooser {
  // visual parameters
  private val MinWidth = 92
  private val MinPreferredWidth = 120
  private val ChooserHeight = 45
  private val Margin = 4
  private val Padding = 14
}

import Chooser._

trait Chooser extends SingleErrorWidget with MouseWheelListener {
  def compiler: CompilerServices
  def name: String

  // The constraint track the list of choices, and ensures the
  // global is always one of them.  We use it to track our current
  // index too (the selected value in the chooser). -- CLB
  protected var constraint = new ChooserConstraint()

  // sub-element of Switch
  private val control = new ComboBox

  locally {
    backgroundColor = InterfaceColors.CHOOSER_BACKGROUND

    setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS))
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

  def populate(): Unit = {
    control.removeAllItems()

    for (choice <- constraint.acceptedValues) {
      control.addItem(Dump.logoObject(choice))
    }
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
    new Dimension(StrictMath.max(MinPreferredWidth, getFontMetrics(font).stringWidth(name) + 2 * Margin + Padding),
                  ChooserHeight)
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
  }

  class ComboBox extends javax.swing.JComboBox[AnyRef] {
    addItemListener(new java.awt.event.ItemListener() {
      override def itemStateChanged(e: java.awt.event.ItemEvent): Unit = {
        if (hasFocus) {
          index(getSelectedIndex)
        }
      }
    })
  }

  def mouseWheelMoved(e: MouseWheelEvent): Unit = {
    val i =
      if (e.getWheelRotation >= 1) {
        val max = constraint.acceptedValues.size - 1
        StrictMath.min(max, index + 1)
      } else {
        StrictMath.max(0, index - 1)
      }

    control.setSelectedIndex(i)
  }
}
