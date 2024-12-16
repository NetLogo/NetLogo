// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.Writer
import java.net.URI
import java.nio.file.{ Files, Paths }

import org.nlogo.api.{ ComponentSerialization, FileIO, LabProtocol, ModelFormat, Version, VersionHistory }
import org.nlogo.core.{ Femto, I18N, LiteralParser, Model, ShapeParser,
  UpdateMode, View, Widget, WorldDimensions }
import org.nlogo.core.model.WidgetReader

import scala.io.{ Codec, Source }, Codec.UTF8
import scala.util.{ Failure, Success, Try }

// THIS format is the 2D format, for changes that affect both 2D and 3D, change AbstractNLogoFormat
class NLogoFormat
  extends ModelFormat[Array[String], NLogoFormat]
  with AbstractNLogoFormat[NLogoFormat] {
  val is3DFormat = false
  val NetLogoRegex = """(^NetLogo \(no version\)|^NetLogo \d+\..*)"""
  def name: String = "nlogo"
  def widgetReaders: Map[String, WidgetReader] = Map()

  override def isCompatible(location: URI): Boolean =
    sections(location) match {
      case Success(sections) =>
        sections("org.nlogo.modelsection.version")
          .find(_.matches(NetLogoRegex))
          .flatMap(_ => Some(true)).getOrElse(false)
      case Failure(ex) => false
    }
  override def isCompatible(source: String): Boolean =
    sectionsFromSource(source) match {
      case Success(sections) =>
        sections("org.nlogo.modelsection.version")
          .find(_.matches(NetLogoRegex))
          .flatMap(_ => Some(true)).getOrElse(false)
      case Failure(ex) => false
    }
  override def isCompatible(model: Model): Boolean =
    model.version.matches(NetLogoRegex)
}

class NLogoFormatException(m: String) extends RuntimeException(m)

trait AbstractNLogoFormat[A <: ModelFormat[Array[String], A]] extends ModelFormat[Array[String], A] {
  def is3DFormat: Boolean
  def name: String
  val Separator = "@#$#@#$#@"
  val SeparatorRegex = "(?m)^@#\\$#@#\\$#@$"

  def widgetReaders: Map[String, WidgetReader]

  def sections(location: URI) =
    Try {
      if (location.getScheme == "jar") {
        Source.fromInputStream(location.toURL.openStream)(UTF8)
      } else {
        Source.fromURI(location)(UTF8)
      }
    }.flatMap { s =>
      val sections = sectionsFromSource(s.mkString)
      s.close()
      sections
    }

  lazy val sectionNames =
    Seq("code",
      "interface",
      "info",
      "turtleshapes",
      "version",
      "previewcommands",
      "systemdynamics",
      "behaviorspace",
      "hubnetclient",
      "linkshapes",
      "modelsettings",
      "deltatick").map(s => "org.nlogo.modelsection." + s)

  def writeSections(sections: Map[String, Array[String]], location: URI): Try[URI] = {
    Try(Paths.get(location)).flatMap { filePath =>
      sectionsToSource(sections).flatMap { fileText =>
        Try {
          val writer = Files.newBufferedWriter(filePath)
          try {
            writer.write(fileText)
            writer.flush()
            location
          }
          finally {
            writer.close
          }
        }
      }
    }
  }

  def sectionsToSource(sections: Map[String, Array[String]]): Try[String] = {
    Try(
      sectionNames.map { name =>
        val sectionLines = sections.getOrElse(name, Array[String]())
        if (sectionLines.isEmpty)
          "\n"
        else if (sectionLines.head.isEmpty || sectionLines.head.startsWith("\n") || name == "org.nlogo.modelsection.code")
          sectionLines.mkString("", "\n", "\n")
        else
          sectionLines.mkString("\n", "\n", "\n")
      }.mkString(Separator))
  }

  def sectionsFromSource(source: String): Try[Map[String, Array[String]]] = {
    try {
      val sectionLines = source
        .split(SeparatorRegex)
        .map(_.linesIterator.toSeq)
        .map(s => if (s.headOption.contains("")) s.tail else s)
        .map(_.toArray)

      if (sectionLines.length < sectionNames.length)
        if (sectionLines(0).contains("xml") || sectionLines(0).contains("XML"))
          Failure(new NLogoFormatException(s"This is not a valid $name file, but you may be able to open it by changing the file extension to match the file type"))
        else
          Failure(new NLogoFormatException(s"Expected $name file to have 12 sections, this had " + sectionLines.length))
      else
        Success((sectionNames zip sectionLines).toMap)
    }
    catch {
      case ex: Exception => Failure(ex)
    }
  }

  def readExperiments(source: String, editNames: Boolean, existingNames: Set[String]): Try[(Seq[LabProtocol], Set[String])] =
    Failure(new Exception) // fix after compile

  def writeExperiments(experiments: Seq[LabProtocol], writer: Writer): Try[Unit] = {
    Try {
      writer.write(s"${LabLoader.XMLVER}\n${LabLoader.DOCTYPE}\n")
      writer.write(LabSaver.save(experiments))
    }
  }

