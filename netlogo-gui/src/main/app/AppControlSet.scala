// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.EventQueue.isDispatchThread
import java.awt.image.BufferedImage

import org.nlogo.api.ControlSet
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.awt.EventQueue

import scala.concurrent.{ Future, Promise }
import scala.util.Try

class AppControlSet extends ControlSet {
  var interfaceTab = Option.empty[InterfaceTab]

  def userInterface: Future[BufferedImage] =
    interfaceTab
      .map({ tab =>
        if (isDispatchThread)
          Promise.fromTry(Try(tab.iP.interfaceImage))
        else {
          val promise = Promise[BufferedImage]()
          EventQueue.invokeLater { () =>
            promise.complete(Try(tab.iP.interfaceImage))
            ()
          }
          promise
        }
      })
      .map(_.future)
      .getOrElse(Future.failed(new IllegalStateException("AppControlSet has not been initialized")))

  def userOutput: Future[String] = {
    def getOutput(interfaceTab: InterfaceTab): String =
      interfaceTab.getOutputArea.valueText

    interfaceTab
      .map({tab =>
        if (isDispatchThread)
          Promise.fromTry(Try(getOutput(tab)))
        else {
          val promise = Promise[String]()
          EventQueue.invokeLater { () =>
            promise.complete(Try(getOutput(tab)))
            ()
          }
          promise
        }
      })
      .map(_.future)
      .getOrElse(Future.failed(new IllegalStateException("AppControlSet has not been initialized")))
  }
}
