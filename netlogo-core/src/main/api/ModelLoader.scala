
package org.nlogo.api

import java.net.URI

import org.nlogo.core.Model

import scala.util.Try
import scala.reflect.ClassTag

class FormatterPair[A, B <: ModelFormat[A, B]](
  val modelFormat: B,
  val serializers: Seq[ComponentSerialization[A, B]]) {
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
  }

object ModelLoader {
  def getURIExtension(uri: URI): Option[String] = {
    if (uri.getScheme == "jar")
      uri.getSchemeSpecificPart.split("\\.").lastOption
    else
      uri.getPath.split("\\.").lastOption
  }
}

trait ModelLoader {
  def formats: Seq[FormatterPair[_, _]]

  protected def uriFormat(uri: URI): Option[FormatterPair[_, _]] =
    ModelLoader.getURIExtension(uri)
      .flatMap(extension => formats.find(_.name == extension))

  def readModel(uri: URI): Try[Model] = {
    val format = uriFormat(uri)
      .getOrElse(throw new Exception("Unable to open NetLogo model " + uri.getPath))
    format.load(uri)
  }

  def readModel(source: String, extension: String): Try[Model] = {
    val format =
      formats.find(_.name == extension)
        .getOrElse(throw new Exception("Unable to open model with extension: " + extension))
    format.load(source)
  }

  def save(model: Model, uri: URI): Try[URI] = {
    val format = uriFormat(uri)
      .getOrElse(throw new Exception("Unable to save NetLogo model in format specified by " + uri.getPath))
    format.save(model, uri)
  }

  def sourceString(model: Model, extension: String): Try[String] = {
    val format =
      formats.find(_.name == extension)
        .getOrElse(throw new Exception("Unable to get source for NetLogo model in format: " + extension))
    format.sourceString(model)
  }

  def emptyModel(extension: String): Model = {
    val format = formats.find(_.name == extension)
      .getOrElse(throw new Exception("Unable to create empty NetLogo model for format: " + extension))
    format.emptyModel
  }
}

class ConfigurableModelLoader(val formats: Seq[FormatterPair[_, _]] = Seq()) extends ModelLoader {
  def addFormat[A, B <: ModelFormat[A, B]](f: B): ConfigurableModelLoader =
    new ConfigurableModelLoader(formats :+ new FormatterPair[A, B](f, Seq()))

  def addSerializers[A, B <: ModelFormat[A, B]](ss: Seq[ComponentSerialization[A, B]])(
    implicit matchingFormat: ClassTag[FormatterPair[A, B]]): ConfigurableModelLoader =
      new ConfigurableModelLoader(formats.map {
        case matchingFormat(fp) => fp.addSerializers(ss)
        case v => v
      })

  def addSerializer[A, B <: ModelFormat[A, B]](s: ComponentSerialization[A, B])(
    implicit matchingFormat: ClassTag[FormatterPair[A, B]]): ConfigurableModelLoader =
      new ConfigurableModelLoader(formats.map {
        case matchingFormat(fp) => fp.addSerializer(s)
        case v => v
      })
}
