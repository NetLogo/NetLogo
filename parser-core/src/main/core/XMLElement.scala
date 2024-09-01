// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class XMLElement(val name: String, val attributes: Map[String, String], val text: String,
                      val children: List[XMLElement]) {
  def getChild(name: String): XMLElement =
    children.find(_.name == name).get
  
  def getOptionalChild(name: String): Option[XMLElement] =
    children.find(_.name == name)
}
