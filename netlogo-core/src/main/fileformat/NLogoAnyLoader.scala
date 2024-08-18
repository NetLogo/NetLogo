// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.io.Writer
import java.net.URI

import org.nlogo.api.{ ComponentSerialization, ConfigurableModelLoader, FormatterPair, GenericModelLoader,
                       LabProtocol, ModelFormat }
import org.nlogo.core.Model

import scala.collection.mutable.Set
import scala.reflect.ClassTag
import scala.util.{ Failure, Try }

class NLogoAnyLoader(loaders: List[GenericModelLoader]) extends ConfigurableModelLoader {
  override def readModel(uri: URI): Try[Model] = {
    for (loader <- loaders) {
      Try {
        return loader.readModel(uri)
      }
    }

    Failure(new Exception("Unable to read model with format \"" +
                          GenericModelLoader.getURIExtension(uri).getOrElse("") + "\"."))
  }

  override def readModel(source: String, extension: String): Try[Model] = {
    for (loader <- loaders) {
      Try {
        return loader.readModel(source, extension)
      }
    }

    Failure(new Exception("Unable to read model with format \"" + extension + "\"."))
  }

  override def save(model: Model, uri: URI): Try[URI] = {
    for (loader <- loaders) {
      Try {
        return loader.save(model, uri)
      }
    }

    Failure(new Exception("Unable to save model with format \"" +
                          GenericModelLoader.getURIExtension(uri).getOrElse("") + "\"."))
  }

  override def sourceString(model: Model, extension: String): Try[String] = {
    for (loader <- loaders) {
      Try {
        return loader.sourceString(model, extension)
      }
    }

    Failure(new Exception("Unable to create source string for model with format \"" + extension + "\"."))
  }

  override def emptyModel(extension: String): Model = {
    for (loader <- loaders) {
      Try {
        return loader.emptyModel(extension)
      }
    }

    throw new Exception("Unable to create empty model for format \"" + extension + "\".")
  }

  override def readExperiments(source: String, editNames: Boolean, existingNames: Set[String]):
    Try[Seq[LabProtocol]] = {
    for (loader <- loaders) {
      Try {
        return loader.readExperiments(source, editNames, existingNames)
      }
    }

    Failure(new Exception("Unable to read experiments."))
  }

  override def writeExperiments(experiments: Seq[LabProtocol], writer: Writer): Try[Unit] = {
    for (loader <- loaders) {
      Try {
        return loader.writeExperiments(experiments, writer)
      }
    }

    Failure(new Exception("Unable to write experiments."))
  }

  override def addSerializers[A, B <: ModelFormat[A, B]](ss: Seq[ComponentSerialization[A, B]])
    (implicit aTag: ClassTag[A], bTag: ClassTag[B], matchingFormat: ClassTag[FormatterPair[A, B]]): NLogoAnyLoader = {
    
    new NLogoAnyLoader(loaders.map(loader => {
      loader match {
        case loader: ConfigurableModelLoader =>
          loader.addSerializers[A, B](ss)(aTag, bTag, matchingFormat)

        case _ => loader
      }
    }))
  }

  override def addSerializer[A, B <: ModelFormat[A, B]](s: ComponentSerialization[A, B])
    (implicit aTag: ClassTag[A], bTag: ClassTag[B], matchingFormat: ClassTag[FormatterPair[A, B]]): NLogoAnyLoader = {

    new NLogoAnyLoader(loaders.map(loader => {
      loader match {
        case loader: ConfigurableModelLoader =>
          loader.addSerializer[A, B](s)(aTag, bTag, matchingFormat)

        case _ => loader
      }
    }))
  }
}
