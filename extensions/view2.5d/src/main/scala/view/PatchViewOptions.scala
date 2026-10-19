package view25d.view

import java.awt.{ Dimension, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ ButtonGroup, JLabel, JPanel }

import org.nlogo.swing.{ RadioButton, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class PatchViewOptions(window: PatchViewGUI, str: Boolean, she: Boolean, pin: Boolean, threeD: Boolean, twoD: Boolean)
  extends JPanel(new GridBagLayout) with ThemeSync {

  private val structuresButton = new RadioButton("Structures", () => {
    threeDButton.setEnabled(true)
    twoDButton.setEnabled(true)

    window.refresh()
  }) {
    if (str)
      setSelected(true)
  }

  private val sheetButton = new RadioButton("Sheet", () => {
    threeDButton.setEnabled(false)
    twoDButton.setEnabled(false)

    window.refresh()
  }) {
    if (she)
      setSelected(true)
  }

  private val pinsButton = new RadioButton("Pins", () => {
    threeDButton.setEnabled(false)
    twoDButton.setEnabled(false)

    window.refresh()
  }) {
    if (pin)
      setSelected(true)
  }

  locally {
    val group = new ButtonGroup

    group.add(structuresButton)
    group.add(sheetButton)
    group.add(pinsButton)
  }

  private val threeDButton = new RadioButton("xyz", window.refresh) {
    if (threeD)
      setSelected(true)
  }

  private val twoDButton = new RadioButton("xy-plane", window.refresh) {
    if (twoD)
      setSelected(true)
  }

  private val displayLabel = new JLabel("Display Options:")
  private val linkLabel = new JLabel("Link Options:")

  locally {
    val group = new ButtonGroup

    group.add(threeDButton)
    group.add(twoDButton)

    val c = new GridBagConstraints

    c.gridy = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    add(displayLabel, c)

    c.insets = new Insets(6, 0, 6, 6)

    add(structuresButton, c)
    add(sheetButton, c)
    add(pinsButton, c)

    c.gridy = 1
    c.insets = new Insets(0, 6, 6, 6)

    add(linkLabel, c)

    c.insets = new Insets(0, 0, 6, 6)

    add(threeDButton, c)
    add(twoDButton, c)

    c.weightx = 1

    // invisible buffer to keep everything on the left
    add(new JPanel with Transparent, c)
  }

  setSize(new Dimension(600, 60))

  def sticks: Boolean =
    pinsButton.isSelected

  def tangents: Boolean =
    sheetButton.isSelected || pinsButton.isSelected

  def structures: Boolean =
    structuresButton.isSelected

  def linksAreThreeD: Boolean =
    threeDButton.isSelected

  def linksAreTwoD: Boolean =
    twoDButton.isSelected

  def setLinksDisplayMode(linksInXYPlane: Boolean): Unit = {
    if (linksInXYPlane)
      twoDButton.setSelected(true)
    else
      threeDButton.setSelected(true)
  }

  def syncTheme(): Unit = {
    setBackground(InterfaceColors.toolbarBackground())

    displayLabel.setForeground(InterfaceColors.toolbarText())
    linkLabel.setForeground(InterfaceColors.toolbarText())

    structuresButton.syncTheme()
    sheetButton.syncTheme()
    pinsButton.syncTheme()
    threeDButton.syncTheme()
    twoDButton.syncTheme()
  }
}
