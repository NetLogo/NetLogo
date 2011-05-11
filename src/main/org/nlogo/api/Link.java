package org.nlogo.api;

/**
 * Interface provides access to NetLogo links.
 */
public interface Link extends Agent {
  /**
   * Returns the first end point of this link.  If the link is directed this is the source
   * turtle if the link is undirected it is the turtle with the lower who number.
   */
  Turtle end1();

  /**
   * Returns the second end point of this link.  If the link is directed this is the destination
   * turtle if the link is undirected it is the turtle with the higher who number.
   */
  Turtle end2();

  /**
   * Returns the breed AgentSet associated with this link, if the link is unbreeded returns the
   * all links AgentSet
   */
  AgentSet getBreed();

  /**
   * Returns the x-coordinate of the midpoint of this link
   * taking wrapping in account.
   */
  double midpointX();

  /**
   * Returns the y-coordinate of the midpoint of this link
   * taking wrapping in account.
   */
  double midpointY();

  /**
   * Returns the x-coordinate of end1
   */
  double x1();

  /**
   * Returns the y-coordinate of end1
   */
  double y1();

  /**
   * Returns the x-coordinate of end2 this coordinate is "unwrapped" so
   * it may actually be outside the bounds of the world
   */
  double x2();

  /**
   * Returns the y-coordinate of end2 this coordinate is "unwrapped" so
   * it may actually be outside the bounds of the world
   */
  double y2();

  /**
   * Returns true if this link is directed
   */
  boolean isDirectedLink();

  /**
   * Returns the heading towards end2 from end1
   */
  double heading();

  /**
   * Returns the size of end2
   */
  double linkDestinationSize();

  /**
   * Returns the value of the <code>hidden?</code> variable
   */
  boolean hidden();

  /**
   * Returns the value of the <code>thinkness</code> variable
   */
  double lineThickness();

  /**
   * Returns true if there is a value in the <code>label</code> variable
   */
  boolean hasLabel();

  /**
   * Returns the value of the <code>color</code> variable
   */
  Object color();

  /**
   * Returns the value of the <code>label</code> variable
   */
  String labelString();

  /**
   * Returns the value of the <code>label-color</code> variable
   */
  Object labelColor();

  /**
   * Returns the index of the breed of this link
   */
  int getBreedIndex();
}
