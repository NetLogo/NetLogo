package org.nlogo.lite;

import java.util.ArrayList;
import java.util.List;

import org.nlogo.agent.Observer;
import org.nlogo.agent.World;
import org.nlogo.agent.World3D;
import org.nlogo.api.CompilerException;
import org.nlogo.nvm.Workspace;
import org.nlogo.api.ModelSection;
import org.nlogo.api.ModelType;
import org.nlogo.api.Version;
import org.nlogo.api.SimpleJobOwner;
import org.nlogo.window.AppletAdPanel;
import org.nlogo.window.CompilerManager;
import org.nlogo.window.Event;
import org.nlogo.window.Events.CompiledEvent;
import org.nlogo.window.Events.LoadSectionEvent;
import org.nlogo.window.InterfacePanelLite;
import org.nlogo.window.InvalidVersionException;
import org.nlogo.window.ModelLoader;
import org.nlogo.window.NetLogoListenerManager;
import org.nlogo.window.RuntimeErrorDialog;

/**
 * The superclass of org.nlogo.lite.InterfaceComponent.
 * Also used by org.nlogo.lite.Applet.
 * <p>See the "Controlling" section of the NetLogo User Manual
 * for example code.
 */

public abstract class AppletPanel
    extends javax.swing.JPanel
    implements
    org.nlogo.util.Exceptions.Handler,
    Event.LinkParent {
  public final InterfacePanelLite iP;
  public final LiteWorkspace workspace;
  public final ProceduresLite procedures;
  public final AppletAdPanel panel;
  protected final SimpleJobOwner defaultOwner;

  /**
   * The NetLogoListenerManager stored in this field can be used to add and remove
   * org.nlogo.api.NetLogoListeners, so the embedding environment can receive
   * notifications of events happening within NetLogo.  The relevant methods
   * on NetLogoListenerManager are addListener(), removeListener(), and
   * clearListeners().  The first two expect a NetLogoListener as input.
   */
  public final NetLogoListenerManager listenerManager = new NetLogoListenerManager();

  public AppletPanel(java.awt.Frame frame, java.awt.event.MouseListener iconListener, boolean isApplet) {
    org.nlogo.workspace.AbstractWorkspace.isApplet(isApplet);
    RuntimeErrorDialog.init(this);
    org.nlogo.util.Exceptions.setHandler(this);
    java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
    java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();

    setLayout(gridbag);
    World world = Version.is3D() ? new World3D() : new World();
    workspace = new LiteWorkspace(this, isApplet, world, frame, listenerManager);
    addLinkComponent(workspace.aggregateManager());
    addLinkComponent(workspace);
    procedures = new ProceduresLite(workspace, workspace);
    addLinkComponent(procedures);
    addLinkComponent(new CompilerManager(workspace, procedures));
    addLinkComponent(new CompiledEvent.Handler() {
      public void handle(CompiledEvent e) {
        if (e.error != null) {
          e.error.printStackTrace();
        }
      }
    });
    addLinkComponent(new LoadSectionEvent.Handler() {
      public void handle(LoadSectionEvent e) {
        if (e.section == ModelSection.AGGREGATE) {
          workspace.aggregateManager().load(e.text, workspace);
        }
      }
    });
    iP = new InterfacePanelLite(workspace.viewWidget, workspace, workspace, workspace.plotManager(),
        new LiteEditorFactory(workspace),
        // for ReviewTab. See InterfacePanelLite for details. - JC 6/13/11
        false);
    workspace.setWidgetContainer(iP);

    defaultOwner = new SimpleJobOwner("AppletPanel", workspace.world.mainRNG, Observer.class);

    setBackground(java.awt.Color.WHITE);
    c.gridwidth = 1;
    c.gridheight = 2;
    c.weighty = 1.0;
    gridbag.setConstraints(iP, c);
    add(iP);

    panel = new AppletAdPanel(iconListener);
    c.anchor = java.awt.GridBagConstraints.SOUTH;
    gridbag.setConstraints(panel, c);
    add(panel);
  }

  /**
   * internal use only
   */
  public java.net.URL getFileURL(String filename)
      throws java.net.MalformedURLException {
    throw new UnsupportedOperationException();
  }

  /**
   * AppletPanel passes the focus request to the InterfacePanel
   */
  @Override
  public void requestFocus() {
    if (iP != null) {
      iP.requestFocus();
    }
  }

  /**
   * internal use only
   */
  public void setAdVisible(boolean visible) {
    panel.setVisible(visible);
  }

  /**
   * sets the current working directory
   *
   * @param url the directory as java.net.URL
   */
  public void setPrefix(java.net.URL url) {
    workspace.fileManager().setPrefix(url);
  }

  /**
   * internal use only
   */
  public void handle(final Throwable throwable) {
    try {
      if (!(throwable instanceof org.nlogo.api.LogoException)) {
        throwable.printStackTrace(System.err);
      }
      final Thread thread = Thread.currentThread();
      org.nlogo.awt.Utils.invokeLater
          (new Runnable() {
            public void run() {
              RuntimeErrorDialog.show("Runtime Error", null, null,
                  thread, throwable);
            }
          });
    } catch (RuntimeException ex) {
      ex.printStackTrace(System.err);
    }
  }

  /// LinkComponent stuff

  public final List<Object> linkComponents =
      new ArrayList<Object>();

  /**
   * internal use only
   */
  public void addLinkComponent(Object c) {
    linkComponents.add(c);
  }

  /**
   * internal use only
   */
  public Object[] getLinkChildren() {
    return linkComponents.toArray();
  }

  /**
   * Runs NetLogo commands and waits for them to complete.
   * <p>This method must <strong>not</strong> be called from the AWT event
   * queue thread or while that thread is blocked.
   * It is an error to do so.
   *
   * @param source The command or commands to run
   * @throws org.nlogo.api.CompilerException
   *                               if the code fails to compile
   * @throws IllegalStateException if called from the AWT event queue thread
   * @see #commandLater
   */
  public void command(String source)
      throws CompilerException {
    org.nlogo.awt.Utils.cantBeEventDispatchThread();
    workspace.evaluateCommands(defaultOwner, source);
  }

  /**
   * Runs NetLogo commands in the background.  Returns immediately,
   * without waiting for the commands to finish.
   * <p>This method may be called from <em>any</em> thread.
   *
   * @param source The command or commands to run
   * @throws org.nlogo.api.CompilerException
   *          if the code fails to compile
   * @see #command
   */
  public void commandLater(String source)
      throws CompilerException {
    workspace.evaluateCommands(defaultOwner, source, false);
  }

  /**
   * Runs a NetLogo reporter.
   * <p>This method must <strong>not</strong> be called from the AWT event
   * queue thread or while that thread is blocked.
   * It is an error to do so.
   *
   * @param source The reporter to run
   * @return the result reported; may be of type java.lang.Integer, java.lang.Double,
   *         java.lang.Boolean, java.lang.String, {@link org.nlogo.api.LogoList},
   *         {@link org.nlogo.api.Agent}, AgentSet, or Nobody
   * @throws org.nlogo.api.CompilerException
   *                               if the code fails to compile
   * @throws IllegalStateException if called from the AWT event queue thread
   */
  public Object report(String source)
      throws CompilerException {
    org.nlogo.awt.Utils.cantBeEventDispatchThread();
    return workspace.evaluateReporter(defaultOwner, source);
  }

  /**
   * Returns the contents of the Code tab.
   *
   * @return contents of Code tab
   */
  public String getProcedures() {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    return procedures.innerSource();
  }

  /**
   * Replaces the contents of the Code tab.
   * Does not recompile the model.
   *
   * @param source new contents
   */
  public void setProcedures(String source) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    procedures.innerSource(source);
  }

  /**
   * Opens a model stored in a string.
   *
   * @param name   Model name (will appear in the main window's title bar)
   * @param source The complete model, including widgets and so forth,
   *               in the same format as it would be stored in a file.
   */
  public void openFromSource(String name, String path, String source)
      throws InvalidVersionException {
    iP.reset();
    // I haven't thoroughly searched for all the places where
    // the type of model matters, but it seems to me like it
    // ought to be OK; the main thing the model type affects
    // in the engine (as opposed to e.g. the behavior of Save
    // in the File menu) is where files are read or written
    // from, but in the applet case 1) you can't write files
    // and 2) we have special code for the reading case that
    // goes out to the web server instead of 1the file
    // system.... so, I think TYPE_LIBRARY is probably OK. -
    // ST 10/11/05
    RuntimeErrorDialog.setModelName(name);
    ModelLoader.load(this, path, ModelType.LIBRARY, source);
  }

}
