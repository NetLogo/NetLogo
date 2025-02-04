// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.nlogo.api.{ AggregateDrawingInterface, XMLElement }

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
  def setDt(dt: Double) {
    // it doesn't make sense for dt to be <= 0
    if(dt <= 0.0)
      throw new Model.ModelException("dt cannot be less than or equal to 0.")
    this.dt = dt
  }
  def addElement(element: ModelElement) {
    elements += element
  }
  @throws(classOf[Model.ModelException])
  def addElement(element: EvaluatedModelElement) {
    if(elementWithName(element.name) != null)
      throw new Model.ModelException("Already element with named " + element.name)
    elements += element
  }
  def removeElement(element: ModelElement) {
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

        stock.setName(el("name"))
        stock.setInitialValueExpression(text)
        stock.setNonNegative(!el("allowNegative").toBoolean)

        addElement(stock)

        (refs :+ stock, conns)

      case ((refs, conns), el @ XMLElement("converter", _, text, _)) =>
        val converter = new Converter

        converter.setName(el("name"))
        converter.setExpression(text)

        addElement(converter)

        (refs :+ converter, conns)

      case ((refs, conns), el @ XMLElement("rate", _, text, _)) =>
        val rate = new Rate

        rate.setName(el("name"))
        rate.setExpression(text)

        addElement(rate)

        (refs :+ rate, conns + (rate -> ((el("startFigure").toInt, el("endFigure").toInt))))

      case ((refs, conns), el @ XMLElement("reservoir", _, _, _)) =>
        val reservoir = new Reservoir

        (refs :+ reservoir, conns)

      case ((refs, conns), el @ XMLElement("binding", _, _, _)) =>
        (refs :+ null, conns)

      case ((refs, conns), el @ XMLElement(otherName, _, _, _)) =>
        throw new Exception(s"Unable to deserialize SDM node with name: ${otherName}")

    }

    conns.foreach {
      case (rate, conn) =>
        rate.setSource(refs(conn._1).asInstanceOf[Stock])
        rate.setSink(refs(conn._2).asInstanceOf[Stock])
    }

    this
  }

  def write(): XMLElement =
    xmlElement
}
