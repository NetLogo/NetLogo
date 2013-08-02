package org.nlogo.engine

object Globals {

  val vars = Array[Any]()

  /*
   compiler generates call to init, which just
   tells the runtime how many globals there are.
   they are all initialized to 0
  */
  def init(n: Int): Unit = 0 until n foreach (x => vars(x) = 0)

  def getGlobal(n: Int):             Any  = vars(n)
  def setGlobal(n: Int, value: Any): Unit = vars(n) = value

}
