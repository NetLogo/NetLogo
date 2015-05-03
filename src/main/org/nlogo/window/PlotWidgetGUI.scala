// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.GridBagConstraints.REMAINDER

import org.nlogo.plot.Plot

import javax.swing.{ JLabel, JPanel }

class PlotWidgetGUI(
  private var _plot: Plot,
  fontSource: java.awt.Component) {

  def plot_=(plot: Plot) {
    _plot = plot
    canvas.setPlot(plot)
    legend.plot = plot
  }
  def plot = _plot

  val canvas = new PlotCanvas(plot)
  val legend = new PlotLegend(plot, fontSource)
  val nameLabel = new JLabel("", javax.swing.SwingConstants.CENTER)
  val xAxis = new XAxisLabels()
  val yAxis = new YAxisLabels()

  def addToPanel(panel: JPanel) {
    val gridbag = new java.awt.GridBagLayout()
    panel.setLayout(gridbag)

    val c = new java.awt.GridBagConstraints()
    c.gridwidth = 1
    c.gridheight = 1
    c.weightx = 0.0
    c.weighty = 0.0
    c.fill = java.awt.GridBagConstraints.NONE

    //ROW1
    //-----------------------------------------
    c.insets = new java.awt.Insets(0, 1, 1, 1)

    c.gridx = 1
    c.gridy = 0
    c.gridwidth = 1
    c.anchor = java.awt.GridBagConstraints.CENTER
    c.fill = java.awt.GridBagConstraints.HORIZONTAL
    gridbag.setConstraints(nameLabel, c)
    panel.add(nameLabel)
    org.nlogo.awt.Fonts.adjustDefaultFont(nameLabel)
    nameLabel.setFont(nameLabel.getFont().deriveFont(java.awt.Font.BOLD))
    nameLabel.setText(plot.name)

    //ROW2
    //-----------------------------------------
    c.insets = new java.awt.Insets(0, 1, 0, 1)

    c.gridx = java.awt.GridBagConstraints.RELATIVE
    c.gridy = 1
    c.gridwidth = 1
    c.gridheight = java.awt.GridBagConstraints.RELATIVE
    c.weighty = 3.0
    c.anchor = java.awt.GridBagConstraints.WEST
    c.fill = java.awt.GridBagConstraints.VERTICAL
    gridbag.setConstraints(yAxis, c)
    panel.add(yAxis)

    c.gridwidth = java.awt.GridBagConstraints.RELATIVE
    c.weightx = 3.0
    c.anchor = java.awt.GridBagConstraints.CENTER
    c.fill = java.awt.GridBagConstraints.BOTH
    gridbag.setConstraints(canvas, c)
    panel.add(canvas)

    c.gridwidth = REMAINDER
    c.weightx = 0.0
    c.anchor = java.awt.GridBagConstraints.NORTH
    c.fill = java.awt.GridBagConstraints.NONE
    c.insets = new java.awt.Insets(0, 3, 0, 1)
    gridbag.setConstraints(legend, c)
    panel.add(legend)

    //ROW3
    //-----------------------------------------
    c.insets = new java.awt.Insets(0, 0, 0, 0)
    c.gridy = 2

    val filler2 = new javax.swing.JLabel()
    c.weightx = 0.0
    c.weighty = 0.0
    c.gridwidth = 1
    c.gridheight = 1
    c.anchor = java.awt.GridBagConstraints.WEST
    c.fill = java.awt.GridBagConstraints.NONE
    gridbag.setConstraints(filler2, c)
    panel.add(filler2)

    c.gridwidth = java.awt.GridBagConstraints.RELATIVE
    c.anchor = java.awt.GridBagConstraints.CENTER
    c.fill = java.awt.GridBagConstraints.HORIZONTAL
    gridbag.setConstraints(xAxis, c)
    panel.add(xAxis)

    val filler3 = new javax.swing.JLabel()
    c.weightx = 0.0
    c.weighty = 0.0
    c.gridwidth = 1
    c.gridheight = 1
    c.anchor = java.awt.GridBagConstraints.EAST
    c.fill = java.awt.GridBagConstraints.NONE
    gridbag.setConstraints(filler3, c)
    panel.add(filler3)

    // make sure to update the gui components in case
    // something changed underneath ev 8/26/08
    refresh()

  }

  def refreshAxisLabels() {
    def getLabel(d: Double) = if (d.toString.endsWith(".0")) d.toString.dropRight(2) else d.toString
    xAxis.setMin(getLabel(plot.state.xMin))
    xAxis.setMax(getLabel(plot.state.xMax))
    yAxis.setMin(getLabel(plot.state.yMin))
    yAxis.setMax(getLabel(plot.state.yMax))
  }
  def refresh() {
    refreshAxisLabels()
    legend.refresh()
  }
}