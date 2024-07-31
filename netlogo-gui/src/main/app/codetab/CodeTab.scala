// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.event.{ ActionEvent, FocusEvent, FocusListener, TextEvent, TextListener }
import java.awt.print.PageFormat
import java.awt.{BorderLayout, Component, Dimension, Graphics, Insets}
import java.io.IOException
import java.net.MalformedURLException
import java.util.prefs.Preferences
import javax.swing.{ AbstractAction, Action, JCheckBox, JComponent, JPanel }

import org.nlogo.agent.Observer
import org.nlogo.app.common.{CodeToHtml, EditorFactory, FindDialog, MenuTab, TabsInterface, Events => AppEvents}
import org.nlogo.core.{ AgentKind, CompilerException, I18N }
import org.nlogo.editor.DumbIndenter
import org.nlogo.ide.FocusedOnlyAction
import org.nlogo.swing.Utils.icon
import org.nlogo.swing.{PrinterManager, ToolBar, ToolBarActionButton, UserAction, WrappedAction, Printable => NlogoPrintable}
import org.nlogo.window.{CommentableError, ProceduresInterface, Zoomable, Events => WindowEvents}
import org.nlogo.workspace.AbstractWorkspace

abstract class CodeTab(val workspace: AbstractWorkspace, tabs: TabsInterface) extends JPanel
with ProceduresInterface
with ProceduresMenuTarget
with AppEvents.SwitchedTabsEvent.Handler
with WindowEvents.CompiledEvent.Handler
with Zoomable
with NlogoPrintable
with MenuTab {
  protected val prefs = Preferences.userRoot.node("/org/nlogo/NetLogo")

  private val tabbing: JCheckBox = new JCheckBox(
    new AbstractAction(I18N.gui.get("tabs.code.indentAutomatically")) {
      def actionPerformed(e: ActionEvent) {
        tabs.smartTabbingEnabled = tabbing.isSelected
      }
    })
  
  private val separate: JCheckBox = new JCheckBox(
    new AbstractAction(I18N.gui.get("tabs.code.separateCodeWindow")) {
      def actionPerformed(e: ActionEvent) {
        tabs.switchWindow(separate.isSelected, true)
      }
    }
  )

  private var _dirty = false // Has the buffer changed since it was compiled?
  def dirty = _dirty

  protected def dirty_=(b: Boolean) = {
    CompileAction.setDirty(b)
    _dirty = b
  }

  private lazy val listener = new TextListener {
    override def textValueChanged(e: TextEvent) = dirty = true
  }

  lazy val editorFactory = new EditorFactory(workspace, workspace.getExtensionManager)

  def editorConfiguration =
    editorFactory.defaultConfiguration(100, 80)
      .withCurrentLineHighlighted(true)
      .withListener(listener)

  val text = {
    val editor = editorFactory.newEditor(editorConfiguration, true)
    editor.setMargin(new Insets(4, 7, 4, 7))

    editor.addFocusListener(new FocusListener {
      def focusGained(fe: FocusEvent) { FindDialog.watch(editor, true) }
      def focusLost(fe: FocusEvent) {}
    })

    editor
  }

  lazy val undoAction: Action = {
    new WrappedAction(text.undoAction,
      UserAction.EditCategory,
      UserAction.EditUndoGroup,
      UserAction.KeyBindings.keystroke('Z', withMenu = true))
  }

  lazy val redoAction: Action = {
    new WrappedAction(text.redoAction,
      UserAction.EditCategory,
      UserAction.EditUndoGroup,
      UserAction.KeyBindings.keystroke('Y', withMenu = true))
  }

  override def zoomTarget = text

  val errorLabel = new CommentableError(text)
  val toolBar = getToolBar
  val scrollableEditor = editorFactory.scrollPane(text)
  def compiler = workspace
  def program = workspace.world.program

  locally {
    setLayout(new BorderLayout)
    add(toolBar, BorderLayout.NORTH)
    val codePanel = new JPanel(new BorderLayout) {
      add(scrollableEditor, BorderLayout.CENTER)
      add(errorLabel.component, BorderLayout.NORTH)
    }
    add(codePanel, BorderLayout.CENTER)
  }

  def getToolBar = new ToolBar {
    override def addControls() {
      // Only want to add toolbar items once
      // This method gets called when the code tab pops in or pops out
      // because org.nlogo.swing.ToolBar overrides addNotify. AAB 10/2020
      if (getActionMap.get("procmenu") == null) {
        val proceduresMenu = new ProceduresMenu(CodeTab.this)
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(UserAction.KeyBindings.keystroke('G', withMenu = true),
                                                           "procmenu")
        getActionMap.put("procmenu", proceduresMenu.getAction)

        add(new ToolBarActionButton(FindDialog.FIND_ACTION_CODE))

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(UserAction.KeyBindings.keystroke('F', withMenu = true), "find")
        getActionMap.put("find", FindDialog.FIND_ACTION_CODE)

        add(new ToolBarActionButton(CompileAction))
        add(new ToolBar.Separator)
        add(proceduresMenu)
        add(new IncludedFilesMenu(getIncludesTable, tabs))
        val additionalComps = Seq(tabbing, separate) ++ getAdditionalToolBarComponents
        if (additionalComps.nonEmpty) {
          add(new ToolBar.Separator)
          additionalComps foreach add
        }
      }
    }
  }

  protected def getAdditionalToolBarComponents: Seq[Component] = Seq.empty[Component]

  override val permanentMenuActions =
    Seq(new CodeToHtml.Action(workspace, this, () => getText)) ++ editorConfiguration.permanentActions

  override val activeMenuActions =
    editorConfiguration.contextActions.filter(_.isInstanceOf[FocusedOnlyAction]) ++ Seq(undoAction, redoAction)

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
    try {
      workspace.compiler.findIncludes(path, getText, workspace.getCompilationEnvironment)
    } catch {
      case e: CompilerException => None
    }
  }

  def agentClass = classOf[Observer]

  def kind = AgentKind.Observer

  def handle(e: AppEvents.SwitchedTabsEvent) {
    if (e.oldTab == this && dirty)
      compile()
    if (!e.newTab.isInstanceOf[CodeTab])
      FindDialog.dontWatch(text, true)
  }

  private var originalFontSize = -1
  override def handle(e: WindowEvents.ZoomedEvent) {
    super.handle(e)
    if (originalFontSize == -1)
      originalFontSize = text.getFont.getSize
    text.setFont(text.getFont.deriveFont(StrictMath.ceil(originalFontSize * zoomFactor).toFloat))
    scrollableEditor.setFont(text.getFont)
    errorLabel.zoom(zoomFactor)
  }

  def handle(e: WindowEvents.CompiledEvent) = {
    dirty = false
    if (e.sourceOwner == this) errorLabel.setError(e.error, headerSource.length)
    // this was needed to get extension colorization showing up reliably in the editor area - RG 23/3/16
    text.revalidate()
  }

  protected def compile(): Unit = new WindowEvents.CompileAllEvent().raise(this)

  override def requestFocus(): Unit = text.requestFocus()

  def innerSource = text.getText
  def getText = text.getText  // for ProceduresMenuTarget
  def headerSource = ""
  def source = headerSource + innerSource

  override def innerSource_=(s: String) = {
    text.setText(s)
    text.setCaretPosition(0)
    text.resetUndoHistory()
  }

  def select(start: Int, end: Int) = text.select(start, end)

  def classDisplayName = "Code"

  @throws(classOf[IOException])
  def print(g: Graphics, pageFormat: PageFormat,pageIndex: Int, printer: PrinterManager) =
    printer.printText(g, pageFormat, pageIndex, text.getText)

  def setIndenter(isSmart: Boolean): Unit = {
    if(isSmart) text.setIndenter(new SmartIndenter(new EditorAreaWrapper(text), workspace))
    else text.setIndenter(new DumbIndenter(text))
    tabbing.setSelected(isSmart)
  }

  def setSeparate(selected: Boolean): Unit =
    separate.setSelected(selected)

  def lineNumbersVisible = scrollableEditor.lineNumbersEnabled
  def lineNumbersVisible_=(visible: Boolean) = scrollableEditor.setLineNumbersEnabled(visible)

  def isTextSelected: Boolean = text.getSelectedText != null && !text.getSelectedText.isEmpty

  private object CompileAction extends AbstractAction(I18N.gui.get("tabs.code.checkButton")) {
    putValue(Action.SMALL_ICON, icon("/images/check-gray.gif"))
    def actionPerformed(e: ActionEvent) = compile()
    def setDirty(isDirty: Boolean) = {
      val iconPath =
        if (isDirty) "/images/check.gif"
        else         "/images/check-gray.gif"
      putValue(Action.SMALL_ICON, icon(iconPath))
    }
  }
}
