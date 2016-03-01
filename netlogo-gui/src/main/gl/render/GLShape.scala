// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

private[render] case class GLShape(shapeName: String, displayListIndex: Int, rotatable: Boolean = true) {
  def this(shapeName: String, displayListIndex: Int) =  // Java compat
    this(shapeName, displayListIndex, true)
  val height = 1.0f
}
