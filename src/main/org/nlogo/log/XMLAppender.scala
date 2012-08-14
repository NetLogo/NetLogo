package org.nlogo.log

import javax.xml.transform.stream.StreamResult
import javax.xml.transform.sax.{SAXTransformerFactory, TransformerHandler}
import org.nlogo.util.Exceptions.ignoring
import javax.xml.transform.{OutputKeys, TransformerFactory}
import org.xml.sax.helpers.AttributesImpl
import reflect.BeanProperty
import org.apache.log4j.WriterAppender
import java.io.Writer

trait XMLAppender {

  self: WriterAppender =>

  @BeanProperty var username: String = _
  @BeanProperty var iPAddress: String = _
  @BeanProperty var modelName: String = _
  @BeanProperty var studentName: String = _
  @BeanProperty var version: String = _

  private var hd: TransformerHandler = _

  /*
   When using this method, you should--99% of the time--pass in `qw` (inherited from `WriterAppender`)
   for the `writer` parameter.  You might wonder why I don't just use that directly.  Yeah... me, too!
   Except that I tried--I _tried_ to use it!  But it won't work.  When trying to access `qw` in this
   trait through the above 'self' type, I got this message:

     Implementation restriction: trait XMLAppender accesses protected variable qw inside a concrete trait method.
     Add an accessor in a class extending class WriterAppender as a workaround.
     val streamResult = new StreamResult(qw)

   This error spawns from:

     scala.tools.nsc.typechecker.SuperAccessors
     -needsProtectedAccessor
     --isJavaProtected

   It seems to be a known bug in the compiler when accessing `protected` Java variables from within Scala traits.
   And, well... I tried the "workaround" that was suggested.  However, that, too, fails to work.  I mean... it
   _appears_ to work, but it results in weird, crash-prone behavior when used; I started getting "IOException:
   Stream Closed" in really, really absurd places when using the workaround.  So I now force the `WriterAppender`
   subclasses that mix this trait in to pass `writer` in, themselves.  _That_ works, fortunately. --JAB (4/4/12)

   The Scala compiler bug in question is SI-4119. The bug is closed in JIRA because (since Scala 2.9) you get the
   above compiler error, instead of the runtime error you used to get in Scala 2.8. - ST 5/16/12
   */
  protected def initializeTransformer(systemId: String = null, writer: Writer) {
    val streamResult = new StreamResult(writer)
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
