// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.Writer
import java.net.URI

import org.nlogo.api.{ AbstractModelLoader, ComponentSerialization, ConfigurableModelLoader, FormatterPair,
                       LabProtocol, ModelFormat }
import org.nlogo.core.Model

import scala.reflect.ClassTag
import scala.util.{ Failure, Try }

class NLogoAnyLoader(loaders: List[AbstractModelLoader]) extends ConfigurableModelLoader {
  override def isCompatible(uri: URI): Boolean =
    loaders.exists(_.isCompatible(uri))

  override def isCompatible(extension: String): Boolean =
    loaders.exists(_.isCompatible(extension))

  private def bruteForce[T](check: AbstractModelLoader => Boolean, transform: AbstractModelLoader => Try[T])
                           (defaultError: String): Try[T] =
    loaders.find(check).map(transform).getOrElse(Failure(new Exception(defaultError)))

  override def readModel(uri: URI): Try[Model] = {
    val errorMessage =
      s"""Unable to read model with format "${AbstractModelLoader.getURIExtension(uri).getOrElse("")}"."""
    bruteForce(_.isCompatible(uri), _.readModel(uri))(errorMessage)
  }

  override def readModel(source: String, extension: String): Try[Model] = {
    val errorMessage = s"""Unable to read model with format "${extension}"."""
    bruteForce(_.isCompatible(extension), _.readModel(source, extension))(errorMessage)
  }

  override def save(model: Model, uri: URI): Try[URI] = {
    val errorMessage =
      s"""Unable to save model with format "${AbstractModelLoader.getURIExtension(uri).getOrElse("")}"."""
    bruteForce(_.isCompatible(uri), _.save(model, uri))(errorMessage)
  }

  override def sourceString(model: Model, extension: String): Try[String] = {
    val errorMessage = s"""Unable to create source string for model with format "${extension}"."""
    bruteForce(_.isCompatible(extension), _.sourceString(model, extension))(errorMessage)
  }

  override def emptyModel(extension: String): Model = {
    val errorMessage =
      s"""Unable to create empty model for format "${extension}"."""
    bruteForce(_.isCompatible(extension), l => Try(l.emptyModel(extension)))(errorMessage).get
  }

  override def readExperiments(source: String, editNames: Boolean, existingNames: Set[String]):
      Try[(Seq[LabProtocol], Set[String])] = {
    val errorMessage = "Unable to read experiments."
    bruteForce(_ => true, _.readExperiments(source, editNames, existingNames))(errorMessage)
  }

  override def writeExperiments(experiments: Seq[LabProtocol], writer: Writer): Try[Unit] = {
    val errorMessage = "Unable to write experiments."
    bruteForce(_ => true, _.writeExperiments(experiments, writer))(errorMessage)
  }

  override def addSerializers[A, B <: ModelFormat[A, B]](ss: Seq[ComponentSerialization[A, B]])
    (implicit aTag: ClassTag[A], bTag: ClassTag[B], matchingFormat: ClassTag[FormatterPair[A, B]]): NLogoAnyLoader = {

    new NLogoAnyLoader(loaders.map(loader => {
      loader match {
        case loader: ConfigurableModelLoader =>
          loader.addSerializers[A, B](ss)(using aTag, bTag, matchingFormat)
        case x => x
      }
    }))
  }

  override def addSerializer[A, B <: ModelFormat[A, B]](s: ComponentSerialization[A, B])
    (implicit aTag: ClassTag[A], bTag: ClassTag[B], matchingFormat: ClassTag[FormatterPair[A, B]]): NLogoAnyLoader = {
      addSerializers(Seq(s))
  }

}
