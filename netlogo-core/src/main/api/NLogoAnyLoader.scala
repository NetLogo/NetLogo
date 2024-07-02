// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.net.URI

import org.nlogo.core.Model

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
