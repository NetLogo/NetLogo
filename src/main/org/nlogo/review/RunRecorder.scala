// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.api
import org.nlogo.api.ReporterRunnable.thunk2ReporterRunnable
import org.nlogo.mirror.FixedViewSettings
import org.nlogo.mirror.Frame
import org.nlogo.mirror.Mirrorables
import org.nlogo.mirror.ModelRun
import org.nlogo.swing.Implicits.thunk2runnable
import org.nlogo.window.GUIWorkspace
import org.nlogo.window.MonitorWidget
import org.nlogo.window.WidgetWrapperInterface

import javax.swing.JOptionPane

case class FrameAddedEvent(run: ModelRun, frame: Frame)

class RunRecorder(
  ws: GUIWorkspace,
  tabState: ReviewTabState,
  saveModel: () => String,
  widgetHooks: () => Seq[WidgetHook],
  disableRecording: () => Unit) {

  private val plotActionBuffer = new api.ActionBuffer(ws.plotManager)
  private val drawingActionBuffer = new api.ActionBuffer(ws.drawingActionBroker)
  private val actionBuffers = Vector(plotActionBuffer, drawingActionBuffer)

  val frameAddedPub = new SimplePublisher[FrameAddedEvent]()

  ws.listenerManager.addListener(
    new api.NetLogoAdapter {
      override def requestedDisplayUpdate() { // called from job thread
        if (ws.waitForResult(() => tabState.recordingEnabled)) {
          ws.waitFor { () =>
            if (!tabState.currentlyRecording) startNewRun()
          }
          updateMonitors() // this must be called from job thread
          ws.waitFor(() => grab())
        }
      }
      override def afterModelOpened() { // called from event thread
        stopRecording()
        for {
          run <- tabState.currentRun
          if saveModel() != run.modelString
        } {
          // if we just opened a model different from the
          // one loaded from the previously current run...
          tabState.currentRun = None
        }
      }
      private var lastTickHeard = -1.0
      override def tickCounterChanged(ticks: Double) { // called from job thread
        /* Normally, you'd want to do that only on ticks == -1.0,
         * but there is actually no guarantee that *every* tick is
         * caught (and, sometimes, -1.0 isn't, and that used to cause #372).
         * Looking for (ticks < lastTickHeard) hopefully allows us to catch every
         * call to clear-all/clear-ticks/reset-ticks. NP 2013-09-09.
         */
        if (ticks < lastTickHeard) {
          ws.waitFor(() => stopRecording())
        }
        lastTickHeard = ticks
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
        val newFrame = run.appendData(mirrorables, actions)
        frameAddedPub.publish(FrameAddedEvent(run, newFrame))
      } catch {
        case e: java.lang.OutOfMemoryError =>
          JOptionPane.showMessageDialog(null,
            "Not enough memory. Turning off recording.",
            "Low memory", JOptionPane.WARNING_MESSAGE)
          disableRecording()
      }
    }
  }

  def stopRecording() {
    for (buffer <- actionBuffers) {
      buffer.clear()
      buffer.suspend()
    }
    for (run <- tabState.currentRun) {
      run.stillRecording = false
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

    val interfaceImage = org.nlogo.awt.Images.paintToImage(
      ws.viewWidget.findWidgetContainer.asInstanceOf[java.awt.Component])

    val name = Option(ws.getModelFileName).map(ReviewTab.removeExtension)
      .orElse(tabState.currentRun.map(_.name))
      .getOrElse("Untitled")

    val initialPlots = ws.plotManager.plots.map(_.clone)
    val initialDrawingImage = org.nlogo.drawing.cloneImage(ws.getAndCreateDrawing(false))
    val run = new ModelRun(
      name, saveModel(),
      viewArea, viewSettings, interfaceImage,
      initialPlots, initialDrawingImage,
      "", Nil)
    actionBuffers.foreach(_.activate())
    tabState.addRun(run)
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
