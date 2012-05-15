// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor;

public strictfp class DumbIndenter
    implements IndenterInterface {

  protected final EditorArea<?> code;

  public DumbIndenter(EditorArea<?> code) {
    this.code = code;
  }

  public void handleTab() {
    code.replaceSelection("  ");
  }

  public void handleCloseBracket() {
  }

  public void handleInsertion(String text) {
  }

  public void handleEnter() {
    javax.swing.text.PlainDocument doc = (javax.swing.text.PlainDocument) code.getDocument();
    int currentLine = code.offsetToLine(doc, code.getSelectionStart());
    int lineStart = code.lineToStartOffset(doc, currentLine);
    int lineEnd = code.lineToEndOffset(doc, currentLine);
    String text = code.getText(lineStart, lineEnd - lineStart);

    StringBuilder spaces = new StringBuilder("\n");
    for (int i = 0; i < text.length()
        && lineStart + i < code.getSelectionStart(); i++) {
      char c = text.charAt(i);
      if (!Character.isWhitespace(c)) {
        break;
      }
      spaces.append(c);
    }
    code.replaceSelection(spaces.toString());
  }

}
