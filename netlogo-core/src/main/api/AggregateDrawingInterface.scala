// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.XMLElement

trait AggregateDrawingInterface {
  def read(element: XMLElement): AnyRef
  def write(): XMLElement
}
