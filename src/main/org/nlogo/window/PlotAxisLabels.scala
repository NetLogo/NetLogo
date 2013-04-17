// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagLayout, Insets }
import java.awt.GridBagConstraints
import java.awt.GridBagConstraints.{ CENTER, EAST, HORIZONTAL, NONE, REMAINDER, VERTICAL, WEST }

import org.nlogo.swing.VTextIcon

import javax.swing.JLabel

trait PlotAxisLabels extends javax.swing.JPanel {
  val min: JLabel = new JLabel()
  val max: JLabel = new JLabel()
  val label: JLabel = new JLabel()
  setBackground(InterfaceColors.PLOT_BACKGROUND)
  val gridBag: GridBagLayout = new GridBagLayout
  setLayout(gridBag)
  val c: GridBagConstraints = new GridBagConstraints
  def setMax(text: String) { max.setText(text) }
  def setMin(text: String) { min.setText(text) }
  def getLabel: String
  def setLabel(text: String): Unit
  def adjustFonts() { Seq(min, label, max).foreach(org.nlogo.awt.Fonts.adjustDefaultFont) }
}

class XAxisLabels extends PlotAxisLabels {
  override val label = new JLabel("", javax.swing.SwingConstants.CENTER)

  c.insets = new Insets(0, 0, 0, 3)
  c.gridheight = 1
  c.weighty = 0.0
  c.fill = NONE
  c.gridwidth = 1
  c.weightx = 0.0
  c.anchor = WEST
  c.fill = NONE
  gridBag.setConstraints(min, c)
  add(min)
  c.weightx = 100.0
  c.anchor = CENTER
  c.fill = HORIZONTAL
  gridBag.setConstraints(label, c)
  add(label)
  c.gridwidth = REMAINDER
  c.weightx = 0.0
  c.anchor = EAST
  c.fill = NONE
  gridBag.setConstraints(max, c)
  add(max)

  adjustFonts()

  override def setLabel(text: String) { label.setText(text) }
  override def getLabel = label.getText
}

class YAxisLabels extends PlotAxisLabels {
  private var labelText: String = ""
  private val labelIcon: VTextIcon = new VTextIcon(label, "", org.nlogo.swing.VTextIcon.ROTATE_LEFT)

  label.setIcon(labelIcon)
  c.insets = new Insets(3, 0, 0, 0)
  c.gridwidth = REMAINDER
  c.gridheight = 1
  c.weightx = 1.0
  c.weighty = 0.0
  c.anchor = EAST
  c.fill = NONE
  gridBag.setConstraints(max, c)
  add(max)
  c.weighty = 100.0
  c.fill = VERTICAL
  gridBag.setConstraints(label, c)
  add(label)
  c.gridheight = REMAINDER
  c.weighty = 0.0
  c.fill = NONE
  gridBag.setConstraints(min, c)
  add(min)

  adjustFonts()

  def getLabel = labelText
  def setLabel(text: String) {
    labelText = text
    labelIcon.setLabel(labelText)
    label.repaint()
  }
}