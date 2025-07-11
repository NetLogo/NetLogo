// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ BorderLayout, Color, Dimension, Font, Graphics, Insets }
import java.awt.event.{ ActionEvent, FocusEvent, FocusListener, TextEvent, TextListener }
import java.awt.print.PageFormat
import java.io.IOException
import java.net.MalformedURLException
import javax.swing.{ AbstractAction, JComponent, JPanel }
import javax.swing.border.EmptyBorder

import org.fife.ui.rsyntaxtextarea.{ Style, SyntaxScheme, TokenTypes }

import org.nlogo.agent.Observer
import org.nlogo.app.common.{CodeToHtml, EditorFactory, FindDialog, MenuTab, TabsInterface, Events => AppEvents}
import org.nlogo.core.{ AgentKind, CompilerException, I18N }
import org.nlogo.editor.{ AdvancedEditorArea, DumbIndenter }
import org.nlogo.ide.FocusedOnlyAction
import org.nlogo.swing.{ Button, CheckBox, PrinterManager, ToolBar, ToolBarActionButton, UserAction,
                         Printable => NlogoPrintable, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ CommentableError, ProceduresInterface, Zoomable, Events => WindowEvents }
import org.nlogo.workspace.AbstractWorkspace

abstract class CodeTab(val workspace: AbstractWorkspace, tabs: TabsInterface)
  extends JPanel
  with ProceduresInterface
  with ProceduresMenuTarget
  with AppEvents.SwitchedTabsEvent.Handler
  with WindowEvents.CompiledEvent.Handler
  with Zoomable
  with NlogoPrintable
  with MenuTab
  with ThemeSync {

  protected val compileButton = new ToolBarActionButton(new AbstractAction(I18N.gui.get("tabs.code.checkButton")) {
    override def actionPerformed(e: ActionEvent): Unit = {
      compile()
    }
  }) {
    setEnabled(false)

    override def syncTheme(): Unit = {
      super.syncTheme()

      setIcon(Utils.iconScaledWithColor("/images/check.png", 15, 15, InterfaceColors.toolbarImage()))
    }
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

  def editorConfiguration =
    editorFactory.defaultConfiguration(100, 80)
      .withCurrentLineHighlighted(true)
      .withListener(listener)

  val text = {
    val editor = editorFactory.newEditor(editorConfiguration, true)
    editor.setMargin(new Insets(4, 7, 4, 7))

    editor.addFocusListener(new FocusListener {
      def focusGained(fe: FocusEvent): Unit = { FindDialog.watch(editor, true) }
      def focusLost(fe: FocusEvent): Unit = {}
    })

    editor
  }

  private val includedFilesMenu = new IncludedFilesMenu(getIncludesTable, tabs)

  lazy val undoAction: UserAction.MenuAction = text.undoAction
  lazy val redoAction: UserAction.MenuAction = text.redoAction

  override def zoomTarget = text

  val errorLabel = new CommentableError(text)
  val toolBar = getToolBar
  val scrollableEditor = editorFactory.scrollPane(text)
  def compiler = workspace
  def program = workspace.world.program

  scrollableEditor.setBorder(null)

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
    setBorder(new EmptyBorder(24, 10, 12, 6))

    override def addControls(): Unit = {
      // Only want to add toolbar items once
      // This method gets called when the code tab pops in or pops out
      // because org.nlogo.swing.ToolBar overrides addNotify. AAB 10/2020
      if (getActionMap.get("procmenu") == null) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(UserAction.KeyBindings.keystroke('G', withMenu = true),
                                                           "procmenu")
        getActionMap.put("procmenu", proceduresMenu.getAction)

        add(compileButton)
        add(findButton)

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(UserAction.KeyBindings.keystroke('F', withMenu = true), "find")
        getActionMap.put("find", FindDialog.FIND_ACTION_CODE)

        add(proceduresMenu)
        add(includedFilesMenu)
        add(separate)
        add(prefsButton)
      }
    }
  }

  override val permanentMenuActions =
    Seq(new CodeToHtml.Action(workspace, this, () => getText)) ++ editorConfiguration.permanentActions

  override val activeMenuActions =
    editorConfiguration.contextActions.filter(_.isInstanceOf[FocusedOnlyAction]) ++ Seq(undoAction, redoAction)

  // don't let the editor influence the preferred size,
  // since the editor tends to want to be huge - ST
  override def getPreferredSize: Dimension = toolBar.getPreferredSize

  def getIncludesTable: Option[Map[String, String]] = {
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
      FindDialog.dontWatch(text, true)
  }

  private var originalFontSize = -1
  override def handle(e: WindowEvents.ZoomedEvent): Unit = {
    super.handle(e)
    if (originalFontSize == -1)
      originalFontSize = text.getFont.getSize
    text.setFont(text.getFont.deriveFont(StrictMath.ceil(originalFontSize * zoomFactor).toFloat))
    scrollableEditor.setFont(text.getFont)
    errorLabel.zoom(zoomFactor)
  }

  def handle(e: WindowEvents.CompiledEvent) = {
    dirty = false
    if (e.sourceOwner == this) {
      errorLabel.setError(Option(e.error), headerSource.length)
      compileButton.setEnabled(true)
    }
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

  def selectError(start: Int, end: Int) = text.selectError(start, end)

  def classDisplayName = "Code"

  @throws(classOf[IOException])
  def print(g: Graphics, pageFormat: PageFormat,pageIndex: Int, printer: PrinterManager) =
    printer.printText(g, pageFormat, pageIndex, text.getText)

  def setIndenter(isSmart: Boolean): Unit = {
    if(isSmart) text.setIndenter(new SmartIndenter(new EditorAreaWrapper(text), workspace))
    else text.setIndenter(new DumbIndenter(text))
  }

  def setSeparate(selected: Boolean): Unit =
    separate.setSelected(selected)

  def lineNumbersVisible = scrollableEditor.lineNumbersEnabled
  def lineNumbersVisible_=(visible: Boolean) = scrollableEditor.setLineNumbersEnabled(visible)

  def setIncludedFilesShown(visible: Boolean): Unit = {
    includedFilesMenu.setAlwaysVisible(visible)
  }

  def isTextSelected: Boolean = text.getSelectedText != null && !text.getSelectedText.isEmpty

  def close(): Unit = {}

  override def syncTheme(): Unit = {
    def boldStyle(color: Color): Style =
      new Style(color, Style.DEFAULT_BACKGROUND, text.getFont.deriveFont(Font.BOLD), false)

    setBackground(InterfaceColors.codeBackground())

    toolBar.setBackground(InterfaceColors.toolbarBackground())

    compileButton.syncTheme()
    findButton.syncTheme()

    proceduresMenu.syncTheme()
    includedFilesMenu.syncTheme()

    separate.setForeground(InterfaceColors.toolbarText())

    prefsButton.syncTheme()

    text.setBackground(InterfaceColors.codeBackground())
    text.setCaretColor(InterfaceColors.textAreaText())

    text match {
      case editor: AdvancedEditorArea =>
        editor.setCurrentLineHighlightColor(InterfaceColors.codeLineHighlight())
        editor.setDefaultSelectionColor(InterfaceColors.codeSelection())
        editor.setSyntaxScheme(new SyntaxScheme(true) {
          setStyle(TokenTypes.IDENTIFIER, new Style(InterfaceColors.defaultColor()))
          setStyle(TokenTypes.RESERVED_WORD, boldStyle(InterfaceColors.keywordColor()))
          setStyle(TokenTypes.COMMENT_KEYWORD, new Style(InterfaceColors.commentColor()))
          setStyle(TokenTypes.FUNCTION, new Style(InterfaceColors.reporterColor()))
          setStyle(TokenTypes.LITERAL_BOOLEAN, boldStyle(InterfaceColors.constantColor()))
          setStyle(TokenTypes.LITERAL_NUMBER_FLOAT, new Style(InterfaceColors.constantColor()))
          setStyle(TokenTypes.LITERAL_STRING_DOUBLE_QUOTE, new Style(InterfaceColors.constantColor()))
          setStyle(TokenTypes.LITERAL_BACKQUOTE, new Style(InterfaceColors.constantColor()))
          setStyle(TokenTypes.OPERATOR, new Style(InterfaceColors.commandColor()))
          setStyle(TokenTypes.SEPARATOR, new Style(InterfaceColors.defaultColor()))
          setStyle(TokenTypes.ERROR_IDENTIFIER, new Style(InterfaceColors.defaultColor()))
        })

      case _ =>
    }

    scrollableEditor.setBackground(InterfaceColors.codeBackground())
    scrollableEditor.getViewport.setBackground(InterfaceColors.codeBackground())
    scrollableEditor.getHorizontalScrollBar.setBackground(InterfaceColors.codeBackground())
    scrollableEditor.getVerticalScrollBar.setBackground(InterfaceColors.codeBackground())

    // for code completion popup
    editorFactory.syncTheme()
  }
}
