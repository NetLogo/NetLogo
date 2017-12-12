// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  java.net.URI

import
  java.io.{ StringReader, StringWriter, Writer }

import
  java.nio.{ charset, file },
    charset.StandardCharsets,
    file.{ Files, Paths }

import
  javax.xml.transform.{ OutputKeys, TransformerFactory }

import
  javax.xml.transform.stream.{ StreamResult, StreamSource }

import
  cats.data.Validated.{ Invalid, Valid }

import
  org.nlogo.{ core, api, xmllib },
    core.{ model, I18N, Model, Shape, UpdateMode, View, Widget, WorldDimensions },
      model.{ LinkShapeXml, VectorShapeXml, WidgetXml, XmlShape },
      Shape.{ LinkShape, VectorShape },
    api.{ FileIO, TwoDVersion },
    xmllib.{ Element, ElementFactory, ScalaXmlElement, Text }

import
  org.nlogo.api.{ ComponentSerialization, ModelFormat }

import
  scala.io.{ Codec, Source }, Codec.UTF8

import
  scala.util.{ Failure, Success, Try }

import
  scala.xml.{ Elem, NamespaceBinding, TopScope, XML }

object NLogoXFormat {
  type Section = Element
}

case class NLogoXFormatException(m: String) extends RuntimeException(m)

class NLogoXFormat(factory: ElementFactory) extends ModelFormat[NLogoXFormat.Section, NLogoXFormat] {
  def name: String = "nlogox"
  import NLogoXFormat.Section

  val namespace = "http://ccl.northwestern.edu/netlogo/netlogox/1"

  private val NLogoXPrettyIndent = 2

  // as of 12/13/17 the default nlogox file "empty.nlogox" is under 25K
  private val EmptyNLogoXFileSize = 25000

  object CodeComponent extends ComponentSerialization[Section, NLogoXFormat] {
    val componentName = "org.nlogo.modelsection.code"
    val EndOfLineSpaces = new scala.util.matching.Regex("(?m)[ ]+$", "content")
    override def addDefault = ((m: Model) => m.copy(code = ""))
    def serialize(m: Model): Element = {
      val cleanedText = EndOfLineSpaces.replaceAllIn(m.code, "")
      factory.newElement("code").withText(cleanedText).build
    }
    def validationErrors(m: Model): Option[String] = None
    override def deserialize(code: Section) = { (m: Model) =>
      val allCode = code.children.map {
        case t: Text => t.text
        case _ => ""
      }.mkString("")
      Try(m.copy(code = allCode))
    }
  }

  object InfoComponent extends ComponentSerialization[Section, NLogoXFormat] {
    val componentName = "org.nlogo.modelsection.info"
    val EmptyInfoPath = "/system/empty-info.md"
    override def addDefault =
      ((m: Model) => m.copy(info = FileIO.url2String(EmptyInfoPath)))
    def serialize(m: Model): Element = factory.newElement("info").withText(m.info).build
    def validationErrors(m: Model): Option[String] = None
    override def deserialize(info: Section) = { (m: Model) =>
      val allInfo = info.children.map {
        case t: Text => t.text
        case _ => ""
      }.mkString("")
      Try(m.copy(info = allInfo))
    }
  }

  object VersionComponent extends ComponentSerialization[Section, NLogoXFormat] {
    val componentName = "org.nlogo.modelsection.version"
    // TwoDVersion is used here because it's "generic".
    override def addDefault = (_.copy(version = TwoDVersion.version))
    def serialize(m: Model): Element = factory.newElement("version").withText(m.version).build
    def validationErrors(m: Model): Option[String] = None
    override def deserialize(e: Element) = { (m: Model) =>
      val versionString = e.children.map {
        case t: Text => t.text
        case _ => ""
      }.mkString("")
      if (versionString.startsWith("NetLogo"))
        Success(m.copy(version = versionString))
      else {
        val errorString = I18N.errors.getN("fileformat.invalidversion", name, TwoDVersion.version, versionString)
        Failure(new NLogoXFormatException(errorString))
      }
    }
  }

  object InterfaceComponent extends ComponentSerialization[Section, NLogoXFormat] {
    val componentName = "org.nlogo.modelsection.interface"

