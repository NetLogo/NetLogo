// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.LogoException
import javax.swing.{JPanel, JComponent}
import java.awt.event._
import java.awt._

object SliderHorizontalPainter {
  // visual parameters
  val LEFT_MARGIN = 1
  val RIGHT_MARGIN = 2
  val MIN_HEIGHT = 33
  val MIN_WIDTH = 92
  val MIN_PREFERRED_WIDTH = 150
  val PADDING = 23
  val CHANNEL_HEIGHT = 12
  val CHANNEL_Y_POS = 3
  val HANDLE_Y_POS = 2
  val HANDLE_WIDTH = 8
  val HANDLE_HEIGHT = 14

  val CHANNEL_LEFT_MARGIN = 6
  val CHANNEL_RIGHT_MARGIN = 6
}

class SliderHorizontalPainter(private val slider:AbstractSliderWidget) extends SliderPainter with MouseWheelListener  {

  import SliderHorizontalPainter._

  // sub-elements of slider visuals
  val handle = new Handle()
  val channel = new Channel()

  locally {
    slider.add(handle)
    slider.add(channel)
    slider.addMouseWheelListener(this)
  }

  override def dettach() {
    slider.remove(handle)
    slider.remove(channel)
    slider.removeMouseWheelListener(this)
  }

  /// size calculations
  override def getMaximumSize = new Dimension(10000, MIN_HEIGHT)
  override def getMinimumSize = {
    if(slider.getBorder() != null) new Dimension(MIN_WIDTH, MIN_HEIGHT)
    else new Dimension(30, HANDLE_HEIGHT)
  }
  override def getPreferredSize(font:Font) = {
    val metrics = slider.getFontMetrics(font)
    val width = maxValueWidth(metrics) + metrics.stringWidth(slider.name) +
      RIGHT_MARGIN + LEFT_MARGIN + PADDING
    new Dimension(StrictMath.max(width, MIN_PREFERRED_WIDTH), MIN_HEIGHT)
  }

  private def maxValueWidth(metrics: FontMetrics) = {
    var result = metrics.stringWidth(slider.valueString(slider.minimum))
    result = StrictMath.max(result, metrics.stringWidth(slider.valueString(slider.maximum)))
    // the following isn't actually guaranteed to find the absolute widest of all possible values,
    // but it's good enough for government work - ST 5/2/02
    result = StrictMath.max(result,
      metrics.stringWidth(slider.valueString(slider.minimum + slider.increment)))
    StrictMath.max(result,
      metrics.stringWidth(slider.valueString(slider.maximum - slider.increment)))
  }

  override def doLayout() {
    val scaleFactor =
      if (slider.getBorder() != null) slider.getHeight.toFloat / MIN_HEIGHT.toFloat
      else slider.getHeight.toFloat / 18.toFloat
    handle.setSize((HANDLE_WIDTH * scaleFactor).toInt, (HANDLE_HEIGHT * scaleFactor).toInt)
    handle.setLocation(handleXPos, HANDLE_Y_POS)
    channel.setBounds(LEFT_MARGIN, CHANNEL_Y_POS,
      slider.getBounds().width - LEFT_MARGIN - RIGHT_MARGIN, (CHANNEL_HEIGHT * scaleFactor).toInt)
  }

  private def handleXPos = {
    LEFT_MARGIN + CHANNEL_LEFT_MARGIN +
      StrictMath.round((slider.value - slider.minimum) / scaleFactor).toInt - handle.getBounds().width / 2
  }

  /// respond to user actions

  def handlePositionChanged(x:Int, buttonRelease:Boolean){
    if(!slider.anyErrors) try{
      val newValue = slider.minimum + x * scaleFactor
      slider.value_=(slider.coerceValue(newValue), buttonRelease)
    }
    catch{ case e:LogoException => org.nlogo.util.Exceptions.ignore(e) }
  }

  def incrementClick(x:Int){
    if(!slider.anyErrors) try {
      val thisRect = channel.getBounds()
      val handleRect = handle.getBounds()
      val center = handleRect.x + handle.getBounds().width / 2
      if(x + thisRect.x > center) slider.value = (slider.coerceValue(slider.value + slider.increment))
      else slider.value = (slider.coerceValue(slider.value - slider.increment))
    }
    catch{ case ex:LogoException=> org.nlogo.util.Exceptions.ignore(ex)  }
  }

