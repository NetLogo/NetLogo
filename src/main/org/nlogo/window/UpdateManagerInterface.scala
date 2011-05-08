package org.nlogo.window

trait UpdateManagerInterface {
  def shouldUpdateNow: Boolean
  def shouldComeUpForAirAgain: Boolean
  def speed: Double
  def speed_=(d: Double): Unit
  def recompute(): Unit
  def pause(): Unit
  def reset(): Unit
  def pseudoTick(): Unit
  def beginPainting(): Unit
  def donePainting(): Unit
  def nudgeSleeper(): Unit
}
