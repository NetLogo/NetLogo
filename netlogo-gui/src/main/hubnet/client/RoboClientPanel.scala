// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.api.CompilerServices
import org.nlogo.hubnet.protocol.{HandshakeFromServer, ActivityCommand}

private class RoboClientPanel(editorFactory:org.nlogo.window.EditorFactory,
                              errorHandler:ErrorHandler, waitTime:Long, workspace:CompilerServices)
        extends ClientPanel(editorFactory, errorHandler, workspace) {
  private lazy val roboClient:RoboWidgetControl = new RoboWidgetControl()

  override def completeLogin(handshake:HandshakeFromServer){
    super.completeLogin(handshake)
    roboClient.start()
  }

  override def disconnect(reason:String){
    roboClient.running = false
    super.disconnect(reason)
  }

  override def logout(){
    roboClient.running = false
    super.logout()
  }

  private class RoboWidgetControl extends Thread("RoboWidgetControl Thread") {
    import org.nlogo.awt.EventQueue.invokeAndWait
    import org.nlogo.swing.Implicits._
    import org.nlogo.window.{ButtonWidget, SliderWidget, Widget}
    import org.nlogo.widget.SwitchWidget

    private val r = new org.nlogo.api.MersenneTwisterFast()
    var running = true

    override def run() {
      val components = clientGUI.getInterfaceComponents
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

    @throws(classOf[InterruptedException])
    private def sendRoboViewMessage(){
      def randomCor(min:Double, max:Double) = min + r.nextDouble() * (max - min) - 0.5
      invokeAndWait(() =>
        sendMouseMessage(randomCor(viewWidget.world.minPxcor, viewWidget.world.maxPxcor),
                          randomCor(viewWidget.world.minPycor, viewWidget.world.maxPycor), true))
    }

    /**
     * NOTE: you CANNOT call this from the eventqueue thread since it
     * calls invokeAndWait() it's called from the roboclient thread
     * --mag 08/04/03, 08/07/03
     */
    @throws(classOf[InterruptedException])
    private def getAndSendRoboWidgetMessage(widget:Widget) {
      // use invokeAndWait() here instead of invokeLater() since
      // then if the event thread gets backed up, we don't send
      // over a whole slew of widget events all at once.  they
      // will always be at least waitTime apart.
      invokeAndWait(() => {
        val (name, value) = widget match {
          case s:SliderWidget =>
            if(s.value == s.maximum) s.value = s.minimum else s.value = s.value + s.increment
            (s.nameWrapper, Some(s.value))
          case b: ButtonWidget => (widget.displayName, Some(if(b.foreverOn) true else false))
          case s: SwitchWidget =>
            s.isOn = !s.isOn
            (widget.displayName, Some(s.isOn))
          case _ => ("", None)
        }
        // need to check if we are still connected since we could
        // have shutdown while we were waiting to be called.
        // --mag 8/26/03
        if (connected && value.isDefined) sendDataAndWait(new ActivityCommand(name, value.get.asInstanceOf[AnyRef]))
      })
    }
  }
}
