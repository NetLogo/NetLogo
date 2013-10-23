// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

case class DummyShape(name: String) extends Shape {
  def getName = name
  def setName(s: String) = throw new UnsupportedOperationException
}
