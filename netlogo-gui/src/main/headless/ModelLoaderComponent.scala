// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.core.Femto
import org.nlogo.api.{ ModelLoader, ComponentSerialization, ConfigurableModelLoader }

import org.nlogo.nvm.{ DefaultCompilerServices, PresentationCompilerInterface }

import org.picocontainer.PicoContainer
import org.picocontainer.adapters.AbstractAdapter

import scala.collection.JavaConverters._

class ModelLoaderComponent extends AbstractAdapter[ModelLoader](classOf[ModelLoader], classOf[ConfigurableModelLoader]) {
  import org.nlogo.fileformat, fileformat.NLogoFormat

  def getDescriptor(): String = "ModelLoaderComponent"

  def verify(x$1: PicoContainer): Unit = {}

  def getComponentInstance(container: PicoContainer, into: java.lang.reflect.Type) = {
    val literalParser =
      Femto.scalaSingleton[org.nlogo.core.LiteralParser]("org.nlogo.parse.CompilerUtilities")
    val loader = fileformat.standardLoader(literalParser)

    val additionalComponents =
      container.getComponents(classOf[ComponentSerialization[Array[String], NLogoFormat]]).asScala
    if (additionalComponents.nonEmpty)
      additionalComponents.foldLeft(loader) {
        case (l, serialization) =>
          l.addSerializer[Array[String], NLogoFormat](serialization)
      }
      else
        loader
  }
}
