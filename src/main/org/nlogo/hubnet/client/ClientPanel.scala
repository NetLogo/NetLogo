// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import javax.swing.JPanel
import org.nlogo.agent.{AbstractExporter, ConstantSliderConstraint}
import org.nlogo.plot.{PlotExporter, Plot, PlotManager}
import java.io.{IOException, PrintWriter}
import org.nlogo.window.Events.{AfterLoadEvent, LoadSectionEvent}
import org.nlogo.swing.OptionDialog
import org.nlogo.hubnet.mirroring.{OverrideList, HubNetLinkStamp, HubNetPlotPoint, HubNetLine, HubNetTurtleStamp}
import java.net.{Socket, ConnectException, UnknownHostException, NoRouteToHostException}
import java.awt.AWTEvent
import org.nlogo.hubnet.protocol._
import org.nlogo.awt.EventQueue.invokeLater
import org.nlogo.awt.Hierarchy.getFrame
import org.nlogo.swing.Implicits._
import org.nlogo.window.{PlotWidgetExportType, MonitorWidget, InterfaceGlobalWidget, Widget, ButtonWidget, PlotWidget}
import org.nlogo.api.{I18N, Version, ModelSection, Dump, PlotInterface, LogoList, DummyLogoThunkFactory, ParserServices}
import org.nlogo.hubnet.connection.{Streamable, ConnectionTypes, AbstractConnection}

