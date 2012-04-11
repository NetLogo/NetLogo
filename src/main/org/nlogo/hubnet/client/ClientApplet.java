// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client;

import org.nlogo.api.I18N;
import org.nlogo.api.Token;
import org.nlogo.hubnet.connection.ClientRole;
import org.nlogo.hubnet.connection.Ports;
import org.nlogo.window.EditorFactory;
import org.nlogo.window.VMCheck;

import java.awt.Frame;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import scala.Enumeration;

public strictfp class ClientApplet
    extends javax.swing.JApplet
    implements ErrorHandler {
  private ClientPanel clientPanel;

  @Override
  public void init() {
    VMCheck.detectBadJVMs();
    org.nlogo.awt.EventQueue.invokeLater(new Runnable() {
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


         //corey add begins --- read in override file if it exists and is referenced.

          URL specurl = null;
          String spec = "";

          System.err.println("About to get the model name...");

          try {
             String name = getParameter("DefaultModel");
                 System.out.println("override url: " + name);
              if ( name != null )
              { specurl = new URL(name);       }
          }
          catch (MalformedURLException me)
          {
              me.printStackTrace();
          }


          if ( specurl != null )
          {
              System.out.println("loading interface from alternative source");
              try{
                    InputStream in = specurl.openStream();
                    java.io.BufferedReader bf = new java.io.BufferedReader(new java.io.InputStreamReader(in));
                    StringBuffer strBuff = new StringBuffer();
                    String line;
                    while((line = bf.readLine()) != null){
                    strBuff.append(line + "\n");
                    }
                    spec = strBuff.toString();
                    clientPanel.setInterfaceSpec(spec);
                }
            catch(java.io.IOException e){
                e.printStackTrace();
                }
          }
          else
          {
              System.out.println("loading interface from the network-delivered specification");
          }

          //corey add'n ends.

        clientPanel.setBackground(java.awt.Color.white);

        java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();

        setLayout(gridbag);
        c.gridwidth = 1;
        c.gridheight = 2;
        c.weighty = 1.0;
        gridbag.setConstraints(clientPanel, c);
        add(clientPanel);

        org.nlogo.window.AppletAdPanel panel = new org.nlogo.window.AppletAdPanel
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
        gridbag.setConstraints(panel, c);
        add(panel);
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

  public void go(final String server, final boolean isApplet) {
    org.nlogo.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        loginDialog = new LoginDialog
            (new Frame(), "", server, Ports.DEFAULT_PORT_NUMBER(), isApplet);
        loginDialog.addWindowListener
            (new java.awt.event.WindowAdapter() {
              @Override
              public void windowClosing(java.awt.event.WindowEvent e) {
                clientPanel.logout();
                attemptLogin = false;
                loginDialog.setVisible(false);
                if (!isApplet) { System.exit(0); }
              }
            });

        org.nlogo.awt.Positioning.center(loginDialog, null);
        doLogin();
      }
    });
  }

  private LoginDialog loginDialog;
  private boolean attemptLogin = true;

  private void doLogin() {
    if (attemptLogin && !loginDialog.isVisible()) {
      loginDialog.go(new LoginCallback() {
        @Override
        public void apply(String user, String host, int port, ClientRole role) {
          if (attemptLogin) { login(user, host, port, role); }
        }});
    }
  }

  private void login(final String userid, final String hostip, final int port,  final ClientRole role) {
    final String[] error = new String[1];
    org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(ClientApplet.this),
        "Entering...",
        new Runnable() {
          public void run() {
            scala.Option<String> e = clientPanel.login(userid, hostip, port, role);
            if(e.isDefined()) {
              error[0] = e.get();
            }
            clientPanel.requestFocus();
            loginDialog.setVisible(false);
          }
        });
    if (error[0] != null) {
      clientPanel.disconnect(error[0]);
      handleLoginFailure(error[0]);
    }
  }

  @Override
  public void destroy() {
    org.nlogo.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        attemptLogin = false;
        clientPanel.logout();
      }
    });
  }

  public void handleLoginFailure(final String errorMessage) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread();
    org.nlogo.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        org.nlogo.swing.OptionDialog.show
            (ClientApplet.this, "Login Failed", errorMessage, new String[]{I18N.guiJ().get("common.buttons.ok")});
      }
    });
  }

  public void handleDisconnect(final String activityName, boolean connected, final String reason) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread();
    if (connected) {
      org.nlogo.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
          String[] ok = {I18N.guiJ().get("common.buttons.ok")};
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
  }

  // this is supplied only for debugging purposes

  private javax.swing.JFrame appFrame = null;

  public static void main(final String[] args) {
    System.setProperty("apple.awt.graphics.UseQuartz", "true");
    System.setProperty("apple.awt.showGrowBox", "true");
    VMCheck.detectBadJVMs();
    org.nlogo.awt.EventQueue.invokeLater
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

    public boolean isReporter(String s) {
      throw new UnsupportedOperationException();
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
