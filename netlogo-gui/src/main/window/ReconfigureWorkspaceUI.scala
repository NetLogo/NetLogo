// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Container
import java.net.URI
import java.nio.file.{ Files, Paths }

import org.nlogo.window.Events.{ AfterLoadEvent, BeforeLoadEvent,
  LoadBeginEvent, LoadEndEvent, LoadModelEvent, LoadWidgetsEvent }
import org.nlogo.api.{ CompilerServices, ModelType }
import org.nlogo.core.Model

import scala.util.Try

object ReconfigureWorkspaceUI {
  def apply(linkParent: Container, uri: URI, modelType: ModelType, model: Model,
    compilerServices: CompilerServices, shouldAutoInstallLibs: Boolean = false): Unit = {
      new Loader(linkParent).loadHelper(uri, modelType, model, compilerServices, shouldAutoInstallLibs)
  }

  private case class Loader(linkParent: Container) extends org.nlogo.window.Event.LinkChild {
    def getLinkParent = linkParent

    def loadHelper( modelURI: URI, modelType: ModelType, model: Model, compilerServices: CompilerServices
                  , shouldAutoInstallLibs: Boolean = false) {
      val uriOption = Try(Paths.get(modelURI)).toOption
        .filterNot(p => p.getFileName.toString.startsWith("empty.nlogox"))
        .filter(p => Files.isRegularFile(p))
        .map(_.toString)
      val beforeEvents = List(
        new BeforeLoadEvent(uriOption, modelType),
        new LoadBeginEvent())

      val loadSectionEvents = List(
        new LoadModelEvent(model, shouldAutoInstallLibs),
        new LoadWidgetsEvent(model.widgets))

      val afterEvents = List(new LoadEndEvent(), new AfterLoadEvent())
      // fire missles! (actually, just fire the events...)
      for (e <- beforeEvents ::: loadSectionEvents ::: afterEvents) e.raise(this)
    }
  }
}
