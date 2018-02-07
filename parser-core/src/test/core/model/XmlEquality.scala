// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import
  org.scalatest.matchers.{ Matcher, MatchResult }

import
  org.nlogo.xmllib.{ DummyXml, Element },
    DummyXml.formatXml

object XmlEquality {
  def isXmlEqual(a: Element, b: Element): Boolean = {
    a.tag == b.tag &&
      a.attributes.sortBy(_.name) == b.attributes.sortBy(_.name) &&
      a.children.length == b.children.length &&
      (a.children zip b.children).forall {
        case (a1: Element, b1: Element) => isXmlEqual(a1, b1)
        case (a1, b1) => a1 == b1
      }
  }
}

trait XmlEquality {
  class XmlElemEqualsMatcher(xml: Element) extends Matcher[Element] {
    def format(e: Element): String = formatXml(e)

    def isXmlEqual(a: Element, b: Element): Boolean =
      XmlEquality.isXmlEqual(a, b)

    def apply(other: Element) = {
      MatchResult(
        isXmlEqual(xml, other),
        s"""${format(xml)} was not the same as ${format(other)}""",
        s"""${format(xml)} was the same as ${format(other)}"""
      )
    }
  }

  def beXmlEqualTo(xml: Element) = new XmlElemEqualsMatcher(xml)
}
