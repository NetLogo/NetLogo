// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Container
import java.net.URI
import java.nio.file.Paths

import org.nlogo.window.Events.{ AfterLoadEvent, BeforeLoadEvent,
  LoadBeginEvent, LoadEndEvent, LoadModelEvent, LoadWidgetsEvent }
import org.nlogo.api.{ CompilerServices, ModelType }
import org.nlogo.core.Model

import scala.util.Try

object ReconfigureWorkspaceUI {
  def apply(linkParent: Container, uri: URI, modelType: ModelType, model: Model,
    compilerServices: CompilerServices): Unit = {
      new Loader(linkParent).loadHelper(uri, modelType, model, compilerServices)
  }

  private case class Loader(linkParent: Container) extends org.nlogo.window.Event.LinkChild {
    def getLinkParent = linkParent

    def loadHelper(modelURI: URI, modelType: ModelType, model: Model, compilerServices: CompilerServices) {
      val uriOption = Try(Paths.get(modelURI).toString).toOption
      val beforeEvents = List(
        new BeforeLoadEvent(uriOption, modelType),
        new LoadBeginEvent())

      val loadSectionEvents = List(
        new LoadModelEvent(model),
        new LoadWidgetsEvent(model.widgets))

      val afterEvents = List(new LoadEndEvent(), new AfterLoadEvent())
      // fire missles! (actually, just fire the events...)
      for (e <- beforeEvents ::: loadSectionEvents ::: afterEvents) e.raise(this)
    }
  }
}
