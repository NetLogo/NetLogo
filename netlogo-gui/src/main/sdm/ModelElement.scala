// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import java.io.{ Serializable => JSerializable }
import scala.beans.{ BeanProperty, BooleanBeanProperty }

// Model Elements need to be serializable because they are contained in the model element figures
// which are added to the jhotdraw figures which require serializability.
class ModelElement(@BeanProperty var name: String) extends JSerializable {
  def this() = this("")
  /** Returns true if all required fields are filled in. */
  def isComplete = !name.isEmpty
}
class EvaluatedModelElement(_name: String)
  extends ModelElement(_name) with Binding.Target
{
  def this() = this("")
  @BeanProperty var expression = ""
  override def isComplete = !expression.isEmpty && !name.isEmpty
}
object Binding {
  trait Source
  trait Target
}
class Binding(@BeanProperty val source: Binding.Source, @BeanProperty val target: Binding.Target)
class Stock(_name: String) extends ModelElement(_name) with Binding.Source {
  def this() = this("")
  @BeanProperty var initialValueExpression = ""
  @BooleanBeanProperty var nonNegative = false
  override def isComplete =
    !initialValueExpression.isEmpty && !name.isEmpty
}
class Reservoir extends Stock
class Converter extends EvaluatedModelElement with Binding.Source
class Rate(_name: String)
  extends EvaluatedModelElement(_name)
  with Binding.Source with Binding.Target
{
  def this() = this("")
  /**
   * The source stock for this Rate.  The value of the Rate is deducted from this stock for each
   * cycle of the model. */
  @BeanProperty var source: Stock = null
  /**
   * The sink stock for this Rate.  The value of the Rate is added to this Stock for each cycle of
   * the model. */
  @BeanProperty var sink: Stock = null
  /**
   * If the Rate is bivalent, a negative value is allowed.  In the case of a negative value, the
   * source Stock will be increased and the sink Stock decreased by the absolute value of the Rate
   * for a given cycle of the model. */
  @BooleanBeanProperty var bivalent = false
}
