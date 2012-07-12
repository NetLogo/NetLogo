// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import collection.mutable.LinkedHashMap
import collection.JavaConverters._

class Program(val interfaceGlobals: collection.immutable.Seq[String] = Nil,
              val is3D: Boolean = false) {

  val globals: collection.mutable.Buffer[String] =
    (AgentVariables.getImplicitObserverVariables ++
     interfaceGlobals.map(_.toUpperCase)).toBuffer

  val turtlesOwn: collection.mutable.Buffer[String] =
    AgentVariables.getImplicitTurtleVariables(is3D).toBuffer

  val patchesOwn: collection.mutable.Buffer[String] =
    AgentVariables.getImplicitPatchVariables(is3D).toBuffer

  val linksOwn: collection.mutable.Buffer[String] =
    AgentVariables.getImplicitLinkVariables.toBuffer

  // use a LinkedHashMap to store the breeds so that the Renderer can retrieve them in order of
  // definition, for proper z-ordering - ST 6/9/04
  // Using LinkedHashMap on the other maps isn't really necessary for proper functioning, but makes
  // writing unit tests easier - ST 1/19/09
  // Yuck on this AnyRef stuff -- should be cleaned up - ST 3/7/08, 6/17/11
  val breeds: collection.mutable.Map[String, AnyRef] = new LinkedHashMap[String, AnyRef]
  val breedsSingular: collection.mutable.Map[String, String] = new LinkedHashMap[String, String]
  val linkBreeds: collection.mutable.Map[String, AnyRef] = new LinkedHashMap[String, AnyRef]
  val linkBreedsSingular: collection.mutable.Map[String, String] = new LinkedHashMap[String, String]
  val breedsOwn: collection.mutable.Map[String, Seq[String]] = new LinkedHashMap[String, Seq[String]]
  val linkBreedsOwn: collection.mutable.Map[String, Seq[String]] = new LinkedHashMap[String, Seq[String]]

  // for convenience of Java callers
  def this(interfaceGlobals: java.util.List[String], is3D: Boolean) =
    this(interfaceGlobals.asScala.toList, is3D)
  def breedsJ: java.util.Map[String, AnyRef] = breeds.asJava
  def breedsSingularJ: java.util.Map[String, String] = breedsSingular.asJava
  def linkBreedsJ: java.util.Map[String, AnyRef] = linkBreeds.asJava
  def linkBreedsSingularJ: java.util.Map[String, String] = linkBreedsSingular.asJava
  def breedsOwnJ: java.util.Map[String, Seq[String]] = breedsOwn.asJava
  def linkBreedsOwnJ: java.util.Map[String, Seq[String]] = linkBreedsOwn.asJava

  // for debugging
  def dump = {
    def seq(xs: Seq[_]) =
      xs.mkString("[", " ", "]")
    def map(xs: collection.Map[_, _]) =
      xs.map{case (k, v) => k.toString + " = " + v.toString}
        .mkString("", "\n", "\n")
        .trim
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
