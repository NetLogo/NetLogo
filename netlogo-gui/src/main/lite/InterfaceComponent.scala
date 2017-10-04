// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import java.net.URI
import java.nio.file.Paths

import org.apache.log4j.xml.DOMConfigurator

import org.nlogo.api.Version
import org.nlogo.awt.EventQueue
import org.nlogo.core.{ CompilerException, Widget => CoreWidget }
import org.nlogo.log.Logger
import org.nlogo.window.{ Event, Widget, ButtonWidget, PlotWidget }
import org.nlogo.swing.BrowserLauncher

/**
 * This component is a wrapper around the contents of the
 * interface panel that can be embedded in another application.
 * <p/>
 * Once created, an InterfaceComponent can't be garbage collected,
 * so you should open successive models in the same InterfaceComponent,
 * rather than making new instances.
 * <p/>
 * <p>See the "Controlling" section of the NetLogo User Manual
 * for example code.
 */

class InterfaceComponent(frame: java.awt.Frame)
extends AppletPanel(frame,
                    new java.awt.event.MouseAdapter {
                      override def mouseClicked(e: java.awt.event.MouseEvent) {
                        BrowserLauncher.openURI(frame, new URI("http://ccl.northwestern.edu/netlogo/"))
                      }})
with Event.LinkChild {

  var logger: Logger = null

  addLinkComponent(listenerManager)

  // this will prevent events from propagating up to our enclosing window.
  // which we want because otherwise someone can't put two InterfaceComponents
  // in the same window without them interfering with each other. - ST 4/16/10
  /** internal use only */
  override def getLinkParent = null

  /**
   * Recompiles the model.  Useful after calling <code>setProcedures()</code>.
   *
   * @see #setProcedures
   */
  def compile() {
    EventQueue.mustBeEventDispatchThread()
    (new org.nlogo.window.Events.CompileAllEvent).raise(this)
  }

  /**
   * Adds new widget to Interface tab given its specification, in the same format
   * found in a saved model.
   *
   * @param text the widget specification
   */
  def makeWidget(widget: CoreWidget) {
    EventQueue.mustBeEventDispatchThread()
    iP.loadWidget(widget)
  }

  /**
   * hides a particular widget. This method makes the specified widget invisible in the NetLogo
   * interface panel. It does not completely remove the widget, which can later be brought back with
   * <code>showWidget()</code>. This method uses the "display name" to identify the widget. Display
   * names are not necessarily unique within a particular model. It is only safe to use this method
   * on widgets with unique display names. Otherwise the behavior is unspecified.
   *
   * @param name the display name of the widget to hide.
   * @see #hideWidget
   */
  def hideWidget(name: String) {
    EventQueue.mustBeEventDispatchThread()
    iP.hideWidget(name)
  }

  /**
   * reveals a particular widget. This method makes the specified widget visible in the NetLogo
   * interface panel, if it has previously been hidden by a call to <code>hideWidget()</code>. This
   * method uses the "display name" to identify the widget. Display names are not necessarily unique
   * within a particular model.  It is only safe to use this method on widgets with unique display
   * names.  Otherwise the behavior is unspecified.
   *
   * @param name the display name of the widget to reveal.
   * @see #hideWidget
   */
  def showWidget(name: String) {
    EventQueue.mustBeEventDispatchThread()
    iP.showWidget(name)
  }

  /**
   * Opens a model stored in a file.
   *
   * @param path the path (absolute or relative) of the NetLogo model to open.
   */
  @throws(classOf[java.io.IOException])
  @throws(classOf[org.nlogo.window.InvalidVersionException])
  def open(path: String) {
    EventQueue.mustBeEventDispatchThread()
    openFromURI(Paths.get(path).toUri)
  }

  /**
   * Starts NetLogo logging using the given file and username
   *
   * @param properties path to the XML properties file as defined by the log4j dtd
   * @param username   user defined username, this should be a unique identifier
   */
  def startLogging(properties: String, username: String) {
    createLogger(username)
    DOMConfigurator.configure(properties)
    logger.modelOpened(workspace.getModelPath)
  }

  /**
   * Starts NetLogo logging using the given file and username
   *
   * @param reader   a reader that contains an XML properties file as defined by the log4j dtd
   * @param username user defined username, this should be a unique identifier
   */
  def startLogging(reader: java.io.Reader, username: String) {
    createLogger(username)
    logger.configure(reader)
    logger.modelOpened(workspace.getModelPath)
  }

  def createLogger(username: String) {
    if (logger == null) {
      logger = new Logger(username)
      listenerManager.addListener(logger)
    }
    Version.startLogging()
  }

  /**
   * Simulates a button press in the current model, exactly as if the user had pressed the button.
   * If the button is a "once" button, this method does not return until the button has popped back
   * up.  (For "forever" buttons, it returns immediately.)
   */
  def pressButton(name: String) {
    EventQueue.mustBeEventDispatchThread()
    val button = findWidget(name, classOf[ButtonWidget]).asInstanceOf[ButtonWidget]
    button.keyTriggered()
  }

  def findWidget(name: String, tpe: Class[_]): Widget = {
    EventQueue.mustBeEventDispatchThread()
    def matches(comp: java.awt.Component) =
      comp.getClass() == tpe && comp.asInstanceOf[Widget].displayName == name
    iP.getComponents.find(matches)
      .getOrElse(throw new IllegalArgumentException("widget \"" + name + "\" not found"))
      .asInstanceOf[Widget]
  }

  /**
   * returns the current contents of the 2D view. This image can be saved to disk, displayed to the
   * user later, etc.
   */
  def getViewImage: java.awt.image.RenderedImage = {
    EventQueue.mustBeEventDispatchThread()
    workspace.exportView
  }

  /**
   * @param writer to writer the contents of the export world feature
   */
  def exportWorld(writer: java.io.PrintWriter) {
    workspace.exportWorld(writer)
    writer.flush()
  }

  /**
   * returns a graphical image of the current contents of the plot with the given name. This image
   * can be saved to disk, displayed to the user later, etc.
   *
   * @param name the display name of the widget to reveal.
   */
  def getPlotContentsAsImage(name: String): java.awt.image.RenderedImage = {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    findWidget(name, classOf[PlotWidget]).asInstanceOf[PlotWidget].exportGraphics
  }

  /**
   * evaluates a reporter and return the value to continuation object.
   * This is a convenience method for evaluating reporters on the event
   * thread. Since it is an error to call <code>report()</code> from the event
   * thread, this method creates a new thread to call <code>report()</code> and
   * then passes the result to the given <code>InvocationListener</code>.
   * <p/>
   * <em>This method may be called from any thread, including the AWT Event
   * Thread.</em>
   */
  def reportAndCallback(code: String, handler: InterfaceComponent.InvocationListener) {
    new Thread("InterfaceComponent.reportAndCallback") {
      override def run() {
        try handler.handleResult(report(code))
        catch {
          case e: CompilerException =>
            handler.handleError(e)
        }}}.start()
  }

}

object InterfaceComponent {
  /** Callback interface used by <code>reportAndCallback()</code> */
  trait InvocationListener extends java.util.EventListener {
    /** Called by <code>reportAndCallback()</code> if the request completes successfully. */
    def handleResult(value: AnyRef)
    /** Called by <code>reportAndCallback()</code> if the code did not compile. */
    def handleError(error: CompilerException)
  }
}
