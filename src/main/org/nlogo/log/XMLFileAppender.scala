// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import javax.xml.transform.{ OutputKeys,
                             TransformerFactory }
import javax.xml.transform.sax.{ SAXTransformerFactory, TransformerHandler }
import javax.xml.transform.stream.StreamResult
import org.xml.sax.helpers.AttributesImpl
import org.apache.log4j.FileAppender
import scala.beans.BeanProperty
import org.nlogo.util.Exceptions.ignoring

//  This class must be public because log4j needs to be able to find it and we refer to it in the
//  configuration file
class XMLFileAppender extends FileAppender {

  @BeanProperty var username: String = _
  @BeanProperty var iPAddress: String = _
  @BeanProperty var modelName: String = _
  @BeanProperty var studentName: String = _
  @BeanProperty var version: String = _

  private var hd: TransformerHandler = _

  @throws(classOf[java.io.IOException])
  override def setFile(fileName: String, append: Boolean, bufferedIO: Boolean, bufferSize: Int) {
    super.setFile(fileName, append, bufferedIO, bufferSize)
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
    hd.startDTD("eventSet", "netlogo_logging.dtd", fileName)
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

  override def closeFile() {
    if(hd != null) {
      hd.endElement("", "", "eventSet")
      hd.endDocument()
      hd = null
    }
    super.closeFile()
  }

  override def close() {
    closeFile()
  }

}
