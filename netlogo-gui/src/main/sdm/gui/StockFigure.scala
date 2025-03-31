// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ Color, Font, Graphics, Rectangle }

import org.jhotdraw.figures.RectangleFigure
import org.jhotdraw.framework.{ FigureAttributeConstant, HandleEnumeration }
import org.jhotdraw.standard.{ HandleEnumerator, NullHandle, RelativeLocator }
import org.jhotdraw.util.{ StorableInput, StorableOutput }

import org.nlogo.sdm.{ ModelElement, Stock }
import org.nlogo.swing.{ Utils => SwingUtils }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.{ Editable, EditPanel }

import scala.collection.JavaConverters.seqAsJavaList

class StockFigure extends RectangleFigure with ModelElementFigure with Editable {
  private var stock: Option[Stock] = Some(new Stock)

  private var _dirty = false

  setAttribute(FigureAttributeConstant.FILL_COLOR, InterfaceColors.stockBackground())

  def getModelElement: ModelElement =
    stock.orNull

  def anyErrors: Boolean =
    false

  def error(o: Object, e: Exception): Unit = {}

  def error(key: Object): Exception =
    null

  def sourceOffset: Int =
    0

  override def draw(g: Graphics): Unit = {
    val g2d = SwingUtils.initGraphics2D(g)

    super.draw(g2d)

    stock.foreach(s => {
      if (!s.isComplete)
        g.setColor(Color.RED)

      val name =
        if (s.getName.trim.isEmpty) {
          "?"
        } else {
          s.getName.trim
        }

      g.setFont(g.getFont.deriveFont(Font.BOLD))

      val height = g.getFontMetrics.getMaxAscent + g.getFontMetrics.getMaxDescent
      val width = g.getFontMetrics.stringWidth(name)

      g.drawString(name, displayBox.x + (displayBox.width - width) / 2,
                   displayBox.y + (displayBox.height - height) / 2 + g.getFontMetrics.getMaxAscent)
    })
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

  override def write(dw: StorableOutput): Unit = {
    super.write(dw)

    dw.writeStorable(Wrapper.wrap(stock.orNull))
  }

  override def read(dr: StorableInput): Unit = {
    super.read(dr)

    stock = Option(dr.readStorable.asInstanceOf[WrappedStock].stock)
  }

  /// For org.nlogo.window.Editable

  def helpLink: Option[String] =
    None

  def classDisplayName: String =
    "Stock"

  override def editPanel: EditPanel =
    null

  def editFinished(): Boolean =
    true

  def dirty: Boolean =
    _dirty

  def nameWrapper(name: String): Unit = {
    stock.foreach(s => {
      _dirty = _dirty || s.getName != name

      s.setName(name)
    })
  }

  def nameWrapper: String =
    stock.map(_.getName).orNull

  def initialValueExpressionWrapper(expression: String): Unit = {
    stock.foreach(s => {
      _dirty = _dirty || s.getInitialValueExpression != expression

      s.setInitialValueExpression(expression)
    })
  }

  def initialValueExpressionWrapper: String =
    stock.map(_.getInitialValueExpression).orNull

  def allowNegative: Boolean =
    !stock.map(_.isNonNegative).getOrElse(false)

  def allowNegative(allow: Boolean): Unit = {
    stock.foreach(s => {
      _dirty = _dirty || s.isNonNegative == allow

      s.setNonNegative(!allow)
    })
  }
}
