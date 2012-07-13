// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import collection.immutable.ListMap
import collection.JavaConverters._

object Program {
  def empty(is3D: Boolean = false) =
    if (is3D) empty3D
    else empty2D
  private val empty2D =
    Program(
      turtlesOwn = AgentVariables.getImplicitTurtleVariables(false),
      patchesOwn = AgentVariables.getImplicitPatchVariables(false),
      linksOwn = AgentVariables.getImplicitLinkVariables)
  private val empty3D =
    Program(
      is3D = true,
      turtlesOwn = AgentVariables.getImplicitTurtleVariables(true),
      patchesOwn = AgentVariables.getImplicitPatchVariables(true),
      linksOwn = AgentVariables.getImplicitLinkVariables)
}

// use ListMaps here so Renderer can retrieve breeds in order of definition, for proper
// z-ordering.  keeping ordering in the other maps isn't really necessary for proper functioning,
// but makes writing unit tests easier - ST 6/9/04, 1/19/09, 7/12/12

// Yuck on the Either stuff -- should be cleaned up - ST 7/12/12

case class Program private(
  is3D: Boolean = false,
  interfaceGlobals: Seq[String] = Seq(),
  userGlobals: Seq[String] = Seq(),
  turtlesOwn: Seq[String] = Seq(),
  patchesOwn: Seq[String] = Seq(),
  linksOwn: Seq[String] = Seq(),
  _breeds: ListMap[String, Breed] = ListMap(),
  _linkBreeds: ListMap[String, Breed] = ListMap()) {

  def globals: Seq[String] =
    AgentVariables.getImplicitObserverVariables ++
      interfaceGlobals.map(_.toUpperCase) ++ userGlobals

  // these six methods are for backwards compatibility with code that
  // doesn't know about the new api.Breed class yet - ST 7/13/12
  def breeds: ListMap[String, Either[String, AgentSet]] =
    _breeds.map{case (name, breed) =>
      name -> Option(breed.agents).toRight(name)}
  def breedsOwn: ListMap[String, Seq[String]] =
    _breeds.map{case (name, breed) =>
      name -> breed.owns}
  def linkBreeds: ListMap[String, Either[String, AgentSet]] =
    _linkBreeds.map{case (name, breed) =>
      name -> Option(breed.agents).toRight(name)}
  def linkBreedsOwn: ListMap[String, Seq[String]] =
    _linkBreeds.map{case (name, breed) =>
      name -> breed.owns}

  // for convenience of Java callers
  def breedsJ: java.util.Map[String, AgentSet] =
    _breeds.collect{
      case (name, breed) if breed.agents != null =>
        name -> breed.agents}.asJava
  def linkBreedsJ: java.util.Map[String, AgentSet] =
    _linkBreeds.collect{
      case (name, breed) if breed.agents != null =>
        name -> breed.agents}.asJava

  // for testing/debugging
  def dump = {
    def seq(xs: Seq[_]) =
      xs.mkString("[", " ", "]")
    def map(xs: collection.Map[_, _]) =
      xs.map(mapEntry)
        .mkString("", "\n", "\n")
        .trim
    def mapEntry[K, V](pair: (K, V)): String =
      pair._1.toString + " = " +
        (pair._2 match {
          case xs: Seq[_] => xs.mkString("[", ", ", "]")
          case x => x.toString
        })
    "globals " + seq(globals) + "\n" +
      "interfaceGlobals " + seq(interfaceGlobals) + "\n" +
      "turtles-own " + seq(turtlesOwn) + "\n" +
      "patches-own " + seq(patchesOwn) + "\n" +
      "links-own " + seq(linksOwn) + "\n" +
      "breeds " + map(_breeds) + "\n" +
      "link-breeds " + map(_linkBreeds) + "\n"
  }

}
