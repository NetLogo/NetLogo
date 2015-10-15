// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait DrawingInterface {
  def colors: Array[Int]
  def isDirty: Boolean
  def isBlank: Boolean
  def markClean()
  def markDirty()
  def getWidth: Int
  def getHeight: Int
}
