// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.hubnet.protocol.{ ActivityCommand, HandshakeFromServer }
import org.nlogo.window.EditorFactory

private class RoboClientPanel(editorFactory: EditorFactory, errorHandler: ErrorHandler, waitTime: Long,
                              workspace: CompilerServices, extensionManager: ExtensionManager)
        extends ClientPanel(editorFactory, errorHandler, workspace, extensionManager) {

  private lazy val roboClient:RoboWidgetControl = new RoboWidgetControl()

  override def completeLogin(handshake:HandshakeFromServer): Unit ={
    super.completeLogin(handshake)
    roboClient.start()
  }

  override def disconnect(reason:String): Unit ={
    roboClient.running = false
    super.disconnect(reason)
  }

  override def logout(): Unit ={
    roboClient.running = false
    super.logout()
  }

  private class RoboWidgetControl extends Thread("RoboWidgetControl Thread") {
    import org.nlogo.awt.EventQueue.invokeAndWait
    import org.nlogo.window.{ ButtonWidget, SliderWidget, SwitchWidget, Widget }

    private val r = new org.nlogo.api.MersenneTwisterFast()
    var running = true

    override def run(): Unit = {
      clientGUI.foreach { gui =>
        val components = gui.getInterfaceComponents
        while (running) {
          val comp = components(r.nextInt(components.length))
          if (comp.isInstanceOf[Widget])
            if (comp.isInstanceOf[ClientView]) {
              sendRoboViewMessage()
              Thread.sleep(waitTime)
            }
            else if (comp.isInstanceOf[SliderWidget] || comp.isInstanceOf[ButtonWidget] || comp.isInstanceOf[SwitchWidget]) {
              getAndSendRoboWidgetMessage(comp.asInstanceOf[Widget])
              Thread.sleep(waitTime)
            }
        }
      }
    }

    @throws(classOf[InterruptedException])
    private def sendRoboViewMessage(): Unit = {
      def randomCor(min:Double, max:Double) = min + r.nextDouble() * (max - min) - 0.5
      viewWidget.foreach { view =>
        invokeAndWait(() =>
          sendMouseMessage(randomCor(view.world.minPxcor, view.world.maxPxcor),
                           randomCor(view.world.minPycor, view.world.maxPycor), true))
      }
    }

    /**
     * NOTE: you CANNOT call this from the eventqueue thread since it
     * calls invokeAndWait() it's called from the roboclient thread
     * --mag 08/04/03, 08/07/03
     */
    @throws(classOf[InterruptedException])
    private def getAndSendRoboWidgetMessage(widget: Widget): Unit = {
      // use invokeAndWait() here instead of invokeLater() since
      // then if the event thread gets backed up, we don't send
      // over a whole slew of widget events all at once.  they
      // will always be at least waitTime apart.
      invokeAndWait(() => {
        val (name, value) = widget match {
          case s: SliderWidget =>
            if (s.value == s.maximum) {
              s.setValue(s.minimum)
            } else {
              s.setValue(s.value + s.increment)
            }
            (s.name, Some(s.value))
          case b: ButtonWidget =>
            (widget.displayName, Some(b.foreverOn))
          case s: SwitchWidget =>
            s.isOn = !s.isOn
            (widget.displayName, s.isOn)
          case _ => ("", null)
        }
        // need to check if we are still connected since we could
        // have shutdown while we were waiting to be called.
        // --mag 8/26/03
        if (connected.get && value != null) sendDataAndWait(new ActivityCommand(name, value.asInstanceOf[AnyRef]))
      })
    }
  }
}
