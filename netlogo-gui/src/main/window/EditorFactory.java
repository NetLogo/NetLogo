// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public interface EditorFactory {
  org.nlogo.editor.AbstractEditorArea newEditor(int cols, int rows, boolean enableFocusTraversal);
}
