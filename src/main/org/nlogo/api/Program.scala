// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.util.{ LinkedHashMap, List => JList, Map => JMap }
import collection.JavaConverters._

class Program(val interfaceGlobals: collection.immutable.Seq[String] = Nil,
              val is3D: Boolean = false) {

  def this(interfaceGlobals: JList[String], is3D: Boolean) =
    this(interfaceGlobals.asScala.toList, is3D)

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
  val breeds: JMap[String, AnyRef] = new LinkedHashMap[String, AnyRef]
  val breedsSingular: JMap[String, String] = new LinkedHashMap[String, String]
  val linkBreeds: JMap[String, AnyRef] = new LinkedHashMap[String, AnyRef]
  val linkBreedsSingular: JMap[String, String] = new LinkedHashMap[String, String]
  val breedsOwn: JMap[String, JList[String]] = new LinkedHashMap[String, JList[String]]
  val linkBreedsOwn: JMap[String, JList[String]] = new LinkedHashMap[String, JList[String]]

  def dump = {
    def seq(xs: Seq[_]) =
      xs.mkString("[", " ", "]")
    def jmap(xs: JMap[_, _]) =
      xs.asScala
        .map{case (k, v) => k.toString + " = " + v.toString}
        .mkString("", "\n", "\n").trim
    "globals " + seq(globals) + "\n" +
      "interfaceGlobals " + seq(interfaceGlobals) + "\n" +
      "turtles-own " + seq(turtlesOwn) + "\n" +
      "patches-own " + seq(patchesOwn) + "\n" +
      "links-own " + seq(linksOwn) + "\n" +
      "breeds " + jmap(breeds) + "\n" +
      "breeds-own " + jmap(breedsOwn) + "\n" +
      "link-breeds " + jmap(linkBreeds) + "\n" +
      "link-breeds-own " + jmap(linkBreedsOwn) + "\n"
  }

}
