// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.XMLElement

trait NLogoXMLWriter {
  def startDocument(): Unit
  def endDocument(): Unit
  def startElement(e: String): Unit
  def endElement(e: String): Unit
  def attribute(name: String, value: String): Unit
  def escapedText(text: String): Unit
  def element(el: XMLElement): Unit
  def close(): Unit
}
