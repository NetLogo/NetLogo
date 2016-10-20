// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ BorderLayout, Dimension, Graphics }
import java.awt.event.{ ActionEvent, TextEvent, TextListener }
import java.awt.print.PageFormat
import java.io.IOException
import java.net.MalformedURLException
import javax.swing.{ JButton, ImageIcon, AbstractAction, Action, BorderFactory, JPanel }

import org.nlogo.agent.Observer
import org.nlogo.app.common.{ CodeToHtml, EditorFactory, Events => AppEvents, FindDialog }
import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.editor.{ DumbIndenter, LineNumbersBar }
import org.nlogo.swing.{ Printable => NlogoPrintable, PrinterManager, ToolBar, ToolBarActionButton }
import org.nlogo.window.{ EditorAreaErrorLabel, Events => WindowEvents, ProceduresInterface, Zoomable }
import org.nlogo.workspace.AbstractWorkspace

class CodeTab(val workspace: AbstractWorkspace) extends JPanel
  with ProceduresInterface
  with ProceduresMenuTarget
  with AppEvents.SwitchedTabsEvent.Handler
  with WindowEvents.CompiledEvent.Handler
  with Zoomable
  with NlogoPrintable {

  private lazy val listener = new TextListener {
    override def textValueChanged(e: TextEvent) {
      needsCompile()
      dirty()
    }
  }

  lazy val editorFactory = new EditorFactory(workspace)

  def editorConfiguration =
    editorFactory.defaultConfiguration(100, 100)
      .withCurrentLineHighlighted(true)
      .withListener(listener)

  val text = {
    val editor = editorFactory.newEditor(editorConfiguration, true)
    editor.setBorder(BorderFactory.createEmptyBorder(4, 7, 4, 7))
    editor
  }

  override def zoomTarget = text

  val errorLabel = new EditorAreaErrorLabel(text)
  val toolBar = getToolBar
  val scrollableEditor = editorFactory.scrollPane(text)
  def compiler = workspace
  def program = workspace.world.program

  locally {
    setIndenter(false)
    setLayout(new BorderLayout)
    add(toolBar, BorderLayout.NORTH)
    val codePanel = new JPanel(new BorderLayout) {
      add(scrollableEditor, BorderLayout.CENTER)
      add(errorLabel, BorderLayout.NORTH)
    }
    add(codePanel, BorderLayout.CENTER)
  }

  val compileAction: Action = new CompileAction

  private class CompileAction extends AbstractAction(I18N.gui.get("tabs.code.checkButton")) {
    putValue(Action.SMALL_ICON,
      new ImageIcon(classOf[CodeTab].getResource(
        "/images/check.gif")))
    def actionPerformed(e: ActionEvent) {
      new WindowEvents.CompileAllEvent().raise(CodeTab.this)
    }
  }

  def getToolBar = new ToolBar {
    override def addControls() {
      add(new ToolBarActionButton(FindDialog.FIND_ACTION))
      add(new ToolBarActionButton(compileAction))
      add(new ToolBar.Separator)
      add(new ProceduresMenu(CodeTab.this))
    }
  }

  def dirty() { new WindowEvents.DirtyEvent().raise(this) }

  def menuActions =
    Seq(new CodeToHtml.Action(workspace, this, () => getText)) ++ editorConfiguration.menuActions

  private def needsCompile() {
    _needsCompile = true
    compileAction.setEnabled(true)
  }

  // don't let the editor influence the preferred size,
  // since the editor tends to want to be huge - ST
  override def getPreferredSize: Dimension = toolBar.getPreferredSize

  def getIncludesTable: Option[Map[String, String]] = {
    val path = Option(workspace.getModelPath).getOrElse{
      // we create an arbitrary model name for checking include paths when we don't have an actual
      // modelPath or directory
      try workspace.attachModelDir("foo.nlogo")
      catch {
        case ex: MalformedURLException =>
          // if we can't even figure out where we are, we certainly can't have includes
          return None
      }
    }
    workspace.compiler.findIncludes(path, getText, workspace.getCompilationEnvironment)
  }

  def agentClass = classOf[Observer]

  def kind = AgentKind.Observer

  protected var _needsCompile = false

  final def handle(e: AppEvents.SwitchedTabsEvent) {
    if(_needsCompile && e.oldTab == this)
      recompile()
  }

  private var originalFontSize = -1
  override def handle(e: WindowEvents.ZoomedEvent) {
    super.handle(e)
    if(originalFontSize == -1)
      originalFontSize = text.getFont.getSize
    text.setFont(text.getFont.deriveFont(StrictMath.ceil(originalFontSize * zoomFactor).toFloat))
    scrollableEditor.setFont(text.getFont)
    errorLabel.zoom(zoomFactor)
  }

  // Error code

  def handle(e: WindowEvents.CompiledEvent) {
    _needsCompile = false
    compileAction.setEnabled(e.error != null)
    if(e.sourceOwner == this) errorLabel.setError(e.error, headerSource.length)
    // this was needed to get extension colorization showing up reliably in the editor area - RG 23/3/16
    text.revalidate()
  }

  def recompile() { new WindowEvents.CompileAllEvent().raise(this) }

  override def requestFocus() { text.requestFocus() }

  def innerSource = text.getText
  def getText = text.getText  // for ProceduresMenuTarget
  def headerSource = ""
  def source = headerSource + innerSource

  override def innerSource_=(s: String): Unit = {
    text.setText(s)
    text.setCaretPosition(0)
  }

  def select(start: Int, end: Int) { text.select(start, end) }

  def classDisplayName = "Code"

  /// printing

  // satisfy org.nlogo.swing.Printable
  @throws(classOf[IOException])
  def print(g: Graphics, pageFormat: PageFormat,pageIndex: Int, printer: PrinterManager): Int =
    printer.printText(g, pageFormat, pageIndex, text.getText)

  def setIndenter(isSmart: Boolean) {
    if(isSmart) text.setIndenter(new SmartIndenter(new EditorAreaWrapper(text), workspace))
    else text.setIndenter(new DumbIndenter(text))
  }

  def lineNumbersVisible = scrollableEditor.lineNumbersEnabled
  def lineNumbersVisible_=(visible: Boolean) = scrollableEditor.setLineNumbersEnabled(visible)

  def isTextSelected(): Boolean = {
    text.getSelectedText() != null && !text.getSelectedText().isEmpty()
  }
}
