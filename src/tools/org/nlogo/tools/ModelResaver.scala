package org.nlogo.tools

import java.awt.EventQueue

import scala.sys.process.Process

import org.nlogo.app.App

/**
 *
 * The original motivation for that was that the version number saved in the model file
 * appears on the Modeling Commons website and it looked weird to have most model from
 * the NetLogo Model Library appear to have been saved with various older earlier versions
 * of NetLogo. Ideally, this script should be run before every release.
 *
 * This script opens and saves all models in the library, excluding 3D and HubNet models.
 * (3D and HubNet are excluded because they are not on the Modeling Commons, but if someone
 * wants to bother adding support for those models in this script, it would be a good thing.)
 *
 * The script fires up the NetLogo GUI because headless NetLogo has no saving facility.
 *
 * Nicolas 2012-10-31
 *
 * Moved to org.nlogo.tools, NP 2013-08-07
 *
 */
object ModelResaver {

  def wait(block: => Unit) {
    EventQueue.invokeAndWait(
      new Runnable() {
        def run() { block }
      })
  }

  def main(args: Array[String]): Unit = {
    val find = "find models -name test -prune -o -name *.nlogo -print"
    val paths = Process(find)
      .lines.toSeq
      .filterNot(_.contains("HubNet")) // for some reason, I'm having a hard time doing this with 'find'

    App.main(Array[String]())
    val modelSaver = new org.nlogo.app.ModelSaver(App.app)
    var failedModels = List[(String, String)]()
    for (path <- paths) {
      println(path)
      wait {
        try {
          App.app.open(path)
          org.nlogo.api.FileIO.writeFile(path, modelSaver.save);
        } catch {
          case e: Exception => failedModels :+= ((path, e.getMessage))
        }
      }
    }
    wait {
      App.app.quit()
    }

    println("FAILED MODELS:")
    println(failedModels.mkString("\n"))
  }

}
