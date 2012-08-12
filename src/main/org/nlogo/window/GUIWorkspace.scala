// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.{ agent, api, shape, workspace }

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

  ///

  override def reload() {
    new Events.AppEvent(AppEventType.RELOAD, Seq())
      .raiseLater(this)
  }

  override def magicOpen(name: String) {
    new Events.AppEvent(AppEventType.MAGIC_OPEN, Seq(name))
      .raiseLater(this)
  }

  override def changeLanguage() {
    new Events.AppEvent(AppEventType.CHANGE_LANGUAGE, Seq())
      .raiseLater(this)
  }

  override def startLogging(properties: String) {
    try new Events.AppEvent(AppEventType.START_LOGGING,
                            Seq(fileManager.attachPrefix(properties)))
          .raiseLater(this);
  }

  override def zipLogFiles(filename: String) {
    new Events.AppEvent(AppEventType.ZIP_LOG_FILES, Seq(fileManager.attachPrefix(filename)))
      .raiseLater(this)
  }

  override def deleteLogFiles() {
    new Events.AppEvent(AppEventType.DELETE_LOG_FILES, Seq())
      .raiseLater(this)
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
    org.nlogo.swing.ModalProgressTask(
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

}
