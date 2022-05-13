// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor;

public class HighlightEditorKit
    extends javax.swing.text.DefaultEditorKit
    implements javax.swing.text.ViewFactory {

  protected javax.swing.JEditorPane pane;
  protected final Colorizer colorizer;

  public HighlightEditorKit(Colorizer colorizer) {
    this.colorizer = colorizer;
  }

  @Override
  public void install(javax.swing.JEditorPane pane) {
    this.pane = pane;
  }

  @Override
  public javax.swing.text.ViewFactory getViewFactory() {
    return this;
  }

  public javax.swing.text.View create(javax.swing.text.Element elem) {
    return new HighlightView(pane, elem, colorizer);
  }

  @Override
  public javax.swing.text.Document createDefaultDocument() {
    return new javax.swing.text.PlainDocument();
  }
}
