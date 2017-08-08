// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

object DummyXML {
  case class Elem(tag: String, attributes: Seq[Attribute], children: Seq[Node]) extends Element
  case class Txt(text: String) extends Text
  case class Attr(name: String, value: String) extends Attribute

  object Factory extends ElementFactory {
    def newElement(tag: String): ElementBuilder = new Builder(tag)
  }

  class Builder(tag: String) extends ElementBuilder {
    var attributes = Seq.empty[Attr]
    var children = Seq.empty[Node]
    def withAttribute(name: String, value: String) = {
      attributes :+= Attr(name, value)
      this
    }
    def withElement(element: Element): ElementBuilder = {
      children :+= element
      this
    }
    def withText(txt: String): ElementBuilder = {
      children :+= Txt(txt)
      this
    }
    def build = Elem(tag, attributes, children)
  }

  def namedText(elemTag: String, text: String): Elem =
    Elem(elemTag, Seq(), Seq(Txt(text)))

  def formatXml(e: Element): String = {
    def formatAttr(a: Attribute): String =
      s"""${a.name}="${a.value}""""

    def format1(n: Node): Seq[String] =
      n match {
        case e: Element =>
          val attrs = e.attributes.map(formatAttr)
          val headTag =
            if (attrs.isEmpty) Seq(s"<${e.tag}>")
            else s"<${e.tag}" +: attrs.init :+ (attrs.last + ">")
          headTag ++ (e.children.flatMap(format1) :+ s"</${e.tag}>")
        case t: Text => Seq(t.text)
      }
    format1(e).mkString("\n")
  }
}
