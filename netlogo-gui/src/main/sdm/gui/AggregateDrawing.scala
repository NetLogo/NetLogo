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
        stock.displayBox( new Point(el("centerX").toInt, el("centerY").toInt)
                        , new Point(el("startX").toInt, el("startY").toInt))
        add(stock)
        refs :+ stock

      case (refs, el @ XMLElement("converter", _, _, _)) =>
        val converter = new ConverterFigure
        converter.nameWrapper(el("name"))
        converter.expressionWrapper(el("expression"))
        converter.displayBox( new Point(el("centerX").toInt, el("centerY").toInt)
                            , new Point(el("startX").toInt, el("startY").toInt))
        add(converter)
        refs :+ converter

      case (refs, el @ XMLElement("reservoir", _, _, _)) =>
        val reservoir = new ReservoirFigure
        reservoir.displayBox( new Point(el("centerX").toInt, el("centerY").toInt)
                            , new Point(el("startX").toInt, el("startY").toInt))
        add(reservoir)
        refs :+ reservoir

      case (refs, el @ XMLElement("binding", _, _, _)) =>
        val binding = new BindingConnection
        binding.displayBox( new Point(el("centerX").toInt, el("centerY").toInt)
                          , new Point(el("startX").toInt, el("startY").toInt))
        val start = refs(el("startFigure").toInt)
        val end = refs(el("endFigure").toInt)
        binding.connectStart(start.connectorAt(start.center.x, start.center.y))
        binding.connectEnd(end.connectorAt(end.center.x, end.center.y))
        add(binding)
        refs :+ binding

      case (refs, el @ XMLElement("rate", attrs, _, _)) =>

        val rate = new RateConnection

        rate.nameWrapper(el("name"))
        rate.expressionWrapper(el("expression"))
        rate.bivalentWrapper(el("bivalent").toBoolean)

        rate.startPoint(el("startX").toInt, el("startY").toInt)

        if (attrs.contains("middleX"))
          rate.insertPointAt(new Point(el("middleX").toInt, el("middleY").toInt), 1)

        rate.endPoint(el("endX").toInt, el("endY").toInt)

        val start = refs(el("startFigure").toInt)
        val end = refs(el("endFigure").toInt)

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
             , "centerX"       -> stock.center.x.toString
             , "centerY"       -> stock.center.y.toString
             , "startX"        -> stock.displayBox.x.toString
             , "startY"        -> stock.displayBox.y.toString
             )
        ("stock", attributes)

      case converter: ConverterFigure =>
        val attributes =
          Map( "name"       -> converter.nameWrapper
             , "expression" -> converter.expressionWrapper
             , "centerX"    -> converter.center.x.toString
             , "centerY"    -> converter.center.y.toString
             , "startX"     -> converter.displayBox.x.toString
             , "startY"     -> converter.displayBox.y.toString
             )
        ("converter", attributes)

      case reservoir: ReservoirFigure =>
        val attributes =
          Map( "centerX" -> reservoir.center.x.toString
             , "centerY" -> reservoir.center.y.toString
             , "startX"  -> reservoir.displayBox.x.toString
             , "startY"  -> reservoir.displayBox.y.toString
             )
        ("reservoir", attributes)

      case binding: BindingConnection =>
        val attributes =
          Map( "startX"      -> binding.startPoint.x.toString
             , "startY"      -> binding.startPoint.y.toString
             , "endX"        -> binding.endPoint.x.toString
             , "endY"        -> binding.endPoint.y.toString
             , "startFigure" -> refs(binding.startFigure).toString
             , "endFigure"   -> refs(binding.endFigure).toString
             )
        ("binding", attributes)

      case rate: RateConnection =>
        val attributes =
          Map( "name"        -> rate.nameWrapper
             , "expression"  -> rate.expressionWrapper
             , "bivalent"    -> rate.bivalentWrapper.toString
             , "startX"      -> rate.startPoint.x.toString
             , "startY"      -> rate.startPoint.y.toString
             , "endX"        -> rate.endPoint.x.toString
             , "endY"        -> rate.endPoint.y.toString
             , "startFigure" -> refs(rate.startFigure).toString
             , "endFigure"   -> refs(rate.endFigure).toString
             )

        val extras =
          if (rate.pointCount == 3)
            Map( "middleX" -> rate.pointAt(1).x.toString
               , "middleY" -> rate.pointAt(2).y.toString
               )
          else
            Map()

        ("rate", attributes ++ extras)

    }

    def iterateFigures: PartialFunction[(Kids, Refs), (Kids, Refs)] = {
      case (children: Kids, refs: Refs) =>
        if (figures.hasNextFigure) {
          val figure            = figures.nextFigure
          val (elemType, attrs) = processFigure(refs)(figure)
          val cs                = children :+ XMLElement(elemType, attrs, "", Seq())
          val rs                = refs + (figure -> refs.size)
          (cs, rs)
        } else {
          (children, refs)
        }
    }

    val attributes    = Map("dt" -> model.dt.toString)
    val (children, _) = iterateFigures((Seq(), Map()))

    XMLElement("systemDynamics", attributes, "", children)
  }

}
