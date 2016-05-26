// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.collection.immutable.ListMap

object Program {
  def empty() = fromDialect(NetLogoCore)

  def fromDialect(dialect: Dialect) =
    Program(
      turtleVars = dialect.agentVariables.implicitTurtleVariableTypeMap,
      patchVars  = dialect.agentVariables.implicitPatchVariableTypeMap,
      linkVars   = dialect.agentVariables.implicitLinkVariableTypeMap,
      dialect    = dialect)
}

// breeds are ListMaps so the z-order in Renderer can match the definition order
// - ST 6/9/04, 7/12/12

case class Program private(
  interfaceGlobals: Seq[String] = Seq(),
  userGlobals: Seq[String] = Seq(),
  turtleVars: ListMap[String, Int] = ListMap(),
  patchVars: ListMap[String, Int] = ListMap(),
  linkVars: ListMap[String, Int] = ListMap(),
  breeds: ListMap[String, Breed] = ListMap(),
  linkBreeds: ListMap[String, Breed] = ListMap(),
  dialect: Dialect = NetLogoCore) {

  val observerVars = dialect.agentVariables.implicitObserverVariableTypeMap

  val globals: Seq[String] =
    observerVars.keys.toSeq ++ interfaceGlobals.map(_.toUpperCase) ++ userGlobals

  val turtlesOwn = turtleVars.keys.toSeq

  val patchesOwn = patchVars.keys.toSeq

  val linksOwn   = linkVars.keys.toSeq

  // for testing/debugging
  def dump = {
    def seq(xs: Seq[_]) =
      xs.mkString("[", " ", "]")
    def map[K, V](xs: collection.Map[K, V]) =
      xs.map{case (k, v) => s"$k = $v"}
        .mkString("", "\n", "\n")
        .trim
    "globals " + seq(globals) + "\n" +
      "interfaceGlobals " + seq(interfaceGlobals) + "\n" +
      "turtles-own " + seq(turtlesOwn) + "\n" +
      "patches-own " + seq(patchesOwn) + "\n" +
      "links-own " + seq(linksOwn) + "\n" +
      "breeds " + map(breeds) + "\n" +
      "link-breeds " + map(linkBreeds) + "\n"
  }

}
