// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Graphics, Insets, LinearGradientPaint }
import javax.swing.{ Box, BoxLayout, JLabel }

import org.nlogo.agent.ChooserConstraint
import org.nlogo.api.{ CompilerServices, Dump }
import org.nlogo.core.{ I18N, LogoList }
import org.nlogo.swing.{ BoxRow, ComboBox, Utils }
import org.nlogo.theme.InterfaceColors

trait Chooser extends SingleErrorWidget {
  def compiler: CompilerServices

  // The constraint track the list of choices, and ensures the
  // global is always one of them.  We use it to track our current
  // index too (the selected value in the chooser). -- CLB
  protected var constraint = new ChooserConstraint()

  protected var _name = ""

  // sub-elements of Switch
  protected val label = new JLabel(I18N.gui.get("edit.chooser.previewName"))
  private val control = new ComboBox[String](searchable = true) {
    addItemListener(_ => index(getSelectedIndex))
  }

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new AdaptableBorder(new Insets(3, 6, 6, 6), new Insets(6, 8, 8, 8)))

  add(new BoxRow(Seq(label, Box.createHorizontalGlue)))
  add(new AdaptableVerticalStrut(0, 6))
  add(control)

  /// attributes

  def name: String =
    _name

  def setVarName(name: String): Unit = {
    _name = name
    if (_name == "") {
      displayName(I18N.gui.get("edit.chooser.previewName"))
    } else {
      displayName(name)
    }
    label.setText(displayName)
    repaint()
  }

  protected def index: Int = constraint.defaultIndex

  protected def index(index: Int): Unit = {
    if (this.index != index) {
      constraint.defaultIndex = index
      updateConstraints()
      control.setSelectedIndex(index)
      repaint()
      new Events.DirtyEvent(None).raise(this)
    }
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
    Utils.zoomSize {
      if (_oldSize) {
        new Dimension(92, 45)
      } else {
        new Dimension(100, 60)
      }
    }
  }

  override def getPreferredSize: Dimension = {
    Utils.zoomSize {
      if (_oldSize) {
        new Dimension(120, 45)
      } else {
        new Dimension(250, 60)
      }
    }
  }

  ///

  override def doLayout(): Unit = {
    super.doLayout()

    if (label.getPreferredSize.width > label.getWidth) {
      label.setToolTipText(label.getText)
    } else {
      label.setToolTipText(null)
    }
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    if (isHover) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setPaint(new LinearGradientPaint(control.getX.toFloat, (control.getY + 3).toFloat, control.getX.toFloat,
                                           (control.getY + control.getHeight + 3).toFloat, Array(0f, 1f),
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
