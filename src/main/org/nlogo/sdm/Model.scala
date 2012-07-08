// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

object Model {
  class ModelException(message: String) extends Exception(message)
}
class Model(name: String, var dt: Double) extends ModelElement(name) {
  val elements = new collection.mutable.ListBuffer[ModelElement]
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
}
