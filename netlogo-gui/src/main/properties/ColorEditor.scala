// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.api.Dump
import org.nlogo.api.Approximate.approximate
import org.nlogo.api.Color.{getClosestColorNumberByARGB, getColorNameByIndex}
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{Graphics, Component, Color}
import javax.swing.{Icon, JPanel, JButton}

abstract class ColorEditor(accessor: PropertyAccessor[Color], frame: java.awt.Frame)
  extends PropertyEditor(accessor)
{

  private val colorIcon = new ColorIcon
  private val colorButton = new JButton("0 (black)", colorIcon)
  private val originalColor: Color = accessor.get

  add(new javax.swing.JLabel(accessor.displayName))
  add(colorButton)
  colorButton.addActionListener(new SelectColorActionListener())
  setColor(originalColor)

  def setColor(color: Color) {
    colorIcon.setColor(color)
    val c = getClosestColorNumberByARGB(color.getRGB)
    val colorString = c match {
      // this logic is duplicated in InputBox
      // black and white are special cases
      case 0 => "0 (black)"
      case 9.9 => "9.9 (white)"
      case _ =>
        val index = (c / 10).toInt
        val baseColor = index * 10 + 5
        Dump.number(c) + " (" + getColorNameByIndex(index) + (
          if (c > baseColor) {" + " + Dump.number(approximate(c - baseColor, 1))}
          else if (c < baseColor) {" - "} + Dump.number(approximate(baseColor - c, 1))
          else ""
        ) + ")"
    }
    colorButton.setText(colorString)
  }

  override def getMinimumSize = colorButton.getMinimumSize
  override def get = Some(colorIcon.getColor)
  override def set(value: Color) { setColor(value) }
  override def requestFocus() { colorButton.requestFocus() }

  override def revert() {
    setColor(originalColor)
    super.revert()
  }

   private class SelectColorActionListener extends ActionListener{
     def actionPerformed(e: ActionEvent){
       val colorDialog = new org.nlogo.window.ColorDialog(frame, true)
       val c = colorDialog.showInputBoxDialog(getClosestColorNumberByARGB(colorIcon.getColor.getRGB))
       setColor(org.nlogo.api.Color.getColor(c: java.lang.Double))
     }
   }

  private class ColorIcon extends Icon {
    val color = new JPanel
    def paintIcon(c:Component, g:Graphics, x:Int, y:Int){
      g.setColor(getColor)
      g.fillRect(x, y, getIconWidth, getIconHeight)
    }
    def getIconWidth = color.getMinimumSize().width
    def getIconHeight = color.getMinimumSize().height
    def setColor(c: Color) { color.setBackground(c) }
    def getColor = color.getBackground
  }
}
