// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.EventQueue.isDispatchThread
import java.awt.image.BufferedImage

import org.nlogo.api.ControlSet
import org.nlogo.awt.EventQueue

import scala.concurrent.{ Future, Promise }
import scala.util.Try

class AppControlSet extends ControlSet {
  var tabs = Option.empty[Tabs]

  def userInterface: Future[BufferedImage] =
    tabs
      .map({ ts =>
        if (isDispatchThread)
          Promise.fromTry(Try(ts.interfaceTab.iP.interfaceImage))
        else {
          val promise = Promise[BufferedImage]()
          EventQueue.invokeLater { () =>
            promise.complete(Try(ts.interfaceTab.iP.interfaceImage))
            ()
          }
          promise
        }
      })
      .map(_.future)
      .getOrElse(Future.failed(new IllegalStateException("AppControlSet has not been initialized")))

  def userOutput: Future[String] = {
    def getOutput(ts: Tabs): String =
      ts.interfaceTab.getOutputArea.map(_.valueText).getOrElse("")

    tabs
      .map({ts =>
        if (isDispatchThread)
          Promise.fromTry(Try(getOutput(ts)))
        else {
          val promise = Promise[String]()
          EventQueue.invokeLater { () =>
            promise.complete(Try(getOutput(ts)))
            ()
          }
          promise
        }
      })
      .map(_.future)
      .getOrElse(Future.failed(new IllegalStateException("AppControlSet has not been initialized")))
  }
}
