package org.nlogo.app

import org.nlogo.api.{ModelReader, Shape, Version}
import org.nlogo.workspace.AbstractWorkspaceScala

import org.nlogo.util.JCL._

class ModelSaver(app: App) {

  def save: String = {

    val buf = new StringBuilder
    def section(fn: => Unit) {
      fn
      if(buf.nonEmpty && buf.last != '\n')
        buf += '\n'
      buf ++= ModelReader.SEPARATOR + "\n"
    }

    // procedures
    section {
      buf ++= app.tabs.proceduresTab.innerSource
      if(buf.nonEmpty && buf.last != '\n')
        buf += '\n'
    }

    // widgets
    section {
      for(w <- app.tabs.interfaceTab.iP.getWidgetsForSaving)
        buf ++= (w.save + "\n")
    }

    // info
    section {
      buf ++= app.tabs.infoTab.info
    }
    
    // turtle shapes
    section {
      for(shape <- app.tabs.workspace.world.turtleShapeList().getShapes) {
        buf ++= shape.asInstanceOf[Shape].toString
        buf ++= "\n\n"
      }
    }

    // version
    section {
      buf ++= Version.version + "\n"
    }

    // preview commands
    section {
      val cmds = app.tabs.workspace.previewCommands.trim
      if(cmds.nonEmpty && cmds != AbstractWorkspaceScala.DefaultPreviewCommands)
        buf ++= app.tabs.workspace.previewCommands.trim + "\n"
    }

    // system dynamics modeler
    section {
      if(app.aggregateManager != null) {
        val s = app.aggregateManager.save
        if(s != null)
          buf.append(s + "\n")
      }
    }

    // BehaviorSpace
    section {
      buf ++= app.labManager.save
    }

    // reserved for HubNet client 
    section {
      for(manager <- Option(app.tabs.workspace.hubnetManager))
        manager.save(buf)
    }

    //link shapes
    section {
      for(shape <- app.tabs.workspace.world.linkShapeList().getShapes()) {
        buf ++= shape.asInstanceOf[Shape].toString
        buf ++= "\n\n"
      }
    }

    // interface tab settings
    section {
      buf ++= (if(app.tabs.workspace.snapOn) "1\n" else "0\n")
    }

    buf.toString
  }

}
