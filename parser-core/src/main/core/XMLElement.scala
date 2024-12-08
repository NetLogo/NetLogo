// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object XMLElement {
  val CDATA_ESCAPE: String = 0xe000.asInstanceOf[Char].toString
}

case class XMLElement(val name: String, val attributes: Map[String, String], val text: String,
                      val children: List[XMLElement]) {
  def apply(attribute: String): String =
    attributes(attribute)

  def apply(attribute: String, default: String): String =
    attributes.getOrElse(attribute, default)

  def get(attribute: String): Option[String] =
    attributes.get(attribute)

  def getChild(name: String): XMLElement =
    children.find(_.name == name).get

  def getOptionalChild(name: String): Option[XMLElement] =
    children.find(_.name == name)

  def getChildren(name: String): List[XMLElement] =
    children.filter(_.name == name)
}
