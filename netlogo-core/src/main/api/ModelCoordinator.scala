// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ Model, Widget }

trait ModelCoordinator {
  def beforeLoad(modelPath: Option[String], modelType: ModelType): Unit
  def loadModel(model: Model): Unit
  def loadWidgets(widgets: Seq[Widget], convertWidgetSizes: Boolean): Unit
  def loadEnd(shouldAutoInstallLibs: Boolean): Unit
  def afterLoad(): Unit
  def modelSaved(modelPath: String, isNew: Boolean): Unit
}
