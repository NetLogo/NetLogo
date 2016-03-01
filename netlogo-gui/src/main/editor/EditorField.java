// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor;

import org.nlogo.core.TokenType;

public strictfp class EditorField
    extends EditorArea {

  public EditorField(int columns, java.awt.Font font,
                     boolean disableFocusTraversalKeys,
                     Colorizer colorizer,
                     scala.Function1<String, String> i18n) {
    super(1, columns, font, disableFocusTraversalKeys, null, colorizer, i18n);
    // shut off the default actions for some keystrokes... let
    // someone add a KeyListener if they want - ST 7/30/03
    getInputMap().remove
        (javax.swing.KeyStroke.getKeyStroke
            (java.awt.event.KeyEvent.VK_ENTER, 0));
  }

  @Override
  public void replaceSelection(String content) {
    // you wouldn't think content would ever be null, but on Mac OS X
    // we've found that typing the dead-key combinations for adding
    // accents to letters causes an exception here unless we do a
    // null check (bug #1079) - ST 10/12/10
    if (content != null) {
      int index = content.indexOf('\n');
      if (index != -1) {
        content = content.substring(0, index);
      }
      index = content.indexOf('\r');
      if (index != -1) {
        content = content.substring(0, index);
      }
    }
    super.replaceSelection(content);
  }

}
