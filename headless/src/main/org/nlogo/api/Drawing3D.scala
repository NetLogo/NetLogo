// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Drawing3D {
  def lines: java.lang.Iterable[DrawingLine3D]
  def turtleStamps: java.lang.Iterable[TurtleStamp3D]
  def linkStamps: java.lang.Iterable[LinkStamp3D]
}
