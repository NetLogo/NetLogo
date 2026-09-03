// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ BorderLayout, Dimension, Font, Graphics }
import java.awt.event.{ ActionEvent, FocusAdapter, FocusEvent, TextEvent, TextListener }
import java.awt.print.PageFormat
import java.io.IOException
import java.net.MalformedURLException
import javax.swing.{ AbstractAction, JPanel }

import org.nlogo.agent.Observer
import org.nlogo.app.common.{CodeToHtml, EditorFactory, FindDialog, MenuTab, TabsInterface, Events => AppEvents}
import org.nlogo.core.{ AgentKind, CompilerException, I18N }
import org.nlogo.editor.{ AdvancedEditorArea, EditorConfiguration }
import org.nlogo.nvm.IncludeSource
import org.nlogo.swing.{ BoxAlign, BoxRow, Button, CheckBox, PrinterManager, ToolBarActionButton, UserAction,
                         Printable => NlogoPrintable, Utils, ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ CommentableError, ProceduresInterface, Events => WindowEvents }
import org.nlogo.workspace.AbstractWorkspace

abstract class CodeTab(val workspace: AbstractWorkspace, tabs: TabsInterface)
  extends JPanel
  with ProceduresInterface
  with ProceduresMenuTarget
  with AppEvents.SwitchedTabsEvent.Handler
  with WindowEvents.CompiledEvent.Handler
  with WindowEvents.AfterLoadEvent.Handler
  with NlogoPrintable
  with MenuTab
  with ThemeSync {

  protected val compileButton = new ToolBarActionButton(new AbstractAction(I18N.gui.get("tabs.code.checkButton")) {
    override def actionPerformed(e: ActionEvent): Unit = {
      compile()
    }
  }) {
    setIcon(Utils.iconScaledWithColor("/images/check.png", 15, 15, () => InterfaceColors.toolbarImage()))
    setEnabled(false)
  }

  private val findButton = new ToolBarActionButton(FindDialog.FIND_ACTION_CODE)

  private val proceduresMenu = new ProceduresMenu(CodeTab.this)

  private val separate = new CheckBox(I18N.gui.get("tabs.code.separateCodeWindow"), (selected) => {
    tabs.switchWindow(selected, true)
  })

  private val prefsButton = new Button(I18N.gui.get("tabs.code.preferences"), () => {
    tabs.showCodeTabPreferences()
  })

  protected var _dirty = false // Has the buffer changed since it was compiled?
  def dirty = _dirty

  protected def dirty_=(b: Boolean) = {
    compileButton.setEnabled(b)
    _dirty = b
  }

  private lazy val listener = new TextListener {
    override def textValueChanged(e: TextEvent) = dirty = true
  }

  lazy val editorFactory = new EditorFactory(workspace, workspace.getExtensionManager)

  private val configuration: EditorConfiguration =
    editorFactory.defaultConfiguration(100, 80).withListener(listener).withSmartIndent(tabs.smartTabbingEnabled)

  protected def editorConfiguration: EditorConfiguration =
    configuration

  protected val text: AdvancedEditorArea = {
    val editor = new AdvancedEditorArea(editorConfiguration)

    editor.addFocusListener(new FocusAdapter {
      override def focusGained(e: FocusEvent): Unit = {
        FindDialog.watch(editor, true)
      }
    })

    editor
  }

  private val includedFilesMenu = new IncludedFilesMenu(getIncludesTable, tabs)

  val errorLabel = new CommentableError(text)

  val toolBar = new BoxRow(Seq(
    compileButton,
    findButton,
    proceduresMenu,
    includedFilesMenu,
    separate,
    prefsButton
  ), 10, BoxAlign.Start) {
    setOpaque(true)
    setBorder(new ZoomableBorder(24, 10, 12, 6))
  }

  def compiler = workspace
  def program = workspace.world.program

  locally {
    setLayout(new BorderLayout)
    add(toolBar, BorderLayout.NORTH)
    val codePanel = new JPanel(new BorderLayout) {
      add(text, BorderLayout.CENTER)
      add(errorLabel.component, BorderLayout.NORTH)
    }
    add(codePanel, BorderLayout.CENTER)
  }

  override val permanentMenuActions: Seq[UserAction.MenuAction] = {
    editorConfiguration.getAdditionalActions :+
      new CodeToHtml.Action(workspace, this, () => getText)
  }

  override def activeMenuActions: Seq[UserAction.MenuAction] =
    text.activeMenuActions :+ FindDialog.FIND_ACTION_CODE :+ FindDialog.FIND_NEXT_ACTION_CODE

  // don't let the editor influence the preferred size,
  // since the editor tends to want to be huge - ST
  override def getPreferredSize: Dimension = toolBar.getPreferredSize

  def getIncludesTable: Option[Map[String, IncludeSource]] = {
    Option(workspace.getModelPath).orElse {
      // we create an arbitrary model name for checking include paths when we don't have an actual
      // modelPath or directory
      try {
        Option(workspace.attachModelDir("foo.nlogox"))
      } catch {
        case ex: MalformedURLException =>
          // if we can't even figure out where we are, we certainly can't have includes
          None
      }
    }.flatMap { path =>
      try {
        tabs.mainCodeTab match {
          case tab: MainCodeTab =>
            workspace.compiler.findIncludes(path, tab.getText, workspace.getCompilationEnvironment)

          // this shouldn't be possible but it can't hurt to have a fallback (Isaac B 6/26/25)
          case _ =>
            workspace.compiler.findIncludes(path, getText, workspace.getCompilationEnvironment)
        }
      } catch {
        case e: CompilerException => None
      }
    }
  }

  def agentClass = classOf[Observer]

  def kind = AgentKind.Observer

  def handle(e: AppEvents.SwitchedTabsEvent): Unit = {
    if (e.oldTab == this && dirty)
      compile()
    if (!e.newTab.isInstanceOf[CodeTab])
      FindDialog.dontWatch(true)
  }

  def handle(e: WindowEvents.CompiledEvent) = {
    dirty = false
    if (e.sourceOwner == this) {
      errorLabel.setError(Option(e.error), headerSource.length)
      compileButton.setEnabled(true)
      setProgram()
    }
  }

  // this forces CodeMirror to redo all syntax highlighting with the newly compiled
  // program context (Isaac B 7/25/26)
  override def handle(e: WindowEvents.AfterLoadEvent): Unit = {
    text.refresh()
  }

  protected def setProgram(): Unit = {
    val procedures: Seq[String] = {
      workspace.procedures.keys.collect {
        case (proc, None) =>
          proc
      }.toSeq
    }

    text.setProgram(workspace.world.program, procedures, workspace.getExtensionManager.extensionCommandNames.toSeq,
                    workspace.getExtensionManager.extensionReporterNames.toSeq)
  }

  protected def compile(): Unit = new WindowEvents.CompileAllEvent().raise(this)

  override def requestFocus(): Unit = text.requestFocus()

  def innerSource = text.getText
  def getText = text.getText  // for ProceduresMenuTarget
  def headerSource = ""
  def source = headerSource + innerSource

  override def innerSource_=(s: String) = {
    text.setText(s)
  }

  def select(start: Int, end: Int) = text.select(start, end)

  def selectError(start: Int, end: Int) = text.selectError(start, end)

  def classDisplayName = "Code"

  @throws(classOf[IOException])
  def print(g: Graphics, pageFormat: PageFormat,pageIndex: Int, printer: PrinterManager) =
    printer.printText(g, pageFormat, pageIndex, text.getText)

  def setIndenter(isSmart: Boolean): Unit = {
    text.setIndenter(isSmart)
  }

  def setSeparate(selected: Boolean): Unit =
    separate.setSelected(selected)

  def lineNumbersVisible: Boolean =
    text.lineNumbersVisible

  def lineNumbersVisible_=(visible: Boolean) = {
    text.setLineNumbersVisible(visible)
  }

  def setCompleteOnType(enabled: Boolean): Unit = {
    text.setCompleteOnType(enabled)
  }

  def setIncludedFilesShown(visible: Boolean): Unit = {
    includedFilesMenu.setAlwaysVisible(visible)
  }

  def setCodeFont(font: Font): Unit = {
    text.setBaseFont(font)

    revalidate()
    repaint()
  }

  def isTextSelected: Boolean = text.getSelectedText != null && !text.getSelectedText.isEmpty

  def close(): Unit = {}

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.codeBackground())

    toolBar.setBackground(InterfaceColors.toolbarBackground())

    compileButton.syncTheme()
    findButton.syncTheme()

    proceduresMenu.syncTheme()
    includedFilesMenu.syncTheme()

    separate.setForeground(InterfaceColors.toolbarText())

    prefsButton.syncTheme()

    text.syncTheme()

    // for code completion popup
    editorFactory.syncTheme()
  }
}
