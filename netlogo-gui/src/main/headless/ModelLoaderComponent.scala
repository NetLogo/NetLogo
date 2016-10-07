// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.{ AutoConvertable, ModelLoader, ComponentSerialization, CompilerServices, ConfigurableModelLoader,
  NetLogoLegacyDialect, NetLogoThreeDDialect, Workspace }

import org.nlogo.nvm.{ DefaultCompilerServices, CompilerInterface }

import org.picocontainer.PicoContainer
import org.picocontainer.adapters.AbstractAdapter

class ModelLoaderComponent extends AbstractAdapter[ModelLoader](classOf[ModelLoader], classOf[ConfigurableModelLoader]) {
  import org.nlogo.fileformat, fileformat.NLogoFormat
  import scala.collection.JavaConversions._

  def getDescriptor(): String = "ModelLoaderComponent"

  def verify(x$1: PicoContainer): Unit = {}

  def getComponentInstance(container: PicoContainer, into: java.lang.reflect.Type) = {
    val compiler =
      container.getComponent(classOf[CompilerInterface])
    val compilerServices = new DefaultCompilerServices(compiler)
    val loader = fileformat.standardLoader(compilerServices)
    val additionalComponents =
      container.getComponents(classOf[ComponentSerialization[Array[String], NLogoFormat]])
    if (additionalComponents.nonEmpty)
      additionalComponents.foldLeft(loader) {
        case (l, serialization) =>
          l.addSerializer[Array[String], NLogoFormat](serialization)
      }
      else
        loader
  }
}
