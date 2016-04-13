// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Container
import java.net.URI
import java.nio.file.Paths

import org.nlogo.window.Events.{ AfterLoadEvent, BeforeLoadEvent,
  LoadBeginEvent, LoadEndEvent, LoadModelEvent, LoadWidgetsEvent }
import org.nlogo.api.{ CompilerServices, ModelType }
import org.nlogo.core.{ I18N, Model }

object ModelLoader {
  def load(linkParent: Container, uri: URI, modelType: ModelType, model: Model,
    compilerServices: CompilerServices): Unit = {
      new Loader(linkParent).loadHelper(uri, modelType, model, compilerServices)
  }

  private case class Loader(linkParent: Container) extends org.nlogo.window.Event.LinkChild {
    def getLinkParent = linkParent

    def loadHelper(modelURI: URI, modelType: ModelType, model: Model, compilerServices: CompilerServices) {
      val beforeEvents = List(
        new BeforeLoadEvent(Paths.get(modelURI).toString, modelType),
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
