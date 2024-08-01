// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Font, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ MouseWheelEvent, MouseWheelListener }
import javax.swing.JLabel

import org.nlogo.agent.ChooserConstraint
import org.nlogo.api.{ CompilerServices, Dump }
import org.nlogo.core.LogoList

object Chooser {
  // visual parameters
  private val MinWidth = 92
  private val MinPreferredWidth = 120
  private val ChooserHeight = 45
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
  protected val label = new JLabel
  private val control = new ComboBox

  label.setForeground(InterfaceColors.WIDGET_TEXT)

  locally {
    backgroundColor = InterfaceColors.CHOOSER_BACKGROUND

    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridx = 0
    c.gridy = 0
    c.gridwidth = 1
    c.weightx = 1
    c.anchor = GridBagConstraints.NORTHWEST
    c.insets = new Insets(3, 6, 0, 6)

    add(label, c)

    c.gridy = 1
    c.fill = GridBagConstraints.BOTH
    c.insets = new Insets(0, 6, 6, 6)

    add(control, c)

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

  /// size calculations

  override def getMinimumSize: Dimension =
    new Dimension(MinWidth, ChooserHeight)

  override def getMaximumSize: Dimension =
    new Dimension(10000, ChooserHeight)

  override def getPreferredSize(font: Font): Dimension = {
    new Dimension(MinPreferredWidth, ChooserHeight)
  }

  ///

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
