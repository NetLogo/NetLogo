// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

// thank you thank you thank you to
//   http://cvs.sourceforge.net/cgi-bin/viewcvs.cgi/uic/uicompiler/uic/widgets/UICTextField.java

public strictfp class TextField
    extends javax.swing.JTextField {

  private boolean mouseEvent = false;

  public TextField(int columns) {
    super(columns);
    setDragEnabled(false);
    initListener();
  }

  public TextField(String text, int columns) {
    super(text, columns);
    setDragEnabled(false);
    initListener();
  }

  public TextField(javax.swing.text.Document doc, String text, int columns) {
    super(doc, text, columns);
    setDragEnabled(false);
    initListener();
  }

  protected void initListener() {
    enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
    addFocusListener
        (new java.awt.event.FocusListener() {
          public void focusGained(java.awt.event.FocusEvent fe) {
            if (!mouseEvent) {
              // this is like selectAll(), but it leaves the
              // caret at the beginning rather than the start;
              // this prevents the enclosing scrollpane from
              // scrolling to the end to make the caret
              // visible; it's nicer to keep the scroll at the
              // start - ST 12/20/04
              setCaretPosition(getText().length());
              moveCaretPosition(0);
            }
          }

          public void focusLost(java.awt.event.FocusEvent fe) {
            mouseEvent = fe.isTemporary();
          }
        });
  }

  @Override
  public void processMouseEvent(java.awt.event.MouseEvent me) {
    if (me.getID() == java.awt.event.MouseEvent.MOUSE_PRESSED) {
      mouseEvent = true;
    }
    super.processMouseEvent(me);
  }

}
