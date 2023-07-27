// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo
package properties

import org.nlogo.awt.Fonts.platformMonospacedFont
import java.awt.{Dimension, Font, GridBagConstraints}
import javax.swing.{JScrollPane, ScrollPaneConstants}
import editor.{EditorField, Colorizer}

abstract class ReporterLineEditor(accessor: PropertyAccessor[String],
                                  colorizer: Colorizer)
         extends CodeEditor(accessor, colorizer, false, false){

  override lazy val editor = new EditorField(
    30, new Font(platformMonospacedFont, Font.PLAIN, 12), true, colorizer)
  override lazy val scrollPane = new JScrollPane(
    editor,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  // get can't have .filter(_.nonEmpty). That would cause an error to be thrown when the metric condition reporter box is empty
  override def get = super.get.map(_.trim)
  override def getConstraints = {
    setMinimumSize(new Dimension(0, 35));
    val c = new GridBagConstraints
    c.fill = GridBagConstraints.HORIZONTAL;
    c
  }
}
