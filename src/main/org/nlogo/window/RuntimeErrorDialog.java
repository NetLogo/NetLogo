// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.LogoException;
import org.nlogo.workspace.AbstractWorkspace;

import java.util.ArrayList;
import java.util.List;

public strictfp class RuntimeErrorDialog
    extends org.nlogo.swing.MessageDialog {

  private String title;
  private boolean ordinaryError;
  private boolean askForBugReport;
  private String errorMessage;
  private String javaStackTrace;
  private String eventTrace;
  private final String className;
  private String threadName;

  private String textWithoutDetails;
  private String textWithDetails;

  private boolean suppressJavaExceptionDialogs = false;
  private String modelName;

  private static final javax.swing.JButton SUPPRESS_BUTTON =
      new javax.swing.JButton("Don't show again");
  private static final javax.swing.JCheckBox CHECKBOX =
      new javax.swing.JCheckBox("Show internal details");

  public static org.nlogo.swing.MessageDialog init(java.awt.Component owner) {
    dialog = new RuntimeErrorDialog(owner);
    return dialog;
  }

  public static void deactivate() {
    dialog = null;
  }

  public static boolean alreadyVisible() {
    return dialog != null && dialog.isVisible();
  }

  public static void show(String title,
                          org.nlogo.nvm.Context context,
                          org.nlogo.nvm.Instruction instruction,
                          Thread thread,
                          Throwable throwable) {
    if (dialog != null) {
      ((RuntimeErrorDialog) dialog)
          .doShow(title, context, instruction, thread, throwable);
    }
  }

  public static void setModelName(String name) {
    ((RuntimeErrorDialog) dialog).modelName = name;
  }

  public static boolean suppressJavaExceptionDialogs() {
    return ((RuntimeErrorDialog) dialog).suppressJavaExceptionDialogs;
  }

  ///

  private void doShow(String title,
                      org.nlogo.nvm.Context context,
                      org.nlogo.nvm.Instruction instruction,
                      Thread thread,
                      Throwable throwable) {
    this.title = title;
    threadName = thread.getName();
    ordinaryError = throwable instanceof LogoException;
    // we don't need bug reports on known issues like OutOfMemoryError - ST 4/29/10
    askForBugReport = !(ordinaryError || hasKnownAncestorCause(throwable));
    // context is only non-null if the exception actually occured inside
    // running Logo code (and not, for example, in the GUI)
    SUPPRESS_BUTTON.setVisible(!ordinaryError && context == null);
    javaStackTrace = org.nlogo.util.Utils.getStackTrace(throwable);
    eventTrace = org.nlogo.window.Event.recentEventTrace();
    if (context != null) {
      errorMessage = context.buildRuntimeErrorMessage(instruction, throwable);
    } else if (ordinaryError) {
      errorMessage = throwable.getMessage();
    } else {
      errorMessage = null;
    }
    buildTexts();
    // don't show spurious dialogs caused by
    // http://bugs.sun.com/view_bug.do?bug_id=6828938
    // http://bugs.sun.com/view_bug.do?bug_id=6899297
    // http://bugs.sun.com/view_bug.do?bug_id=6857057
    // the underlying issue was fixed as of Java 6 Update 20, but we might
    // as well still be careful
    // - ST 9/12/09
    if (textWithDetails.indexOf("sun.font.FontDesignMetrics.charsWidth") == -1) {
      showJavaDetails(CHECKBOX.isSelected());
    }
  }

  private boolean hasKnownAncestorCause(Throwable t) {
    return t instanceof java.lang.OutOfMemoryError ||
        (t.getCause() != null && hasKnownAncestorCause(t.getCause()));
  }

  protected RuntimeErrorDialog(java.awt.Component owner) {
    super(owner);
    className = owner.getClass().getName();
  }

  @Override
  protected List<javax.swing.JComponent> makeButtons() {
    List<javax.swing.JComponent> buttons =
        new ArrayList<javax.swing.JComponent>();
    buttons.addAll(super.makeButtons());
    if (!AbstractWorkspace.isApplet()) {
      javax.swing.JButton copyButton = new javax.swing.JButton("Copy");
      copyButton.addActionListener
          (new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
              textArea.selectAll();
              textArea.copy();
              textArea.setCaretPosition(0);
            }
          });
      buttons.add(copyButton);
    }
    CHECKBOX.addItemListener
        (new java.awt.event.ItemListener() {
          public void itemStateChanged(java.awt.event.ItemEvent e) {
            showJavaDetails(CHECKBOX.isSelected());
          }
        });
    buttons.add(CHECKBOX);
    SUPPRESS_BUTTON.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            suppressJavaExceptionDialogs = true;
            setVisible(false);
          }
        });
    buttons.add(SUPPRESS_BUTTON);
    return buttons;
  }

  private void buildTexts() {
    String extra = "";
    if (askForBugReport) {
      extra = "NetLogo is unable to supply you with more details about this error.  Please report the problem\n"
          + "at https://github.com/NetLogo/NetLogo/issues, or to bugs@ccl.northwestern.edu, and paste the\n"
          + "contents of this window into your report.\n\n";
    }
    textWithoutDetails = "";
    if (errorMessage != null) {
      textWithoutDetails = errorMessage;
    }
    textWithDetails = textWithoutDetails;
    if (!textWithDetails.equals("")) {
      textWithDetails += "\n\n";
    }
    textWithDetails +=
        extra + javaStackTrace + "\n" +
            org.nlogo.api.Version.version() + "\n" +
            "main: " + className + "\n" +
            "thread: " + threadName + "\n" +
            org.nlogo.util.SysInfo.getVMInfoString() + "\n" +
            org.nlogo.util.SysInfo.getOSInfoString() + "\n" +
            org.nlogo.util.SysInfo.getScalaVersionString() + "\n" +
            org.nlogo.util.SysInfo.getJOGLInfoString() + "\n" +
            org.nlogo.util.SysInfo.getGLInfoString() + "\n" +
            "model: " + modelName + "\n\n" +
            eventTrace;
    if (!AbstractWorkspace.isApp()) {
      String browser = org.nlogo.util.SysInfo.getBrowserInfoString();
      textWithDetails += "\n";
      textWithDetails +=
          (browser == null)
              ? "running as applet in unknown browser"
              : ("browser info: " + browser);
    }
  }

  private void showJavaDetails(boolean flag) {
    if (ordinaryError) {
      CHECKBOX.setVisible(true);
    } else {
      CHECKBOX.setVisible(false);
      flag = true;
    }
    int lines = 1;
    int lineBegin = 0;
    int longestLine = 0;
    String text =
        flag
            ? textWithDetails
            : textWithoutDetails;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '\n' || c == '\r') {
        lines++;
        if (i - lineBegin > longestLine) {
          longestLine = i - lineBegin;
        }
        lineBegin = i;
      }
    }
    longestLine += 2; // just in case
    if (lines < 5) {
      lines = 5;
    }
    if (lines > 15) {
      lines = 15;
    }
    if (longestLine > 70) {
      longestLine = 70;
    }
    ((RuntimeErrorDialog) dialog)
        .doShow(title, text, lines, longestLine);
  }

}
