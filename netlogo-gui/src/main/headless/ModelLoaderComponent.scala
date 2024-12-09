// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.{ AbstractModelLoader, ComponentSerialization, ConfigurableModelLoader }
import org.nlogo.fileformat.{ FileFormat, NLogoFormat, NLogoXMLLoader }
import org.nlogo.nvm.{ DefaultCompilerServices, PresentationCompilerInterface }

import org.picocontainer.PicoContainer
import org.picocontainer.adapters.AbstractAdapter

class ModelLoaderComponent extends AbstractAdapter[AbstractModelLoader](classOf[AbstractModelLoader], classOf[NLogoXMLLoader]) {

  override def getDescriptor: String = "ModelLoaderComponent"
  override def verify(x$1: PicoContainer): Unit = ()

  override def getComponentInstance(container: PicoContainer, into: java.lang.reflect.Type) = {
    val compiler         = container.getComponent(classOf[PresentationCompilerInterface])
    val compilerServices = new DefaultCompilerServices(compiler)
    FileFormat.standardAnyLoader(compilerServices, true)
  }

}

class LegacyModelLoaderComponent extends AbstractAdapter[AbstractModelLoader](classOf[AbstractModelLoader], classOf[ConfigurableModelLoader]) {

  override def getDescriptor: String = "LegacyModelLoaderComponent"
  override def verify(x$1: PicoContainer): Unit = ()

  override def getComponentInstance(container: PicoContainer, into: java.lang.reflect.Type) = {

    import scala.collection.JavaConverters.iterableAsScalaIterableConverter

    val compiler             = container.getComponent(classOf[PresentationCompilerInterface])
    val compilerServices     = new DefaultCompilerServices(compiler)
    val loader               = FileFormat.standardAnyLoader(compilerServices, true)
    val additionalComponents = container.getComponents(classOf[ComponentSerialization[Array[String], NLogoFormat]]).asScala

    if (additionalComponents.nonEmpty)
      additionalComponents.foldLeft(loader) {
        case (l, serialization) =>
          l.addSerializer[Array[String], NLogoFormat](serialization)
      }
    else
      loader

  }

}
