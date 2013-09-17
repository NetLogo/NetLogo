package org.nlogo.tortoise.adt

class SetJS(xs: ArrayJS[String]) {

  private val dict = Dictionary(AnyJS.toArray(xs.E map (x => x -> true)): _*)

  def apply(x: String): Boolean = dict(x).asInstanceOf[Boolean] == true

}

object SetJS {
  def apply(xs: ArrayJS[String]): SetJS = new SetJS(xs)
}
