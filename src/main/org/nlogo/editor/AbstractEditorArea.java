// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor;

public abstract class AbstractEditorArea
    extends javax.swing.JEditorPane {
  public abstract void enableBracketMatcher(boolean enable);
}
