// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

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
    val compiler =
      container.getComponent(classOf[PresentationCompilerInterface])
    val compilerServices = new DefaultCompilerServices(compiler)
    val loader = fileformat.standardLoader(compilerServices)
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
