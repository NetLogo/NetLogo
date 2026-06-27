// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ BorderLayout, Component, Dimension, Font, Graphics }
import java.awt.event.{ ActionEvent, FocusAdapter, FocusEvent, KeyAdapter, KeyEvent, TextEvent, TextListener }
import java.awt.print.PageFormat
import java.io.IOException
import java.net.MalformedURLException
import javax.swing.{ AbstractAction, JPanel }
import javax.swing.border.EmptyBorder

import org.nlogo.agent.Observer
import org.nlogo.app.common.{CodeToHtml, EditorFactory, FindDialog, MenuTab, TabsInterface, Events => AppEvents}
import org.nlogo.awt.RowLayout
import org.nlogo.core.{ AgentKind, CompilerException, I18N }
import org.nlogo.editor.{ AdvancedEditorArea, EditorConfiguration }
import org.nlogo.swing.{ Button, CheckBox, FocusRoot, PrinterManager, ToolBarActionButton, UserAction,
                         Printable => NlogoPrintable, Transparent, Utils }
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
  with FocusRoot
  with ThemeSync {

  protected val compileButton = new ToolBarActionButton(new AbstractAction(I18N.gui.get("tabs.code.checkButton")) {
    override def actionPerformed(e: ActionEvent): Unit = {
      compile()
      text.requestFocus()
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

  private val configuration: EditorConfiguration =
    editorFactory.defaultConfiguration(100, 80).withListener(listener).withSmartIndent(tabs.smartTabbingEnabled)

  protected def editorConfiguration: EditorConfiguration =
    configuration

  protected val text: AdvancedEditorArea = new AdvancedEditorArea(editorConfiguration) {
    addFocusListener(new FocusAdapter {
      override def focusGained(e: FocusEvent): Unit = {
        FindDialog.watch(text, true)
      }
    })

    addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent): Unit = {
        e.getKeyCode match {
          case KeyEvent.VK_TAB if e.isControlDown =>
            if (e.isShiftDown) {
              transferFocusBackward()
            } else {
              transferFocus()
            }

            e.consume()

          case _ =>
        }
      }
    })
  }

  private val includedFilesMenu = new IncludedFilesMenu(getIncludesTable, tabs)

  override def zoomTarget = text

  protected val errorLabel = new CommentableError(text)

  private val toolBar = new JPanel(new RowLayout(10, Component.LEFT_ALIGNMENT, Component.CENTER_ALIGNMENT)) {
    setBorder(new EmptyBorder(24, 10, 12, 6))
    setFocusable(false)

    add(compileButton)
    add(findButton)
    add(proceduresMenu)
    add(includedFilesMenu)
    add(separate)
    add(prefsButton)
  }

  def compiler = workspace
  def program = workspace.world.program

  locally {
    setLayout(new BorderLayout)
    setCanFocus(false)

    add(toolBar, BorderLayout.NORTH)
    add(new JPanel(new BorderLayout) with Transparent {
      setFocusable(false)

      add(text, BorderLayout.CENTER)
      add(errorLabel.component, BorderLayout.NORTH)
    }, BorderLayout.CENTER)
  }

  override def getDefaultComponent: Option[Component] =
    Option(text)

  override def getFocusOrder: Map[Component, (Component, Component)] = {
    Map(
      text -> (null, if (compileButton.isEnabled) compileButton else findButton),
      findButton -> (if (compileButton.isEnabled) compileButton else text, null),
      compileButton -> (text, null)
    )
  }

  override val permanentMenuActions: Seq[UserAction.MenuAction] = {
    text.permanentMenuActions ++ editorConfiguration.getAdditionalActions :+
      new CodeToHtml.Action(workspace, this, () => getText)
  }

  override def activeMenuActions: Seq[UserAction.MenuAction] =
    text.activeMenuActions :+ FindDialog.FIND_ACTION_CODE :+ FindDialog.FIND_NEXT_ACTION_CODE

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
      FindDialog.dontWatch(true)
  }

  private var originalFontSize = -1
  override def handle(e: WindowEvents.ZoomedEvent): Unit = {
    super.handle(e)
    if (originalFontSize == -1)
      originalFontSize = text.getFont.getSize
    text.setFont(text.getFont.deriveFont(StrictMath.ceil(originalFontSize * zoomFactor).toFloat))
    errorLabel.zoom(zoomFactor)
  }

  def handle(e: WindowEvents.CompiledEvent) = {
    dirty = false
    if (e.sourceOwner == this) {
      errorLabel.setError(Option(e.error), headerSource.length)
      compileButton.setEnabled(true)
    }
    setProgram()
  }

  protected def setProgram(): Unit = {
    text.setProgram(workspace.world.program, workspace.procedures.keys.map(_._1).toSeq,
                    workspace.getExtensionManager.extensionCommandNames.toSeq,
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
    text.setFont(font)

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
