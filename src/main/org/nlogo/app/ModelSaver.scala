// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.{ModelReader, PreviewCommands, Shape, Version}
import org.nlogo.util.Implicits.RichString
import org.nlogo.util.Implicits.RichStringLike
import org.nlogo.workspace.AbstractWorkspaceScala
import collection.JavaConverters._

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
      for(w <- app.tabs.interfaceTab.iP.getWidgetsForSaving.asScala)
        buf ++= (w.save + "\n")
    }

    // info
    section {
      buf ++= app.tabs.infoTab.info
    }

    // turtle shapes
    section {
      for(shape <- app.tabs.workspace.world.turtleShapeList.getShapes.asScala) {
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
      app.tabs.workspace.previewCommands match {
        case PreviewCommands.Default => // do nothing
        case commands => buf ++= commands.source.stripTrailingWhiteSpace + "\n"
      }
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
      for(shape <- app.tabs.workspace.world.linkShapeList.getShapes.asScala) {
        buf ++= shape.asInstanceOf[Shape].toString
        buf ++= "\n\n"
      }
    }

    // interface tab settings
    section {
      buf ++= (if(app.tabs.workspace.snapOn) "1\n" else "0\n")
    }

    buf.stripTrailingWhiteSpace + "\n"
  }

}
