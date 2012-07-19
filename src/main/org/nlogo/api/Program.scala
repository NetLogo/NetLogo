// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import collection.immutable.ListMap
import collection.JavaConverters._

object Program {
  def empty() =
    Program(
      turtlesOwn = AgentVariables.getImplicitTurtleVariables,
      patchesOwn = AgentVariables.getImplicitPatchVariables,
      linksOwn = AgentVariables.getImplicitLinkVariables)
}

// breeds are ListMaps so the z-order in Renderer can match the definition order
// - ST 6/9/04, 7/12/12

case class Program private(
  interfaceGlobals: Seq[String] = Seq(),
  userGlobals: Seq[String] = Seq(),
  turtlesOwn: Seq[String] = Seq(),
  patchesOwn: Seq[String] = Seq(),
  linksOwn: Seq[String] = Seq(),
  breeds: ListMap[String, Breed] = ListMap(),
  linkBreeds: ListMap[String, Breed] = ListMap()) {

  def globals: Seq[String] =
    AgentVariables.getImplicitObserverVariables ++
      interfaceGlobals.map(_.toUpperCase) ++ userGlobals

  // for testing/debugging
  def dump = {
    def seq(xs: Seq[_]) =
      xs.mkString("[", " ", "]")
    def map(xs: collection.Map[_, _]) =
      xs.map{case (k, v) => k + " = " + v}
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
