// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.{ StringReader, Writer }
import javax.xml.stream.{ XMLInputFactory, XMLOutputFactory, XMLStreamConstants, XMLStreamException }

import org.nlogo.api.XMLElement

import scala.util.{ Failure, Try }
import scala.util.matching.Regex

// this wrapper around XMLStreamWriter allows for pretty-printing and other formatting (Isaac B 12/16/24)
class XMLWriter(dest: Writer) {
  private val writer = XMLOutputFactory.newFactory.createXMLStreamWriter(dest)

  private var indentLevel = 0
  private var lastStart = ""

  def startDocument() {
    writer.writeStartDocument("utf-8", "1.0")
  }

  def startElement(name: String) {
    writer.writeCharacters("\n")

    for (i <- 0 until indentLevel)
      writer.writeCharacters("\t")

    writer.writeStartElement(name)

    indentLevel += 1

    lastStart = name
  }

  def attribute(name: String, value: String) {
    writer.writeAttribute(name, value)
  }

  def cData(text: String) {
    writer.writeCharacters("\n")

    for (i <- 0 until indentLevel)
      writer.writeCharacters("\t")

    writer.writeCData(new Regex("]]>").replaceAllIn(text, "]]" + XMLElement.CDataEscape + ">"))

    lastStart = ""
  }

  def endElement(name: String) {
    indentLevel -= 1

    if (lastStart != name) {
      writer.writeCharacters("\n")

      for (i <- 0 until indentLevel)
        writer.writeCharacters("\t")
    }

    writer.writeEndElement()
  }

  def element(el: XMLElement) {
    startElement(el.name)

    for ((key, value) <- el.attributes)
      attribute(key, value)

    if (el.text.isEmpty)
      el.children.foreach(element)
    else
      cData(el.text)

    endElement(el.name)
  }

  def endDocument() {
    writer.writeEndDocument()
    writer.writeCharacters("\n")
  }

  def close() {
    writer.close()
  }
}

object XMLReader {
  def read(source: String): Try[XMLElement] = {
    val reader = XMLInputFactory.newFactory.createXMLStreamReader(new StringReader(source))

    try {
      while (reader.hasNext && reader.next != XMLStreamConstants.START_ELEMENT) {}
    }

    catch {
      case e: XMLStreamException => return Failure(new Exception(e))
    }

    def readElement(): Try[XMLElement] = {
      def parseElement(acc: XMLElement): Try[XMLElement] = {
        if (reader.hasNext)
          reader.next match {
            case XMLStreamConstants.START_ELEMENT =>
              readElement().flatMap(nexties =>
                parseElement(acc.copy(children = acc.children :+ nexties)))
            case XMLStreamConstants.END_ELEMENT =>
              Try(acc)
            case XMLStreamConstants.CHARACTERS =>
              if (reader.isWhiteSpace)
                parseElement(acc)
              else
                parseElement(acc.copy(
                  text = new Regex(s"]]${XMLElement.CDataEscape}>").replaceAllIn(reader.getText, "]]>")))
            case x =>
              Failure(throw new Exception(s"Unexpected value found while parsing XML: ${x}"))
          }
        else
          Try(acc)
      }

      val attributes =
        (0 until reader.getAttributeCount).
          map((i) => reader.getAttributeLocalName(i) -> reader.getAttributeValue(i)).
          toMap

      parseElement(XMLElement(reader.getLocalName, attributes, "", Seq[XMLElement]()))
    }

    val elementTry = readElement()

    reader.close()

    elementTry
  }
}