  private def scaleFactor = {
    (slider.maximum - slider.minimum) /
            (slider.getBounds().width - LEFT_MARGIN - RIGHT_MARGIN - CHANNEL_LEFT_MARGIN - CHANNEL_RIGHT_MARGIN)
  }

  override def paintComponent(g: Graphics) {
    val padNameHeight = 3
    val nameXOffset = 10
    val rect = slider.getBounds()
    if (!(rect.width == 0 || rect.height == 0)) {
      // this next check is a very kludgey way to distinguish whether we're embedded
      // in the control strip or not - ST 9/15/03
      if (slider.getBorder != null) {
        g.setColor(slider.getForeground)
        val valueString = slider.valueString(slider.value)
        val fontMetrics = g.getFontMetrics
        val valueWidth = fontMetrics.stringWidth(valueString)
        val shortenedName = org.nlogo.awt.Fonts.shortenStringToFit(
            slider.name, rect.width - nameXOffset - valueWidth - RIGHT_MARGIN - CHANNEL_LEFT_MARGIN - 2, fontMetrics)
        g.setColor(slider.getForeground)
        g.drawString(shortenedName, nameXOffset, rect.height - fontMetrics.getMaxDescent - padNameHeight)
        g.drawString(valueString,
          rect.width - valueWidth - RIGHT_MARGIN - CHANNEL_RIGHT_MARGIN,
          rect.height - fontMetrics.getMaxDescent - padNameHeight)
        slider.setToolTipText(if (shortenedName != slider.name) slider.name else null)
      }
    }
  }

  def mouseWheelMoved(e: MouseWheelEvent) {
    if(!slider.anyErrors) try {
      if (e.getWheelRotation() >= 1) slider.value = slider.coerceValue(slider.value - slider.increment)
      else slider.value = slider.coerceValue(slider.value + slider.increment)
    }
    catch { case ex: LogoException => org.nlogo.util.Exceptions.ignore(ex) }
  }

  class Channel extends JComponent {
    setOpaque(false)
    setBackground(org.nlogo.awt.Colors.mixColors(InterfaceColors.SLIDER_BACKGROUND, Color.BLACK, 0.5))
    addMouseListener(new MouseAdapter() {
      override def mousePressed(e: MouseEvent) {
        new Events.InputBoxLoseFocusEvent().raise(Channel.this)
        if ((!e.isPopupTrigger) && org.nlogo.awt.Mouse.hasButton1(e)) incrementClick(e.getX())
      }
    })

    // make tooltips appear in the same location onscreen as they do
    // for the parent Slider
    override def getToolTipLocation(e: MouseEvent) = {
      var loc = slider.getToolTipLocation(e)
      if (loc != null) {
        loc = new Point(loc)
        loc.translate(- getX, - getY)
      }
      loc
    }

    override def paintComponent(g: Graphics) {
      val x = CHANNEL_LEFT_MARGIN
      val y = 0
      val width = getWidth - CHANNEL_LEFT_MARGIN - CHANNEL_RIGHT_MARGIN
      val height = getHeight
      g.setColor(getBackground)
      g.fillRect(x, y, width, height)
      org.nlogo.swing.Utils.createWidgetBorder().paintBorder(this, g, x, y, width, height)
    }
  }

  ///
  class Handle extends JPanel {
    setBackground(InterfaceColors.SLIDER_HANDLE)
    setBorder(org.nlogo.swing.Utils.createWidgetBorder())
    setOpaque(true)
    addMouseListener(new MouseAdapter() {
      override def mousePressed(e: MouseEvent) {new Events.InputBoxLoseFocusEvent().raise(Handle.this)}
      override def mouseReleased(e: MouseEvent) {changed(e.getX, true)}
    })
    addMouseMotionListener(new MouseMotionAdapter() {
      override def mouseDragged(e: MouseEvent) {changed(e.getX, false)}
    })

    // make tooltips appear in the same location onscreen as they do
    // for the parent Slider
    override def getToolTipLocation(e:MouseEvent) = {
      var loc = slider.getToolTipLocation(e)
      if (loc != null) {
        loc = new Point(loc)
        loc.translate(-getX, -getY)
      }
      loc
    }
    private def changed(mouseX: Int, buttonReleased: Boolean) {
      handlePositionChanged(mouseX + getX - getWidth / 2, buttonReleased)
    }
  }
}
