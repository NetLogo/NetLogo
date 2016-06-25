// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.agent.Observer
import org.nlogo.editor.LineNumbersBar
import org.nlogo.window.EditorAreaErrorLabel
import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.swing.ToolBarActionButton
import java.awt.{BorderLayout, Dimension, Graphics}
import java.awt.event._
import java.awt.print.PageFormat
import javax.swing.{AbstractAction, Action, BorderFactory, ImageIcon, JButton, JPanel, JScrollPane, ScrollPaneConstants}

import org.nlogo.ide.ShowUsageBoxAction
import org.nlogo.ide.ShowUsageBox

class CodeTab(val workspace: AbstractWorkspace) extends JPanel
  with org.nlogo.window.ProceduresInterface
  with ProceduresMenuTarget
  with Events.SwitchedTabsEvent.Handler
  with org.nlogo.window.Events.CompiledEvent.Handler
  with org.nlogo.window.Zoomable
  with org.nlogo.swing.Printable {

  private val listener = new TextListener() {
    override def textValueChanged(e: TextEvent) {
      needsCompile()
      dirty()
    }
  }
  val showUsageBox = new ShowUsageBox()
  val showUsageBoxAction = new ShowUsageBoxAction(showUsageBox)
  val mL = new MouseListener {
    override def mouseExited(e: MouseEvent): Unit = {}
    override def mouseClicked(e: MouseEvent): Unit = {
      if(e.isControlDown){
        showUsageBoxAction.actionPerformed(e)
      }
    }

    override def mouseEntered(e: MouseEvent): Unit = ()

    override def mousePressed(e: MouseEvent): Unit = {}

    override def mouseReleased(e: MouseEvent): Unit = {}
  }

  val text = new EditorFactory(workspace).newEditor(100, 100, true, listener, true)
  text.addListener(mL)
  text.setBorder(BorderFactory.createEmptyBorder(4, 7, 4, 7))
  override def zoomTarget = text

  val errorLabel = new EditorAreaErrorLabel(text)
  val lineNumbers = new LineNumbersBar(text)
  val toolBar = getToolBar
  val scrollableEditor = new JScrollPane(
    text,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
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
      new org.nlogo.window.Events.CompileAllEvent().raise(CodeTab.this)
    }
  }

  def getToolBar = new org.nlogo.swing.ToolBar() {
    override def addControls() {
      add(new ToolBarActionButton(org.nlogo.app.FindDialog.FIND_ACTION))
      add(new ToolBarActionButton(compileAction))
      add(new org.nlogo.swing.ToolBar.Separator())
      add(new ProceduresMenu(CodeTab.this))
    }
  }

  def dirty() { new org.nlogo.window.Events.DirtyEvent().raise(this) }

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
        case ex: java.net.MalformedURLException =>
          // if we can't even figure out where we are, we certainly can't have includes
          return None
      }
    }
    workspace.compiler.findIncludes(path, getText, workspace.getCompilationEnvironment)
  }

  def agentClass = classOf[Observer]

  def kind = AgentKind.Observer

  protected var _needsCompile = false

  final def handle(e: Events.SwitchedTabsEvent) {
    if(_needsCompile && e.oldTab == this)
      recompile()
  }

  private var originalFontSize = -1
  override def handle(e: org.nlogo.window.Events.ZoomedEvent) {
    super.handle(e)
    if(originalFontSize == -1)
      originalFontSize = text.getFont.getSize
    text.setFont(text.getFont.deriveFont(StrictMath.ceil(originalFontSize * zoomFactor).toFloat))
    lineNumbers.setFont(text.getFont)
    errorLabel.zoom(zoomFactor)
  }

  // Error code

  def handle(e: org.nlogo.window.Events.CompiledEvent) {
    _needsCompile = false
    compileAction.setEnabled(e.error != null)
    if(e.sourceOwner == this) errorLabel.setError(e.error, headerSource.length)
    // this was needed to get extension colorization showing up reliably in the editor area - RG 23/3/16
    text.revalidate()
  }

  def recompile() { new org.nlogo.window.Events.CompileAllEvent().raise(this) }

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
  @throws(classOf[java.io.IOException])
  def print(g: Graphics, pageFormat: PageFormat,pageIndex: Int, printer: org.nlogo.swing.PrinterManager): Int =
    printer.printText(g, pageFormat, pageIndex, text.getText)

  def setIndenter(isSmart: Boolean) {
    if(isSmart) text.setIndenter(new SmartIndenter(new EditorAreaWrapper(text), workspace))
    else text.setIndenter(new org.nlogo.editor.DumbIndenter(text))
  }

  def lineNumbersVisible = scrollableEditor.getRowHeader != null && scrollableEditor.getRowHeader.getView != null
  def lineNumbersVisible_=(visible: Boolean) = scrollableEditor.setRowHeaderView(if(visible) lineNumbers else null)

  def isTextSelected(): Boolean = {
    text.getSelectedText() != null && !text.getSelectedText().isEmpty()
  }
}
