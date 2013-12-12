// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.widget.PaintableNote

class NotePanel(
  override val panelBounds: java.awt.Rectangle,
  override val originalFont: java.awt.Font,
  override val text: String,
  override val color: java.awt.Color)
  extends WidgetPanel with PaintableNote
