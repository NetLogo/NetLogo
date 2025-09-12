// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Font
import javax.swing.ScrollPaneConstants
import javax.swing.event.{ DocumentEvent, DocumentListener}

import org.nlogo.api.CompilerServices
import org.nlogo.awt.Fonts.platformMonospacedFont
import org.nlogo.editor.{ Colorizer, EditorField }
import org.nlogo.swing.ScrollPane

class ReporterLineEditor(accessor: PropertyAccessor[String], compiler: CompilerServices, colorizer: Colorizer,
                         optional: Boolean)
  extends CodeEditor(accessor, compiler, colorizer, false, false) {

  override lazy val editor = new EditorField(30, new Font(platformMonospacedFont, Font.PLAIN, 12), true, compiler,
                                             colorizer) {
    getDocument.addDocumentListener(new DocumentListener {
      def insertUpdate(e: DocumentEvent): Unit = { accessor.changed() }
      def removeUpdate(e: DocumentEvent): Unit = { accessor.changed() }
      def changedUpdate(e: DocumentEvent): Unit = {accessor.changed() }
    })
  }

  override lazy val scrollPane = new ScrollPane(
    editor,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

  override def get: Option[String] = {
    val trimmed = super.get.map(_.trim)
    if (optional) {
      trimmed
    } else {
      trimmed.filter(_.nonEmpty)
    }
  }
}
