// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Font, Graphics, GridBagConstraints, GridBagLayout, Insets, LinearGradientPaint }
import java.awt.event.{ MouseWheelEvent, MouseWheelListener }
import javax.swing.JLabel

import org.nlogo.agent.ChooserConstraint
import org.nlogo.api.{ CompilerServices, Dump }
import org.nlogo.core.{ I18N, LogoList }
import org.nlogo.swing.{ ComboBox, Utils }
import org.nlogo.theme.InterfaceColors

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
  protected val label = new JLabel(I18N.gui.get("edit.chooser.previewName"))
  private val control = new ComboBox[String] {
    addItemListener(_ => index(getSelectedIndex))

    override def paintComponent(g: Graphics): Unit = {
      setDiameter(zoom(6))

      super.paintComponent(g)
    }
  }

  if (boldName)
    label.setFont(label.getFont.deriveFont(Font.BOLD))

  setLayout(new GridBagLayout)

  addMouseWheelListener(this)

  initGUI()

  override def initGUI(): Unit = {
    removeAll()

    val c = new GridBagConstraints

    c.gridx = 0
    c.gridwidth = 1
    c.weightx = 1
    c.anchor = GridBagConstraints.NORTHWEST
    c.insets = {
      if (_oldSize) {
        new Insets(zoom(3), zoom(6), 0, zoom(6))
      } else {
        new Insets(zoom(6), zoom(8), zoom(6), zoom(8))
      }
    }

    add(label, c)

    c.fill = GridBagConstraints.BOTH
    c.weighty = 1
    c.insets = {
      if (_oldSize) {
        new Insets(0, zoom(6), zoom(6), zoom(6))
      } else {
        new Insets(0, zoom(8), zoom(8), zoom(8))
      }
    }

    add(control, c)
  }

  /// attributes

  protected def index: Int = constraint.defaultIndex

  protected def index(index: Int): Unit = {
    constraint.defaultIndex = index
    updateConstraints()
    control.setSelectedIndex(index)
    repaint()
  }

  protected def choices(acceptedValues: LogoList): Unit = {
    constraint.acceptedValues(acceptedValues)
  }

  def value: AnyRef =
    constraint.defaultValue

  def populate(): Unit = {
    control.setItems(constraint.acceptedValues.map(Dump.logoObject).toList)
  }

  override def updateConstraints(): Unit = {
    if (name.length > 0) {
      new Events.AddChooserConstraintEvent(name, constraint).raise(this)
    }
  }

  /// size calculations

  override def getMinimumSize: Dimension = {
    if (_oldSize) {
      new Dimension(MinWidth, ChooserHeight)
    } else {
      new Dimension(100, 60)
    }
  }

  override def getPreferredSize: Dimension = {
    if (_oldSize) {
      new Dimension(MinPreferredWidth, ChooserHeight)
    } else {
      new Dimension(250, 60)
    }
  }

  ///

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

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    if (isHover) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setPaint(new LinearGradientPaint(control.getX.toFloat, control.getY + 3, control.getX.toFloat,
                                           control.getY + control.getHeight + 3, Array(0f, 1f),
                                           Array(InterfaceColors.widgetHoverShadow(), InterfaceColors.Transparent)))
      g2d.fillRoundRect(control.getX, control.getY + 3, control.getWidth, control.getHeight, 6, 6)
    }

    if (label.getPreferredSize.width > label.getWidth)
      label.setToolTipText(label.getText)
    else
      label.setToolTipText(null)
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.chooserBackground())

    label.setForeground(InterfaceColors.widgetText())

    control.syncTheme()
    control.setBorderColor(InterfaceColors.chooserBorder())
  }
}
