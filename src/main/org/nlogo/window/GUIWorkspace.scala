// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.{ agent, api, shape, workspace }

abstract class GUIWorkspace(
  _world: agent.World,
  _kioskLevel: GUIWorkspaceJ.KioskLevel,
  _frame: java.awt.Frame,
  _linkParent: java.awt.Component,
  _listenerManager: NetLogoListenerManager)
extends GUIWorkspaceJ(
  _world, _kioskLevel, _frame, _linkParent, _listenerManager)
with Events.LoadSectionEventHandler {

  def handle(e: Events.LoadSectionEvent) {
    e.section match {
      case api.ModelSection.PreviewCommands =>
        if (e.text.trim.nonEmpty)
          previewCommands = e.text
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

  def reload() {
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

}