    lazy val defaultView: View = View(left = 210, top = 10, right = 649, bottom = 470,
      dimensions = WorldDimensions(-16, 16, -16, 16, 13.0), fontSize = 10, updateMode = UpdateMode.Continuous,
      showTickCounter = true, frameRate = 30)

    override def addDefault = ((m: Model) => m.copy(widgets = Seq(defaultView)))

    def serialize(m: Model): Section = {
      val widgets = m.widgets.map((w: Widget) => WidgetXml.write(w, factory))
      factory.newElement("widgets").withElementList(widgets).build
    }

    def validationErrors(m: Model): Option[String] = None

    private def parseWidgets(elems: Seq[Element]): Try[Seq[Widget]] =
      elems.foldLeft(Try(Seq.empty[Widget])) {
        case (Success(acc), e) => WidgetXml.read(e) match {
          case Valid(w) => Success(acc :+ w)
          case Invalid(err) => Failure(new NLogoXFormatException(err.message))
        }
        case (failure, e) => failure
      }

    override def deserialize(e: Element) = { (m: Model) =>
      parseWidgets(e.children.collect { case e: Element => e })
        .map { widgets => m.copy(widgets = widgets) }
    }
  }

  object LinkShapeComponent extends ComponentSerialization[Section, NLogoXFormat] {
    val componentName = "org.nlogo.modelsection.linkshapes"
    override def addDefault = ((m: Model) => m.copy(linkShapes = Model.defaultLinkShapes.toSeq))
    def serialize(m: Model): Section = factory.newElement("linkShapes")
      .withElementList(m.linkShapes.map(s => LinkShapeXml.write(XmlShape.coerceLinkShape(s), factory)))
      .build
    def validationErrors(m: Model): Option[String] = None
    override def deserialize(shapes: Section) = { (m: Model) =>
      shapes.children
        .collect { case e: Element => e }
        .foldLeft(Try(Seq.empty[LinkShape])) {
          case (Success(acc), e) => LinkShapeXml.read(e) match {
            case Valid(v: LinkShape) => Success(acc :+ v)
            case Valid(_) => Success(acc)
            case Invalid(err) => Failure(new NLogoXFormatException(err.message))
          }
            case (failure, e) => failure
        }
        .map(linkShapes =>
            if (linkShapes.isEmpty) addDefault(m)
            else                    m.copy(linkShapes = linkShapes))
    }
  }

  object ShapeComponent extends ComponentSerialization[Section, NLogoXFormat] {
    val componentName = "org.nlogo.modelsection.turtleshapes"
    override def addDefault = _.copy(turtleShapes = Model.defaultShapes)
    def serialize(m: Model): Section =
      factory.newElement("shapes")
        .withElementList(m.turtleShapes.map(s => VectorShapeXml.write(XmlShape.coerceVectorShape(s), factory)))
        .build
    def validationErrors(m: Model): Option[String] = None
    override def deserialize(shapes: Section) = { (m: Model) =>
      shapes.children
        .collect { case e: Element => e }
        .foldLeft(Try(Seq.empty[VectorShape])) {
          case (Success(acc), e) => VectorShapeXml.read(e) match {
            case Valid(v: VectorShape) => Success(acc :+ v)
            case Valid(_) => Success(acc)
            case Invalid(err) =>
              Failure(new NLogoXFormatException(err.message))
          }
          case (failure, e) => failure
        }
        .map(shapes =>
            if (shapes.isEmpty) addDefault(m)
            else                m.copy(turtleShapes = shapes))
    }
  }

  val sectionNamesToKeys =
    Map(
      "code"            -> "org.nlogo.modelsection.code",
      "info"            -> "org.nlogo.modelsection.info",
      "shapes"          -> "org.nlogo.modelsection.turtleshapes",
      "linkShapes"      -> "org.nlogo.modelsection.linkshapes",
      "version"         -> "org.nlogo.modelsection.version",
      "widgets"         -> "org.nlogo.modelsection.interface",
      "previewCommands" -> "org.nlogo.modelsection.previewcommands",
      "experiments"     -> "org.nlogo.modelsection.behaviorspace",
      "systemDynamics"  -> "org.nlogo.modelsection.systemdynamics",
      "hubnet"          -> "org.nlogo.modelsection.hubnetclient",
      "modelInfo"       -> "org.nlogo.modelsection.modelinfo",
      "modelSettings"   -> "org.nlogo.modelsection.modelsettings"
    )

