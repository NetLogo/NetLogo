// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.ByteArrayInputStream
import org.nlogo.core.LiteralParser
import org.nlogo.api.{ RefEnumeratedValueSet, LabProtocol, SteppedValueSet }
import org.w3c.dom
import org.xml.sax
import scala.language.implicitConversions

object LabLoader {
  val DOCTYPE = "<!DOCTYPE experiments SYSTEM \"behaviorspace.dtd\">"
}

import LabLoader._

class LabLoader(literalParser: LiteralParser) {
  if (literalParser == null)
    throw new Exception("Invalid lab loader!")
  def apply(xml: String): Seq[LabProtocol] = {
    // what about character encodings?  String.getBytes() will use the platform's default encoding;
    // presumably sax.InputSource will also then use that same encoding?  I'm not really sure...  it
    // doesn't seem worth stressing about - ST 12/21/04
    val taggedXml =
      if (xml.contains("DOCTYPE experiments")) xml
      else DOCTYPE + "\n" + xml
    val inputSource = new sax.InputSource(new ByteArrayInputStream(
      ticksToSteps(taggedXml).getBytes))
    apply(inputSource)
  }

  def apply(inputSource: sax.InputSource): Seq[LabProtocol] = {
    inputSource.setSystemId(getClass.getResource("/system/").toString)
    val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance
    factory.setValidating(true)
    val builder = factory.newDocumentBuilder
    builder.setErrorHandler(new sax.ErrorHandler {
      def error(ex: sax.SAXParseException) { throw ex }
      def fatalError(ex: sax.SAXParseException) { throw ex }
      def warning(ex: sax.SAXParseException) { throw ex }
    })
    builder.parse(inputSource)
      .getElementsByTagName("experiment")
      .map(readProtocolElement)
  }

  def readProtocolElement(element: dom.Element): LabProtocol = {
    def readOneAttribute(name: String, attr: String) =
      element.getElementsByTagName(name).head.getAttribute(attr)
    def readAll(name: String) =
      element.getElementsByTagName(name).map(_.getLastChild.getNodeValue)
    def readOptional(name: String) =
      readAll(name) match { case List(x) => x ; case Nil => "" }
    def exists(name: String) =
      !element.getElementsByTagName(name).isEmpty
    def valueSets = {
      def readSteppedValueSetElement(e: dom.Element) = {
        def parse(name: String) = BigDecimal(e.getAttribute(name))
        new SteppedValueSet(e.getAttribute("variable"),parse("first"),
          parse("step"),parse("last"))
      }
      def readEnumeratedValueSetElement(e: dom.Element) = {
        val valueElems = e.getElementsByTagName("value")
        val values = for {
          i <- 0 to valueElems.getLength
          elem = valueElems.item(i) if elem != null
        } yield {
          literalParser.readFromString(
            elem.getAttributes.getNamedItem("value").getNodeValue)
        }
        new RefEnumeratedValueSet(e.getAttribute("variable"), values.toList)
      }
      for{e <- element.getChildNodes
        valueSet <- e.getNodeName match {
          case "steppedValueSet" => Some(readSteppedValueSetElement(e))
          case "enumeratedValueSet" => Some(readEnumeratedValueSetElement(e))
      case _ => None } }
      yield valueSet
    }
    new LabProtocol(
      element.getAttribute("name"),
      readOptional("setup"),
      readOptional("go"),
      readOptional("final"),
      element.getAttribute("repetitions").toInt,
      { val defaultOrder = element.getAttribute("sequentialRunOrder").toString
        if(defaultOrder == "") true else defaultOrder == "true"
      },
      element.getAttribute("runMetricsEveryStep") == "true",
      if(!exists("timeLimit")) 0 else readOneAttribute("timeLimit","steps").toInt,
      if(!exists("exitCondition")) "" else readOptional("exitCondition"),
      readAll("metric"),
      valueSets)
  }
  // implicits to keep the code from getting too verbose
  implicit def nodes2list(nodes: dom.NodeList): List[dom.Element] =
    (0 until nodes.getLength)
      .map(nodes.item(_))
      .collect{case e: dom.Element => e}
      .toList

  private def ticksToSteps(str: String) =
    str.replaceAll("runMetricsEveryTick=\"", "runMetricsEveryStep=\"")
       .replaceAll("<timeLimit ticks=\"", "<timeLimit steps=\"")

}
