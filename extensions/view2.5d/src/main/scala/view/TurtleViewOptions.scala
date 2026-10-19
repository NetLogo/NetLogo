package view25d.view

import java.awt.{ GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ ButtonGroup, JLabel, JPanel }

import org.nlogo.swing.{ CheckBox, RadioButton, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class TurtleViewOptions(window: TurtleViewGUI, co: Boolean, sz: Boolean, sh: Boolean, pco: Boolean, threeD: Boolean,
                        twoD: Boolean) extends JPanel(new GridBagLayout) with ThemeSync {

  private var stemThickness = 0.0

  private val color = new CheckBox("Color", (selected) => window.refresh()) {
    if (co)
      setSelected(true)
  }

  private val sizeBox = new CheckBox("Size", (selected) => window.refresh()) {
    if (sz)
      setSelected(true)
  }

  private val shape = new CheckBox("Shape", (selected) => window.refresh()) {
    if (sh)
      setSelected(true)
  }

  private val pcolor = new CheckBox("PColor", (selected) => window.refresh()) {
    if (pco)
      setSelected(true)
  }

  private val threeDButton = new RadioButton("xyz", window.refresh) {
    if (threeD)
      setSelected(true)
  }

  private val twoDButton = new RadioButton("xy-plane", window.refresh) {
    if (twoD)
      setSelected(true)
  }

  private val viewLabel = new JLabel("View Options:")
  private val linkLabel = new JLabel("Link Options:")

  locally {
    val group = new ButtonGroup

    group.add(threeDButton)
    group.add(twoDButton)

    val c = new GridBagConstraints

    c.gridy = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    add(viewLabel, c)

    c.insets = new Insets(6, 0, 6, 6)

    add(color, c)
    add(sizeBox, c)
    add(shape, c)
    add(pcolor, c)

    c.weightx = 1

    // invisible buffer to keep everything on the left
    add(new JPanel with Transparent, c)

    c.gridy = 1
    c.weightx = 0
    c.insets = new Insets(0, 6, 6, 6)

    add(linkLabel, c)

    c.insets = new Insets(0, 0, 6, 6)

    add(threeDButton, c)
    add(twoDButton, c)
  }

  def showColor: Boolean =
    color.isSelected

  def showSize: Boolean =
    sizeBox.isSelected

  def showShape: Boolean =
    shape.isSelected

  def usePColor: Boolean =
    pcolor.isSelected

  def getStemThickness: Double =
    stemThickness

  def setStemThickness(value: Double): Unit = {
    stemThickness = value.max(0)
  }

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

    viewLabel.setForeground(InterfaceColors.toolbarText())
    linkLabel.setForeground(InterfaceColors.toolbarText())
    color.setForeground(InterfaceColors.toolbarText())
    sizeBox.setForeground(InterfaceColors.toolbarText())
    shape.setForeground(InterfaceColors.toolbarText())
    pcolor.setForeground(InterfaceColors.toolbarText())

    threeDButton.syncTheme()
    twoDButton.syncTheme()
  }
}
