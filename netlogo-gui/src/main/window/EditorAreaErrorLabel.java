// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.CompilerException;

public strictfp class EditorAreaErrorLabel
    extends ErrorLabel {
  org.nlogo.editor.EditorArea<?> editorArea = null;

  public EditorAreaErrorLabel(org.nlogo.editor.EditorArea<?> editorArea) {
    super();
    this.editorArea = editorArea;
  }

  @Override
  public void setError(Exception compilerError, int offset) {
    super.setError(compilerError, offset);

    if (compilerError instanceof CompilerException) {
      CompilerException compilerEx = (CompilerException) compilerError;
      editorArea.select(compilerEx.start() - offset, compilerEx.end() - offset);
      editorArea.setSelection(false);
      editorArea.requestFocus();
    } else {
      editorArea.setSelection(true);
    }
  }
}
