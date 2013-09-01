package org.nlogo.tortoise.engine

object Globals {

  val vars = Array[AnyJS]()

  // Used by the compiler to reserve space for all global variables and initialize them to 0 --JAB (8/3/13)
  def init(n: Int): Unit = 0 until n foreach (x => vars(x) = 0)

  def getGlobal(n: Int):               AnyJS = vars(n)
  def setGlobal(n: Int, value: AnyJS): Unit  = vars(n) = value

}