  def sections(location: URI): Try[Map[String,Section]] =
    Try {
      if (location.getScheme == "jar") Source.fromInputStream(location.toURL.openStream)(UTF8)
      else Source.fromURI(location)(UTF8)
      }.flatMap { s =>
        val sections = sectionsFromSource(s.mkString)
        s.close()
        sections
      }

  def sectionsFromSource(source: String): Try[Map[String,Section]] = {
    Try {
      val scalaXml = XML.loadString(source)
      val wrapped = new ScalaXmlElement(scalaXml)
      wrapped.children.foldLeft(Map[String, Section]()) {
        case (m, e: Element) =>
          sectionNamesToKeys.get(e.tag).map(sectionKey => m + (sectionKey -> e)).getOrElse(m)
        case (m, _) => m
      }
    }.recoverWith {
      case e: org.xml.sax.SAXParseException if e.getMessage.contains("Content is not allowed in prolog.") =>
        Failure(new NLogoXFormatException("Tried to open a non-xml file as nlogox, check that the file extension is correct"))
    }
  }

  private def normalizedElems(sections: Map[String,Section]): Try[Elem] = {
    val topScope = new NamespaceBinding(null, namespace, null)
    factory.newElement("model").withElementList(sections.values.toSeq).build match {
      case s: ScalaXmlElement =>
        val elem = s.elem
        val normalizedElem =
          elem.copy(scope = topScope,
            child = elem.child.map {
              case e: Elem => e.copy(scope = TopScope)
              case other => other
            })
        Success(normalizedElem)
      case _ => Failure(new NLogoXFormatException("internal error: unable to write nlogox file"))
    }
  }

  def writeSections(sections: Map[String,Section], location: URI): Try[URI] = {
    val tryRootElem = normalizedElems(sections)
    val tryWriter = Try(Files.newBufferedWriter(Paths.get(location), StandardCharsets.UTF_8))

    for {
      rootElem <- tryRootElem
      basicXml <- writeScalaXMLElements(rootElem)
      fileWriter <- tryWriter
    } yield {
      writeFormatted(basicXml, fileWriter)
      location
    }
  }

  private def writeScalaXMLElements(rootElem: Elem): Try[StringWriter] =
    Try {
      val writer = new StringWriter(EmptyNLogoXFileSize)
      XML.write(writer, rootElem, "UTF-8", true, null)
      writer.flush()
      writer
    }

  // once https://github.com/scala/scala-xml/issues/76 is fixed, we can probably use the
  // scala XML pretty-printer
  private def writeFormatted(input: StringWriter, output: Writer): Try[Writer] = {
    Try {
      output.append("<?xml version='1.0' encoding='UTF-8'?>\n")
      val xmlInput = new StreamSource(new StringReader(input.toString))
      val xmlOutput = new StreamResult(output)
      val transformer = TransformerFactory.newInstance.newTransformer
      transformer.setOutputProperty(OutputKeys.INDENT, "yes")
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", NLogoXPrettyIndent.toString)
      transformer.transform(xmlInput, xmlOutput)
      output.flush()
      output.close()
      output
    }
  }

  def sectionsToSource(sections: Map[String,Section]): Try[String] =
    normalizedElems(sections)
      .flatMap(writeScalaXMLElements _)
      .flatMap(xml => writeFormatted(xml, new StringWriter()))
      .map(_.toString)

  def codeComponent: ComponentSerialization[Section,NLogoXFormat] = CodeComponent
  def infoComponent: ComponentSerialization[Section,NLogoXFormat] = InfoComponent
  def version: ComponentSerialization[Section,NLogoXFormat] = VersionComponent
  def interfaceComponent: ComponentSerialization[Section,NLogoXFormat] = InterfaceComponent
  def linkShapesComponent: ComponentSerialization[Section,NLogoXFormat] = LinkShapeComponent
  def shapesComponent: ComponentSerialization[Section,NLogoXFormat] = ShapeComponent
}
