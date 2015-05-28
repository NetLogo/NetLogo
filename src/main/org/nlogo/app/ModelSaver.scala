// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.{ModelReader, ModelSections, Shape, Version}
import org.nlogo.workspace.AbstractWorkspaceScala
import collection.JavaConverters._

class ModelSaver(model: ModelSections) {

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
      buf ++= model.procedureSource
      if(buf.nonEmpty && buf.last != '\n')
        buf += '\n'
    }

    // widgets
    section {
      for(w <- model.widgets)
        buf ++= (w.save + "\n")
    }

    // info
    section {
      buf ++= model.info
    }

    // turtle shapes
    section {
      for(shape <- model.turtleShapes) {
        buf ++= shape.asInstanceOf[Shape].toString
        buf ++= "\n\n"
      }
    }

    // version
    section {
      buf ++= model.version + "\n"
    }

    // preview commands
    section {
      val cmds = model.previewCommands
      if(cmds.nonEmpty && cmds != AbstractWorkspaceScala.DefaultPreviewCommands)
        buf ++= model.previewCommands.trim + "\n"
    }

    // system dynamics modeler
    section {
      if(model.aggregateManager != null) {
        val s = model.aggregateManager.save
        if(s != null)
          buf.append(s + "\n")
      }
    }

    // BehaviorSpace
    section {
      buf ++= model.labManager.save
    }

    // reserved for HubNet client
    section {
      for(manager <- Option(model.hubnetManager))
        manager.save(buf)
    }

    //link shapes
    section {
      for(shape <- model.linkShapes) {
        buf ++= shape.asInstanceOf[Shape].toString
        buf ++= "\n\n"
      }
    }

    // interface tab settings
    section {
      buf ++= (if(model.snapOn) "1\n" else "0\n")
    }

    buf.toString
  }

}
