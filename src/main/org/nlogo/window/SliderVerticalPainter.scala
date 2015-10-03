// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt._
import event._
import font.TextLayout
import javax.swing.{JPanel, JComponent}
import org.nlogo.api.LogoException

object SliderVerticalPainter {
  // visual parameters
  val BOTTOM_MARGIN = 1
  val TOP_MARGIN = 2
  val MIN_HEIGHT = 92
  val MIN_WIDTH = 33
  val MIN_PREFERRED_HEIGHT = 150
  val PADDING = 23
  val CHANNEL_WIDTH = 12
  val CHANNEL_X_POS = 3
  val HANDLE_X_POS = 2
  val HANDLE_WIDTH = 14
  val HANDLE_HEIGHT = 8

  val CHANNEL_BOTTOM_MARGIN = 6
  val CHANNEL_TOP_MARGIN = 6
}

class SliderVerticalPainter(private val slider: AbstractSliderWidget) extends SliderPainter with MouseWheelListener {
  import SliderVerticalPainter._

  // sub-elements of slider visuals
  private val handle = new Handle()
  private val channel = new Channel()

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
  override def getMinimumSize = new Dimension(MIN_WIDTH, MIN_HEIGHT)
  override def getMaximumSize = new Dimension(MIN_WIDTH, 10000)
  override def getPreferredSize(font: Font) = {
    val metrics = slider.getFontMetrics(font)
    val height = maxValueWidth(metrics) + metrics.stringWidth(slider.name) +
            TOP_MARGIN + BOTTOM_MARGIN + PADDING
    new Dimension(MIN_WIDTH, StrictMath.max(height, MIN_PREFERRED_HEIGHT))
  }

  private def maxValueWidth(metrics: FontMetrics) = {
    var result = metrics.stringWidth(slider.valueString(slider.minimum))
    result = StrictMath.max(result, metrics.stringWidth(slider.valueString(slider.maximum)))
    // the following isn't actually guaranteed to find the absolute widest of all possible values,
    // but it's good enough for government work - ST 5/2/02
    result = StrictMath.max(result, metrics.stringWidth(slider.valueString(slider.minimum + slider.increment)))
    StrictMath.max(result, metrics.stringWidth(slider.valueString(slider.maximum - slider.increment)))
  }

  override def doLayout() {
    val scaleFactor =
      if (slider.getBorder() != null) slider.getWidth.toFloat / MIN_WIDTH.toFloat
      else slider.getWidth.toFloat / 18f
    handle.setSize((HANDLE_WIDTH * scaleFactor).toInt, (HANDLE_HEIGHT * scaleFactor).toInt)
    handle.setLocation(HANDLE_X_POS, handleYPos)
    channel.setBounds(CHANNEL_X_POS, TOP_MARGIN, (CHANNEL_WIDTH * scaleFactor).toInt,
            slider.getBounds().height - BOTTOM_MARGIN - TOP_MARGIN)
  }

  private def handleYPos = BOTTOM_MARGIN + CHANNEL_TOP_MARGIN -
    StrictMath.round((slider.value - slider.maximum) / scaleFactor).toInt - handle.getBounds().height / 2

  /// respond to user actions
  def handlePositionChanged(y: Int, buttonRelease: Boolean) {
    if(!slider.anyErrors){
      try slider.value_=(slider.coerceValue(slider.maximum - y * scaleFactor), buttonRelease)
      catch { case e: LogoException => org.nlogo.util.Exceptions.ignore(e) }
    }
  }

  def incrementClick(y: Int) {
    if(!slider.anyErrors) try {
      val thisRect = channel.getBounds()
      val handleRect = handle.getBounds()
      val center = handleRect.y + handle.getBounds().height / 2
      slider.value =
        if (y + thisRect.y > center) slider.coerceValue(slider.value - slider.increment)
        else slider.coerceValue(slider.value + slider.increment)
    }
    catch { case e: LogoException => org.nlogo.util.Exceptions.ignore(e) }
  }

  private def scaleFactor = (slider.maximum - slider.minimum) /
    (slider.getBounds().height - BOTTOM_MARGIN - TOP_MARGIN - CHANNEL_BOTTOM_MARGIN - CHANNEL_TOP_MARGIN)

