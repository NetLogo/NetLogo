// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.api
import org.nlogo.mirror.{ FixedViewSettings, Mirrorables, ModelRun }
import org.nlogo.swing.Implicits.thunk2runnable
import org.nlogo.window.{ GUIWorkspace, MonitorWidget, Widget, WidgetWrapperInterface }

import javax.swing.JOptionPane

class RunRecorder(
  ws: GUIWorkspace,
  tabState: ReviewTabState,
  runList: RunList,
  saveModel: () => String,
  widgetHooks: () => Seq[WidgetHook],
  disableRecording: () => Unit,
  refreshInterface: () => Unit // TODO replace with event
  ) {

  /**
   * The PlotActionBuffer logs all plotting actions, whether we are
   * recording or not. This is important because if we want to start
   * recording at some point, we need to mirror all actions from the
   * start to bring the plots to their actual state. NP 2012-11-29
   * Same logic goes for the drawingActionBuffer. NP 2013-01-28.
   */
  private val plotActionBuffer = new api.ActionBuffer(ws.plotManager)
  private val drawingActionBuffer = new api.ActionBuffer(ws.drawingActionBroker)
  private val actionBuffers = Vector(plotActionBuffer, drawingActionBuffer)
  //  actionBuffers.foreach(_.clear()) // make sure object is constructed and subscribed

  ws.listenerManager.addListener(
    new api.NetLogoAdapter {
      override def requestedDisplayUpdate() {
        if (tabState.currentlyRecording) {
          updateMonitors()
          // switch from job thread to event thread
          ws.waitFor(() => grab())
          refreshInterface()
        }
      }
      override def modelOpened(name: String) {
        // clearing the ticks doesn't send tickCounterChanged if the ticks
        // were already at -1.0, so we make sure to clear the actions of a
        // potentially "tickless" model when we open a new one.
        actionBuffers.foreach(_.clear())
        tabState.currentRun.foreach(_.stillRecording = false)
      }
      override def tickCounterChanged(ticks: Double) {
        ticks match {
          case -1.0 =>
            actionBuffers.foreach(_.clear())
            tabState.currentRun.foreach(_.stillRecording = false)
          case 0.0 =>
            ws.waitFor(() => startNewRun())
          case _ => // requestedDisplayUpdate() takes care of the rest
        }
      }
    })

  def grab() {
    for (run <- tabState.currentRun) {
      try {
        val widgetValues = widgetHooks()
          .map(_.valueStringGetter.apply)
          .zipWithIndex
        val mirrorables = Mirrorables.allMirrorables(ws.world, widgetValues)
        val actions = actionBuffers.flatMap(_.grab())
        run.data match {
          case None       => run.start(ws.plotManager.plots, mirrorables, actions)
          case Some(data) => data.append(mirrorables, actions)
        }
      } catch {
        case e: java.lang.OutOfMemoryError =>
          JOptionPane.showMessageDialog(null,
            "Not enough memory. Turning off recording.",
            "Low memory", JOptionPane.WARNING_MESSAGE)
          disableRecording()
      }
      refreshInterface()
    }
  }

  def startNewRun() {

    val wrapper = org.nlogo.awt.Hierarchy.findAncestorOfClass(
      ws.viewWidget, classOf[WidgetWrapperInterface])
    val wrapperPos = wrapper.map(_.getLocation).getOrElse(new java.awt.Point(0, 0))

    // The position is the position of the view, but the image is the
    // whole interface, including the view.
    val view = ws.viewWidget.view
    val viewArea = new java.awt.geom.Area(new java.awt.Rectangle(
      wrapperPos.x + view.getLocation().x, wrapperPos.y + view.getLocation().y,
      view.getWidth, view.getHeight))

    // remove widgets from the clip area of the view:
    val container = ws.viewWidget.findWidgetContainer
    for {
      w <- widgetHooks().map(_.widget)
      bounds = container.getUnzoomedBounds(w)
      widgetArea = new java.awt.geom.Area(bounds)
    } viewArea.subtract(widgetArea)

    val viewSettings = FixedViewSettings(ws.view)

    val image = org.nlogo.awt.Images.paintToImage(
      ws.viewWidget.findWidgetContainer.asInstanceOf[java.awt.Component])

    val name = Option(ws.getModelFileName).map(ReviewTab.removeExtension)
      .orElse(tabState.currentRun.map(_.name))
      .getOrElse("Untitled")
    val run = new ModelRun(name, saveModel(), viewArea, viewSettings, image, "", Nil)
    tabState.addRun(run)
    runList.setSelectedValue(run, true)
    refreshInterface()
  }

  def updateMonitors() {
    widgetHooks()
      .collect { case WidgetHook(m: MonitorWidget, _) => m }
      .foreach(updateMonitor)
  }

  // only callable from the job thread, and only once evaluator.withContext(...) has properly
  // set up ProcedureRunner's execution context. it would be problematic to try to trigger
  // monitors to update from the event thread, because the event thread is not allowed to
  // block waiting for the job thread. - ST 10/12/12
  def updateMonitor(monitor: MonitorWidget) {
    for {
      reporter <- monitor.reporter
      runner = ws.evaluator.ProcedureRunner
      if runner.hasContext
      result = try {
        runner.report(reporter)
      } catch {
        case _: api.LogoException => "N/A"
      }
    } monitor.value(result)
  }

}
