// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

sealed trait Node

trait Text extends Node {
  def text: String
}

trait Element extends Node {
  def tag: String
  def attributes: Seq[Attribute]
  def children: Seq[Node]
}

trait Attribute {
  def name: String
  def value: String
}

trait ElementFactory {
  def newElement(tag: String): ElementBuilder

}

trait ElementBuilder {
  def withAttribute(name: String, value: String): ElementBuilder
  def withElement(element: Element): ElementBuilder
  def withText(text: String): ElementBuilder
  def build: Element
  def withOptionalElement(el: Option[Element]): ElementBuilder =
    el.map(withElement).getOrElse(this)
  def withOptionalAttribute(name: String, value: Option[String]): ElementBuilder =
    value.map(e => withAttribute(name, e)).getOrElse(this)
  def withDefaultAttribute(name: String, value: String, default: String): ElementBuilder =
    if (value == default) this
    else                  withAttribute(name, value)
  def withElementList(els: Seq[Element]): ElementBuilder =
    els.foldLeft(this) { case (b, el) => b.withElement(el) }
}
