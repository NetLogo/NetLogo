// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import java.util.{ ArrayList, List => JList }

import org.nlogo.api.{ LogoException, ModelType, Version, SimpleJobOwner }
import org.nlogo.agent.{ World, World3D }
import org.nlogo.core.{ AgentKind, CompilerException }
import org.nlogo.window.{ Event, FileController, AppletAdPanel, CompilerManager, LinkRoot,
  InterfacePanelLite, InvalidVersionException, ReconfigureWorkspaceUI, NetLogoListenerManager, RuntimeErrorDialog }
import org.nlogo.window.Events.{ CompiledEvent, LoadModelEvent }
import org.nlogo.workspace.OpenModel
import org.nlogo.fileformat

import java.net.URI

/**
 * The superclass of org.nlogo.lite.InterfaceComponent.  Also used by org.nlogo.lite.Applet.
 *
 * See the "Controlling" section of the NetLogo User Manual for example code.
 */

abstract class AppletPanel(
  frame: java.awt.Frame, iconListener: java.awt.event.MouseListener, isApplet: Boolean)
extends javax.swing.JPanel
with org.nlogo.api.Exceptions.Handler
with Event.LinkParent
with LinkRoot {

  /**
   * The NetLogoListenerManager stored in this field can be used to add and remove NetLogoListeners,
   * so the embedding environment can receive notifications of events happening within NetLogo.
   * Relevant methods on NetLogoListenerManager are addListener(), removeListener(), and
   * clearListeners().  The first two take a NetLogoListener as input.
   */
  val listenerManager = new NetLogoListenerManager

  org.nlogo.workspace.AbstractWorkspace.isApplet(isApplet)
  RuntimeErrorDialog.init(this)
  org.nlogo.api.Exceptions.setHandler(this)

  protected val world = if(Version.is3D) new World3D() else new World
  val workspace = new LiteWorkspace(this, isApplet, world, frame, listenerManager)
  val procedures = new ProceduresLite(workspace, workspace)
  protected val liteEditorFactory = new LiteEditorFactory(workspace)

  val iP = createInterfacePanel(workspace)

  val defaultOwner = new SimpleJobOwner("AppletPanel", workspace.world.mainRNG, AgentKind.Observer)

  val panel = new AppletAdPanel(iconListener)

  addLinkComponent(workspace.aggregateManager)
  addLinkComponent(workspace)
  addLinkComponent(procedures)
  addLinkComponent(new CompilerManager(workspace, procedures))
  addLinkComponent(new CompiledEvent.Handler {
    override def handle(e: CompiledEvent) {
      if (e.error != null)
        e.error.printStackTrace()
  }})
  addLinkComponent(new LoadModelEvent.Handler {
    override def handle(e: LoadModelEvent) {
      workspace.aggregateManager.load(e.model, workspace)
  }})
  workspace.setWidgetContainer(iP)
  setBackground(java.awt.Color.WHITE)
  setLayout(new java.awt.BorderLayout)
  add(iP, java.awt.BorderLayout.CENTER)
  add(panel, java.awt.BorderLayout.EAST)

  /** internal use only */
  @throws(classOf[java.net.MalformedURLException])
  def getFileURL(filename: String): java.net.URL =
    throw new UnsupportedOperationException

  /** AppletPanel passes the focus request to the InterfacePanel */
  override def requestFocus() {
    if (iP != null)
      iP.requestFocus()
  }

  protected def createInterfacePanel(workspace: LiteWorkspace): InterfacePanelLite =
    new InterfacePanelLite(workspace.viewWidget, workspace, workspace, workspace.plotManager, liteEditorFactory)

  /** internal use only */
  def setAdVisible(visible: Boolean) {
    panel.setVisible(visible)
  }

  /**
   * sets the current working directory
   *
   * @param url the directory as java.net.URL
   */
  def setPrefix(url: java.net.URL) {
    workspace.fileManager.setPrefix(url)
  }

  /** internal use only */
  def handle(throwable: Throwable) {
    try {
      if (!throwable.isInstanceOf[LogoException])
        throwable.printStackTrace(System.err)
      val thread = Thread.currentThread
      org.nlogo.awt.EventQueue.invokeLater(
        new Runnable {
          override def run() {
            RuntimeErrorDialog.show(null, null, thread, throwable)
          }})
    }
    catch {
      case ex: RuntimeException =>
        ex.printStackTrace(System.err)
    }
  }

  /**
   * Runs NetLogo commands and waits for them to complete.
   * <p>This method must <strong>not</strong> be called from the AWT event
   * queue thread or while that thread is blocked.
   * It is an error to do so.
   *
   * @param source The command or commands to run
   * @throws org.nlogo.core.CompilerException
   *                               if the code fails to compile
   * @throws IllegalStateException if called from the AWT event queue thread
   * @see #commandLater
   */
  @throws(classOf[CompilerException])
  def command(source: String) {
    org.nlogo.awt.EventQueue.cantBeEventDispatchThread()
    workspace.evaluateCommands(defaultOwner, source)
  }

  /**
   * Runs NetLogo commands in the background.  Returns immediately,
   * without waiting for the commands to finish.
   * <p>This method may be called from <em>any</em> thread.
   *
   * @param source The command or commands to run
   * @throws org.nlogo.core.CompilerException
   *          if the code fails to compile
   * @see #command
   */
  @throws(classOf[CompilerException])
  def commandLater(source: String) {
    workspace.evaluateCommands(defaultOwner, source, false)
  }

  /**
   * Runs a NetLogo reporter.
   *
   * This method must <strong>not</strong> be called from the AWT event queue thread or while that
   * thread is blocked.  It is an error to do so.
   *
   * @param source The reporter to run
   * @return the result reported; may be of type java.lang.Integer, java.lang.Double,
   *         java.lang.Boolean, java.lang.String, {@link org.nlogo.core.LogoList},
   *         {@link org.nlogo.api.Agent}, AgentSet, or Nobody
   * @throws org.nlogo.core.CompilerException
   *                               if the code fails to compile
   * @throws IllegalStateException if called from the AWT event queue thread
   */
  @throws(classOf[CompilerException])
  def report(source: String): AnyRef = {
    org.nlogo.awt.EventQueue.cantBeEventDispatchThread()
    workspace.evaluateReporter(defaultOwner, source)
  }

  /**
   * Returns the contents of the Code tab.
   *
   * @return contents of Code tab
   */
  def getProcedures: String = {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    procedures.innerSource
  }

  /**
   * Replaces the contents of the Code tab. Does not recompile the model.
   *
   * @param source new contents
   */
  def setProcedures(source: String): Unit = {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    procedures.innerSource = source
  }

  /**
   * Opens a model stored in a string.
   *
   * @param name   Model name (will appear in the main window's title bar)
   * @param source The complete model, including widgets and so forth,
   *               in the same format as it would be stored in a file.
   */
  @throws(classOf[InvalidVersionException])
  def openFromURI(uri: URI) {
    iP.reset()
    // I haven't thoroughly searched for all the places where the type of model matters, but it
    // seems to me like it ought to be OK; the main thing the model type affects in the engine (as
    // opposed to e.g. the behavior of Save in the File menu) is where files are read or written
    // from, but in the applet case 1) you can't write files and 2) we have special code for the
    // reading case that goes out to the web server instead of 1the file system.... so, I think
    // TYPE_LIBRARY is probably OK. - ST 10/11/05
    RuntimeErrorDialog.setModelName(uri.getPath.split("/").last)
    val controller = new FileController(this, workspace)
    val loader = fileformat.standardLoader(workspace.compiler.compilerUtilities, workspace.getExtensionManager, workspace.getCompilationEnvironment)
    val modelOpt = OpenModel(uri, controller, loader, Version)
    modelOpt.foreach(model => ReconfigureWorkspaceUI(this, uri, ModelType.Library, model, workspace))
  }
}
