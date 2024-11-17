// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ Color, Font, Graphics, Rectangle }
import java.util.List

import org.jhotdraw.figures.RectangleFigure
import org.jhotdraw.framework.{ FigureAttributeConstant, HandleEnumeration }
import org.jhotdraw.standard.{ HandleEnumerator, NullHandle, RelativeLocator }
import org.jhotdraw.util.{ StorableInput, StorableOutput }

import org.nlogo.api.{ Editable, Property }
import org.nlogo.sdm.{ ModelElement, Stock }
import org.nlogo.swing.{ Utils => SwingUtils }
import org.nlogo.theme.InterfaceColors

import scala.collection.JavaConverters.seqAsJavaList

class StockFigure extends RectangleFigure with ModelElementFigure with Editable {
  private var stock = new Stock

  setAttribute(FigureAttributeConstant.FILL_COLOR, InterfaceColors.STOCK_BACKGROUND)

  def getModelElement: ModelElement =
    stock

  def anyErrors: Boolean =
    false

  def error(o: Object, e: Exception) {}

  def error(key: Object): Exception =
    null

  def sourceOffset: Int =
    0

  override def draw(g: Graphics) {
    val g2d = SwingUtils.initGraphics2D(g)

    super.draw(g2d)

    if (stock != null) {
      if (!stock.isComplete)
        g.setColor(Color.RED)

      val name =
        if (stock.getName.trim.isEmpty)
          "?"
        else
          stock.getName.trim

      g.setFont(g.getFont.deriveFont(Font.BOLD))

      val height = g.getFontMetrics.getMaxAscent + g.getFontMetrics.getMaxDescent
      val width = g.getFontMetrics.stringWidth(name)

      g.drawString(name, displayBox.x + (displayBox.width - width) / 2,
                   displayBox.y + (displayBox.height - height) / 2 + g.getFontMetrics.getMaxAscent)
    }
  }

  override protected def invalidateRectangle(r: Rectangle): Rectangle = {
    val box = super.invalidateRectangle(r)

    box.grow(50, 50)

    box
  }

  override def displayBox: Rectangle = {
    val box = super.displayBox

    box.grow(12, 12)

    box
  }

  // Return no resize handles
  override def handles: HandleEnumeration = {
    new HandleEnumerator(seqAsJavaList(Seq(
      new NullHandle(this, RelativeLocator.southEast),
      new NullHandle(this, RelativeLocator.southWest),
      new NullHandle(this, RelativeLocator.northEast),
      new NullHandle(this, RelativeLocator.northWest)
    )))
  }

  override def write(dw: StorableOutput) {
    super.write(dw)

    dw.writeStorable(Wrapper.wrap(stock))
  }

  override def read(dr: StorableInput) {
    super.read(dr)

    stock = dr.readStorable.asInstanceOf[WrappedStock].stock
  }

  /// For org.nlogo.window.Editable

  def helpLink: Option[String] =
    None

  def propertySet: List[Property] =
    Properties.stock

  def classDisplayName: String =
    "Stock"

  def editFinished(): Boolean =
    true

  private var _dirty = false

  def dirty: Boolean =
    _dirty

  def nameWrapper(name: String) {
    _dirty = _dirty || stock.getName != name

    stock.setName(name)
  }

  def nameWrapper: String =
    stock.getName

  def initialValueExpressionWrapper(expression: String) {
    _dirty = _dirty || stock.getInitialValueExpression != expression

    stock.setInitialValueExpression(expression)
  }

  def initialValueExpressionWrapper: String =
    stock.getInitialValueExpression

  def allowNegative: Boolean =
    !stock.isNonNegative

  def allowNegative(allow: Boolean) {
    _dirty = _dirty || stock.isNonNegative == allow

    stock.setNonNegative(!allow)
  }
}
