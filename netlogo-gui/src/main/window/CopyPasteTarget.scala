// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

// this allows arbitrary non-text components to use the Edit menu Copy/Paste actions (Isaac B 8/11/25)
trait CopyPasteTarget {
  def copy(): Unit
  def paste(): Unit
}
