// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor;

public strictfp class HighlightView
    extends javax.swing.text.PlainView {

  private final javax.swing.JEditorPane pane;
  private final Colorizer colorizer;

  public HighlightView(javax.swing.JEditorPane pane,
                javax.swing.text.Element element, Colorizer colorizer) {
    super(element);
    this.pane = pane;
    this.colorizer = colorizer;
  }

  @Override
  protected void drawLine(int lineIndex, java.awt.Graphics g, int x, int y) {
    try {
      studyLine(lineIndex);
      super.drawLine(lineIndex, g, x, y);
    } catch (javax.swing.text.BadLocationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  ///

  // This could be more efficient if it cached already-studied lines if
  // they don't change - ST 8/27/03

  private int lineStart;
  private java.awt.Color charColors[];

  protected void studyLine(int lineIndex)
      throws javax.swing.text.BadLocationException {
    javax.swing.text.Element element = getElement().getElement(lineIndex);
    javax.swing.text.PlainDocument doc =
        (javax.swing.text.PlainDocument) getDocument();
    javax.swing.text.Segment text = getLineBuffer();
    lineStart = element.getStartOffset();
    int lineEnd = element.getEndOffset();
    // we got one error report where lineEnd - lineStart wasn't
    // positive.  don't know if it's our bug, but we might as well
    // guard against it - ST 2/27/12
    int length = lineEnd - lineStart;
    if(length < 0) {
      length = 0;
    }
    doc.getText(lineStart, length, text);
    charColors = colorizer.getCharacterColors(text.toString());
  }

  ///

  @Override
  protected int drawUnselectedText(java.awt.Graphics g,
                                   int x, int y,
                                   int p0, int p1)
      throws javax.swing.text.BadLocationException {
    return drawText(g, x, y, p0, p1, false);
  }

  @Override
  protected int drawSelectedText(java.awt.Graphics g,
                                 int x, int y,
                                 int p0, int p1)
      throws javax.swing.text.BadLocationException {
    return drawText(g, x, y, p0, p1, true);
  }

  private static final boolean LEAVE_COLORS_ALONE =
      System.getProperty("os.name").startsWith("Mac");

  protected int drawText(java.awt.Graphics g,
                       int x, int y,
                       int p0, int p1,
                       boolean isSelected)
      throws javax.swing.text.BadLocationException {
    javax.swing.text.PlainDocument doc =
        (javax.swing.text.PlainDocument) getDocument();
    javax.swing.text.Segment text = getLineBuffer();
    // this isn't very efficient, to draw a character at a time;
    // we should wait until the color changes - ST 8/27/03
    while (p0 < p1) {
      doc.getText(p0, 1, text);
      java.awt.Color color = charColors[p0 - lineStart];
      if (!pane.isEnabled()) {
        color = lightenColor(color, 0.5f);
      }
      g.setColor(isSelected && !LEAVE_COLORS_ALONE ?
          selectedColor(color) : color);
      x = javax.swing.text.Utilities.drawTabbedText
          (text, x, y, g, this, p0);
      p0++;
    }
    return x;
  }

  private static java.awt.Color retrieveDefaultUIColor(String propertyName, java.awt.Color fallBack) {
    java.awt.Color col = javax.swing.UIManager.getDefaults().getColor(propertyName);
    if (col == null) {
      return fallBack;
    }
    return col;
  }

  private static final java.awt.Color SELECTED_BACKGROUND_COLOR =
      retrieveDefaultUIColor("textHighlight", java.awt.Color.BLUE);
  private static final java.awt.Color UNSELECTED_TEXT_COLOR =
      retrieveDefaultUIColor("textText", java.awt.Color.BLACK);
  private static final java.awt.Color SELECTED_TEXT_COLOR =
      retrieveDefaultUIColor("textHighlightText", java.awt.Color.WHITE);

  private java.awt.Color selectedColor(java.awt.Color color) {
    if (color.getRGB() == UNSELECTED_TEXT_COLOR.getRGB()) {
      return SELECTED_TEXT_COLOR;
    }
    if (isDarkSelectionColor()) {
      return lightenColor(color, 0.75f);
    } else {
      return darkenColor(color, 0.75f);
    }
  }

  // return true if the selection color is dark (we arbitrarilly picked 375 to
  // be the too dark point)
  private boolean isDarkSelectionColor() {
    int red = SELECTED_BACKGROUND_COLOR.getRed();
    int green = SELECTED_BACKGROUND_COLOR.getGreen();
    int blue = SELECTED_BACKGROUND_COLOR.getBlue();
    return ((red + green + blue) < 375);
  }

  private java.awt.Color lightenColor(java.awt.Color color, float amount) {
    int red = color.getRed();
    int green = color.getGreen();
    int blue = color.getBlue();
    return new java.awt.Color
        ((int) (red + (255 - red) * amount),
            (int) (green + (255 - green) * amount),
            (int) (blue + (255 - blue) * amount));
  }

  private java.awt.Color darkenColor(java.awt.Color color, float amount) {
    int red = color.getRed();
    int green = color.getGreen();
    int blue = color.getBlue();
    return new java.awt.Color
        ((int) (red * amount),
            (int) (green * amount),
            (int) (blue * amount));
  }
}
