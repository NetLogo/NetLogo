// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Component, Container }
import java.awt.event.{ MouseAdapter, MouseEvent, TextListener, TextEvent }
import javax.swing.{ JLabel, JPanel, ScrollPaneConstants }

import org.nlogo.awt.{ Hierarchy, RowLayout }
import org.nlogo.editor.{ Colorizer, EditorArea, EditorConfiguration }
import org.nlogo.swing.{ CollapsibleArrow, ScrollPane, Transparent }
import org.nlogo.theme.InterfaceColors

object CodeEditor {
  def apply(displayName: String, colorizer: Colorizer, collapsible: Boolean = false,
            collapseWhenEmpty: Boolean = false, rows: Int = 5, columns: Int = 30, err: Option[Exception] = None,
            changedFunc: => Unit = {}): CodeEditor = {

    val accessor = new PropertyAccessor[String](new DummyEditable, displayName, () => "", _ => {}, () => changedFunc)

    new CodeEditor(accessor, colorizer, collapsible, collapseWhenEmpty, rows, columns)
  }
}

class CodeEditor(accessor: PropertyAccessor[String], colorizer: Colorizer, collapsible: Boolean = false,
                 collapseWhenEmpty: Boolean = false, rows: Int = 5, columns: Int = 30)
  extends PropertyEditor(accessor) {

  val editorConfig =
    EditorConfiguration.default(rows, columns, colorizer)
      .withFocusTraversalEnabled(true)
      .withListener(new TextListener { def textValueChanged(e: TextEvent) { accessor.changed() } })

  protected lazy val editor = new EditorArea(editorConfig)
  protected lazy val scrollPane = new ScrollPane(editor, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  private val errorLabel = new EditorAreaErrorLabel(editor)

  // the panel that should collapse
  private val collapso = new JPanel(new BorderLayout) with Transparent {
    add(errorLabel, BorderLayout.NORTH)
    add(scrollPane, BorderLayout.CENTER)
    if (collapseWhenEmpty) setVisible(false)
  }

  private val arrow = new CollapsibleArrow(!collapsed)

  private val nameLabel = new JLabel(accessor.name) {
    addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent): Unit = {
        setVisibility(collapsed)
      }
    })
  }

  setLayout(new BorderLayout(0, 3))

  add(new JPanel(new RowLayout(2, Component.LEFT_ALIGNMENT, Component.CENTER_ALIGNMENT)) with Transparent {
    if (collapsible) {
      add(new JLabel(arrow) {
        addMouseListener(new MouseAdapter {
          override def mouseClicked(e: MouseEvent): Unit = {
            setVisibility(collapsed)
          }
        })
      })
    }

    add(nameLabel)
  }, BorderLayout.NORTH)

  add(collapso, BorderLayout.CENTER)

  private def collapsed: Boolean = !collapso.isVisible()

  private def setVisibility(newVisibility: Boolean): Unit = {
    if (collapsible && collapseWhenEmpty) {
      collapso setVisible newVisibility

      if (newVisibility) {
        add(collapso, BorderLayout.CENTER)
      } else {
        remove(collapso)
      }

      arrow.setOpen(!collapsed)

      Hierarchy.getWindow(this).pack()

      if (!collapsed) editor.requestFocus()
    }
  }

  override def get: Option[String] = Option(editor.getText)
  override def set(value: String): Unit = {
    editor setText value
    setVisibility(value.nonEmpty)
    editor.select(0, 0)
    accessor.target.error(accessor.name).foreach(errorLabel.setError(_, accessor.target.sourceOffset))
  }

  override def setToolTipText(text: String): Unit = {
    nameLabel.setToolTipText(text)
  }

  override def requestFocus(): Unit = { editor.requestFocus() }
  override def setEnabled(state: Boolean): Unit = {
    def setEnabledRecursive(component: Container, state: Boolean): Unit = {
      component.getComponents().foreach(c => {
          c.setEnabled(state)

          c match {
            case con: Container => setEnabledRecursive(con, state)
          }
      })
    }

    super.setEnabled(state)
    setEnabledRecursive(this, state)
  }

  override def syncTheme(): Unit = {
    editor.setBackground(InterfaceColors.textAreaBackground())
    editor.setCaretColor(InterfaceColors.textAreaText())

    scrollPane.setBackground(InterfaceColors.textAreaBackground())

    nameLabel.setForeground(InterfaceColors.dialogText())
  }
}

class NonEmptyCodeEditor(accessor: PropertyAccessor[String], colorizer: Colorizer)
  extends CodeEditor(accessor, colorizer) {

  override def get: Option[String] =
    super.get.map(_.trim).filter(_.nonEmpty)
}
