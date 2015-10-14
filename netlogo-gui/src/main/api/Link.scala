// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Interface provides access to NetLogo links.
 */
trait Link extends Agent {

  /**
   * Returns the first end point of this link.  If the link is directed this is the source
   * turtle if the link is undirected it is the turtle with the lower who number.
   */
  def end1: Turtle

  /**
   * Returns the second end point of this link.  If the link is directed this is the destination
   * turtle if the link is undirected it is the turtle with the higher who number.
   */
  def end2: Turtle

  /**
   * Returns the breed AgentSet associated with this link.  Iif the link is unbreeded returns the
   * all links AgentSet.
   */
  def getBreed: AgentSet

  /** Returns the x-coordinate of the midpoint of this link taking wrapping in account. */
  def midpointX: Double

  /** Returns the y-coordinate of the midpoint of this link taking wrapping in account. */
  def midpointY: Double

  /** Returns the x-coordinate of end1. */
  def x1: Double

  /** Returns the y-coordinate of end1. */
  def y1: Double

  /** Returns the x-coordinate of end2.  This coordinate is "unwrapped" so it may actually be outside the bounds of the world. */
  def x2: Double

  /** Returns the y-coordinate of end2.  This coordinate is "unwrapped" so it may actually be outside the bounds of the world. */
  def y2: Double

  /** Returns true if this link is directed. */
  def isDirectedLink: Boolean

  /** Returns the heading towards end2 from end1. */
  def heading: Double

  /** Returns the size of end2. */
  def linkDestinationSize: Double

  /** Returns the value of the <code>hidden?</code> variable. */
  def hidden: Boolean

  /** Returns the value of the <code>thinkness</code> variable. */
  def lineThickness: Double

  /** Returns true if there is a value in the <code>label</code> variable. */
  def hasLabel: Boolean

  /** Returns the value of the <code>color</code> variable, a Double or LogoList. */
  def color: AnyRef

  /** Returns the value of the <code>label</code> variable. */
  def labelString: String

  /** Returns the value of the <code>label-color</code> variable. */
  def labelColor: AnyRef

  /** Returns the index of the breed of this link. */
  def getBreedIndex: Int

}
