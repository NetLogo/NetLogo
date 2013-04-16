// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagLayout, Insets }
import java.awt.GridBagConstraints
import java.awt.GridBagConstraints.REMAINDER

import org.nlogo.swing.VTextIcon

import javax.swing.JLabel

class XAxisLabels extends javax.swing.JPanel {
  private val min: JLabel = new JLabel()
  private val label: JLabel = new JLabel("", javax.swing.SwingConstants.CENTER)
  private val max: JLabel = new JLabel()

  setBackground(InterfaceColors.PLOT_BACKGROUND)
  val gridbag: GridBagLayout = new GridBagLayout
  setLayout(gridbag)
  val c: GridBagConstraints = new GridBagConstraints
  c.insets = new Insets(0, 0, 0, 3)
  c.gridheight = 1
  c.weighty = 0.0
  c.fill = java.awt.GridBagConstraints.NONE
  c.gridwidth = 1
  c.weightx = 0.0
  c.anchor = java.awt.GridBagConstraints.WEST
  c.fill = java.awt.GridBagConstraints.NONE
  gridbag.setConstraints(min, c)
  add(min)
  c.weightx = 100.0
  c.anchor = java.awt.GridBagConstraints.CENTER
  c.fill = java.awt.GridBagConstraints.HORIZONTAL
  gridbag.setConstraints(label, c)
  add(label)
  c.gridwidth = REMAINDER
  c.weightx = 0.0
  c.anchor = java.awt.GridBagConstraints.EAST
  c.fill = java.awt.GridBagConstraints.NONE
  gridbag.setConstraints(max, c)
  add(max)
  org.nlogo.awt.Fonts.adjustDefaultFont(min)
  org.nlogo.awt.Fonts.adjustDefaultFont(label)
  org.nlogo.awt.Fonts.adjustDefaultFont(max)

  def setLabel(text: String) { label.setText(text) }
  def setMax(text: String) { max.setText(text) }
  def setMin(text: String) { min.setText(text) }
  def getLabel = label.getText
}

class YAxisLabels extends javax.swing.JPanel {
  private val label: JLabel = new JLabel()
  private var labelText: String = ""
  private val max: JLabel = new JLabel()
  private val labelIcon: VTextIcon = new VTextIcon(label, "", org.nlogo.swing.VTextIcon.ROTATE_LEFT)
  private val min: JLabel = new JLabel()

  setBackground(InterfaceColors.PLOT_BACKGROUND)
  label.setIcon(labelIcon)
  val gridbag: GridBagLayout = new GridBagLayout
  setLayout(gridbag)
  val c: GridBagConstraints = new GridBagConstraints
  c.insets = new Insets(3, 0, 0, 0)
  c.gridwidth = REMAINDER
  c.gridheight = 1
  c.weightx = 1.0
  c.weighty = 0.0
  c.anchor = java.awt.GridBagConstraints.EAST
  c.fill = java.awt.GridBagConstraints.NONE
  gridbag.setConstraints(max, c)
  add(max)
  c.weighty = 100.0
  c.fill = java.awt.GridBagConstraints.VERTICAL
  gridbag.setConstraints(label, c)
  add(label)
  c.gridheight = REMAINDER
  c.weighty = 0.0
  c.fill = java.awt.GridBagConstraints.NONE
  gridbag.setConstraints(min, c)
  add(min)
  org.nlogo.awt.Fonts.adjustDefaultFont(min)
  org.nlogo.awt.Fonts.adjustDefaultFont(label)
  org.nlogo.awt.Fonts.adjustDefaultFont(max)

  def setMin(text: String) { min.setText(text) }
  def setMax(text: String): Unit = { max.setText(text) }
  def getLabel = labelText
  def setLabel(text: String) {
    labelText = text
    labelIcon.setLabel(labelText)
    label.repaint()
  }
}