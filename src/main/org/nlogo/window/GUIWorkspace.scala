// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.{ agent, api, nvm, shape, workspace }
import org.nlogo.swing.{ FileDialog, InputDialog, OptionDialog, ModalProgressTask }
import org.nlogo.awt.UserCancelException

abstract class GUIWorkspace(
  _world: agent.World,
  _kioskLevel: GUIWorkspaceJ.KioskLevel,
  _frame: java.awt.Frame,
  _linkParent: java.awt.Component,
  _hubNetManagerFactory: workspace.AbstractWorkspace.HubNetManagerFactory,
  _externalFileManager: ExternalFileManager,
  _listenerManager: NetLogoListenerManager)
extends GUIWorkspaceJ(
  _world, _kioskLevel, _frame, _linkParent, _hubNetManagerFactory,
  _externalFileManager, _listenerManager)
with Events.LoadSectionEventHandler {

  def handle(e: Events.LoadSectionEvent) {
    e.section match {
      case api.ModelSection.PreviewCommands =>
        if (e.text.trim.nonEmpty)
          previewCommands = e.text
      case api.ModelSection.HubNetClient =>
        if (e.lines.nonEmpty && !workspace.AbstractWorkspace.isApplet)
          getHubNetManager.load(e.lines.toArray, e.version)
      case api.ModelSection.TurtleShapes =>
        world.turtleShapeList.replaceShapes(
          shape.VectorShape.parseShapes(e.lines.toArray, e.version))
      case api.ModelSection.LinkShapes =>
        world.linkShapeList.replaceShapes(
          shape.LinkShape.parseShapes(e.lines.toArray, e.version))
      case _ =>
    }
  }

  /// tick counter

  // withContext sets up evaluator so that if someone wants to respond to ticks on the job thread by
  // running some code, they can. we're using this to run monitor code from the model run recording
  // code.  and, TickStateChangeEvent is there too if someone wants to respond to ticks on the event
  // thread (example: ButtonWidget uses it to enable/disable a button depending on whether the tick
  // counter has been started) - ST 10/11/12

  override def notifyListeners(context: nvm.Context) {
    val ticks: Double = world.tickCounter.ticks
    if (ticks != (lastTicksListenersHeard: Double)) {
      lastTicksListenersHeard = ticks
      evaluator.withContext(context) {
        listenerManager.tickCounterChanged(ticks)
      }
    }
    listenerManager.possibleViewUpdate()
  }

  override def tick(context: nvm.Context, originalInstruction: nvm.Instruction) {
    evaluator.withContext(context) {
      super.tick(context, originalInstruction)
    }
  }

  override def resetTicks(context: nvm.Context) {
    evaluator.withContext(context) {
      super.resetTicks(context)
    }
    new Events.TickStateChangeEvent(true).raiseLater(this)
  }

  override def clearTicks() {
    super.clearTicks()
    new Events.TickStateChangeEvent(false).raiseLater(this)
  }

  override def clearAll() {
    super.clearAll()
    new Events.TickStateChangeEvent(false).raiseLater(this)
  }

  ///

  override def startLogging(properties: String) {
    try new Events.LoggingEvent(LoggingEventType.START_LOGGING,
                                fileManager.attachPrefix(properties))
      .raiseLater(this);
  }

  override def zipLogFiles(filename: String) {
    new Events.LoggingEvent(LoggingEventType.ZIP_LOG_FILES,
                            fileManager.attachPrefix(filename))
      .raiseLater(this)
  }

  override def deleteLogFiles() {
    new Events.LoggingEvent(LoggingEventType.DELETE_LOG_FILES, "")
      .raiseLater(this)
  }

  ///

  override def choosePlot(frame: java.awt.Frame): org.nlogo.plot.Plot = {
    val plotNames = plotManager.getPlotNames.toArray[AnyRef]
    if (plotNames.isEmpty) {
      val message = "There are no plots to export.";
      val options = Array[AnyRef](api.I18N.gui.get("common.buttons.ok"))
      org.nlogo.swing.OptionDialog.show(frame, "Export Plot", message, options)
      return null
    }
    val message = "Which plot would you like to export?"
    val plotnum = org.nlogo.swing.OptionDialog.showAsList(
      frame, "Export Plot", message, plotNames)
    if (plotnum < 0)
      null
    else
      plotManager.getPlot(plotNames(plotnum).asInstanceOf[String])
        .getOrElse(null)
  }

  ///

  @throws(classOf[java.io.IOException])
  def exportInterface(filename: String) {
    // there's a form of ImageIO.write that just takes a filename, but if we use that when the
    // filename is invalid (e.g. refers to a directory that doesn't exist), we get an
    // IllegalArgumentException instead of an IOException, so we make our own OutputStream so we get
    // the proper exceptions. - ST 8/19/03, 11/26/03
    val stream = new java.io.FileOutputStream(new java.io.File(filename))
    try new Events.ExportInterfaceEvent(stream, throw _)
          .raise(this)
    finally stream.close()
  }

  ///

  // when we've got two views going the mouse reporters should
  // be smart about which view we might be in and return something that makes
  // sense ev 12/20/07
  override def mouseDown = {
    // we must first make sure the event thread has had the
    // opportunity to detect any recent mouse clicks - ST 5/3/04
    waitForQueuedEvents()
    viewManager.mouseDown
  }

  override def mouseInside = {
    // we must first make sure the event thread has had the
    // opportunity to detect any recent mouse movement - ST 5/3/04
    waitForQueuedEvents()
    viewManager.mouseInside
  }

  override def mouseXCor = {
    // we must first make sure the event thread has had the
    // opportunity to detect any recent mouse movement - ST 5/3/04
    waitForQueuedEvents()
    viewManager.mouseXCor
  }

  override def mouseYCor = {
    // we must first make sure the event thread has had the
    // opportunity to detect any recent mouse movement - ST 5/3/04
    waitForQueuedEvents()
    viewManager.mouseYCor
  }

  ///

  override def beep() {
    java.awt.Toolkit.getDefaultToolkit().beep()
  }

  override def updateMonitor(owner: api.JobOwner, value: AnyRef) {
    owner.asInstanceOf[MonitorWidget].value(value)
  }

  ///

  override def movieIsOpen =
    movieEncoder != null

  override def movieCancel() {
    if (movieEncoder != null) {
      movieEncoder.cancel()
      movieEncoder = null
    }
  }

  override def movieClose() {
    ModalProgressTask(
      getFrame, "Exporting movie...",
      new Runnable() {
        override def run() {
          movieEncoder.stop()
          movieEncoder = null
        }})
  }

  override def movieGrabInterface() {
    movieEncoder.add(
      org.nlogo.awt.Images.paintToImage(
        viewWidget.findWidgetContainer.asInstanceOf[java.awt.Component]))
  }

  override def movieGrabView() {
    movieEncoder.add(exportView())
  }

  override def movieSetRate(rate: Float) {
    movieEncoder.setFrameRate(rate)
  }

  override def movieStart(path: String) {
    movieEncoder = new org.nlogo.awt.JMFMovieEncoder(15, path)
  }

  override def movieStatus: String =
    if(movieEncoder == null)
      "No movie."
    else {
      val builder = new StringBuilder
      builder ++= movieEncoder.getNumFrames + " frames" + "; "
      builder ++= "frame rate = " + movieEncoder.getFrameRate
      if (movieEncoder.isSetup) {
        val size = movieEncoder.getFrameSize
        builder ++= "; size = " + size.width + "x" + size.height
      }
      builder.toString
    }

  ///

  override def userDirectory: Option[String] =
    try {
      view.mouseDown(false)
      FileDialog.setDirectory(fileManager.prefix)
      val chosen =
        FileDialog.show(getFrame, "Choose Directory", java.awt.FileDialog.LOAD,
                        true)  // directories only please
      Some(chosen + java.io.File.separatorChar)
    }
    catch {
      case _: UserCancelException =>
        None
    }

  override def userFile: Option[String] =
    try {
      view.mouseDown(false)
      FileDialog.setDirectory(fileManager.prefix)
      val chosen =
        FileDialog.show(getFrame, "Choose File", java.awt.FileDialog.LOAD)
      Some(chosen)
    }
    catch {
      case _: UserCancelException =>
        None
    }

  override def userInput(message: String): Option[String] = {
    view.mouseDown(false)
    Option(
      new InputDialog(
        getFrame, "User Input", message,
        api.I18N.gui.fn).showInputDialog())
  }

  override def userMessage(message: String): Boolean = {
    view.mouseDown(false)
    val choice =
      OptionDialog.show(getFrame, "User Message", message,
                        Array(api.I18N.gui.get("common.buttons.ok"),
                              api.I18N.gui.get("common.buttons.halt")))
    choice == 1
  }

  override def userNewFile: Option[String] =
    try {
      view.mouseDown(false)
      FileDialog.setDirectory(fileManager.prefix)
      val chosen = FileDialog.show(getFrame, "Choose File", java.awt.FileDialog.SAVE)
      Some(chosen)
    }
    catch {
      case _: UserCancelException =>
        None
    }

  override def userOneOf(message: String, xs: api.LogoList): Option[AnyRef] = {
    val items = xs.map(api.Dump.logoObject).toArray[AnyRef]
    view.mouseDown(false)
    val chosen =
      new OptionDialog(
        getFrame, "User One Of", message,
        items, api.I18N.gui.fn).showOptionDialog()
    for(boxedInt <- Option(chosen))
    yield
      xs.get(boxedInt.asInstanceOf[java.lang.Integer].intValue)
  }

  override def userYesOrNo(message: String): Option[Boolean] = {
    view.mouseDown(false)
    val response = OptionDialog.showIgnoringCloseBox(
      getFrame, "User Yes or No", message,
      Array(api.I18N.gui.get("common.buttons.yes"),
            api.I18N.gui.get("common.buttons.no"),
            api.I18N.gui.get("common.buttons.halt")),
      false)
    response match {
      case 0 => Some(true)
      case 1 => Some(false)
      case _ => None
    }
  }

}
