// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.util.List;
import java.util.StringTokenizer;

public strictfp class OutputArea
    extends javax.swing.JPanel {

  // when someone prints something that
  // ends in a carriage return, we don't want to print it immediately,
  // because this will make a blank line appear at the bottom of the
  // command center -- so instead we use this flag to delay adding
  // the carriage return until the *next* time something is output!
  private boolean addCarriageReturn = false;

  private String lastTemporaryAddition;

  String help = null;
  public final javax.swing.JTextArea text;
  private final javax.swing.JScrollPane scrollPane;

  public OutputArea() {
    this(new javax.swing.JTextArea() {
      @Override
      public java.awt.Dimension getMinimumSize() {
        return new java.awt.Dimension
            (50, (int) (getRowHeight() * 1.25));
      }
    });
  }

  public OutputArea(final java.awt.Component nextFocus) {
    this(new javax.swing.JTextArea() {
      @Override
      public java.awt.Dimension getMinimumSize() {
        return new java.awt.Dimension
            (50, (int) (getRowHeight() * 1.25));
      }

      @Override
      public void transferFocus() {
        nextFocus.requestFocus();
      }
    });
  }

  public OutputArea(javax.swing.JTextArea textArea) {
    this.text = textArea;

    text.setEditable(false);
    text.setDragEnabled(false);
    text.setFont(new java.awt.Font
        (org.nlogo.awt.Fonts.platformMonospacedFont(),
            java.awt.Font.PLAIN, 12));
    setLayout(new java.awt.BorderLayout());
    scrollPane = new javax.swing.JScrollPane
        (text,
            javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    add(scrollPane, java.awt.BorderLayout.CENTER);
  }

  public int fontSize() {
    return text.getFont().getSize();
  }

  public void fontSize(int fontSize) {
    text.setFont(new java.awt.Font
        (org.nlogo.awt.Fonts.platformMonospacedFont(),
            java.awt.Font.PLAIN, fontSize));
  }

  public void clear() {
    text.setText("");
    addCarriageReturn = false;
  }

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension
        (50, text.getMinimumSize().height);
  }

  @Override
  public java.awt.Dimension getPreferredSize() {
    return new java.awt.Dimension(200, 45);
  }

  @Override
  public boolean isFocusable() {
    return false;
  }

  public void append(org.nlogo.agent.OutputObject oo, boolean wrapLines) {
    String message = oo.get();
    if (lastTemporaryAddition != null) {
      String contents = text.getText();
      if (contents.length() >= lastTemporaryAddition.length()
          && contents.substring(contents.length() - lastTemporaryAddition.length()).equals(lastTemporaryAddition)) {
        text.replaceRange("", contents.length() - lastTemporaryAddition.length(), contents.length());
      }
      lastTemporaryAddition = null;
    }
    if (wrapLines) {
      java.awt.FontMetrics fontMetrics = getFontMetrics(text.getFont());
      List<String> messageLines =
          org.nlogo.awt.LineBreaker.breakLines
              (message, fontMetrics,
                  text.getWidth() - 24); // 24 = a guess at the scrollbar width
      StringBuilder wrappedMessage = new StringBuilder();
      for (int i = 0; i < messageLines.size(); i++) {
        wrappedMessage.append(messageLines.get(i));
        wrappedMessage.append("\n");
      }
      message = wrappedMessage.toString();
    }
    StringBuilder buf = new StringBuilder();
    if (addCarriageReturn) {
      buf.append('\n');
      addCarriageReturn = false;
    }
    buf.append(message);
    if (buf.length() > 0 && buf.charAt(buf.length() - 1) == '\n') {
      buf.setLength(buf.length() - 1);
      addCarriageReturn = true;
    }
    text.append(buf.toString());
    lastTemporaryAddition = null;
    if (oo.isTemporary()) {
      text.select(text.getText().length() - buf.length(), text.getText().length());
      lastTemporaryAddition = text.getSelectedText();
    }
    // doesn't always work unless we wait til later to do it - ST 8/18/03
    org.nlogo.awt.EventQueue.invokeLater
        (new Runnable() {
          public void run() {
            scrollPane.getVerticalScrollBar().setValue
                (scrollPane.getVerticalScrollBar().getMaximum());
            scrollPane.getHorizontalScrollBar().setValue(0);
          }
        });
  }

  public void export(String filename) {
    org.nlogo.api.File file =
        new org.nlogo.api.LocalFile(filename);
    try {
      file.open(org.nlogo.api.FileModeJ.WRITE());
      StringTokenizer lines =
          new StringTokenizer(text.getText(), "\n");
      while (lines.hasMoreTokens()) {
        // note that since we always use println, we always output a final carriage return
        // even if the TextArea doesn't have one; hmm, bug or feature? let's call it a feature
        file.println(lines.nextToken());
      }
      file.close(true);
    } catch (java.io.IOException ex) {
      try {
        file.close(false);
      } catch (java.io.IOException ex2) {
        org.nlogo.util.Exceptions.ignore(ex2);
      }
    }
  }

}
