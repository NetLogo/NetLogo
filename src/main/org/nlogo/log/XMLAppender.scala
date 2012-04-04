package org.nlogo.log

import org.apache.log4j.WriterAppender
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.sax.{SAXTransformerFactory, TransformerHandler}
import org.nlogo.util.Exceptions.ignoring
import javax.xml.transform.{OutputKeys, TransformerFactory}
import org.xml.sax.helpers.AttributesImpl
import reflect.BeanProperty

trait XMLAppender {

  self: WriterAppender =>

  @BeanProperty var username: String = _
  @BeanProperty var iPAddress: String = _
  @BeanProperty var modelName: String = _
  @BeanProperty var studentName: String = _
  @BeanProperty var version: String = _

  private var hd: TransformerHandler = _

  protected def initializeTransformer(systemId: String = null) {
    val streamResult = new StreamResult(qw)
    val tf = TransformerFactory.newInstance.asInstanceOf[SAXTransformerFactory]
    ignoring(classOf[IllegalArgumentException]) {
      tf.setAttribute("indent-number", 2: java.lang.Integer)
    }
    hd = tf.newTransformerHandler()
    val transformer = hd.getTransformer
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    hd.setResult(streamResult)
    hd.startDocument()
    hd.startDTD("eventSet", "netlogo_logging.dtd", systemId)
    hd.endDTD()
    val attributes = new AttributesImpl
    attributes.addAttribute("", "", "username", "CDATA", username)
    attributes.addAttribute("", "", "name", "CDATA", studentName)
    attributes.addAttribute("", "", "ipaddress", "CDATA", iPAddress)
    attributes.addAttribute("", "", "modelName", "CDATA", modelName)
    attributes.addAttribute("", "", "version", "CDATA", version)
    hd.startElement("", "", "eventSet", attributes)
    getLayout match {
      case x: XMLLayout => x.setTransformerHandler(hd)
      case _ =>
    }
  }

  protected def closeDocument() {
    if(hd != null) {
      hd.endElement("", "", "eventSet")
      hd.endDocument()
      hd = null
    }
  }

}
