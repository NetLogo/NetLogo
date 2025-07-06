// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.util.hashing.MurmurHash3

object XMLElement {
  val CDataEscape = 0xe000.asInstanceOf[Char].toString
}

case class XMLElement(val name: String, val attributes: Map[String, String], val text: String,
                      val children: Seq[XMLElement]) {

  private val hash: Int = MurmurHash3.unorderedHash(Seq(name, text) ++ attributes)

  private val childHashes: Map[Int, Map[XMLElement, Int]] = {
    val grouped = children.groupBy(_.hash)
    val counts  = grouped.view.mapValues(_.foldLeft(Map[XMLElement, Int]())
                    ((acc, x) => acc + (x -> (acc.getOrElse(x, 0) + 1)))).toMap
    counts
  }

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

  def getChildren(name: String): Seq[XMLElement] =
    children.filter(_.name == name)

  // this ensures that child order is ignored when checking for semantic equivalence
  override def equals(other: Any): Boolean = {
    other match {
      case el: XMLElement =>
        name == el.name && text == el.text && attributes == el.attributes && childHashes == el.childHashes
      case _ => false
    }
  }
}
