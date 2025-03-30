// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Font, GridBagConstraints }
import javax.swing.ScrollPaneConstants

import org.nlogo.awt.Fonts.platformMonospacedFont
import org.nlogo.editor.{ Colorizer, EditorField }
import org.nlogo.swing.ScrollPane

abstract class ReporterLineEditor(accessor: PropertyAccessor[String],
                                  colorizer: Colorizer,
                                  optional: Boolean
                                  )
         extends CodeEditor(accessor, colorizer, false, false) {

  override lazy val editor = new EditorField(
    30, new Font(platformMonospacedFont, Font.PLAIN, 12), true, colorizer)
  override lazy val scrollPane = new ScrollPane(
    editor,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  override def get = {
    val trimmed = super.get.map(_.trim)
    if (optional) trimmed
    else trimmed.filter(_.nonEmpty)
  }
  override def getConstraints = {
    setMinimumSize(new Dimension(0, 35));
    val c = new GridBagConstraints
    c.fill = GridBagConstraints.HORIZONTAL;
    c
  }
}
