// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.CompilerServices
import org.w3c.dom
import org.xml.sax
import language.implicitConversions

object ProtocolLoader
{
  val DOCTYPE = "<!DOCTYPE experiments SYSTEM \"behaviorspace.dtd\">"
}

class ProtocolLoader(services: CompilerServices)
{
  def loadOne(file: java.io.File):Protocol =
    new Loader().load(file) match { case Seq(ps) => ps }
  def loadOne(xml: String):Protocol =
    new Loader().load(xml) match { case Seq(ps) => ps }
  def loadOne(file: java.io.File,name:String):Protocol =
    new Loader().load(file).find(_.name == name)
      .getOrElse(throw new IllegalStateException(
        "no experiment named \"" + name + "\""))
  def loadOne(xml: String, name: String) =
    new Loader().load(xml).find(_.name == name)
      .getOrElse(throw new IllegalStateException(
        "no experiment named \"" + name + "\""))
  def loadAll(file: java.io.File):List[Protocol] =
    new Loader().load(file)
  def loadAll(xml: String):List[Protocol] =
    new Loader().load(xml)
  // old NetLogo versions used "tick" where we now use "step" because "tick" got added to the language
  def ticksToSteps(str: String) =
    str.replaceAll("runMetricsEveryTick=\"", "runMetricsEveryStep=\"")
       .replaceAll("<timeLimit ticks=\"", "<timeLimit steps=\"")
  implicit def file2inputSource(file: java.io.File): sax.InputSource =
    new sax.InputSource(
      new java.io.StringReader(
        ticksToSteps(
          org.nlogo.util.Utils.reader2String(
            new java.io.FileReader(file)))))
  implicit def xml2inputSource(xml: String): sax.InputSource =
    // what about character encodings?  String.getBytes() will use the platform's default encoding;
    // presumably sax.InputSource will also then use that same encoding?  I'm not really sure...  it
    // doesn't seem worth stressing about - ST 12/21/04
    new sax.InputSource(new java.io.ByteArrayInputStream(
      (ProtocolLoader.DOCTYPE + "\n" + ticksToSteps(xml)).getBytes))
  ///
  def file2xml(file: java.io.File): String = ""
  class Loader {
    def load(inputSource: sax.InputSource): List[Protocol] = {
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
    def readProtocolElement(element: dom.Element): Protocol = {
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
        def readEnumeratedValueSetElement(e: dom.Element) =
          new EnumeratedValueSet(e.getAttribute("variable"),
                                 e.getElementsByTagName("value")
                                 .map(e =>
                                   services.readFromString(e.getAttribute("value"))))
        for{e <- element.getChildNodes
            valueSet <- e.getNodeName match {
              case "steppedValueSet" => Some(readSteppedValueSetElement(e))
              case "enumeratedValueSet" => Some(readEnumeratedValueSetElement(e))
              case _ => None } }
        yield valueSet
      }
      new Protocol(
        element.getAttribute("name"),
        readOptional("setup"),
        readOptional("go"),
        readOptional("final"),
        element.getAttribute("repetitions").toInt,
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
  }
}
