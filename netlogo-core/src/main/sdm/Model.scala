// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.nlogo.api.AggregateDrawingInterface
import org.nlogo.core.XMLElement

import scala.collection.mutable.ListBuffer

object Model {
  class ModelException(message: String) extends Exception(message)
}
class Model(modelName: String,
  var dt: Double,
  val elements: ListBuffer[ModelElement] = new ListBuffer[ModelElement],
  val serializedGUI: String = "") extends ModelElement(modelName) with AggregateDrawingInterface {

  def this(modelName: String, dt: Double) = this(modelName, dt, new ListBuffer[ModelElement], "")
  def this() = this("default", 1)

  var xmlElement: XMLElement = null

  def getDt = dt
  @throws(classOf[Model.ModelException])
  def setDt(dt: Double): Unit = {
    // it doesn't make sense for dt to be <= 0
    if(dt <= 0.0)
      throw new Model.ModelException("dt cannot be less than or equal to 0.")
    this.dt = dt
  }
  def addElement(element: ModelElement): Unit = {
    elements += element
  }
  @throws(classOf[Model.ModelException])
  def addElement(element: EvaluatedModelElement): Unit = {
    if(elementWithName(element.name) != null)
      throw new Model.ModelException("Already element with named " + element.name)
    elements += element
  }
  def removeElement(element: ModelElement): Unit = {
    elements -= element
  }
  def elementWithName(name: String): ModelElement =
    elements.find(_.name == name).orNull

  def copy(modelName: String = modelName,
    dt: Double = dt,
    elements: ListBuffer[ModelElement] = elements,
    serializedGUI: String = serializedGUI) =
      new Model(name, dt, elements, serializedGUI)

  def read(element: XMLElement): AnyRef = {
    xmlElement = element

    setDt(element("dt").toDouble)

    val (refs, conns) = element.children.foldLeft((Seq[ModelElement](), Map[Rate, (Int, Int)]())) {
      case ((refs, conns), el @ XMLElement("stock", _, text, _)) =>
        val stock = new Stock

        stock.name = el("name")
        stock.initialValueExpression = text
        stock.nonNegative = !el("allowNegative").toBoolean

        addElement(stock)

        (refs :+ stock, conns)

      case ((refs, conns), el @ XMLElement("converter", _, text, _)) =>
        val converter = new Converter

        converter.name = el("name")
        converter.expression = text

        addElement(converter)

        (refs :+ converter, conns)

      case ((refs, conns), el @ XMLElement("rate", _, text, _)) =>
        val rate = new Rate

        rate.name = el("name")
        rate.expression = text

        addElement(rate)

        (refs :+ rate, conns + (rate -> ((el("startFigure").toInt, el("endFigure").toInt))))

      case ((refs, conns), el @ XMLElement("reservoir", _, _, _)) =>
        val reservoir = new Reservoir

        (refs :+ reservoir, conns)

      case ((refs, conns), el @ XMLElement("binding", _, _, _)) =>
        (refs :+ null, conns)

      case ((refs, conns), _) => (refs, conns) // ignore other figures for compatibility with other versions in the future (Isaac B 2/12/25)

    }

    conns.foreach {
      case (rate, conn) =>
        rate.source = refs(conn._1).asInstanceOf[Stock]
        rate.sink = refs(conn._2).asInstanceOf[Stock]
    }

    this
  }

  def write(): XMLElement =
    xmlElement
}
