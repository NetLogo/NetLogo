// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.{ InputStream, Reader }

import java.nio.file.{ Path, Paths }

import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import org.xml.sax.{ ErrorHandler, SAXParseException }

import org.w3c.dom.ls.{ LSInput, LSResourceResolver }

import org.scalatest.{ Assertions, FunSuite }

import scala.io.{ Source => ScalaSource }

class SchemaValidationTest extends FunSuite with SchemaValidator {
  test("validates test against xml schema") { new Helper {
    validate("test/test.nlogox")
  } }

  test("validates sample model is correct") { new Helper {
    validate("test/fileformat/Sample.nlogox")
  } }

  test("validates empty model against xml schema") { new Helper {
    validateResource("/system/empty.nlogox")
  } }

  test("validates empty-3d model against xml schema") { new Helper {
    validateResource("/system/empty-3d.nlogox")
  } }
}

trait SchemaValidator {
  val JaxpSchemaLanguage =
    "http://java.sun.com/xml/jaxp/properties/schemaLanguage"
  val JaxpSchemaSource =
    "http://java.sun.com/xml/jaxp/properties/schemaSource"
  val W3CXmlSchema =
    "http://www.w3.org/2001/XMLSchema";

  class AccumulatingErrorHandler extends ErrorHandler {
    var errors = Seq.empty[(String, SAXParseException)]
    def error(e: SAXParseException): Unit = { errors :+= (("ERROR", e)) }
    def fatalError(e: SAXParseException): Unit = { errors :+= (("FATAL", e)) }
    def warning(e: SAXParseException): Unit = { errors :+= (("WARN", e)) }
    def errorString: String = errors.map(e => "* [" + e._1 + "] " + e._2.toString).mkString("\n")
  }

  val resolver = new LSResourceResolver {
    def resolveResource(tpe: String, namespaceURI: String, publicId: String, systemId: String, baseURI: String): LSInput = {
      if (systemId == "types.xsd") {
        new TestLSInput(
          publicId = namespaceURI,
          systemId = systemId,
          baseURI = baseURI,
          inputStream = getClass.getResourceAsStream("/org/nlogo/xmllib/plugin/types.xsd"))
      } else {
        println(s"Unknown resource requested: $tpe, $namespaceURI, $publicId, $systemId, $baseURI")
        null
      }
    }
  }

  val netlogoSchema = {
    val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    val schemaErrorHandler = new AccumulatingErrorHandler()
    sf.setErrorHandler(schemaErrorHandler)
    sf.setResourceResolver(resolver)
    assert(schemaErrorHandler.errors.isEmpty,
      s"model.xsd contains errors:\n${schemaErrorHandler.errorString}")
    sf.newSchema(new StreamSource(getClass.getResourceAsStream("/org/nlogo/fileformat/model.xsd")))
  }

  val factory = DocumentBuilderFactory.newInstance
  factory.setNamespaceAware(true)
  factory.setValidating(false)
  factory.setNamespaceAware(true)
  factory.setSchema(netlogoSchema)

  trait Helper {
    import Assertions._

    val errorHandler = new AccumulatingErrorHandler()
    def errors = errorHandler.errors
    lazy val db = {
      val _db = factory.newDocumentBuilder
      _db.setErrorHandler(errorHandler)
      _db
    }
    def validate(path: Path): Unit = {
      db.parse(path.toFile)
      val isErrorFree = errors.isEmpty
      assert(isErrorFree, s"Found the following errors:\n${errorHandler.errorString}")
    }
    def validate(pathString: String): Unit = {
      validate(Paths.get(pathString))
    }
    def validateResource(name: String): Unit = {
      db.parse(getClass.getResourceAsStream(name))
      val isErrorFree = errors.isEmpty
      assert(isErrorFree, s"Found the following errors:\n${errorHandler.errorString}")
    }
  }

  class TestLSInput(
    var baseURI: String = null,
    var inputStream: InputStream = null,
    var characterStream: Reader = null,
    var certifiedText: Boolean = false,
    var encoding: String = null,
    var publicId: String = null,
    var systemId: String = null) extends LSInput {
      var cachedStringData = Option.empty[String]
    def getBaseURI(): String = baseURI
    def getByteStream(): InputStream = null
    def getCertifiedText(): Boolean = certifiedText
    def getCharacterStream(): Reader = null
    def getEncoding(): String = encoding
    def getPublicId(): String = publicId
    def getStringData(): String = {
      cachedStringData match {
        case Some(data) => data
        case None =>
          cachedStringData =
            Some(ScalaSource.fromInputStream(inputStream).mkString.replace("\uFEFF", ""))
          cachedStringData.get
      }
    }
    def getSystemId(): String = systemId
    def setBaseURI(uri: String): Unit = baseURI = uri
    def setByteStream(stream: InputStream): Unit = {}
    def setCertifiedText(certified: Boolean): Unit = certifiedText = certified
    def setCharacterStream(reader: Reader): Unit = {}
    def setEncoding(enc: String): Unit = encoding = enc
    def setPublicId(id: String): Unit = publicId = id
    def setStringData(data: String): Unit = {}
    def setSystemId(id: String): Unit = systemId = id
  }
}
