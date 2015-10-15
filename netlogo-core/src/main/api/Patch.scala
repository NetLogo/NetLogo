// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/** Provides access to NetLogo patches. */
trait Patch extends Agent {

  /** Returns the value of the <code>pxcor</code> variable. */
  def pxcor: Int

  /** Returns the value of the <code>pycor</code> variable. */
  def pycor: Int

  /** Returns the value of the <code>label</code> variable. */
  def labelString: String

  /** Returns the value of the <code>label-color</code> variable. */
  def labelColor: AnyRef

  /** Returns true if the <code>label</code> variable contains something other than an empty string. */
  def hasLabel: Boolean

  /** Returns the value of the <code>pcolor</code> variable. */
  def pcolor: AnyRef

  /**
   * Returns the patch at dx and dy from this patch
   *
   * @param dx the x offset from this patch
   * @param dy the y offset from this patch
   */
  @throws(classOf[AgentException])
  def getPatchAtOffsets(dx: Double, dy: Double): Patch

}
