// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.XMLElement
import org.nlogo.core.model.NLogoXMLWriter

import java.io.{ StringReader, Writer }
import javax.xml.stream.{ XMLInputFactory, XMLOutputFactory, XMLStreamConstants, XMLStreamException }

import scala.util.{ Failure, Try }

// this wrapper around XMLStreamWriter allows for pretty-printing and other formatting (Isaac B 12/16/24)
class XMLWriter(dest: Writer) extends NLogoXMLWriter {
  private val writer = XMLOutputFactory.newFactory.createXMLStreamWriter(dest)

  private val indentStr = "  "

  private var indentLevel = 0
  private var lastStart = ""

  def startDocument(): Unit = {
    writer.writeStartDocument("utf-8", "1.0")
  }

  def startElement(name: String): Unit = {
    writer.writeCharacters("\n")

    for (i <- 0 until indentLevel)
      writer.writeCharacters(indentStr)

    writer.writeStartElement(name)

    indentLevel += 1

    lastStart = name
  }

  def attribute(name: String, value: String): Unit = {
    writer.writeAttribute(name, value)
  }

  def escapedText(text: String, force: Boolean): Unit = {
    if (force || text.contains('<') || text.contains('>') || text.contains('&')) {
      writer.writeCData("]]>".r.replaceAllIn(text, s"]]${XMLElement.CDataEscape}>"))
    } else {
      writer.writeCharacters(text)
    }
  }

  def endElement(name: String): Unit = {
    indentLevel -= 1

    if (lastStart != name) {
      writer.writeCharacters("\n")

      for (i <- 0 until indentLevel)
        writer.writeCharacters(indentStr)
    }

    writer.writeEndElement()
  }

  def element(el: XMLElement): Unit = {
    startElement(el.name)

    for ((key, value) <- el.attributes)
      attribute(key, value)

    if (el.text.isEmpty)
      el.children.foreach(element)
    else
      escapedText(el.text)

    endElement(el.name)
  }

  def endDocument(): Unit = {
    writer.writeEndDocument()
    writer.writeCharacters("\n")
  }

  def close(): Unit = {
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

        return Failure(filterParseError(e))
    }

    def readElement(): Try[XMLElement] = {
      def parseElement(acc: XMLElement): Try[XMLElement] = {
        if (reader.hasNext) {
          Try {
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
                  parseElement(acc.copy(text = s"]]${XMLElement.CDataEscape}>".r.replaceAllIn(reader.getText, "]]>")))
              case XMLStreamConstants.COMMENT =>
                parseElement(acc)
              case x =>
                Failure(throw new Exception(s"Unexpected value found while parsing XML: ${x}"))
            }
          }.flatten
        } else {
          Try(acc)
        }
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

    elementTry match {
      case Failure(e: XMLStreamException) =>
        Failure(filterParseError(e))

      case t =>
        t
    }
  }

  private def filterParseError(e: Exception): Exception =
    new Exception("ParseError.*?Message: ".r.replaceFirstIn(e.getMessage.replace("\n", ""), ""))
}
