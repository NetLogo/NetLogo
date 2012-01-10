// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo
package properties

import org.nlogo.awt.Fonts.platformMonospacedFont
import javax.swing.{JScrollPane, ScrollPaneConstants}
import editor.{EditorField, Colorizer}
import java.awt.{Dimension, Font}

abstract class ReporterLineEditor(accessor: PropertyAccessor[String],
                                  colorizer: Colorizer[_])
         extends CodeEditor(accessor, colorizer, false, false){
  
  override lazy val editor = new EditorField(
    30, new Font(platformMonospacedFont, Font.PLAIN, 12), false, colorizer,
    org.nlogo.api.I18N.gui.get _)
  override lazy val scrollPane = new JScrollPane(
    editor,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  override def get = super.get.map(_.trim).filter(_.nonEmpty)
  override def getMinimumSize = new Dimension(500, 500)
}