  override def paintComponent(g: Graphics) {
    val nameYOffset = 10
    val padNameWidth = 3
    val rect = slider.getBounds()
    if (!(rect.width == 0 || rect.height == 0)) {
      // this next check is a very kludgey way to distinguish whether we're embedded
      // in the control strip or not - ST 9/15/03
      if (slider.getBorder != null) {
        g.setColor(slider.getForeground)

        val valueString = slider.valueString(slider.value)
        val fontMetrics = g.getFontMetrics

        // remember, we are writing rotated counter-clockwise 90 degrees, so that var names
        // reflect that.
        val valueHeight = fontMetrics.stringWidth(valueString)
        val shortenedName = org.nlogo.awt.Fonts.shortenStringToFit(
          slider.name, rect.height - nameYOffset - valueHeight - TOP_MARGIN - CHANNEL_BOTTOM_MARGIN - 2, fontMetrics)

        // write name and value text rotated -90 degrees
        g.setColor(slider.getForeground())
        val rot = -StrictMath.toRadians(90)
        val y = rect.height - nameYOffset
        val x = rect.width - fontMetrics.getMaxDescent - padNameWidth

        val g2d = g.asInstanceOf[Graphics2D]
        if (shortenedName.length > 0) {
          val nameLayout = new TextLayout(shortenedName, g.getFont, g2d.getFontRenderContext)
          g.translate(x, y)
          g2d.rotate(rot)
          nameLayout.draw(g2d, 0, 0)
          g2d.rotate(-rot)
          g.translate(-x, -y)
        }
        if (valueString.length > 0) {
          val valueLayout = new TextLayout(valueString, g.getFont, g2d.getFontRenderContext)
          val y2 = StrictMath.round(valueHeight + TOP_MARGIN + CHANNEL_TOP_MARGIN)
          g.translate(x, y2)
          g2d.rotate(rot)
          valueLayout.draw(g2d, 0, 0)
          g2d.rotate(-rot)
          g.translate(-x, -y2)
        }

        slider.setToolTipText(if (shortenedName != slider.name) slider.name else null)
      }
    }
  }

  def mouseWheelMoved(e: MouseWheelEvent) {
    if(!slider.anyErrors) try {
      if (e.getWheelRotation >= 1) slider.value = slider.coerceValue(slider.value - slider.increment)
      else slider.value = slider.coerceValue(slider.value + slider.increment)
    }
    catch {case ex: LogoException => org.nlogo.util.Exceptions.ignore(ex)}
  }

  private class Channel extends JComponent {
    setOpaque(false)
    setBackground(org.nlogo.awt.Colors.mixColors(InterfaceColors.SLIDER_BACKGROUND, Color.BLACK, 0.5))
    addMouseListener(new MouseAdapter() {
      override def mousePressed(e: MouseEvent) {
        new Events.InputBoxLoseFocusEvent().raise(Channel.this)
        if ((!e.isPopupTrigger) && org.nlogo.awt.Mouse.hasButton1(e)) incrementClick(e.getY)
      }
    })
    // make tooltips appear in the same location onscreen as they do
    // for the parent Slider
    override def getToolTipLocation(e: MouseEvent) = {
      val loc = slider.getToolTipLocation(e)
      if (loc != null) {
        val newLoc = new Point(loc)
        newLoc.translate(-getX, -getY)
        newLoc
      } else null
    }
    override def paintComponent(g: Graphics) {
      val x = 0
      val y = CHANNEL_TOP_MARGIN
      val height = getHeight - CHANNEL_BOTTOM_MARGIN - CHANNEL_TOP_MARGIN
      val width = getWidth
      g.setColor(getBackground)
      g.fillRect(x, y, width, height)
      org.nlogo.swing.Utils.createWidgetBorder().paintBorder(this, g, x, y, width, height)
    }
  }

  private class Handle extends JPanel {
    setBackground(InterfaceColors.SLIDER_HANDLE)
    setBorder(org.nlogo.swing.Utils.createWidgetBorder())
    setOpaque(true)
    addMouseListener(new MouseAdapter() {
      override def mousePressed(e: MouseEvent) { new Events.InputBoxLoseFocusEvent().raise(Handle.this) }
      override def mouseReleased(e: MouseEvent) { changed(e.getY, true) }
    })
    addMouseMotionListener(new MouseMotionAdapter() {
      override def mouseDragged(e: MouseEvent) {changed(e.getY, false)}
    })
    // make tooltips appear in the same location onscreen as they do
    // for the parent Slider
    override def getToolTipLocation(e: MouseEvent) = {
      val loc = slider.getToolTipLocation(e)
      if (loc != null) {
        val newLoc = new Point(loc)
        newLoc.translate(-getX, -getY)
        newLoc
      } else null
    }
    private def changed(mouseY: Int, buttonReleased: Boolean) {
      handlePositionChanged(mouseY + getY - getHeight / 2, buttonReleased)
    }
  }
}
