// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.util.{ ArrayList, LinkedHashMap, List => JList, Map => JMap }
import collection.JavaConverters._

final class Program(val interfaceGlobals: JList[String], val is3D: Boolean) {

  def this(is3D: Boolean) = this(new ArrayList[String], is3D)

  val globals: JList[String] = new ArrayList[String]
  for(s <- AgentVariables.getImplicitObserverVariables)
    globals.add(s)
  for(s <- interfaceGlobals.asScala)
    globals.add(s.toUpperCase)

  val turtlesOwn: JList[String] = new ArrayList[String]
  for(s <- AgentVariables.getImplicitTurtleVariables(is3D))
    turtlesOwn.add(s)

  val patchesOwn: JList[String] = new ArrayList[String]
  for(s <- AgentVariables.getImplicitPatchVariables(is3D))
    patchesOwn.add(s)

  val linksOwn: JList[String] = new ArrayList[String]
  for(s <- AgentVariables.getImplicitLinkVariables)
    linksOwn.add(s)

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
    val buf = new StringBuilder
    def list(xs: JList[_]) =
      xs.asScala.mkString("[", " ", "]")
    def map(xs: JMap[_, _]) =
      xs.asScala.map{case (k, v) => k.toString + " = " + v.toString}.mkString("", "\n", "\n").trim
    buf ++= "globals " + list(globals) + "\n"
    buf ++= "interfaceGlobals " + list(interfaceGlobals) + "\n"
    buf ++= "turtles-own " + list(turtlesOwn) + "\n"
    buf ++= "patches-own " + list(patchesOwn) + "\n"
    buf ++= "links-own " + list(linksOwn) + "\n"
    buf ++= "breeds " + map(breeds) + "\n"
    buf ++= "breeds-own " + map(breedsOwn) + "\n"
    buf ++= "link-breeds " + map(linkBreeds) + "\n"
    buf ++= "link-breeds-own " + map(linkBreedsOwn) + "\n"
    buf.toString
  }

}
