// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.{ GenericModelLoader, ComponentSerialization, ConfigurableModelLoader, NLogoXMLLoader }
import org.nlogo.fileformat
import org.nlogo.nvm.{ DefaultCompilerServices, PresentationCompilerInterface }

import org.picocontainer.PicoContainer
import org.picocontainer.adapters.AbstractAdapter

class ModelLoaderComponent extends AbstractAdapter[GenericModelLoader](classOf[GenericModelLoader], classOf[NLogoXMLLoader]) {
  def getDescriptor(): String = "ModelLoaderComponent"
  def verify(x$1: PicoContainer): Unit = {}

  def getComponentInstance(container: PicoContainer, into: java.lang.reflect.Type) = {
    val compiler         = container.getComponent(classOf[PresentationCompilerInterface])
    val compilerServices = new DefaultCompilerServices(compiler)

    fileformat.standardAnyLoader(compilerServices, true)
  }
}

class LegacyModelLoaderComponent extends AbstractAdapter[GenericModelLoader](classOf[GenericModelLoader], classOf[ConfigurableModelLoader]) {
  import fileformat.NLogoFormat

  def getDescriptor(): String = "LegacyModelLoaderComponent"

  def verify(x$1: PicoContainer): Unit = {}

  def getComponentInstance(container: PicoContainer, into: java.lang.reflect.Type) = {
    import scala.collection.JavaConverters._

    val compiler =
      container.getComponent(classOf[PresentationCompilerInterface])
    val compilerServices = new DefaultCompilerServices(compiler)
    val loader = fileformat.standardAnyLoader(compilerServices, true)
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
