// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

class OriginConfiguration(name: String, enabled: Array[Boolean], _setValue: Array[Boolean]) {
  def getEditorEnabled(i: Int) = enabled(i)
  def setValue(i: Int) = _setValue(i)
  override def toString = name
}
