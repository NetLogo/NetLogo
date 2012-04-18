// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import javax.xml.transform.sax.TransformerHandler

import org.apache.log4j.Layout
import org.apache.log4j.spi.LoggingEvent
import org.xml.sax.helpers.AttributesImpl

//  This class must be public because log4j needs to be able to find it and we refer to it in the
//  configuration file
class XMLLayout extends Layout {

  /** No options to activate. */
  override def activateOptions() { }

  var hd: TransformerHandler = null
  def setTransformerHandler(hd: TransformerHandler) { this.hd = hd }

  override def format(event: LoggingEvent): String = {
    val attributes = new AttributesImpl
    val obj = event.getMessage
    val tyype = obj match {
      case msg: LogMessage => msg.attributes(0)(1)
      case _ => "log"
    }
    attributes.addAttribute("", "", "logger", "CDATA", event.getLoggerName)
    attributes.addAttribute("", "", "timestamp", "CDATA", event.timeStamp.toString)
    attributes.addAttribute("", "", "level", "CDATA", event.getLevel.toString)
    attributes.addAttribute("", "", "type", "CDATA", tyype)
    hd.startElement("", "", "event", attributes)
    attributes.clear()
    obj match {
      case msg: LogMessage =>
        Option(msg.elements).foreach(_.foreach(renderLogMessage))
      case _ =>
        hd.startElement("", "", "message", attributes)
        hd.characters(event.getRenderedMessage.toCharArray, 0,
                      event.getRenderedMessage.length)
        hd.endElement("", "", "message")
    }
    hd.endElement("", "", "event")
    ""
  }

  private def renderLogMessage(msg: LogMessage) {
    val attributes = new AttributesImpl
    for(a <- Option(msg.attributes); attr <- a)
      attributes.addAttribute("", "", attr(0), "CDATA", attr(1))
    hd.startElement("", "", msg.tag, attributes)
    if(msg.data != null)
      hd.characters(msg.data.toCharArray, 0, msg.data.length)
    Option(msg.elements).foreach(_.foreach(renderLogMessage))
    hd.endElement("", "", msg.tag)
  }

  override val ignoresThrowable = true

}
