// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.framework.Figure;
import org.jhotdraw.standard.StandardDrawingView;

class AggregateDrawingView
    extends StandardDrawingView {
  AggregateDrawingView(AggregateModelEditor editor) {
    super(editor);
    setBackground(java.awt.Color.WHITE);
  }

  AggregateDrawingView(AggregateModelEditor editor, int width, int height) {
    super(editor, width, height);
    setBackground(java.awt.Color.WHITE);
  }
}
