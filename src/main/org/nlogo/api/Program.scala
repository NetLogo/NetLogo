// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import collection.immutable.ListMap
import collection.JavaConverters._

object Program {
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
  def empty(is3D: Boolean = false) =
    if (is3D) empty3D else empty2D
}

case class Program private(
  is3D: Boolean = false,
  interfaceGlobals: Seq[String] = Seq(),
  userGlobals: Seq[String] = Seq(),
  turtlesOwn: Seq[String] = Seq(),
  patchesOwn: Seq[String] = Seq(),
  linksOwn: Seq[String] = Seq(),
  // use ListMaps here so Renderer can retrieve breeds in order of definition, for proper
  // z-ordering.  keeping ordering in the other maps isn't really necessary for proper functioning,
  // but makes writing unit tests easier - ST 6/9/04, 1/19/09, 7/12/12
  // Yuck on this Either stuff -- should be cleaned up - ST 7/12/12
  breeds: ListMap[String, Either[String, AgentSet]] = ListMap(),
  breedsSingular: ListMap[String, String] = ListMap(),
  linkBreeds: ListMap[String, Either[String, AgentSet]] = ListMap(),
  linkBreedsSingular: ListMap[String, String] = ListMap(),
  breedsOwn: ListMap[String, Seq[String]] = ListMap(),
  linkBreedsOwn: ListMap[String, Seq[String]] = ListMap()) {

  def globals: Seq[String] =
    AgentVariables.getImplicitObserverVariables ++
      interfaceGlobals.map(_.toUpperCase) ++ userGlobals

  // for convenience of Java callers
  def breedsJ: java.util.Map[String, Either[String, AgentSet]] = breeds.asJava
  def breedsSingularJ: java.util.Map[String, String] = breedsSingular.asJava
  def linkBreedsJ: java.util.Map[String, Either[String, AgentSet]] = linkBreeds.asJava
  def linkBreedsSingularJ: java.util.Map[String, String] = linkBreedsSingular.asJava
  def breedsOwnJ: java.util.Map[String, Seq[String]] = breedsOwn.asJava
  def linkBreedsOwnJ: java.util.Map[String, Seq[String]] = linkBreedsOwn.asJava
  def withInterfaceGlobals(interfaceGlobals: java.util.List[String]) =
    copy(interfaceGlobals = interfaceGlobals.asScala.toSeq)

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
      "breeds " + map(breeds) + "\n" +
      "breeds-own " + map(breedsOwn) + "\n" +
      "link-breeds " + map(linkBreeds) + "\n" +
      "link-breeds-own " + map(linkBreedsOwn) + "\n"
  }

}