  object CodeComponent extends ComponentSerialization[Array[String], A] {
    val componentName = "org.nlogo.modelsection.code"
    val EndOfLineSpaces = new scala.util.matching.Regex("(?m)[ ]+$", "content")
    override def addDefault = ((m: Model) => m.copy(code = ""))
    def serialize(m: Model): Array[String] =
      EndOfLineSpaces.replaceAllIn(m.code, "").linesIterator.toArray
    def validationErrors(m: Model): Option[String] = None
    override def deserialize(lines: Array[String]) = { (m: Model) =>
      Try(m.copy(code = lines.mkString("\n")))
    }
  }

  object InfoComponent extends ComponentSerialization[Array[String], A] {
    val componentName = "org.nlogo.modelsection.info"
    val EmptyInfoPath = "/system/empty-info.md"
    override def addDefault =
      ((m: Model) => m.copy(info = FileIO.url2String(EmptyInfoPath)))
    def serialize(m: Model): Array[String] = m.info.linesIterator.toArray
    def validationErrors(m: Model): Option[String] = None
    override def deserialize(s: Array[String]) = {(m: Model) =>
      Try {
        val finalInfo =
          if (VersionHistory.olderThan42pre2(m.version))
            InfoConverter.convert(s.mkString("\n"))
          else
            s.mkString("\n")
        m.copy(info = finalInfo)
      }
    }
  }

  object VersionComponent extends ComponentSerialization[Array[String], A] {
    val componentName = "org.nlogo.modelsection.version"
    override def addDefault = (_.copy(version = Version.version))
    def serialize(m: Model): Array[String] = Array(m.version)
    def validationErrors(m: Model): Option[String] = None
    override def deserialize(s: Array[String]) = {(m: Model) =>
      val versionString = s.mkString.trim
      if (versionString.startsWith("NetLogo") &&
        Version.is3D(versionString) == is3DFormat)
        Success(m.copy(version = versionString))
      else {
        val errorString =
          I18N.errors.getN("fileformat.invalidversion", AbstractNLogoFormat.this.name, Version.version, versionString)
        Failure(new NLogoFormatException(errorString))
      }
    }
  }

  lazy val defaultView: View = View(x = 210, y = 10, width = 439, height = 460,
    dimensions = WorldDimensions(-16, 16, -16, 16, 13.0), fontSize = 10, updateMode = UpdateMode.Continuous,
    showTickCounter = true, frameRate = 30)

  object InterfaceComponent extends ComponentSerialization[Array[String], A] {
    val componentName = "org.nlogo.modelsection.interface"
    private val additionalReaders = AbstractNLogoFormat.this.widgetReaders
    private val literalParser = Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")
    override def addDefault = _.copy(widgets = Seq(defaultView))

    def serialize(m: Model): Array[String] =
      m.widgets.flatMap((w: Widget) => (WidgetReader.format(w, additionalReaders).linesIterator.toSeq :+ "")).toArray

    def validationErrors(m: Model): Option[String] = None

    private def parseWidgets(lines: Array[String]): List[List[String]] = {
      val widgets = new collection.mutable.ListBuffer[List[String]]
      val widget = new collection.mutable.ListBuffer[String]
      for(line <- lines)
        if(line.nonEmpty)
          widget += line
        else {
          if(!widget.forall(_.isEmpty))
            widgets += widget.toList
          widget.clear()
        }
        if(!widget.isEmpty)
          widgets += widget.toList
        widgets.toList
    }

    override def deserialize(s: Array[String]) = {(m: Model) =>
      Try {
        val widgets = parseWidgets(s)
        m.copy(widgets = widgets.map(w => WidgetReader.read(w.toList, literalParser, additionalReaders)))
      }
    }
  }

  object VectorShapesComponent extends ComponentSerialization[Array[String], A] {
    val componentName = "org.nlogo.modelsection.turtleshapes"
    override def addDefault = _.copy(turtleShapes = Model.defaultShapes)
    def serialize(m: Model): Array[String] =
      ShapeParser.formatVectorShapes(m.turtleShapes).linesIterator.toArray
    def validationErrors(m: Model): Option[String] = None
    override def deserialize(s: Array[String]) = {(m: Model) =>
      Try {
        if (s.isEmpty) addDefault(m)
        else m.copy(turtleShapes = ShapeParser.parseVectorShapes(s))
      }
    }
  }

  object LinkShapesComponent extends ComponentSerialization[Array[String], A] {
    val componentName = "org.nlogo.modelsection.linkshapes"
    override def addDefault = ((m: Model) => m.copy(linkShapes = Model.defaultLinkShapes.toSeq))
    def serialize(m: Model): Array[String] = ShapeParser.formatLinkShapes(m.linkShapes).linesIterator.toArray
    def validationErrors(m: Model): Option[String] = None
    override def deserialize(s: Array[String]) = { (m: Model) =>
      Try {
        if (s.isEmpty) addDefault(m)
        else {
          val parsedShapes = ShapeParser.parseLinkShapes(s)
          m.copy(linkShapes = parsedShapes)
        }
      }
    }
  }

  def codeComponent       = CodeComponent
  def infoComponent       = InfoComponent
  def interfaceComponent  = InterfaceComponent
  def version             = VersionComponent
  def shapesComponent     = VectorShapesComponent
  def linkShapesComponent = LinkShapesComponent
}
