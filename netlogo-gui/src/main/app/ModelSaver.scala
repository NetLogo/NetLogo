// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.ModelLoader
import org.nlogo.core.Model
import org.nlogo.workspace.ModelTracker
import org.nlogo.window.Events, Events.{ LoadModelEvent, ModelSavedEvent }

@deprecated("ModelSaver will be removed in a future version", "6.1.0")
class ModelSaver(modelTracker: ModelTracker, loader: ModelLoader)
  extends LoadModelEvent.Handler
  with ModelSavedEvent.Handler {

  def this(app: App, loader: ModelLoader) = this(app.modelTracker, loader)

  private var _priorModel: Model = Model()

  def handle(e: LoadModelEvent): Unit = {
    _priorModel = e.model
  }

  def handle(e: ModelSavedEvent): Unit = {
    _priorModel = currentModel
  }

  @deprecated("Use ModelSaver.priorModel will be removed in a future version", "6.1.0")
  def priorModel: Model = _priorModel

  @deprecated("Use workspace.modelTracker.model instead", "6.1.0")
  def currentModel = modelTracker.model

  @deprecated("Use workspace.modelTracker.model instead", "6.1.0")
  def currentModelInCurrentVersion: Model = currentModel

  @deprecated("Have the workspace open a model instead of setting it as current", "6.1.0")
  def setCurrentModel(m: Model) = {
    _priorModel = m
  }

  @deprecated("Use modelLoader.sourceString(Model, String) instead", "6.1.0")
  // this is only used by Modeling Commons and NetLogo Web Export
  // at the moment. It should *not* be used by anything else
  // (and it shouldn't be used by NLW or MC either if they can be changed).
  private[nlogo] def modelAsString(model: Model, format: String): String = {
    loader.sourceString(model, format).get
  }
}
