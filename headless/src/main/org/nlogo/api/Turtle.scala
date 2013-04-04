// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Interface provides access to NetLogo turtles.
 */
trait Turtle extends Agent {

  /** Returns the value of the <code>xcor</code> variable. */
  def xcor: Double

  /** Returns the value of the <code>ycor</code> variable. */
  def ycor: Double

  /** Returns the value of the <code>heading</code> variable. */
  def heading: Double

  /**
   * Sets the value of the <code>heading</code> variable
   *
   * @param d the new heading
   */
  def heading(d: Double)

  /** Returns the name of the current shape */
  def shape: String

  /** Returns the value of the <code>hidden?</code> variable. */
  def hidden: Boolean

  /** Returns the <code>line-thickness</code>. */
  def lineThickness: Double

  /** Returns true if the <code>label</code> variable has a non-empty string value. */
  def hasLabel: Boolean

  /** Returns the value of the <code>color</code> variable, a Double or LogoList. */
  def color: AnyRef

  /** Returns the value of the <code>label</code> variable. */
  def labelString: String

  /** Returns the value of the <code>label-color</code> variable. */
  def labelColor: AnyRef

  /** Returns the breed AgentSet of this turtle, all turtles if the turtle is unbreeded. */
  def getBreed: AgentSet

  /** Returns the index of this turtle's breed. */
  def getBreedIndex: Int

  /** Returns the patch that this turtle is on. */
  def getPatchHere: Patch

  /**
   * Moves the turtle forward distance as if the command <code>jump</code> were used
   *
   * @param distance this amount to jump
   */
  @throws(classOf[AgentException])
  def jump(distance: Double)

}
