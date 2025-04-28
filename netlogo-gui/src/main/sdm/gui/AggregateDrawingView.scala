// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.jhotdraw.standard.StandardDrawingView

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class AggregateDrawingView(editor: AggregateModelEditor, width: Int, height: Int)
  extends StandardDrawingView(editor, width, height) with ThemeSync {

  syncTheme()

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.interfaceBackground())
  }
}
