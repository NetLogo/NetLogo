// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.{ModelReader, ModelSections, PreviewCommands, Version}
import org.nlogo.util.Implicits.RichString
import org.nlogo.util.Implicits.RichStringLike
import org.nlogo.core.{ LiteralParser, Shape }
import org.nlogo.core.model.WidgetReader
import org.nlogo.fileformat
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
      val additionalReaders = fileformat.nlogoReaders(Version.is3D)
      for(w <- model.widgets)
        buf ++=
          WidgetReader.format(w, additionalReaders) + "\n\n"
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
      buf ++= (model.previewCommands match {
        case PreviewCommands.Default => ""
        case commands                => commands.source.stripTrailingWhiteSpace + "\n"
      })
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
      model.hubnetInterface.foreach { interface =>
        buf ++= interface.map(w => WidgetReader.format(w, fileformat.hubNetReaders)).mkString("", "\n", "\n")
      }
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

    buf.stripTrailingWhiteSpace + "\n"
  }

}
