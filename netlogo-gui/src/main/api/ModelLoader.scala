
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
  }

trait ModelLoader {
  def formats: Seq[FormatterPair[_, _]]

  def readModel(uri: URI): Try[Model] = {
    val format =
      uri.getPath.split("\\.").lastOption
        .flatMap(extension => formats.find(_.name == extension))
        .getOrElse(
          throw new Exception("Unable to open NetLogo model " + uri.getPath))
    format.load(uri)
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
