// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.lang.reflect.{ Type => JType }

import org.nlogo.core.Dialect
import org.nlogo.api.{ AbstractModelLoader, AddableComponent, AutoConvertable, ConfigurableModelLoader,
                       Workspace => ApiWorkspace }
import org.nlogo.fileformat.{ FileFormat, ModelConverter, NLogoAnyLoader, NLogoXMLLoader }
import org.nlogo.fileformat.FileFormat.ModelConversion
import org.nlogo.nvm.{ DefaultCompilerServices, PresentationCompilerInterface }

import org.picocontainer.PicoContainer
import org.picocontainer.adapters.AbstractAdapter

object Adapters {

  class AnyModelLoaderComponent extends AbstractAdapter[AbstractModelLoader](classOf[AbstractModelLoader], classOf[NLogoAnyLoader]) {

    override def getDescriptor: String = "AnyModelLoaderComponent"
    override def verify(p: PicoContainer): Unit = ()

    override def getComponentInstance(container: PicoContainer, into: JType): AbstractModelLoader = {

      import scala.jdk.CollectionConverters.IterableHasAsScala

      val compiler = container.getComponent(classOf[PresentationCompilerInterface])
      val compilerServices = new DefaultCompilerServices(compiler)

      val loader: ConfigurableModelLoader = FileFormat.standardAnyLoader(false, compilerServices, true)

      val components = container.getComponents(classOf[AddableComponent]).asScala

      if (components.nonEmpty) {
        components.foldLeft(loader) {
          case (loader, component) => component.addToLoader(loader)
        }
      } else {
        loader
      }

    }

  }

  class XMLModelLoaderComponent extends AbstractAdapter[AbstractModelLoader](classOf[AbstractModelLoader], classOf[NLogoXMLLoader]) {

    override def getDescriptor: String = "XMLModelLoaderComponent"
    override def verify(p: PicoContainer): Unit = ()

    override def getComponentInstance(container: PicoContainer, into: JType): AbstractModelLoader = {
      val compiler = container.getComponent(classOf[PresentationCompilerInterface])
      val compilerServices = new DefaultCompilerServices(compiler)
      FileFormat.standardXMLLoader(false, compilerServices, true)
    }

  }

  class ModelLoaderComponent extends AbstractAdapter[AbstractModelLoader](classOf[AbstractModelLoader], classOf[ConfigurableModelLoader]) {

    override def getDescriptor: String = "ModelLoaderComponent"
    override def verify(p: PicoContainer): Unit = ()

    override def getComponentInstance(container: PicoContainer, into: JType): AbstractModelLoader = {

      import scala.jdk.CollectionConverters.IterableHasAsScala

      val compiler             = container.getComponent(classOf[PresentationCompilerInterface])
      val compilerServices     = new DefaultCompilerServices(compiler)
      val loader               = FileFormat.standardLoader(compilerServices, true)
      val additionalComponents = container.getComponents(classOf[AddableComponent]).asScala

      if (additionalComponents.nonEmpty) {
        additionalComponents.foldLeft(loader) {
          case (l, component) => component.addToLoader(l)
        }
      } else {
        loader
      }

    }

  }

  class ModelConverterComponent extends AbstractAdapter[ModelConversion](classOf[ModelConversion], classOf[ModelConverter]) {

    override def getDescriptor: String = "ModelConverterComponent"
    override def verify(x: PicoContainer): Unit = ()

    override def getComponentInstance(container: PicoContainer, into: JType): ModelConversion = {

      import scala.jdk.CollectionConverters.IterableHasAsScala

      val workspace = container.getComponent(classOf[ApiWorkspace])

      val allAutoConvertables =
        FileFormat.defaultAutoConvertables ++ container.getComponents(classOf[AutoConvertable]).asScala

      FileFormat.converter( workspace.getExtensionManager, workspace.getLibraryManager
                          , workspace.getCompilationEnvironment, workspace, allAutoConvertables)(
                            container.getComponent(classOf[Dialect]))
    }

  }

}
