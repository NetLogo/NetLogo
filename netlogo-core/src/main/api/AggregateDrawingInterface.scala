// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait AggregateDrawingInterface {
  def read(element: XMLElement): AnyRef
  def write(): XMLElement
}
