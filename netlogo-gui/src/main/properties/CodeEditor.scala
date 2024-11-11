// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties


import java.awt.{ BorderLayout, Component, Container }
import java.awt.event.{ TextListener, TextEvent, ActionEvent, ActionListener }
import javax.swing.{ JLabel, JPanel, JScrollPane, ScrollPaneConstants, SwingConstants }
import javax.swing.plaf.basic.BasicArrowButton

import org.nlogo.api.DummyEditable
import org.nlogo.awt.RowLayout
import org.nlogo.editor.{ Colorizer, EditorArea, EditorConfiguration }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.EditorAreaErrorLabel

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
  extends PropertyEditor(accessor) {

  val editorConfig =
    EditorConfiguration.default(rows, columns, colorizer)
      .withFocusTraversalEnabled(true)
      .withListener(new TextListener() {def textValueChanged(e: TextEvent) {changed()}})

  protected lazy val editor = new EditorArea(editorConfig)
  protected lazy val scrollPane = new JScrollPane(editor, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  private val errorLabel = new EditorAreaErrorLabel(editor)
  // the panel that should collapse
  private lazy val collapso = new JPanel(new BorderLayout()) {
    setOpaque(false)
    setBackground(InterfaceColors.TRANSPARENT)
    add(errorLabel, BorderLayout.NORTH)
    add(scrollPane, BorderLayout.CENTER)
    if (collapseWhenEmpty) setVisible(false)
  }
  private def collapsed = !collapso.isVisible()
  private def arrowDirection = if (collapsed) SwingConstants.EAST else SwingConstants.SOUTH
  private val arrow = new BasicArrowButton(arrowDirection) { self =>
    addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) { setVisibility(collapsed) }
    })
    def updateDirection() { self.setDirection(arrowDirection) }
  }

  private val nameLabel = new JLabel(accessor.displayName)

  setLayout(new BorderLayout)
  // add the panel containing the button that forces the collapse, and a label.
  add(new JPanel(rowLayout(2)) {
    setOpaque(false)
    setBackground(InterfaceColors.TRANSPARENT)
    if (collapsible) add(arrow)
    add(nameLabel)
  }, BorderLayout.NORTH)
  add(collapso, BorderLayout.CENTER)

  private def setVisibility(newVisibility: Boolean) {
    if (collapsible && collapseWhenEmpty) {
      collapso setVisible newVisibility
      if (newVisibility)
        add(collapso, BorderLayout.CENTER)
      else
        remove(collapso)
      arrow.updateDirection()
      org.nlogo.awt.Hierarchy.getWindow(this).pack()
      if (!collapsed) editor.requestFocus()
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
  private def rowLayout(rows:Int) = new RowLayout(rows, Component.LEFT_ALIGNMENT, Component.TOP_ALIGNMENT)
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = java.awt.GridBagConstraints.BOTH
    c.weightx = 1.0
    c.weighty = if (collapsible) 0.0 else 1.0
    c
  }
  override def setEnabled(state: Boolean): Unit = {
    def setEnabledRecursive(component: Container, state: Boolean): Unit = {
        component.getComponents().foreach(c => {
            c.setEnabled(state)
            if (c.isInstanceOf[Container]) {
              setEnabledRecursive(c.asInstanceOf[Container], state)
            }
        })
    }
    super.setEnabled(state)
    setEnabledRecursive(this, state)
  }

  def syncTheme() {
    editor.setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    editor.setCaretColor(InterfaceColors.TOOLBAR_TEXT)

    scrollPane.getHorizontalScrollBar.setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    scrollPane.getVerticalScrollBar.setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)

    nameLabel.setForeground(InterfaceColors.DIALOG_TEXT)
  }
}
