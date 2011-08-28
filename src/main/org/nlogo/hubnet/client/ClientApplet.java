package org.nlogo.hubnet.client;

import org.nlogo.api.I18N;
import org.nlogo.hubnet.connection.ClientRoles;
import org.nlogo.hubnet.connection.Ports;
import org.nlogo.window.AppletAdPanel;
import org.nlogo.window.EditorFactory;

import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import org.nlogo.window.VMCheck;
import org.nlogo.api.Token;
import scala.Enumeration;

import netscape.javascript.JSObject;

public strictfp class ClientApplet
    extends javax.swing.JApplet
    implements ErrorHandler {
  private ClientPanel clientPanel;
  private AppletAdPanel adPanel;

  @Override
  public void init() {
    VMCheck.detectBadJVMs();
    org.nlogo.awt.Utils.invokeLater(new Runnable() {
      public void run() {
        setBackground(java.awt.Color.white);
        getContentPane().setBackground(java.awt.Color.white);
        EditorFactory editorFactory = new EditorFactory() {
          public org.nlogo.editor.AbstractEditorArea newEditor(int cols, final int rows, boolean disableFocusTraversal) {
            return newEditor(cols, rows, disableFocusTraversal, null);
          }

          org.nlogo.editor.AbstractEditorArea newEditor
              (int cols, final int rows, boolean disableFocusTraversal,
               java.awt.event.TextListener listener) {
            return new AppletEditor(rows, cols, disableFocusTraversal);
          }
        };

        clientPanel = new ClientPanel(editorFactory, ClientApplet.this, new DummyCompilerServices());

        clientPanel.setBackground(java.awt.Color.white);

        // Whenever the size of the client panel changes, notify the page containing the applet.
        clientPanel.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            notifyNewSize();
          }
        });

        java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();

        setLayout(gridbag);
        c.gridwidth = 1;
        c.gridheight = 2;
        c.weighty = 1.0;
        gridbag.setConstraints(clientPanel, c);
        add(clientPanel);

        adPanel = new org.nlogo.window.AppletAdPanel
            (new java.awt.event.MouseAdapter() {
              @Override
              public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                  java.applet.AppletContext context = ClientApplet.this.getAppletContext();
                  context.showDocument
                      (new java.net.URL("http://ccl.northwestern.edu/netlogo/"), "_blank");
                } catch (java.net.MalformedURLException ex) {
                  throw new IllegalStateException();
                }
              }
            });
        c.anchor = java.awt.GridBagConstraints.SOUTH;
        gridbag.setConstraints(adPanel, c);
        add(adPanel);
        attemptLogin = true;
        go(getDocumentBase().getHost(), true);
      }
    });
  }

  @Override
  public void start() {
    clientPanel.setDisplayOn(true);
  }

  @Override
  public void stop() {
    clientPanel.setDisplayOn(false);
  }

  // Returns information about the parameters that are understood by this applet.  Each element of the
  // array is a set of three Strings containing the name, the type, and a description. Parameters are
  // supplied in the applet tag like this:
  //
  // <applet code="...">
  //    <param name="role" value="controller">
  //    <param name="port" value="9173">
  // </applet>
  @Override
  public String[][] getParameterInfo() {
    String pinfo[][] = {
        {"role",    "String",   "If specified, then the login dialog will be skipped and the applet will log in "
                              + "immediately with the specified role, which can be either \"controller\" or "
                              + "\"participant\"."},
        {"user",    "String",   "The user name to use for connecting to the activity. This is only used if the "
                              + "\"role\" parameter was also specified. A default user name will be used if this "
                              + "parameter is omitted."},
        {"port",    "String",   "The port number to connect to."},
        {"notify",  "Boolean",  "If \"true\", notify the page containing the applet of changes in the activity's "
                              + "status. The page must define a JavaScript function called handleActivityEvent "
                              + "that accepts two arguments (event, and eventArgs). This function will be called "
                              + "by the applet for certain events that happen in the activity. (Currently, the "
                              + "only events supported are \"login complete\" and \"size changed\". The first one "
                              + "doesn't have any eventArgs. The eventArgs for the second one is an array {width, "
                              + "height} containing the new ideal applet size for the activity.)" }
    };
    return pinfo;
  }

  // Returns true if the given parameter was supplied in the applet.
  private boolean hasParam(String param) {
    return getParameter(param) != null;
  }

  // Returns the default port number that should be used for connecting to the activity, or pre-filled
  // in the login dialog.
  private int getDefaultPort() {
    return hasParam("port") ? Integer.parseInt(getParameter("port")) : Ports.DEFAULT_PORT_NUMBER();
  }

  // Returns the username to use for logging in to the activity. This is only used when the login
  // dialog is skipped.
  private String getDefaultUsername() {
    if (hasParam("user"))
      return getParameter("user");

    if (getParameter("role").equals("controller"))
      return "controller";

    // "user" will be the default if the role is "participant" or if the role is unspecified.
    return "user";
  }

  public void go(final String server, final boolean isApplet) {
    org.nlogo.awt.Utils.invokeLater(new Runnable() {
      public void run() {

        if (hasParam("role") && getParameter("role").equals("controller")) {
          login(getDefaultUsername(), server, getDefaultPort(), ClientRoles.Controller());
        }
        else if (hasParam("role") && getParameter("role").equals("participant")) {
          login(getDefaultUsername(), server, getDefaultPort(), ClientRoles.Participant());
        }
        else {
          showLoginDialog(server, isApplet);
        }
      }
    });
  }

  private LoginDialog loginDialog;
  private boolean attemptLogin = true;

  private void showLoginDialog(final String server, final boolean isApplet) {
    loginDialog = new LoginDialog
        (new Frame(), "", server, getDefaultPort(), !isApplet);
    loginDialog.addWindowListener
        (new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e) {
            clientPanel.logout();
            attemptLogin = false;
            if (!isApplet) {
              System.exit(0);
            }
          }
        });

    org.nlogo.awt.Utils.center(loginDialog, null);
    doLogin();
  }

  private void doLogin() {
    if (attemptLogin && !loginDialog.isVisible()) {
      loginDialog.doLogin();
      if (attemptLogin) {
        login(
          loginDialog.getUserName(), loginDialog.getServer(),
          loginDialog.getPort(), loginDialog.getClientRole());
      }
    }
  }

  private void login(final String userid, final String hostip, final int port, final Enumeration.Value role) {
    final String[] exs = new String[]{null};
    org.nlogo.swing.ModalProgressTask.apply(
      org.nlogo.awt.Utils.getFrame(ClientApplet.this),
      "Entering...",
      new Runnable() {
        public void run() {
          exs[0] = clientPanel.login(userid, hostip, port, role);
          clientPanel.requestFocus();
        }});
    if (exs[0] != null) {
      clientPanel.disconnect(exs[0]);
      handleLoginFailure(exs[0]);
    }
  }

  @Override
  public void destroy() {
    org.nlogo.awt.Utils.invokeLater(new Runnable() {
      public void run() {
        attemptLogin = false;
        clientPanel.logout();
      }
    });
  }

  public void handleLoginFailure(final String errorMessage) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    org.nlogo.awt.Utils.invokeLater(new Runnable() {
      public void run() {
        org.nlogo.swing.OptionDialog.show
            (ClientApplet.this, "Login Failed", errorMessage, new String[]{I18N.gui().get("common.buttons.ok")});
      }
    });
  }

  public void handleDisconnect(final String activityName, boolean connected, final String reason) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread();
    if (connected) {
      org.nlogo.awt.Utils.invokeLater(new Runnable() {
        public void run() {
          String[] ok = {I18N.gui().get("common.buttons.ok")};
          org.nlogo.swing.OptionDialog.show
              (ClientApplet.this, "",
                  "You have been disconnected from " + activityName + ".\nReason: " + reason, ok);
        }
      });
    }
    doLogin();
  }

  public void completeLogin() {
    // we only have a frame when we're not
    // really in an applet ev 8/1/08
    if (appFrame != null) {
      appFrame.pack();
      appFrame.setVisible(true);
    }

    // Notify the page containing the applet that we finished logging in to the activity.
    notifyActivityEvent("login complete", null);
  }

  /**
   * Send a notification to the web page containing the applet of the ideal applet size for running
   * this activity (the notification only gets sent if the applet tag had the "notify" parameter set
   * to "true").
   */
  public void notifyNewSize() {
    int width = clientPanel.getPreferredSize().width + adPanel.getPreferredSize().width;
    int height = Math.max(clientPanel.getPreferredSize().height, adPanel.getPreferredSize().height);
    notifyActivityEvent("size changed", new int[] {width, height});
  }

  /**
   * If the applet tag had the "notify" parameter set to "true", then send a notification to the
   * web page containing the applet by evaluating a JavaScript function called handleActivityEvent
   * (which must be defined by the page).  This function must accept a string argument called
   * eventName, and a second argument called eventArgs (whose type depends on the event).
   *
   * @param eventName The name of the event (e.g., "login complete" or "size changed").
   * @param eventArgs Any additional information associated with this event (e.g., the new dimensions
   * of the activity for the "size changed" event). This may be null.
   */
  public void notifyActivityEvent(String eventName, Object eventArgs) {
    if (hasParam("notify") && getParameter("notify").equals("true")) {
      try {
        JSObject win = JSObject.getWindow(this);
        win.call("handleActivityEvent", new Object[] {eventName, eventArgs});
      }
      catch (Exception e) {
        // Possibly, the applet is being viewed in an applet viewer. In any case, ignore the exception.
      }
    }
  }

  // this is supplied only for debugging purposes

  private javax.swing.JFrame appFrame = null;

  public static void main(final String[] args) {
    System.setProperty("apple.awt.graphics.UseQuartz", "true");
    System.setProperty("apple.awt.showGrowBox", "true");
    VMCheck.detectBadJVMs();
    org.nlogo.awt.Utils.invokeLater
        (new Runnable() {
          public void run() {
            final ClientApplet applet = new ClientApplet();
            applet.appFrame = new javax.swing.JFrame("HubNet Client");
            applet.appFrame.setResizable(false);
            applet.appFrame.addWindowListener
                (new java.awt.event.WindowAdapter() {
                  @Override
                  public void windowClosing(java.awt.event.WindowEvent e) {
                    applet.stop();
                    System.exit(0);
                  }
                });
            applet.appFrame.getContentPane().setLayout(new java.awt.BorderLayout());
            applet.appFrame.getContentPane().add(applet, java.awt.BorderLayout.CENTER);
            applet.init();
            applet.appFrame.pack();
            String host = "";
            try {
              host = java.net.InetAddress.getLocalHost().getHostAddress().toString();
            } catch (java.net.UnknownHostException ex) {
              host = "";
            }
            applet.go(host, false);
          }
        });
  }

  public strictfp class DummyCompilerServices
      implements org.nlogo.api.CompilerServices {
    public String autoConvert(String source, boolean subprogram, boolean reporter, String modelVersion) {
      return source;
    }

    public Object readNumberFromString(String source) {
      return source;
    }

    public void checkReporterSyntax(String source) {
    }

    public void checkCommandSyntax(String source) {
    }

    public Object readFromString(String source) {
      return source;
    }

    public boolean isValidIdentifier(String s) {
      return true;
    }

    public boolean isConstant(String s) {
      throw new UnsupportedOperationException();
    }

    public Token[] tokenizeForColorization(String s) {
      throw new UnsupportedOperationException();
    }

    public Token getTokenAtPosition(String s, int pos) {
      throw new UnsupportedOperationException();
    }

    public java.util.Map<String, java.util.List<Object>> findProcedurePositions(String source) {
      throw new UnsupportedOperationException();
    }
  }
}
