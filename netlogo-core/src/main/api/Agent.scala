// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core

/**
 * NetLogo agents include turtles, patches, links and the observer.
 */
trait Agent {

  /** Returns a displayable name of this type of agent (Turtle, Link, Patch, Observer) */
  def classDisplayName: String

  /** Returns the world object associated with this agent */
  def world: World

  /** Returns the id number of this agent.  The who number in the case of a turtle, index into the array in the case of patches */
  def id: Long

  /** Returns the kind of this agent (Turtle, Link, Patch, Observer). */
  def kind: core.AgentKind

  /** Returns the size of this agent */
  def size: Double

  /** Returns the name of the current shape, empty string in the case of patches. */
  def shape: String

  /**
   * 0-255, 0 = invisible, 255 = opaque
   */
  def alpha: Int

  /** Sets the variable in the position vn of the agent variable array to value
    * @param vn    the index into the agent variable array
    * @param value the new value for the variable
    * @throws api.LogoException
    * @throws api.AgentException If value is the wrong type for the given variable or if you try to change variables that cannot be changed
    */
  @throws(classOf[LogoException])
  @throws(classOf[AgentException])
  def setVariable(vn: Int, value: AnyRef): Unit

  /** Returns the value of the variable in the given position of the agent variable array
    * @param vn the index into the agent variable array
    */
  def getVariable(vn: Int): AnyRef

  /** Returns raw array of all agent variables.  Warning: this method is liable to change return type
    * in a future API version.
    */
  def variables: Array[AnyRef]

}
