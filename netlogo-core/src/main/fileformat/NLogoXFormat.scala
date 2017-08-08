// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  java.net.URI

import
  java.nio.{ charset, file },
    charset.StandardCharsets,
    file.{ Files, Paths }

import
  cats.data.Validated.{ Invalid, Valid }

import
  org.nlogo.{ core, api },
    core.{ model, I18N, Model, Shape, Widget },
      model.{ Element, ElementFactory, LinkShapeXml, Text, VectorShapeXml, WidgetXml },
      Shape.{ LinkShape, VectorShape },
    api.{ FileIO, Version }

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

class NLogoXFormatException(m: String) extends RuntimeException(m)

class NLogoXFormat(factory: ElementFactory) extends ModelFormat[NLogoXFormat.Section, NLogoXFormat] {
  def name: String = "nlogox"
  import NLogoXFormat.Section

  val namespace ="http://ccl.northwestern.edu/netlogo/6.1"

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
    override def addDefault = (_.copy(version = Version.version))
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
        val errorString = I18N.errors.getN("fileformat.invalidversion", name, Version.version, versionString)
        Failure(new NLogoXFormatException(errorString))
      }
    }
  }

  object InterfaceComponent extends ComponentSerialization[Section, NLogoXFormat] {
    val componentName = "org.nlogo.modelsection.interface"

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
      .withElementList(m.linkShapes.map(s => LinkShapeXml.write(s, factory)))
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
        .withElementList(m.turtleShapes.map(s => VectorShapeXml.write(s, factory)))
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
      "hubnet"          -> "org.nlogo.modelsection.hubnetclient"
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

  def writeSections(sections: Map[String,Section],location: URI): Try[URI] = {
    val topScope = new NamespaceBinding(null, namespace, null)
    val tryRootElem = factory.newElement("model").withElementList(sections.values.toSeq).build match {
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

    val tryWriter = Try(Files.newBufferedWriter(Paths.get(location), StandardCharsets.UTF_8))

    for {
      rootElem <- tryRootElem
      writer <- tryWriter
    } yield {
      XML.write(writer, rootElem, "UTF-8", true, null)
      writer.write("\n")
      writer.flush()
      writer.close()
      location
    }
    // this doesn't work right now, see https://github.com/scala/scala-xml/issues/76
    /*
    for {
      rootElem <- tryRootElem
      writer <- tryWriter
    } yield {
      val prettyPrinter = new PrettyPrinter(1000, 2)
      val xmlText = prettyPrinter.format(rootElem)
      writer.write("""<?xml version="1.0"?>""")
      writer.write("\n")
      println(xmlText)
      writer.write(xmlText)
      writer.write("\n")
      writer.flush()
      writer.close()
      location
    }
    */
  }

  def sectionsToSource(sections: Map[String,Section]): Try[String] = ???

  def codeComponent: ComponentSerialization[Section,NLogoXFormat] = CodeComponent
  def infoComponent: ComponentSerialization[Section,NLogoXFormat] = InfoComponent
  def version: ComponentSerialization[Section,NLogoXFormat] = VersionComponent
  def interfaceComponent: ComponentSerialization[Section,NLogoXFormat] = InterfaceComponent
  def linkShapesComponent: ComponentSerialization[Section,NLogoXFormat] = LinkShapeComponent
  def shapesComponent: ComponentSerialization[Section,NLogoXFormat] = ShapeComponent
}
