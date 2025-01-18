// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.Point

import org.jhotdraw.framework.Figure
import org.jhotdraw.standard.StandardDrawing

import org.nlogo.api.{ AggregateDrawingInterface, XMLElement }
import org.nlogo.sdm.Model

class AggregateDrawing extends StandardDrawing with AggregateDrawingInterface {

  private val model = new Model("default", 1)

  def getModel: Model =
    model

  def synchronizeModel() {
    model.elements.clear()

    val figs = figures

    while (figs.hasNextFigure) {
      figs.nextFigure match {
        case mef: ModelElementFigure if mef.getModelElement != null =>
          model.addElement(mef.getModelElement)
        case _ =>
      }
    }
  }

  override def orphan(figure: Figure): Figure = {
    figure match {
      case mef: ModelElementFigure if mef.getModelElement != null =>
        model.removeElement(mef.getModelElement)
    }

    super.orphan(figure)
  }

  def read(element: XMLElement): AnyRef = {
    model.setDt(element("dt").toDouble)

    element.children.foldLeft(Seq[Figure]()) {

      case (refs, el @ XMLElement("stock", _, _, _)) =>
        val stock = new StockFigure

        stock.nameWrapper(el("name"))
        stock.initialValueExpressionWrapper(el("initialValue"))
        stock.allowNegative(el("allowNegative").toBoolean)
        stock.displayBox( new Point(el("x").toInt, el("y").toInt)
                        , new Point(el("x").toInt + 60, el("y").toInt + 40))

        add(stock)

        refs :+ stock

      case (refs, el @ XMLElement("converter", _, _, _)) =>
        val converter = new ConverterFigure

        converter.nameWrapper(el("name"))
        converter.expressionWrapper(el("expression"))
        converter.displayBox( new Point(el("x").toInt, el("y").toInt)
                            , new Point(el("x").toInt + 50, el("y").toInt + 50))

        add(converter)

        refs :+ converter

      case (refs, el @ XMLElement("reservoir", _, _, _)) =>
        val reservoir = new ReservoirFigure

        reservoir.displayBox( new Point(el("x").toInt, el("y").toInt)
                            , new Point(el("x").toInt + 30, el("y").toInt + 30))

        add(reservoir)

        refs :+ reservoir

      case (refs, el @ XMLElement("binding", _, _, _)) =>
        val binding = new BindingConnection

        val start = refs(el("startFigure").toInt)
        val end = refs(el("endFigure").toInt)

        binding.startPoint(start.center.x, start.center.y)
        binding.endPoint(end.center.x, end.center.y)

        binding.connectStart(start.connectorAt(start.center.x, start.center.y))
        binding.connectEnd(end.connectorAt(end.center.x, end.center.y))

        binding.updateConnection()

        add(binding)

        refs :+ binding

      case (refs, el @ XMLElement("rate", attrs, _, _)) =>

        val rate = new RateConnection

        rate.nameWrapper(el("name"))
        rate.expressionWrapper(el("expression"))
        rate.bivalentWrapper(el("bivalent").toBoolean)

        val start = refs(el("startFigure").toInt)
        val end = refs(el("endFigure").toInt)

        rate.startPoint(start.center.x, start.center.y)
        rate.endPoint(end.center.x, end.center.y)

        rate.connectStart(start.connectorAt(start.center.x, start.center.y))
        rate.connectEnd(end.connectorAt(end.center.x, end.center.y))

        add(rate)

        refs :+ rate

      case (refs, el @ XMLElement(otherName, _, _, _)) =>
        throw new Exception(s"Unable to deserialize SDM node with name: ${otherName}")

    }

    this
  }

  def write(): XMLElement = {
    type Kids = Seq[XMLElement]
    type Refs = Map[Figure, Int]

    def processFigure(refs: Refs): PartialFunction[Figure, (String, Map[String, String])] = {

      case stock: StockFigure =>
        val attributes =
          Map( "name"          -> stock.nameWrapper
             , "initialValue"  -> stock.initialValueExpressionWrapper
             , "allowNegative" -> stock.allowNegative.toString
             , "x"             -> (stock.displayBox.x + 12).toString
             , "y"             -> (stock.displayBox.y + 12).toString
             )
        ("stock", attributes)

      case converter: ConverterFigure =>
        val attributes =
          Map( "name"       -> converter.nameWrapper
             , "expression" -> converter.expressionWrapper
             , "x"          -> converter.displayBox.x.toString
             , "y"          -> converter.displayBox.y.toString
             )
        ("converter", attributes)

      case reservoir: ReservoirFigure =>
        val attributes =
          Map( "x" -> reservoir.displayBox.x.toString
             , "y" -> reservoir.displayBox.y.toString
             )
        ("reservoir", attributes)

      case binding: BindingConnection =>
        val attributes =
          Map( "startFigure" -> refs(binding.startFigure).toString
             , "endFigure"   -> refs(binding.endFigure).toString
             )
        ("binding", attributes)

      case rate: RateConnection =>
        val attributes =
          Map( "name"        -> rate.nameWrapper
             , "expression"  -> rate.expressionWrapper
             , "bivalent"    -> rate.bivalentWrapper.toString
             , "startFigure" -> refs(rate.startFigure).toString
             , "endFigure"   -> refs(rate.endFigure).toString
             )

        ("rate", attributes)

    }

    val figs = figures

    def iterateFigures: PartialFunction[(Kids, Refs), (Kids, Refs)] = {
      case (children: Kids, refs: Refs) =>
        if (figs.hasNextFigure) {
          val figure            = figs.nextFigure
          val (elemType, attrs) = processFigure(refs)(figure)
          val cs                = children :+ XMLElement(elemType, attrs, "", Seq())
          val rs                = refs + (figure -> refs.size)
          iterateFigures((cs, rs))
        } else {
          (children, refs)
        }
    }

    val attributes    = Map("dt" -> model.dt.toString)
    val (children, _) = iterateFigures((Seq(), Map()))

    XMLElement("systemDynamics", attributes, "", children)
  }

}
