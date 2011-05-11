package org.nlogo.lite;

import java.util.ArrayList;
import java.util.List;

import org.nlogo.window.Event;

import java.util.StringTokenizer;

import org.nlogo.api.CompilerException;

import org.nlogo.log.Logger;
import org.apache.log4j.xml.DOMConfigurator;

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

public strictfp class InterfaceComponent
    extends AppletPanel
    implements Event.LinkChild {
  public Logger logger;

  public InterfaceComponent(final java.awt.Frame frame) {
    super(frame,
        new java.awt.event.MouseAdapter() {
          @Override
          public void mouseClicked(java.awt.event.MouseEvent e) {
            org.nlogo.swing.BrowserLauncher.openURL
                (frame, "http://ccl.northwestern.edu/netlogo/",
                    false);
          }
        },
        false);
    addLinkComponent(listenerManager);
  }

  /**
   * internal use only
   */
  public Object getLinkParent() {
    // this will prevent events from propagating up to our enclosing window.
    // which we want because otherwise someone can't put two InterfaceComponents
    // in the same window without them interfering with each other. - ST 4/16/10
    return null;
  }

  /**
   * Recompiles the model.  Useful after calling
   * <code>setProcedures()</code>.
   *
   * @see #setProcedures
   */
  public void compile() {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    new org.nlogo.window.Events.CompileAllEvent().raise(this);
  }

  /**
   * Adds new widget to Interface tab given its specification,
   * in the same (undocumented) format found in a saved model.
   *
   * @param text the widget specification
   */
  public void makeWidget(String text) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    List<String> result = new ArrayList<String>();
    StringTokenizer tokenizer = new StringTokenizer(text, "\n");
    while (tokenizer.hasMoreTokens()) {
      result.add(tokenizer.nextToken());
    }
    String[] strings = result.toArray(new String[result.size()]);
    iP.loadWidget(strings, org.nlogo.api.Version.version());
  }

  /**
   * hides a particular widget. This method makes the specified widget invisible
   * in the NetLogo interface panel. It does not completely remove the widget,
   * which can later be brought back with <code>showWidget()</code>. This method
   * uses the "display name" to identify the widget. Display names are not
   * necessarily unique within a particular model. It is only safe to use this
   * method on widgets with unique display names. Otherwise the behavior is
   * unspecified.
   *
   * @param name the display name of the widget to hide.
   * @see #hideWidget
   */
  public void hideWidget(String name) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    iP.hideWidget(name);
  }

  /**
   * reveals a particular widget. This method makes the specified widget visible
   * in the NetLogo interface panel, if it has previously been hidden by a call to
   * <code>hideWidget()</code>. This method uses the "display name" to identify the
   * widget. Display names are not necessarily unique within a particular model.
   * It is only safe to use this method on widgets with unique display names.
   * Otherwise the behavior is unspecified.
   *
   * @param name the display name of the widget to reveal.
   * @see #hideWidget
   */
  public void showWidget(String name) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    iP.showWidget(name);
  }

  /**
   * Opens a model stored in a file.
   *
   * @param path the path (absolute or relative) of the NetLogo model to open.
   */
  public void open(String path)
      throws java.io.IOException,
      org.nlogo.window.InvalidVersionException {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    String source = org.nlogo.api.FileIO.file2String(path);
    if (source == null) {
      throw new IllegalStateException("couldn't open: '" + path + "'");
    }
    openFromSource(path, path, source);
  }

  /**
   * Starts NetLogo logging using the given file and username
   *
   * @param properties path to the XML properties file as defined by the log4j dtd
   * @param username   user defined username, this should be a unique identifier
   */
  public void startLogging(String properties, String username) {
    createLogger(username);
    DOMConfigurator.configure(properties);
    logger.modelOpened(workspace.getModelPath());
  }

  /**
   * Starts NetLogo logging using the given file and username
   *
   * @param reader   a reader that contains an XML properties file as defined by the log4j dtd
   * @param username user defined username, this should be a unique identifier
   */
  public void startLogging(java.io.Reader reader, String username) {
    createLogger(username);
    logger.configure(reader);
    logger.modelOpened(workspace.getModelPath());
  }

  public void createLogger(String username) {
    if (logger == null) {
      logger = new Logger(username);
      listenerManager.addListener(logger);
    }
    org.nlogo.api.Version.startLogging();
  }

  /**
   * Simulates a button press in the current model, exactly as if the user
   * had pressed the button.  If the button is a "once" button, this method
   * does not return until the button has popped back up.  (For "forever"
   * buttons, it returns immediately.)
   */
  public void pressButton(String name) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    final org.nlogo.window.ButtonWidget button =
        (org.nlogo.window.ButtonWidget) findWidget(name, org.nlogo.window.ButtonWidget.class);
    button.keyTriggered();
  }

  public org.nlogo.window.Widget findWidget(String name, Class<?> type) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    java.awt.Component[] components = iP.getComponents();
    for (int i = 0; i < components.length; i++) {
      java.awt.Component comp = components[i];
      if (comp.getClass() == type && ((org.nlogo.window.Widget) comp).displayName().equals(name)) {
        return (org.nlogo.window.Widget) comp;
      }
    }
    throw new IllegalArgumentException("widget \"" + name + "\" not found");
  }

  /**
   * returns the current contents of the NetLogo Graphics Window. This image can
   * be saved to disk, displayed to the user later, etc.
   */
  public java.awt.image.RenderedImage getViewImage() {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    return workspace.exportView();
  }

  /**
   * @param writer to writer the contents of the export world
   *               feature
   */
  public void exportWorld(java.io.PrintWriter writer) {
    workspace.exportWorld(writer);
    writer.flush();
  }

  /**
   * returns a graphical image of the current contents of the plot
   * with the given name. This image can be saved to disk, displayed
   * to the user later, etc.
   *
   * @param name the display name of the widget to reveal.
   */
  public java.awt.image.RenderedImage getPlotContentsAsImage(String name) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    return ((org.nlogo.window.PlotWidget) findWidget
        (name, org.nlogo.window.PlotWidget.class)).exportGraphics();
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
  public void reportAndCallback(final String code, final InvocationListener handler) {
    new Thread("reportAndCallback") {
      @Override
      public void run() {
        try {
          handler.handleResult(report(code));
        } catch (CompilerException e) {
          handler.handleError(e);
        }
      }
    }.start();
  }

  /**
   * Callback interface used by
   * <code>reportAndCallback()</code>.
   */
  public interface InvocationListener
      extends java.util.EventListener {
    /**
     * Called by
     * <code>reportAndCallback()</code>
     * if the request completes successfully.
     */
    void handleResult(Object value);

    /**
     * Called by
     * <code>reportAndCallback()</code>
     * if the code did not compile.
     */
    void handleError(CompilerException error);
  }
}
