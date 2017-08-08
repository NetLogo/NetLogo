// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.model.{ Element, XMLProviderTest }

import scala.xml.{ Elem, Null, TopScope, XML }

class ScalaXmlAdapterTest extends XMLProviderTest {
  def newElementFactory = ScalaXmlElementFactory
  def emptyElement(tag: String): Element = new ScalaXmlElement(Elem(null, tag, Null, TopScope, true))
  def parseXMLString(s: String): Element = new ScalaXmlElement(XML.loadString(s))
}
