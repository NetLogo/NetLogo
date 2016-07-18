// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.editor.{ Colorizer, EditorArea }
import javax.swing.plaf.basic.BasicArrowButton

import org.nlogo.awt.RowLayout
import org.nlogo.window.EditorAreaErrorLabel

import javax.swing.ScrollPaneConstants.{HORIZONTAL_SCROLLBAR_AS_NEEDED, VERTICAL_SCROLLBAR_ALWAYS}
import java.awt.{Font, BorderLayout}
import java.awt.Component.{LEFT_ALIGNMENT, TOP_ALIGNMENT}
import java.awt.event.{TextListener, TextEvent, ActionEvent, ActionListener}
import org.nlogo.awt.Fonts.platformMonospacedFont
import javax.swing.{SwingConstants, JLabel, JPanel, JScrollPane}
import org.nlogo.api.DummyEditable

import scala.language.reflectiveCalls

object CodeEditor {
  def apply(displayName: String, colorizer: Colorizer,
            collapsible: Boolean = false,
            collapseWhenEmpty: Boolean = false,
            rows: Int = 5, columns: Int = 30,
            err: Option[Exception] = None, changedFunc: => Unit = {}): CodeEditor = {

    class Dummy extends DummyEditable {var dummy = ""}

    val accessor = new PropertyAccessor[String](new Dummy, displayName, "dummy"){
      override def error = err
    }
    new CodeEditor(accessor, colorizer, rows=rows, columns=columns,
      collapsible=collapsible, collapseWhenEmpty=collapseWhenEmpty){
      def changed{ changedFunc }
    }
  }
}

abstract class CodeEditor(accessor: PropertyAccessor[String],
                              colorizer: Colorizer,
                              collapsible: Boolean = false,
                              collapseWhenEmpty: Boolean = false,
                              rows: Int = 5, columns: Int = 30)
  extends PropertyEditor(accessor){

  lazy val editor = new EditorArea(rows, columns,
    new Font(platformMonospacedFont, Font.PLAIN, 12), true,
    new TextListener() {def textValueChanged(e: TextEvent) {changed()}}, colorizer,
    org.nlogo.core.I18N.gui.get _)
  lazy val scrollPane = new JScrollPane(editor, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED)
  private val errorLabel = new EditorAreaErrorLabel(editor)
  // the panel that should collapse
  private lazy val collapso = new JPanel(new BorderLayout()) {
    add(errorLabel, BorderLayout.NORTH)
    add(scrollPane, BorderLayout.CENTER)
    if (collapseWhenEmpty) setVisible(false)
  }
  private def collapsed = !collapso.isVisible()
  private def arrowDirection = if(collapsed) SwingConstants.EAST else SwingConstants.SOUTH
  private val arrow = new BasicArrowButton(arrowDirection) { self =>
    addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) { setVisibility(collapsed) }
    })
    def updateDirection() { self.setDirection(arrowDirection) }
  }

  locally{
    setLayout(new BorderLayout())
    // add the panel containing the button that forces the collapse, and a label.
    add(new JPanel(rowLayout(2)) {
      if (collapsible) add(arrow)
      add(new JLabel(accessor.displayName))
    }, BorderLayout.NORTH)
    add(collapso, BorderLayout.CENTER)
  }

  private def setVisibility(newVisibility: Boolean) {
    if (collapsible && collapseWhenEmpty) {
      collapso setVisible newVisibility
      if(newVisibility)
        add(collapso, BorderLayout.CENTER)
      else
        remove(collapso)
      arrow.updateDirection()
      org.nlogo.awt.Hierarchy.getWindow(this).pack()
      if(!collapsed) editor.requestFocus()
    }
  }
  override def get = Option(editor.getText)
  override def set(value: String) {
    editor setText value
    setVisibility(value.nonEmpty)
    editor.select(0, 0)
    accessor.error.foreach{ errorLabel.setError(_, accessor.target.sourceOffset) }
  }
  override def requestFocus() { editor.requestFocus() }
  private def rowLayout(rows:Int) = new RowLayout(rows, LEFT_ALIGNMENT, TOP_ALIGNMENT)
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = java.awt.GridBagConstraints.BOTH
    c.weightx = 1.0
    c.weighty = if (collapsible) 0.0 else 1.0
    c
  }
}
