// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.{ RefEnumeratedValueSet, FileIO, LabProtocol, SteppedValueSet }
import org.nlogo.core.CompilerUtilitiesInterface
import org.w3c.dom
import org.xml.sax
import language.implicitConversions

import scala.io.Codec

object ProtocolLoader
{
  val PREAMBLE = """<?xml version="1.0" encoding="us-ascii"?>"""
  val DOCTYPE = "<!DOCTYPE experiments SYSTEM \"behaviorspace.dtd\">"
}

class ProtocolLoader(services: CompilerUtilitiesInterface)
{
  def loadOne(file: java.io.File):LabProtocol =
    new Loader().load(file) match { case Seq(ps) => ps }
  def loadOne(xml: String):LabProtocol =
    new Loader().load(xml) match { case Seq(ps) => ps }
  def loadOne(file: java.io.File,name:String):LabProtocol =
    new Loader().load(file).find(_.name == name)
      .getOrElse(throw new IllegalStateException(
        "no experiment named \"" + name + "\""))
  def loadOne(xml: String, name: String) =
    new Loader().load(xml).find(_.name == name)
      .getOrElse(throw new IllegalStateException(
        "no experiment named \"" + name + "\""))
  def loadAll(file: java.io.File):List[LabProtocol] =
    new Loader().load(file)
  def loadAll(xml: String):List[LabProtocol] =
    new Loader().load(xml)
  // old NetLogo versions used "tick" where we now use "step" because "tick" got added to the language
  def ticksToSteps(str: String) =
    str.replaceAll("runMetricsEveryTick=\"", "runMetricsEveryStep=\"")
       .replaceAll("<timeLimit ticks=\"", "<timeLimit steps=\"")
  implicit def file2inputSource(file: java.io.File): sax.InputSource =
    xml2inputSource(FileIO.fileToString(file)(Codec.UTF8))

  implicit def xml2inputSource(xml: String): sax.InputSource = {
    val doctypedXml =
      if (xml.startsWith(ProtocolLoader.PREAMBLE))
        ticksToSteps(xml)
      else
        ProtocolLoader.PREAMBLE + "\n" + ProtocolLoader.DOCTYPE + "\n" + ticksToSteps(xml)
    // what about character encodings?  String.getBytes() will use the platform's default encoding;
    // presumably sax.InputSource will also then use that same encoding?  I'm not really sure...  it
    // doesn't seem worth stressing about - ST 12/21/04
    new sax.InputSource(new java.io.ByteArrayInputStream(doctypedXml.getBytes))
  }
  ///
  def file2xml(file: java.io.File): String = ""
  class Loader {
    def load(inputSource: sax.InputSource): List[LabProtocol] = {
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
        def readEnumeratedValueSetElement(e: dom.Element) =
          new RefEnumeratedValueSet(e.getAttribute("variable"),
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
  }
}