// Normally we try not to use the org.nlogo.window.Events stuff except in
// the app and window packages.  But currently there's no better
// way to find out when a button was pressed or a slider (etc.)
// moved, so we use events.  - ST 8/24/03
class ClientPanel(editorFactory: org.nlogo.window.EditorFactory,
                  errorHandler: ErrorHandler,
                  parser: ParserServices) extends JPanel with
        org.nlogo.window.Events.AddJobEventHandler with
        org.nlogo.window.Events.ExportPlotEventHandler with
        org.nlogo.window.Events.InterfaceGlobalEventHandler with
        org.nlogo.window.Events.AddSliderConstraintEventHandler {

  var clientGUI:ClientGUI = null
  var viewWidget:ClientView = null
  private val plotManager = new PlotManager(new DummyLogoThunkFactory())

  locally {
    setBackground(java.awt.Color.white)
    setLayout(new java.awt.BorderLayout())
  }

  def setDisplayOn(on: Boolean) { if (viewWidget != null) viewWidget.setDisplayOn(on) }

  def sendMouseMessage(mouseXCor: Double, mouseYCor: Double, down: Boolean) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    val coords = LogoList(mouseXCor.asInstanceOf[AnyRef], mouseYCor.asInstanceOf[AnyRef])
    sendDataAndWait(new ActivityCommand(if (down) "View" else "Mouse Up", coords))
  }

  def handlePlotUpdate(msg: PlotInterface) {
    for (pw <- clientGUI.getInterfaceComponents.collect {case pw: PlotWidget => pw}) {
      if (pw.plot.name==msg.name) {
        pw.plot.clear()
        updatePlot(msg.asInstanceOf[org.nlogo.plot.Plot], pw.plot)
        pw.repaintIfNeeded()
      }
    }
  }

  // TODO: couldnt we use case class copy here or something?
  private def updatePlot(plot1: Plot, plot2: Plot) {
    plot2.currentPen = plot2.getPen(plot1.currentPen.get.name)
    plot2.state = plot1.state.copy()
    for (pen1 <- plot1.pens) {
      val pen2 = if (pen1.temporary) plot2.createPlotPen(pen1.name, true)
      else plot2.getPen(pen1.name).get
      pen2.state = pen1.state.copy()
      pen2.points ++= pen1.points
    }
  }

  /// Interface EventHandlers
  def handle(e: org.nlogo.window.Events.AddJobEvent) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    val button = e.owner.asInstanceOf[ButtonWidget]
    sendDataAndWait(new ActivityCommand(button.displayName, button.foreverOn.asInstanceOf[AnyRef]))
    button.popUpStoppingButton()
  }

  def handle(e: org.nlogo.window.Events.ExportPlotEvent) {
    e.whichPlots match {
      case PlotWidgetExportType.ALL => throw new UnsupportedOperationException("can't export all plots yet.")
      case _ =>
        if (e.target != null) {
          try new AbstractExporter(e.filename) {
            override def export(writer: PrintWriter) {
              new PlotExporter(e.target, Dump.csv).export(writer)
            }
          }.export("plot", "HubNet Client", "")
          catch {case ex: IOException => org.nlogo.util.Exceptions.handle(ex)}
        }
    }
  }

  def handle(e: org.nlogo.window.Events.InterfaceGlobalEvent) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    sendDataAndWait(new ActivityCommand(e.widget.name, e.widget.valueObject))
  }

  def handle(e: org.nlogo.window.Events.AddSliderConstraintEvent) {
    e.slider.setSliderConstraint(
      new ConstantSliderConstraint(e.minSpec.toDouble, e.maxSpec.toDouble, e.incSpec.toDouble){ defaultValue = e.value })
  }

  /// Message Handlers
  private def handleWidgetControlMessage(value: Any, widgetName: String) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    if (widgetName == "VIEW") value match {
      case t: HubNetTurtleStamp => viewWidget.renderer.stamp(t)
      case ls: HubNetLinkStamp => viewWidget.renderer.stamp(ls)
      case l: HubNetLine => viewWidget.renderer.drawLine(l)
      case _ => viewWidget.renderer.clearDrawing()
    }
    else if (widgetName=="ALL PLOTS") {
      plotManager.clearAll()
      for (pw <- clientGUI.getInterfaceComponents.collect { case pw: PlotWidget => pw }) {
        pw.plot.dirty = true
        pw.repaintIfNeeded()
      }
    }
    // `foreach` is the reasonable choice here; widgets of different types may share the same
    // name as the Monitor and can intercept the search if we use `find`! -- JAB (5/9/12)
    clientGUI.getInterfaceComponents filter { case w: Widget => w.displayName == widgetName } foreach {
      case i: InterfaceGlobalWidget => i.valueObject(value)
      case m: MonitorWidget         => m.value(value)
      case _                        => // Ignore
    }
  }

  private def findWidget(name:String) = {
    clientGUI.getInterfaceComponents.find { case w: Widget => w.displayName == name }
  }

  // this is the master method for handling plot messages. it should probably be redone.
  private def handlePlotControlMessage(value: Any, plotName:String) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    val plotWidget = findWidget(plotName).asInstanceOf[Option[PlotWidget]].get // horrible.
    value match {
      // This instance sets the current-plot-pen
      case s:String =>
        plotWidget.plot.currentPen=plotWidget.plot.getPen(s).getOrElse(plotWidget.plot.createPlotPen(s, true))
      // This instance sets the plot-pen-color
      case i: Int =>
        val plotPen = plotWidget.plot.currentPen.get
        plotPen.state = plotPen.state.copy(color = i)
      // This instance sets plot-pen-up and down
      case b: Boolean =>
        val plotPen = plotWidget.plot.currentPen.get
        plotPen.state = plotPen.state.copy(isDown = b)
        plotWidget.plot.dirty = true
        plotWidget.repaintIfNeeded()
      // This instance is a point to plot
      case p: HubNetPlotPoint =>
        // points may or may not contain a specific X coordinate.
        // however, this is only the case in narrowcast plotting
        // plot mirroring always sends both coordinates even if
        // auto-plot is on. ev 8/18/08
        if (p.specifiesXCor) plotWidget.plot.plot(p.xcor, p.ycor)
        // if not, we'll just let the plot use the next one.
        else plotWidget.plot.plot(p.ycor)
        plotWidget.plot.dirty = true
        plotWidget.repaintIfNeeded()
      // These instances do various plotting commands
      case c: Char => {
        try c match {
          case 'c' =>
            plotWidget.plot.clear()
            plotWidget.plot.dirty = true
            plotWidget.repaintIfNeeded()
          case 'r' =>
            plotWidget.plot.currentPen.get.hardReset()
            plotWidget.plot.dirty = true
            plotWidget.repaintIfNeeded()
          case 'p' =>
            plotWidget.plot.currentPen.get.softReset()
            plotWidget.plot.dirty = true
            plotWidget.repaintIfNeeded()
          case 'n' =>
            plotWidget.plot.state = plotWidget.plot.state.copy(autoPlotOn = true)
          case 'f' =>
            plotWidget.plot.state = plotWidget.plot.state.copy(autoPlotOn = false)
          case _ => throw new IllegalStateException()
        } catch {case ex: RuntimeException => org.nlogo.util.Exceptions.handle(ex)}
      }
      // This instance changes the plot-pen-mode
      case s:Short =>
        plotWidget.plot.currentPen.get.state = plotWidget.plot.currentPen.get.state.copy(mode = s.toInt)
        plotWidget.plot.dirty = true
        plotWidget.repaintIfNeeded()
      // This instance changes the plot-pen-interval
      case d:Double =>
        plotWidget.plot.currentPen.get.state = plotWidget.plot.currentPen.get.state.copy(interval = d)
      // This instance is used for anything that has a lot of data
      case list: List[_] => list(0) match {
        case 'x' =>
          val min: Double = list(1).asInstanceOf[Double]
          val max: Double = list(2).asInstanceOf[Double]
          plotWidget.plot.state = plotWidget.plot.state.copy(xMin = min, xMax = max)
          plotWidget.plot.dirty = true
          plotWidget.repaintIfNeeded()
        case _ =>
          val min: Double = list(1).asInstanceOf[Double]
          val max: Double = list(2).asInstanceOf[Double]
          plotWidget.plot.state = plotWidget.plot.state.copy(xMin = min, xMax = max)
          plotWidget.plot.dirty = true
          plotWidget.repaintIfNeeded()
      }
    }
  }

  /**
   * Completes the login process. Called when a handshake message is received
   * from the server.
   */
  def completeLogin(handshake: HandshakeFromServer) {
    errorHandler.completeLogin()
    activityName = handshake.activityName
    if (clientGUI != null) remove(clientGUI)
    plotManager.forgetAll()
    viewWidget = new ClientView(this)
    clientGUI = new ClientGUI(editorFactory, viewWidget, plotManager, parser)
    add(clientGUI, java.awt.BorderLayout.CENTER)
    clientGUI.setStatus(userid, activityName, hostip, port)
    val clientInterface = handshake.interfaceSpecList.head.asInstanceOf[ClientInterface]
    val widgets = clientInterface.widgetDescriptions
    new LoadSectionEvent("HubNet", ModelSection.Interface, widgets, widgets.mkString("\n")).raise(this)
    // so that constrained widgets can initialize themselves -- CLB
    new AfterLoadEvent().raise(this)
    clientGUI.setChoices(clientInterface.chooserChoices.toMap)
    viewWidget.renderer.replaceTurtleShapes(
      scala.collection.JavaConversions.seqAsJavaList(clientInterface.turtleShapes))
    viewWidget.renderer.replaceLinkShapes(
      scala.collection.JavaConversions.seqAsJavaList(clientInterface.linkShapes))
    sendDataAndWait(EnterMessage)
    connected = true
    invokeLater(() => {
      getFrame(ClientPanel.this).pack()
      // in robo fixture, this generated exceptions now and again
      clientGUI.requestFocus()
    })
  }

  def handleProtocolMessage(message: org.nlogo.hubnet.protocol.Message) {
    message match {
      case h: HandshakeFromServer => completeLogin(h)
      case LoginFailure(content) => handleLoginFailure(content)
      case ExitMessage(reason) => disconnect(reason)
      case WidgetControl(content, tag) => handleWidgetControlMessage(content, tag)
      case DisableView => setDisplayOn(false)
      case ViewUpdate(worldData) => viewWidget.updateDisplay(worldData)
      case PlotControl(content, plotName) => handlePlotControlMessage(content, plotName)
      case PlotUpdate(plot) => handlePlotUpdate(plot)
      case OverrideMessage(data, clear) => viewWidget.handleOverrideList(data.asInstanceOf[OverrideList], clear)
      case ClearOverrideMessage => viewWidget.clearOverrides()
      case AgentPerspectiveMessage(bytes) => viewWidget.handleAgentPerspective(bytes)
      case Text(content, messageType) => messageType match {
        case Text.MessageType.TEXT => clientGUI.addMessage(content.toString)
        case Text.MessageType.USER =>
          OptionDialog.show(getFrame(this), "User Message", content.toString,
            Array(I18N.gui.get("common.buttons.ok"), I18N.gui.get("common.buttons.halt")))
        case Text.MessageType.CLEAR => clientGUI.clearMessages()
      }
    }
  }

  /// Connection Management

  private var userid: String = null
  private var hostip: String = null
  private var port: Int = 0
  private var activityName: String = null
  private var listener: Listener = null
  var connected:Boolean = false

  def login(userid: String, hostip: String, port: Int): Option[String] = {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    // there used to be a flag called loggingIn
    // to "ensure that we only execute this one at a time"
    // but I don't understand what it was trying to prevent,
    // each client panel had one and I can't think of any situations
    // where the same client panel would be trying to login at the same
    // time. Also, it wasn't thread safe anyway ev 7/30/08
    this.userid = userid
    this.hostip = hostip
    this.port = port

    try {
      val socket = new java.net.Socket(hostip, port)
      socket.setSoTimeout(0)
      /**
       * do not uncomment the following line to enable tcp_no_delay.
       * in theory, it should get our messages out to clients slightly faster.
       * in practice, it was creating hundreds of extra tiny packets, consuming bandwidth.
       * this caused clients to skip and pause erratically and rendered them unusable.
       * this typically went unnoticed in small simulations, or simulations sending
       * only a few messages. --josh 11/19/09
       */
      //socket.setTcpNoDelay( true )
      listener = new Listener(userid, socket)
      listener.start()
      sendDataAndWait(Version.version)
      None
    }
    catch {
      case e: NoRouteToHostException => Some("Login failed:\n" + hostip + " could not be reached.")
      case e: UnknownHostException => Some("Login failed:\n" + hostip + " does not resolve to a valid IP address.")
      case e: ConnectException => Some("Login failed:\n" + "There was no server running at " + hostip + " on port " + port)
      case e: Throwable => Some("Login failed:\nUnknown cause:\n" + org.nlogo.util.Utils.getStackTrace(e))
    }
  }

  def handleLoginFailure(errorMessage: String) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    listener.disconnect(errorMessage)
    errorHandler.handleLoginFailure(errorMessage)
  }

  def disconnect(reason:String) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    if (listener != null) listener.disconnect(reason)
    else {
      invokeLater(() => errorHandler.handleDisconnect(activityName, connected, reason))
      connected = false
    }
  }

  def logout() {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    if (connected) {
      connected = false
      listener.stopWriting()
      sendDataAndWait(ExitMessage("Client Exited"))
    }
  }

  // I'd make this an anonymous class, but we need the constructor
  // to throw an exception, and jikes doesn't properly add the "throws"
  // clause (at least, my understanding is that jikes is in error here
  // see section 15.9.5.1 of the Java Language Specification,
  // "Anonymous Constructors") - ST 8/15/02

  @throws(classOf[java.io.IOException])
  private class Listener(userName: String, socket: Socket)
          extends AbstractConnection("Listener: " + userName, Streamable(socket)) {
    var clientId = userName
    // kill the writingThread since we don't need it because
    // we only do synchronous I/O via waitForSendData() -- CB 09/28/04
    stopWriting()
    override def receiveData(data:AnyRef) {
      getToolkit.getSystemEventQueue.postEvent(new ClientAWTEvent(ClientPanel.this, data.asInstanceOf[AnyRef], true))
    }
    override def handleEx(e:Exception, sendingEx: Boolean) {
      getToolkit.getSystemEventQueue.postEvent(new ClientAWTExceptionEvent(ClientPanel.this, e, sendingEx))
    }
    override def disconnect(reason:String) {
      super.disconnect(reason)
      val oldConnected = connected
      invokeLater(() => errorHandler.handleDisconnect(activityName, oldConnected, reason))
      connected = false
    }
  }

  // EVENT HANDLING

  // TODO: all casting here is terrible.
  override def processEvent(e: AWTEvent) {
    if (e.isInstanceOf[ClientAWTEvent] && e.getSource == this) {
      val clientEvent = e.asInstanceOf[ClientAWTEvent]
      try if (clientEvent.isInstanceOf[ClientAWTExceptionEvent])
        handleEx(clientEvent.info.asInstanceOf[Exception],
          clientEvent.asInstanceOf[ClientAWTExceptionEvent].sendingException)
      else if (clientEvent.receivedData) receiveData(clientEvent.info)
      catch {case ex: RuntimeException => org.nlogo.util.Exceptions.handle(ex)}
    } else super.processEvent(e)
  }


  private def handleEx(e: Exception, sendingEx: Boolean) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    e.printStackTrace()
    // if it is not an exception in sending, we still might be
    // able to notify the server that we are dead
    if (listener != null && !sendingEx) logout()
    disconnect(e.toString)
  }

  /**
   * Sends data and waits until the data is sent.
   */
  def sendDataAndWait(obj: AnyRef) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    if (listener != null) {
      try listener.waitForSendData(obj)
      // If we have a socket exception writing rather than give
      // the user an error message they will not understand,
      // let's disconnect and let them re-enter.  This will
      // really only occur whent he socket is already in an
      // unusable state -- CLB 11/22/04
      catch { case e: IOException => org.nlogo.util.Exceptions.warn(e) }
    } else System.err.println("Attempted to send data on a shutdown listener, ignoring.")
  }

  private def receiveData(a: Any) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    a match {
      case m: Message => handleProtocolMessage(m)
      case info: String => if (!connected) {
        if (info == Version.version)
          sendDataAndWait(new HandshakeFromClient(listener.clientId, ConnectionTypes.COMP_CONNECTION))
        else handleLoginFailure("The version of the HubNet Client" +
                " you are using does not match the version of the " +
                "server. Please use the HubNet Client that comes with " + info)
      }
    }
  }
}
