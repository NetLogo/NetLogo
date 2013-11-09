// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo

import scala.Option.option2Iterable
import scala.collection.JavaConverters._

import org.nlogo.window.GUIWorkspace
import org.nlogo.window.Widget

package object review {

  /** Returns all widgets in workspace */
  def workspaceWidgets(ws: GUIWorkspace): Seq[Widget] =
    Option(ws.viewWidget.findWidgetContainer)
      .toSeq.flatMap(_.getWidgetsForSaving.asScala)

}