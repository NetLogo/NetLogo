// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait ViewSettings {
  def fontSize: Int
  def patchSize: Double
  def viewWidth: Double
  def viewHeight: Double
  def perspective: Perspective
  def viewOffsetX: Double
  def viewOffsetY: Double
  def drawSpotlight: Boolean
  def renderPerspective: Boolean
  def isHeadless: Boolean
}
