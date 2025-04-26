// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait DrawingInterface {
  def colors: Array[Int]
  def isDirty: Boolean
  def isBlank: Boolean
  def markClean(): Unit
  def markDirty(): Unit
  def getWidth: Int
  def getHeight: Int
}
