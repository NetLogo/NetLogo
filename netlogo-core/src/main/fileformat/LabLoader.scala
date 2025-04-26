// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.ByteArrayInputStream
import org.nlogo.core.LiteralParser
import org.nlogo.api.{ RefEnumeratedValueSet, LabProtocol, SteppedValueSet }
import org.w3c.dom
import org.xml.sax
import scala.collection.mutable.Set
import scala.language.implicitConversions

object LabLoader {
  val XMLVER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
  val DOCTYPE = "<!DOCTYPE experiments SYSTEM \"behaviorspace.dtd\">"
}

import LabLoader._

class LabLoader(literalParser: LiteralParser) {
  if (literalParser == null)
    throw new Exception("Invalid lab loader!")
  def apply(xml: String, editNames: Boolean, existingNames: Set[String]): Seq[LabProtocol] = {
    // what about character encodings?  String.getBytes() will use the platform's default encoding;
    // presumably sax.InputSource will also then use that same encoding?  I'm not really sure...  it
    // doesn't seem worth stressing about - ST 12/21/04
    val taggedXml =
      if (xml.contains("DOCTYPE experiments")) xml
      else DOCTYPE + "\n" + xml
    val inputSource = new sax.InputSource(new ByteArrayInputStream(
      finalToPost(ticksToSteps(taggedXml)).getBytes))
    apply(inputSource, editNames, existingNames)
  }

  def apply(inputSource: sax.InputSource, editNames: Boolean, existingNames: Set[String]): Seq[LabProtocol] = {
    inputSource.setSystemId(getClass.getResource("/system/").toString)
    val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance
    factory.setValidating(true)
    val builder = factory.newDocumentBuilder
    builder.setErrorHandler(new sax.ErrorHandler {
      def error(ex: sax.SAXParseException): Unit = { throw ex }
      def fatalError(ex: sax.SAXParseException): Unit = { throw ex }
      def warning(ex: sax.SAXParseException): Unit = { throw ex }
    })
    builder.parse(inputSource)
      .getElementsByTagName("experiment")
      .map(x => fixEmptyNames(readProtocolElement(x, editNames, existingNames),
                              editNames, existingNames))
  }

  def readProtocolElement(element: dom.Element, editNames: Boolean, existingNames: Set[String]): LabProtocol = {
    def readOneAttribute(name: String, attr: String) =
      element.getElementsByTagName(name).head.getAttribute(attr)
    def readAll(name: String) =
      element.getElementsByTagName(name).map(_.getLastChild.getNodeValue)
    def readOptional(name: String): String = {
      readAll(name) match {
        case List(x) => x
        case _ => ""
      }
    }
    def exists(name: String) =
      !element.getElementsByTagName(name).isEmpty
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
    def constants = {
      for{e <- element.getChildNodes
        valueSet <- e.getNodeName match {
          case "steppedValueSet" => Some(readSteppedValueSetElement(e))
          case "enumeratedValueSet" => Some(readEnumeratedValueSetElement(e))
      case _ => None } }
      yield valueSet
    }
    def subExperiments = {
      element.getElementsByTagName("subExperiment").map(_.getChildNodes.map(e => e.getNodeName match {
        case "steppedValueSet" => readSteppedValueSetElement(e)
        case "enumeratedValueSet" => readEnumeratedValueSetElement(e)
      }))
    }
    var name = element.getAttribute("name")
    if (editNames && !name.isEmpty) {
      if (existingNames.contains(name))
      {
        var n = 1
        while (existingNames.contains(s"$name ($n)")) n += 1
        name = s"$name ($n)"
      }
      existingNames += name
    }
    new LabProtocol(
      name,
      readOptional("preExperiment"),
      readOptional("setup"),
      readOptional("go"),
      readOptional("postRun"),
      readOptional("postExperiment"),
      element.getAttribute("repetitions").toInt,
      { val defaultOrder = element.getAttribute("sequentialRunOrder").toString
        if (defaultOrder == "") true else defaultOrder == "true"
      },
      element.getAttribute("runMetricsEveryStep") == "true",
      readOptional("runMetricsCondition"),
      if (!exists("timeLimit")) 0 else readOneAttribute("timeLimit","steps").toInt,
      if (!exists("exitCondition")) "" else readOptional("exitCondition"),
      readAll("metric"),
      constants,
      subExperiments)
  }

  def fixEmptyNames(protocol: LabProtocol, editNames: Boolean, existingNames: Set[String]): LabProtocol = {
    if (editNames && protocol.name.isEmpty && existingNames.contains("no name")) {
      var n = 1
      while (existingNames.contains(s"no name ($n)")) n += 1
      existingNames += s"no name ($n)"
      protocol.copy(name = s"no name ($n)")
    }
    else protocol
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

  private def finalToPost(str: String) =
    str.replaceAll("<final>", "<postRun>")
       .replaceAll("</final>", "</postRun>")

}
