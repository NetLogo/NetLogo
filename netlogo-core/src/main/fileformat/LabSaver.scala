// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.{ CartesianProductParameterSet, EnumeratedValueSet,
  LabProtocol, RefEnumeratedValueSet, SteppedValueSet }
import javax.xml.transform
import transform.{OutputKeys,TransformerFactory}
import transform.sax.{SAXTransformerFactory,TransformerHandler}
import transform.stream.StreamResult
import org.xml.sax.helpers.AttributesImpl
import org.nlogo.api.Dump

// this is based on the sample code all the way at the bottom of
// http://www.javazoom.net/services/newsletter/xmlgeneration.html; don't worry if you don't
// understand all the details because I don't either - ST 12/18/04

// It would probably be nicer to use Scala's built-in XML support; this code is converted Java code.
// It would mean pulling in a whole sector of the Scala standard library that we don't depend on at
// present, but that's fine since it wouldn't go in the lite jar. - ST 3/4/09

object LabSaver {
  // it's a bit ugly to return a String instead of writing to a PrintWriter, but the assumption is
  // that we're never going to be writing huge amounts of data - ST 2/23/04
  def save(protocols: Iterable[LabProtocol]): String = {
    val out = new java.io.StringWriter
    val tf = TransformerFactory.newInstance.asInstanceOf[SAXTransformerFactory]
    tf.setAttribute("indent-number", 2)
    val hd = tf.newTransformerHandler
    val transformer = hd.getTransformer
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    hd.setResult(new StreamResult(out))
    hd.startDocument()
    hd.startElement("", "", "experiments", attributes())
    protocols.foreach(saveProtocol(hd, _))
    hd.endElement("", "", "experiments")
    hd.endDocument()
    out.toString.replaceAll("\r\n", "\n")
  }
  def attributes(specs: (String,String)*) = {
    val result = new AttributesImpl
    for((name,value) <- specs if name != "sequentialRunOrder" ||
        (name == "sequentialRunOrder" && value == "false"))
      result.addAttribute("", "", name, "CDATA", value)
    result
  }
  def saveProtocol(hd: TransformerHandler, protocol: LabProtocol) {
    def element(name: String, value: String) {
      hd.startElement("","",name,attributes())
      hd.characters(value.toCharArray,0,value.length)
      hd.endElement("","",name)
    }
    def elementWithAttributes(name: String, attributes: AttributesImpl) {
      hd.startElement("", "", name, attributes)
      hd.endElement("", "", name)
    }
    val (reps, sro, valueSets) = protocol.parameterSet match {
      case cpps: CartesianProductParameterSet =>
        (cpps.repetitions, cpps.sequentialRunOrder, cpps.valueSets)
      case _ => (0, true, Nil)
    }
    hd.startElement("", "", "experiment",
                    attributes(("name", protocol.name),
                               ("repetitions", reps.toString),
                               ("sequentialRunOrder", sro.toString),
                               ("runMetricsEveryStep", protocol.runMetricsEveryStep.toString)))
    if(protocol.setupCommands.trim != "")
      element("setup", protocol.setupCommands)
    if(protocol.goCommands.trim != "")
      element("go", protocol.goCommands)
    if(protocol.finalCommands.trim != "")
      element("final", protocol.finalCommands)
    if(protocol.timeLimit != 0)
      elementWithAttributes("timeLimit",
                            attributes(("steps",protocol.timeLimit.toString)))
    if(protocol.exitCondition != "")
      element("exitCondition", protocol.exitCondition)
    for(metric <- protocol.metrics)
      element("metric", metric)
    for(valueSet <- valueSets)
      valueSet match {
        case steppedValueSet:SteppedValueSet =>
          elementWithAttributes(
            "steppedValueSet",
            attributes(("variable", valueSet.variableName),
                       ("first", Dump.number(steppedValueSet.firstValue.toDouble)),
                       ("step", Dump.number(steppedValueSet.step.toDouble)),
                       ("last", Dump.number(steppedValueSet.lastValue.toDouble))))
        case enumeratedValueSet: EnumeratedValueSet =>
          hd.startElement("", "", "enumeratedValueSet",
                          attributes(("variable", valueSet.variableName)))
          for(value <- enumeratedValueSet)
            elementWithAttributes("value",
                                  attributes(("value", Dump.logoObject(
                                    value.asInstanceOf[AnyRef],true,false))))
          hd.endElement("", "", "enumeratedValueSet")
        case enumeratedValueSet: RefEnumeratedValueSet =>
          hd.startElement("", "", "enumeratedValueSet",
            attributes(("variable", valueSet.variableName)))
          for (value <- enumeratedValueSet)
            elementWithAttributes("value",
              attributes(("value", Dump.logoObject(value, true, false))))
          hd.endElement("", "", "enumeratedValueSet")
      }
    hd.endElement("", "", "experiment")
  }
}
