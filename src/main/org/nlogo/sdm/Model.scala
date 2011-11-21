// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm
object Model {
  class ModelException(message: String) extends Exception(message)
  // here comes kludginess!  we don't want the classes in org.nlogo.sdm to depend on JHotDraw, but
  // if they don't depend on JHotDraw, then JHotDraw's StorableInput stuff can't read them.  But in
  // our old save format, the org.nlogo.sdm class names are hardcoded, so to speak.  In order to
  // resolve this dilemma, we're going to preprocess the saved data and replace the names of the
  // org.nlogo.sdm classes with wrapper classes that implement Storable.  The wrapper objects will
  // have the real objects, the org.nlogo.sdm objects, stored inside them, and the read methods on
  // the GUI classes can do the unwrapping.  When saving, now, we output the wrapper class names
  // instead of the original class names. - ST 1/27/05
  def mungeClassNames(text: String) =
    text.replaceAll(" *org.nlogo.sdm.Stock ",
                    "org.nlogo.sdm.gui.WrappedStock ")
        .replaceAll(" *org.nlogo.sdm.Rate ",
                    "org.nlogo.sdm.gui.WrappedRate ")
        .replaceAll(" *org.nlogo.sdm.Reservoir ",
                    "org.nlogo.sdm.gui.WrappedReservoir")
        .replaceAll(" *org.nlogo.sdm.Converter ",
                    "org.nlogo.sdm.gui.WrappedConverter")
}
import scala.reflect.BeanProperty
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
