// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.Point

import org.jhotdraw.framework.Figure
import org.jhotdraw.standard.StandardDrawing

import org.nlogo.api.AggregateDrawingInterface
import org.nlogo.sdm.Model
import org.nlogo.core.model.XMLElement

class AggregateDrawing extends StandardDrawing with AggregateDrawingInterface {

  private val model = new Model("default", 1)

  def getModel: Model =
    model

  def synchronizeModel(): Unit = {
    model.elements.clear()

    val figs = figures

    while (figs.hasNextFigure) {
      figs.nextFigure match {
        case f: ModelElementFigure if f.getModelElement != null =>
          model.addElement(f.getModelElement)
        case _ =>
      }
    }
  }

  override def orphan(figure: Figure): Figure = {
    figure match {
      case f: ModelElementFigure if f.getModelElement != null =>
        model.removeElement(f.getModelElement)
      case _ =>
    }

    super.orphan(figure)
  }

  def read(element: XMLElement): AnyRef = {
    model.setDt(element("dt").toDouble)

    val (refs, conns) = element.children.foldLeft((Seq[Figure](), Map[Figure, (Int, Int)]())) {

      case ((refs, conns), el @ XMLElement("stock", _, text, _)) =>
        val stock = new StockFigure

        stock.nameWrapper(el("name"))
        stock.initialValueExpressionWrapper(text)
        stock.allowNegative(el("allowNegative").toBoolean)
        stock.displayBox( new Point(el("x").toInt, el("y").toInt)
                        , new Point(el("x").toInt + 60, el("y").toInt + 40))

        add(stock)

        (refs :+ stock, conns)

      case ((refs, conns), el @ XMLElement("converter", _, text, _)) =>
        val converter = new ConverterFigure

        converter.nameWrapper(el("name"))
        converter.expressionWrapper(text)
        converter.displayBox( new Point(el("x").toInt, el("y").toInt)
                            , new Point(el("x").toInt + 50, el("y").toInt + 50))

        add(converter)

        (refs :+ converter, conns)

      case ((refs, conns), el @ XMLElement("reservoir", _, _, _)) =>
        val reservoir = new ReservoirFigure

        reservoir.displayBox( new Point(el("x").toInt, el("y").toInt)
                            , new Point(el("x").toInt + 30, el("y").toInt + 30))

        add(reservoir)

        (refs :+ reservoir, conns)

      case ((refs, conns), el @ XMLElement("binding", _, _, _)) =>
        val binding = new BindingConnection

        add(binding)

        (refs :+ binding, conns + (binding -> ((el("startFigure").toInt, el("endFigure").toInt))))

      case ((refs, conns), el @ XMLElement("rate", _, text, _)) =>

        val rate = new RateConnection

        rate.nameWrapper(el("name"))
        rate.expressionWrapper(text)
        rate.bivalentWrapper(el("bivalent").toBoolean)

        add(rate)

        (refs :+ rate, conns + (rate -> ((el("startFigure").toInt, el("endFigure").toInt))))

      case ((refs, conns), _) => (refs, conns) // ignore other figures for compatibility with other versions in the future (Isaac B 2/12/25)

    }

    conns.foreach {
      case (rate: RateConnection, conns) =>
        val start = refs(conns._1)
        val end = refs(conns._2)

        rate.startPoint(start.center.x, start.center.y)
        rate.endPoint(end.center.x, end.center.y)

        rate.connectStart(start.connectorAt(start.center.x, start.center.y))
        rate.connectEnd(end.connectorAt(end.center.x, end.center.y))

      case _ =>
    }

    conns.foreach {
      case (binding: BindingConnection, conns) =>
        val start = refs(conns._1)
        val end = refs(conns._2)

        binding.startPoint(start.center.x, start.center.y)
        binding.endPoint(end.center.x, end.center.y)

        binding.connectStart(start.connectorAt(start.center.x, start.center.y))
        binding.connectEnd(end.connectorAt(end.center.x, end.center.y))

      case _ =>
    }

    this
  }

  def write(): XMLElement = {
    type Kids = Seq[XMLElement]

    var figs = figures

    def buildRefs(refs: Map[Figure, Int]): Map[Figure, Int] = {
      if (figs.hasNextFigure)
        buildRefs(refs + (figs.nextFigure -> refs.size))
      else
        refs
    }

    val refs = buildRefs(Map())

    def processFigure(figure: Figure): XMLElement = {

      figure match {

        case stock: StockFigure =>
          val attributes =
            Map( "name"          -> stock.nameWrapper
              , "allowNegative" -> stock.allowNegative.toString
              , "x"             -> (stock.displayBox.x + 12).toString
              , "y"             -> (stock.displayBox.y + 12).toString
              )
          XMLElement("stock", attributes, stock.initialValueExpressionWrapper, Seq())

        case converter: ConverterFigure =>
          val attributes =
            Map( "name"       -> converter.nameWrapper
              , "x"          -> converter.displayBox.x.toString
              , "y"          -> converter.displayBox.y.toString
              )
          XMLElement("converter", attributes, converter.expressionWrapper, Seq())

        case reservoir: ReservoirFigure =>
          val attributes =
            Map( "x" -> reservoir.displayBox.x.toString
              , "y" -> reservoir.displayBox.y.toString
              )
          XMLElement("reservoir", attributes, "", Seq())

        case binding: BindingConnection =>
          val attributes =
            Map( "startFigure" -> refs(binding.startFigure).toString
              , "endFigure"   -> refs(binding.endFigure).toString
              )
          XMLElement("binding", attributes, "", Seq())

        case rate: RateConnection =>
          val attributes =
            Map( "name"        -> rate.nameWrapper
              , "bivalent"    -> rate.bivalentWrapper.toString
              , "startFigure" -> refs(rate.startFigure).toString
              , "endFigure"   -> refs(rate.endFigure).toString
              )
          XMLElement("rate", attributes, rate.expressionWrapper, Seq())

        case _ =>
          throw new IllegalStateException

      }

    }

    figs = figures

    def iterateFigures: PartialFunction[Kids, Kids] = {
      case (kids: Kids) =>
        if (figs.hasNextFigure)
          iterateFigures(kids :+ processFigure(figs.nextFigure))
        else
          kids
    }

    val attributes    = Map("dt" -> model.dt.toString)
    val children = iterateFigures(Seq())

    XMLElement("systemDynamics", attributes, "", children)
  }

}
