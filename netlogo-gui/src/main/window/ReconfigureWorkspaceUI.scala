// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Container
import java.net.URI
import java.nio.file.{ Files, Paths }

import org.nlogo.api.{ CompilerServices, ModelCoordinator, ModelType }
import org.nlogo.core.Model
import org.nlogo.window.Events.LoadBeginEvent

import scala.util.Try

object ReconfigureWorkspaceUI {
  def apply(coordinator: ModelCoordinator, linkParent: Container, uri: URI, modelType: ModelType, model: Model,
            compilerServices: CompilerServices, shouldAutoInstallLibs: Boolean = false,
            convertWidgetSizes: Boolean = false): Unit = {
    new Loader(linkParent).loadHelper(coordinator, uri, modelType, model, compilerServices, shouldAutoInstallLibs,
                                      convertWidgetSizes)
  }

  private case class Loader(linkParent: Container) extends org.nlogo.window.Event.LinkChild {
    def getLinkParent = linkParent

    def loadHelper(coordinator: ModelCoordinator, modelURI: URI, modelType: ModelType, model: Model,
                   compilerServices: CompilerServices, shouldAutoInstallLibs: Boolean,
                   convertWidgetSizes: Boolean): Unit = {
      val uriOption = Try(Paths.get(modelURI)).toOption
        .filterNot(p => p.getFileName.toString.startsWith("empty.nlogox"))
        .filter(p => Files.isRegularFile(p))
        .map(_.toString)

      coordinator.beforeLoad(uriOption, modelType)

      new LoadBeginEvent().raise(this)

      coordinator.loadModel(model)
      coordinator.loadWidgets(model.widgets, convertWidgetSizes)
      coordinator.loadEnd(shouldAutoInstallLibs)
      coordinator.afterLoad()
    }
  }
}
