package org.nlogo.tortoise

object ScalaJSLookups {

  val AgentSetObj = scalaJSObject("AgentSet")
  val GlobalsObj  = scalaJSObject("Globals")
  val OverlordObj = scalaJSObject("Overlord")
  val PatchObj    = scalaJSObject("Patch")
  val PrimsObj    = scalaJSObject("Prims")
  val TurtleObj   = scalaJSObject("Turtle")

  val WorldClass = scalaJSClass ("World")

  // Takes the name of a class in the 'org.nlogo.engine' ScalaJS package and returns the JavaScript code for referencing it
  private[tortoise] def scalaJSClass(name: String): String =
    s"""ScalaJS.classes.org\ufe33nlogo\ufe33engine\ufe33$name"""

  // Takes the name of an object in the 'org.nlogo.engine' ScalaJS package and returns the JavaScript code for referencing it
  private[tortoise] def scalaJSObject(name: String): String =
    s"""ScalaJS.modules.org\ufe33nlogo\ufe33engine\ufe33$name()"""

}
