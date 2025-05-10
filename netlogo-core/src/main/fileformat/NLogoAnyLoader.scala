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

  private def bruteForce[T, U](ts: Seq[T], f: (T) => Try[U])(errorMessage: String): Try[U] = {
    val init: Try[U] = Failure(new Exception(errorMessage))
    ts.foldLeft(init) {
      case (acc, t) => if (acc.isSuccess) acc else f(t)
    }
  }

  override def readModel(uri: URI): Try[Model] = {
    val errorMessage = s"""Unable to read model with format "${AbstractModelLoader.getURIExtension(uri).getOrElse("")}"."""
    bruteForce(loaders, (_: AbstractModelLoader).readModel(uri))(errorMessage)
  }

  override def readModel(source: String, extension: String): Try[Model] = {
    val errorMessage = s"""Unable to read model with format "${extension}"."""
    bruteForce(loaders, (_: AbstractModelLoader).readModel(source, extension))(errorMessage)
  }

  override def save(model: Model, uri: URI): Try[URI] = {
    val errorMessage = s"""Unable to save model with format "${AbstractModelLoader.getURIExtension(uri).getOrElse("")}"."""
    bruteForce(loaders, (_: AbstractModelLoader).save(model, uri))(errorMessage)
  }

  override def sourceString(model: Model, extension: String): Try[String] = {
    val errorMessage = s"""Unable to create source string for model with format "${extension}"."""
    bruteForce(loaders, (_: AbstractModelLoader).sourceString(model, extension))(errorMessage)
  }

  override def emptyModel(extension: String): Model = {
    val errorMessage = s"""Unable to create empty model for format "${extension}"."""
    bruteForce(loaders, (gml: AbstractModelLoader) => Try(gml.emptyModel(extension)))(errorMessage).get
  }

  override def readExperiments(source: String, editNames: Boolean, existingNames: Set[String]):
      Try[(Seq[LabProtocol], Set[String])] = {
    val errorMessage = "Unable to read experiments."
    bruteForce(loaders, (_: AbstractModelLoader).readExperiments(source, editNames, existingNames))(errorMessage)
  }

  override def writeExperiments(experiments: Seq[LabProtocol], writer: Writer): Try[Unit] = {
    val errorMessage = "Unable to write experiments."
    bruteForce(loaders, (_: AbstractModelLoader).writeExperiments(experiments, writer))(errorMessage)
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
