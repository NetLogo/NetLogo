package org.nlogo.api;

/**
 * Interface provides access to NetLogo patches.
 */
public interface Patch extends Agent {
  /**
   * Returns the value of the <code>pxcor</code> variable
   */
  int pxcor();

  /**
   * Returns the value of the <code>pycor</code> variable
   */
  int pycor();

  /**
   * Returns the value of the <code>label</code> variable
   */
  String labelString();

  /**
   * Returns the value of the <code>label-color</code> variable
   */
  Object labelColor();

  /**
   * Returns true if the <code>label</code> variable contains something other than an empty string
   */
  boolean hasLabel();

  /**
   * Returns the value of the <code>pcolor</code> variable
   */
  Object pcolor();

  /**
   * Returns the patch at dx and dy from this patch
   *
   * @param dx the x offset from this patch
   * @param dy the y offset from this patch
   */
  Patch getPatchAtOffsets(double dx, double dy) throws AgentException;
}
