// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import scala.collection.mutable.ListBuffer

object Model {
  class ModelException(message: String) extends Exception(message)
}
class Model(modelName: String,
  var dt: Double,
  val elements: ListBuffer[ModelElement] = new ListBuffer[ModelElement],
  val serializedGUI: String = "") extends ModelElement(modelName) {

  def this(modelName: String, dt: Double) = this(modelName, dt, new ListBuffer[ModelElement], "")

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
}
