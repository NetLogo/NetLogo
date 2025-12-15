// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.{ FileNotFoundException, Writer }
import java.net.URI
import java.nio.file.{ Files, Paths }

import org.nlogo.core.{ I18N, Model }

import scala.reflect.ClassTag
import scala.util.{ Failure, Try }

trait AbstractModelLoader {
  def isCompatible(uri: URI): Boolean
  def isCompatible(extension: String): Boolean
  def readModel(uri: URI): Try[Model]
  def readModel(source: String, extension: String): Try[Model]
  def save(model: Model, uri: URI): Try[URI]
  def sourceString(model: Model, extension: String): Try[String]
  def emptyModel(extension: String): Model
  // these next two allow ManagerDialog to use the correct format for experiment loading/saving (Isaac B 8/17/24)
  def readExperiments(source: String, editNames: Boolean, existingNames: Set[String]): Try[(Seq[LabProtocol], Set[String])]
  def writeExperiments(experiments: Seq[LabProtocol], writer: Writer): Try[Unit]
}

object AbstractModelLoader {
  def getURIExtension(uri: URI): Option[String] = {
    if (uri.getScheme == "jar")
      uri.getSchemeSpecificPart.split("\\.").lastOption
    else
      uri.getPath.split("\\.").lastOption
  }
}

class FormatterPair[A, B <: ModelFormat[A, B]](
  val modelFormat: B,
  val serializers: Seq[ComponentSerialization[A, B]])(implicit aTag: ClassTag[A]) {

    def isCompatible(source: String) = modelFormat.isCompatible(source)
    def isCompatible(uri: java.net.URI) = modelFormat.isCompatible(uri)
    def isCompatible(model: Model) = modelFormat.isCompatible(model)
    def serializationClass = aTag.runtimeClass
    def formatClass = modelFormat.getClass

    def addSerializer(s: ComponentSerialization[A, B]): FormatterPair[A, B] =
      new FormatterPair[A, B](modelFormat, serializers :+ s)

    def addSerializers(s: Seq[ComponentSerialization[A, B]]): FormatterPair[A, B] =
      new FormatterPair[A, B](modelFormat, serializers ++ s)

    def name = modelFormat.name

    def load(uri: URI): Try[Model] =
      modelFormat.load(uri, serializers)

    def load(source: String): Try[Model] =
      modelFormat.load(source, serializers)

    def save(model: Model, uri: URI): Try[URI] =
      modelFormat.save(model, uri, serializers)

    def sourceString(model: Model): Try[String] =
      modelFormat.sourceString(model, serializers)

    def emptyModel: Model =
      modelFormat.emptyModel(serializers)

    def readExperiments(source: String, editNames: Boolean, existingNames: Set[String]): Try[(Seq[LabProtocol], Set[String])] =
      modelFormat.readExperiments(source, editNames, existingNames)

    def writeExperiments(experiments: Seq[LabProtocol], writer: Writer): Try[Unit] =
      modelFormat.writeExperiments(experiments, writer)

  }

trait ModelLoader extends AbstractModelLoader {
  def formats: Seq[FormatterPair[?, ?]]

  override def isCompatible(uri: URI): Boolean =
    AbstractModelLoader.getURIExtension(uri).exists(isCompatible)

  override def isCompatible(extension: String): Boolean =
    extension == "nlogo" || extension == "nlogo3d"

  def readModel(uri: URI): Try[Model] = {
    formats.find(_.isCompatible(uri)) match {
      case None =>
        Failure(new Exception(
          s"Unable to open model with current format: ${uri.getPath}."))
      case Some(formatter) => formatter.load(uri)
    }
  }

  def readModel(source: String, extension: String): Try[Model] = {
    formats.find(_.isCompatible(source)) match {
      case None =>
        Failure(new Exception(
          s"Unable to open model with current format: $extension."))
      case Some(formatter) => formatter.load(source)
    }
  }

  def save(model: Model, uri: URI): Try[URI] = {
    formats.find(_.isCompatible(model)) match {
      case None =>
        Failure(new Exception(s"Unable to save NetLogo model in format specified by ${uri.getPath}."))
      case Some(formatter) => formatter.save(model, uri)
    }
  }

  def sourceString(model: Model, extension: String): Try[String] = {
    formats.find(_.isCompatible(model)) match {
      case None =>
        Failure(new Exception(
          s"Unable to get source for NetLogo model in format: $extension."))
      case Some(formatter) => formatter.sourceString(model)
    }
  }

  def emptyModel(extension: String): Model = {
    val format = formats.find(_.name == extension)
      .getOrElse(throw new Exception(s"Unable to create empty NetLogo model for format: $extension."))
    format.emptyModel
  }

  def readExperiments(source: String, editNames: Boolean, existingNames: Set[String]): Try[(Seq[LabProtocol], Set[String])] = {
    val init: Try[(Seq[LabProtocol], Set[String])] = Failure(new Exception("Unable to read experiments."))
    formats.foldLeft(init) {
      case (acc, format) => if (acc.isSuccess) acc else format.readExperiments(source, editNames, existingNames)
    }
  }

  def writeExperiments(experiments: Seq[LabProtocol], writer: Writer): Try[Unit] = {
    val init: Try[Unit] = Failure(new Exception("Unable to write experiments."))
    formats.foldLeft(init) {
      case (acc, format) => if (acc.isSuccess) acc else format.writeExperiments(experiments, writer)
    }
  }
}

class ConfigurableModelLoader(val formats: Seq[FormatterPair[?, ?]] = Seq()) extends ModelLoader {
  override def readModel(uri: URI): Try[Model] = {
    if (uri.getScheme == "file") {
      val path = Paths.get(uri)
      if (!Files.exists(path)) {
        val message = I18N.errors.getN("fileformat.notFound", path)
        return Failure(new FileNotFoundException(message))
      }
    }
    super.readModel(uri)
  }

  def addFormat[A, B <: ModelFormat[A, B]](f: B)(implicit aTag: ClassTag[A]): ConfigurableModelLoader =
    new ConfigurableModelLoader(formats :+ new FormatterPair[A, B](f, Seq()))

  def addSerializers[A, B <: ModelFormat[A, B]](ss: Seq[ComponentSerialization[A, B]])(
    implicit aTag:  ClassTag[A],
    bTag:           ClassTag[B],
    matchingFormat: ClassTag[FormatterPair[A, B]]): ConfigurableModelLoader =
      new ConfigurableModelLoader(formats.map {
        case matchingFormat(fp) if fp.serializationClass == aTag.runtimeClass &&
        fp.formatClass == bTag.runtimeClass =>
          fp.addSerializers(ss)
        case f => f
      })

  def addSerializer[A, B <: ModelFormat[A, B]](s: ComponentSerialization[A, B])(
    implicit aTag:  ClassTag[A],
    bTag:           ClassTag[B],
    matchingFormat: ClassTag[FormatterPair[A, B]]): ConfigurableModelLoader =
      new ConfigurableModelLoader(formats.map { formatPair =>
        formatPair match {
          case matchingFormat(fp) if fp.serializationClass == aTag.runtimeClass
            && fp.formatClass == bTag.runtimeClass =>
            fp.addSerializer(s)
          case v => v
        }
      })
}
