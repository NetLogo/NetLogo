// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Camera {
  def dist: Double
  def heading: Double
  def pitch: Double
  def roll: Double
  def ozcor: Double
  def dx: Double
  def dy: Double
  def dz: Double
}
