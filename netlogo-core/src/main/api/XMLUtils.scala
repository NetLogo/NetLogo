// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.{ StringReader, Writer }
import javax.xml.stream.{ XMLInputFactory, XMLOutputFactory, XMLStreamConstants, XMLStreamException }

import scala.util.{ Failure, Try }

// this wrapper around XMLStreamWriter allows for pretty-printing and other formatting (Isaac B 12/16/24)
class XMLWriter(dest: Writer) {
  private val writer = XMLOutputFactory.newFactory.createXMLStreamWriter(dest)

  private val indentStr = "  "

  private var indentLevel = 0
  private var lastStart = ""

  def startDocument() {
    writer.writeStartDocument("utf-8", "1.0")
    writer.writeCharacters("\n")
    writer.writeComment(" WARNING: All text sections in this file must use &lt; in place of <, &gt; in place of >, and &amp; in place of & ")
  }

  def startElement(name: String) {
    writer.writeCharacters("\n")

    for (i <- 0 until indentLevel)
      writer.writeCharacters(indentStr)

    writer.writeStartElement(name)

    indentLevel += 1

    lastStart = name
  }

  def attribute(name: String, value: String) {
    writer.writeAttribute(name, value)
  }

  def escapedText(text: String) {
    writer.writeCharacters(text)
  }

  def endElement(name: String) {
    indentLevel -= 1

    if (lastStart != name) {
      writer.writeCharacters("\n")

      for (i <- 0 until indentLevel)
        writer.writeCharacters(indentStr)
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
      escapedText(el.text)

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
    val sourceReader = new StringReader(source)
    val reader = {
      val factory = XMLInputFactory.newFactory

      factory.setProperty("javax.xml.stream.isCoalescing", true)

      factory.createXMLStreamReader(sourceReader)
    }

    try {
      while (reader.hasNext && reader.next != XMLStreamConstants.START_ELEMENT) {}
    }

    catch {
      case e: XMLStreamException =>
        reader.close()
        sourceReader.close()

        return Failure(new Exception(e))
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
                parseElement(acc.copy(text = reader.getText))
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
    sourceReader.close()

    elementTry
  }
}
