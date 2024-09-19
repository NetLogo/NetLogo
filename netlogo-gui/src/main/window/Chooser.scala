// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Graphics, GridBagConstraints, GridBagLayout, Insets, LinearGradientPaint }
import java.awt.event.{ ItemEvent, ItemListener, MouseAdapter, MouseEvent, MouseWheelEvent, MouseWheelListener }
import javax.swing.{ JComboBox, JLabel, JPanel }

import org.nlogo.agent.ChooserConstraint
import org.nlogo.api.{ CompilerServices, Dump }
import org.nlogo.core.LogoList
import org.nlogo.swing.Utils

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

  private var hover = false

  // The constraint track the list of choices, and ensures the
  // global is always one of them.  We use it to track our current
  // index too (the selected value in the chooser). -- CLB
  protected var constraint = new ChooserConstraint()

  // sub-elements of Switch
  protected val label = new JLabel
  private val control = new JComboBox[AnyRef]
  private val controlPanel = new ComboBoxPanel(control)

  setLayout(new GridBagLayout)

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.gridwidth = 1
    c.weightx = 1
    c.anchor = GridBagConstraints.NORTHWEST
    c.insets =
      if (preserveWidgetSizes)
        new Insets(3, 6, 0, 6)
      else
        new Insets(6, 12, 6, 6)

    add(label, c)

    c.fill = GridBagConstraints.BOTH
    c.insets =
      if (preserveWidgetSizes)
        new Insets(0, 6, 6, 6)
      else
        new Insets(0, 12, 6, 12)

    add(controlPanel, c)
  }

  addMouseWheelListener(this)

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
    if (preserveWidgetSizes)
      new Dimension(MinWidth, ChooserHeight)
    else
      new Dimension(100, 60)

  override def getMaximumSize: Dimension =
    if (preserveWidgetSizes)
      new Dimension(10000, ChooserHeight)
    else
      new Dimension(10000, 60)

  override def getPreferredSize: Dimension =
    if (preserveWidgetSizes)
      new Dimension(MinPreferredWidth, ChooserHeight)
    else
      new Dimension(250, 60)

  ///

  class ComboBoxPanel(comboBox: JComboBox[AnyRef]) extends JPanel {
    setBackground(InterfaceColors.TRANSPARENT)

    comboBox.setBorder(null)
    comboBox.setBackground(InterfaceColors.TRANSPARENT)
    
    setLayout(new GridBagLayout)

    locally {
      val c = new GridBagConstraints

      c.weightx = 1
      c.weighty = 1
      c.fill = GridBagConstraints.BOTH

      if (!preserveWidgetSizes)
        c.insets = new Insets(3, 3, 3, 3)

      add(comboBox, c)
    }

    comboBox.addItemListener(new ItemListener {
      override def itemStateChanged(e: ItemEvent) {
        if (hasFocus)
          index(comboBox.getSelectedIndex)
      }
    })

    addMouseListener(new MouseAdapter {
      override def mouseEntered(e: MouseEvent) {
        hover = true

        getParent.repaint()
      }

      override def mouseExited(e: MouseEvent) {
        if (!contains(e.getPoint)) {
          hover = false

          getParent.repaint()
        }
      }
    })

    override def paintComponent(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)
      g2d.setColor(Color.WHITE)
      g2d.fillRoundRect(0, 0, getWidth, getHeight, (6 * zoomFactor).toInt, (6 * zoomFactor).toInt)
      g2d.setColor(InterfaceColors.CHOOSER_BORDER)
      g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, (6 * zoomFactor).toInt, (6 * zoomFactor).toInt)
      super.paintComponent(g)
    }
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

  override def paintComponent(g: Graphics) {
    backgroundColor = InterfaceColors.CHOOSER_BACKGROUND

    label.setForeground(InterfaceColors.WIDGET_TEXT)

    super.paintComponent(g)

    if (hover) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setPaint(new LinearGradientPaint(controlPanel.getX.toFloat, controlPanel.getY + 3, controlPanel.getX.toFloat,
                                           controlPanel.getY + controlPanel.getHeight + 3, Array(0f, 1f),
                                           Array(InterfaceColors.WIDGET_HOVER_SHADOW, InterfaceColors.TRANSPARENT)))
      g2d.fillRoundRect(controlPanel.getX, controlPanel.getY + 3, controlPanel.getWidth, controlPanel.getHeight, 6, 6)
    }

    if (label.getPreferredSize.width > label.getWidth)
      label.setToolTipText(label.getText)
    else
      label.setToolTipText(null)
  }
}
