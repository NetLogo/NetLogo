// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Font, Graphics }
import java.util.List
import org.nlogo.api.{ Editable, I18N, Property }
import org.nlogo.awt.Fonts

class DummyButtonWidget extends SingleErrorWidget with Editable {
  setBackground(InterfaceColors.BUTTON_BACKGROUND)
  setBorder(widgetBorder)
  Fonts.adjustDefaultFont(this)

  def propertySet = Properties.dummyButton

  private var _actionKey = 0: Char
  def actionKey = _actionKey
  def actionKey(__actionKey: Char) = _actionKey = __actionKey
  private def actionKeyString = if(actionKey==0) "" else actionKey.toString
  
  private var _keyEnabled = false
  def keyEnabled = _keyEnabled
  def keyEnabled(__keyEnabled: Boolean) = {
    _keyEnabled = __keyEnabled
    repaint()
  }

  /// editability

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.button")
  private var _name = ""
  def name = _name
  def name(__name: String) = {
    _name = __name
    displayName(_name)
  }
  
  /// sizing

  override def getMinimumSize = new Dimension(55, 33)
  override def getPreferredSize(font: Font) = {
    val size = getMinimumSize
    val fontMetrics = getFontMetrics(font)
    size.width = StrictMath.max(size.width, fontMetrics.stringWidth(displayName) + 28)
    size.height = StrictMath.max(size.height, fontMetrics.getMaxDescent + fontMetrics.getMaxAscent + 12)
    size
  }

  /// painting

  override def paintComponent(g: Graphics) = {
    g.setColor(getBackground)
    g.fillRect(0, 0, getWidth, getHeight)
    val size = getSize()
    val fontMetrics = g.getFontMetrics
    val labelHeight = fontMetrics.getMaxDescent + fontMetrics.getMaxAscent
    val availableWidth = size.width - 8
    val stringWidth = fontMetrics.stringWidth(displayName)
    g.setColor(getForeground)

    val shortString = org.nlogo.awt.Fonts.shortenStringToFit(displayName, availableWidth, fontMetrics)
    val nx = if(stringWidth > availableWidth) 4 else (size.width / 2) - (stringWidth / 2)
    val ny = (size.height / 2) + (labelHeight / 2)
    g.drawString(shortString, nx, ny)
    // now draw keyboard shortcut
    if(actionKeyString!="") {
      val ax = size.width - 4 - fontMetrics.stringWidth(actionKeyString)
      val ay = fontMetrics.getMaxAscent + 2
      g.setColor(if(keyEnabled) Color.BLACK else Color.GRAY)
      g.drawString(actionKeyString, ax - 1, ay)
    }
  }

  ///

  override def save() = "BUTTON\n" +
    s"$getBoundsString${if(name.trim!="") name else "NIL"}\nNIL\nNIL\n" +
    (1 + "\n") + "T\n" +
    "OBSERVER\n" +
    s"NIL\n${if(actionKey==0||actionKey==' ') "NIL" else actionKey}\n"
  
  override def load(strings: scala.collection.Seq[String], helper: Widget.LoadHelper) = {
    if(strings.size > 12 && strings(12)!="NIL") {
      actionKey(strings(12)(0))
    }

    name("")
    var dName = strings(5)
    if(dName!="NIL") {
      name(dName)
    } else {
      // to support importing old clients, sometimes there is
      // no display name but there is code intended to be used as
      // the display name ev 8/11/06
      dName = strings(6)
      if(dName!="NIL")
        name(dName)
    }

    val x1 = strings(1).toInt
    val y1 = strings(2).toInt
    val x2 = strings(3).toInt
    val y2 = strings(4).toInt
    setSize(x2-x1, y2-y1)
    this
  }

}
