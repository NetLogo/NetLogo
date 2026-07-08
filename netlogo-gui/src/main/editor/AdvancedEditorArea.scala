// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Color, EventQueue, Font, Point }
import java.awt.event.{ ActionEvent, KeyEvent, MouseAdapter, MouseEvent, TextListener }
import java.util.{ Base64, Locale }

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.concurrent.Worker.State
import javafx.embed.swing.JFXPanel
import javafx.scene.input.ScrollEvent
import javafx.scene.Scene
import javafx.scene.web.{ WebEngine, WebView }

import javax.swing.AbstractAction

import netscape.javascript.JSObject

import org.nlogo.core.{ BreedIdentifierHandler, ColorConstants, I18N, Keywords, Program }
import org.nlogo.editor.MouseQuickHelpAction
import org.nlogo.swing.{ ClipboardUtils, Menu, MenuItem, PopupMenu, ScrollableTextComponent, UserAction },
  UserAction.{ EditCategory, EditClipboardGroup, EditFoldGroup, EditFoldSubcategory, EditFormatGroup,
               EditSelectionGroup, EditUndoGroup, KeyBindings, MenuAction }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.collection.mutable.Buffer
import scala.concurrent.{ Await, ExecutionContext, Promise }
import scala.concurrent.duration.{ Duration, SECONDS }
import scala.util.Try

class AdvancedEditorArea(configuration: EditorConfiguration)
  extends JFXPanel with AbstractEditorArea with ScrollableTextComponent with ThemeSync {

  private implicit val prefix: I18N.Prefix = I18N.Prefix("menu.edit")

  private implicit val ec: ExecutionContext = ExecutionContext.global

  private val bridge = new Bridge

  private var webEngine: Option[WebEngine] = None

  private val engineReady = Promise[Unit]()

  private var currentText = ""

  private var refreshText = false

  private var errorSelection = false

  private lazy val textListeners = Buffer[TextListener]()

  private val undoAction: MenuAction = {
    new AbstractAction(I18N.gui("undo")) with MenuAction {
      category = EditCategory
      group = EditUndoGroup
      accelerator = KeyBindings.keystroke('Z', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.undo()")
      }
    }
  }

  private val redoAction: MenuAction = {
    new AbstractAction(I18N.gui("redo")) with MenuAction {
      category = EditCategory
      group = EditUndoGroup
      accelerator = KeyBindings.keystroke('Y', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.redo()")
      }
    }
  }

  private val copyAction: MenuAction = {
    new AbstractAction(I18N.gui("copy")) with MenuAction {
      category = EditCategory
      group = EditClipboardGroup
      accelerator = KeyBindings.keystroke('C', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.copy(window.view)")
      }
    }
  }

  private val cutAction: MenuAction = {
    new AbstractAction(I18N.gui("cut")) with MenuAction {
      category = EditCategory
      group = EditClipboardGroup
      accelerator = KeyBindings.keystroke('X', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.cut(window.view)")
      }
    }
  }

  private val pasteAction: MenuAction = {
    new AbstractAction(I18N.gui("paste")) with MenuAction {
      category = EditCategory
      group = EditClipboardGroup
      accelerator = KeyBindings.keystroke('V', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.paste(window.view)")
      }
    }
  }

  private val deleteAction: MenuAction = {
    new AbstractAction(I18N.gui("delete")) with MenuAction {
      category = EditCategory
      group = EditClipboardGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_DELETE)

      override def actionPerformed(e: ActionEvent): Unit = {
        replaceSelection("")
      }
    }
  }

  private val selectAllAction: MenuAction = {
    new AbstractAction(I18N.gui("selectAll")) with MenuAction {
      category = EditCategory
      group = EditSelectionGroup
      accelerator = KeyBindings.keystroke('A', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.selectAll()")
      }
    }
  }

  private val toggleCommentsAction: MenuAction = {
    new AbstractAction(s"${I18N.gui("comment")} / ${I18N.gui("uncomment")}")
      with MenuAction {

      category = EditCategory
      group = EditFormatGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_SEMICOLON, withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.toggleComments(window.view)")
      }
    }
  }

  private val shiftLeftAction: MenuAction = {
    new AbstractAction(I18N.gui("shiftLeft")) with MenuAction {
      category = EditCategory
      group = EditFormatGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_OPEN_BRACKET, withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.shiftLeft()")
      }
    }
  }

  private val shiftRightAction: MenuAction = {
    new AbstractAction(I18N.gui("shiftRight")) with MenuAction {
      category = EditCategory
      group = EditFormatGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_CLOSE_BRACKET, withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.shiftRight()")
      }
    }
  }

  private val foldSelectedAction: MenuAction = {
    new AbstractAction(I18N.gui("foldSelected")) with MenuAction {
      category = EditCategory
      subcategory = EditFoldSubcategory
      group = EditFoldGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_OPEN_BRACKET, withMenu = true, withShift = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.foldSelected()")
      }
    }
  }

  private val unfoldSelectedAction: MenuAction = {
    new AbstractAction(I18N.gui("unfoldSelected")) with MenuAction {
      category = EditCategory
      subcategory = EditFoldSubcategory
      group = EditFoldGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_CLOSE_BRACKET, withMenu = true, withShift = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.unfoldSelected()")
      }
    }
  }

  private val foldAllAction: MenuAction = {
    new AbstractAction(I18N.gui("foldAll")) with MenuAction {
      category = EditCategory
      subcategory = EditFoldSubcategory
      group = EditFoldGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_OPEN_BRACKET, withMenu = true, withAlt = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.foldAll()")
      }
    }
  }

  private val unfoldAllAction: MenuAction = {
    new AbstractAction(I18N.gui("unfoldAll")) with MenuAction {
      category = EditCategory
      subcategory = EditFoldSubcategory
      group = EditFoldGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_CLOSE_BRACKET, withMenu = true, withAlt = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.unfoldAll()")
      }
    }
  }

  private val quickHelpAction = new MouseQuickHelpAction(configuration.colorizer)
  private val jumpToDeclarationAction = new JumpToDeclarationAction(this)

  val activeMenuActions: Seq[MenuAction] = {
    Seq(
      undoAction,
      redoAction,
      copyAction,
      cutAction,
      pasteAction,
      deleteAction,
      selectAllAction,
      toggleCommentsAction,
      shiftLeftAction,
      shiftRightAction,
      jumpToDeclarationAction,
      foldSelectedAction,
      unfoldSelectedAction,
      foldAllAction,
      unfoldAllAction
    )
  }

  private val popupMenu = new PopupMenu {
    add(new MenuItem(undoAction))
    add(new MenuItem(redoAction))

    addSeparator()

    add(new MenuItem(cutAction))
    add(new MenuItem(copyAction))
    add(new MenuItem(pasteAction))
    add(new MenuItem(deleteAction))

    addSeparator()

    add(new MenuItem(selectAllAction))

    addSeparator()

    add(new Menu(I18N.gui("folding")) {
      add(new MenuItem(foldSelectedAction))
      add(new MenuItem(unfoldSelectedAction))
      add(new MenuItem(foldAllAction))
      add(new MenuItem(unfoldAllAction))
    })

    addSeparator()

    add(new MenuItem(quickHelpAction))
    add(new MenuItem(toggleCommentsAction))
    add(new MenuItem(shiftLeftAction))
    add(new MenuItem(shiftRightAction))
    add(new MenuItem(jumpToDeclarationAction))
  }

  private val menuKeyMask: Int = getToolkit.getMenuShortcutKeyMaskEx

  configuration.configureAdvancedEditorArea(this)

  quickHelpAction.install(this)
  jumpToDeclarationAction.install(this)

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      if (e.isPopupTrigger) {
        showPopup(e.getPoint)
      } else {
        selectNormal()
      }
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      if (e.isPopupTrigger)
        showPopup(e.getPoint)
    }
  })

  runInWeb("window.setNormalSelection()")

  Platform.runLater(() => {
    val webView = new WebView

    webView.setContextMenuEnabled(false)
    webView.setFontScale(0.75)

    val engine = webView.getEngine

    engine.getLoadWorker.stateProperty.addListener(new ChangeListener[State] {
      override def changed(ov: ObservableValue[? <: State], oldState: State, newState: State): Unit = {
        if (newState == State.SUCCEEDED) {
          engine.executeScript("window").asInstanceOf[JSObject].setMember("bridge", bridge)

          webEngine = Some(engine)

          engineReady.success({})
        }
      }
    })

    webView.addEventFilter(ScrollEvent.SCROLL, event => {
      webEngine.foreach(_.executeScript(
        if (event.isShiftDown) {
          s"window.doScroll(${event.getX}, ${event.getY}, ${-event.getDeltaY}, 0)"
        } else {
          s"window.doScroll(${event.getX}, ${event.getY}, 0, ${-event.getDeltaY})"
        }
      ))

      event.consume()
    })

    setScene(new Scene(webView))

    engine.load(getClass.getResource("/codetab/index.html").toExternalForm)
  })

  def showPopup(point: Point): Unit = {
    popupMenu.syncTheme()
    popupMenu.show(this, point.x, point.y)
  }

  override def processKeyEvent(e: KeyEvent): Unit = {
    selectNormal()

    if ((e.getModifiersEx & menuKeyMask) == menuKeyMask && e.isShiftDown) {
      e.getKeyCode match {
        case KeyEvent.VK_OPEN_BRACKET =>
          runInWeb("window.foldSelected()")

        case KeyEvent.VK_CLOSE_BRACKET =>
          runInWeb("window.unfoldSelected()")

        case _ =>
          super.processKeyEvent(e)
      }
    } else {
      super.processKeyEvent(e)
    }
  }

  override def requestFocus(): Unit = {
    super.requestFocus()

    runInWeb("window.view.focus()")
  }

  override def isEditable: Boolean =
    getWebValue("window.isEditable()", true)

  def setEditable(editable: Boolean): Unit = {
    runInWeb(s"window.setEditable($editable)")
  }

  override def setSelection(value: Boolean): Unit = {}

  override def setIndenter(smart: Boolean): Unit = {
    runInWeb(s"window.setIndenter($smart)")
  }

  def lineNumbersVisible: Boolean =
    getWebValue("window.getLineNumbers()", true)

  def setLineNumbersVisible(visible: Boolean): Unit = {
    runInWeb(s"window.setLineNumbers($visible)")
  }

  def setCompleteOnType(enabled: Boolean): Unit = {
    runInWeb(s"window.setCompleteOnType($enabled)")
  }

  override def setText(text: String): Unit = {
    webEngine synchronized {
      currentText = text

      refreshText = false
    }

    runInWeb(s"window.setText('${Base64.getEncoder.encodeToString(text.getBytes)}')")
  }

  override def getText: String = {
    webEngine synchronized {
      if (refreshText) {
        currentText = new String(Base64.getDecoder.decode(getWebValue("window.getText()", "")))

        refreshText = false
      }

      currentText
    }
  }

  override def getSelectedText: String =
    new String(Base64.getDecoder.decode(getWebValue("window.getSelectedText()", "")))

  override def getSelectionStart: Int =
    getWebValue("window.getSelectionStart()", 0)

  override def getSelectionEnd: Int =
    getWebValue("window.getSelectionEnd()", 0)

  override def getTokenAtCaret: Option[String] =
    Option(getWebValue("window.getTokenAtCaret()", "")).filter(_.nonEmpty)

  override def select(start: Int, end: Int): Unit = {
    runInWeb(s"window.select($start, $end)")
  }

  override def selectNormal(): Unit = {
    if (errorSelection) {
      errorSelection = false

      runInWeb("window.setNormalSelection()")
    }
  }

  override def selectError(start: Int, end: Int): Unit = {
    errorSelection = true

    runInWeb("window.setErrorSelection()")

    select(start, end)
  }

  override def replaceSelection(text: String): Unit = {
    runInWeb(s"window.replaceSelection('$text')")
  }

  override def getCaretPosition: Int =
    getWebValue("window.getCaretPosition()", 0)

  override def setFont(font: Font): Unit = {
    super.setFont(font)

    runInWeb(s"window.setFont('${font.getFamily}', ${font.getSize})")
  }

  override def addTextListener(listener: TextListener): Unit = {
    textListeners += listener
  }

  override def scrollTo(index: Int): Unit = {}

  def setProgram(program: Program, procedures: Seq[String], extensionCommands: Seq[String],
                 extensionReporters: Seq[String]): Unit = {
    def format(names: Seq[String]): String =
      names.map(name => s"\"${name.toLowerCase(Locale.US)}\"").mkString("[", ",", "]")

    val keywords: String = format(Keywords.keywords.toSeq ++ program.breeds.keys.map(name => s"$name-own") :+ "breed")
    val constants: String = format(ColorConstants.ColorNames.toSeq ++
                                   Seq("grey", "false", "true", "nobody", "e", "pi"))
    val globals: String = format(program.globals ++ procedures)
    val variables: String = format(program.turtlesOwn ++ program.patchesOwn ++ program.linksOwn)

    val coreCommands: Seq[String] = program.dialect.tokenMapper.allCommandNames.filterNot(_.startsWith("__")).toSeq
    val coreReporters: Seq[String] = program.dialect.tokenMapper.allReporterNames.filterNot(_.startsWith("__")).toSeq

    val breedCommands: Seq[String] = program.breeds.values.flatMap(BreedIdentifierHandler.breedCommands).toSeq
    val breedReporters: Seq[String] = program.breeds.values.flatMap(BreedIdentifierHandler.breedReporters).toSeq

    val commands: String = format(coreCommands ++ extensionCommands ++ breedCommands)
    val reporters: String = format(coreReporters ++ extensionReporters ++ breedReporters)

    runInWeb(s"window.setProgram($keywords, $constants, $globals, $variables, $commands, $reporters)")
  }

  private def runInWeb(function: String): Unit = {
    engineReady.future.foreach { _ =>
      Platform.runLater(() => {
        webEngine.foreach(_.executeScript(function))
      })
    }
  }

  private def getWebValue[T](function: String, default: T): T = {
    val value = Promise[T]()

    Platform.runLater(() => {
      value.success(webEngine.fold(default)(_.executeScript(function).asInstanceOf[T]))
    })

    Try(Await.result(value.future, Duration(1, SECONDS))).getOrElse(default)
  }

  private def colorString(color: Color): String =
    s"rgba(${color.getRed}, ${color.getGreen}, ${color.getBlue}, ${color.getAlpha / 255f})"

  override def syncTheme(): Unit = {
    runInWeb(s"""window.syncTheme({
      |  background: "${colorString(InterfaceColors.codeBackground())}",
      |  gutterBorder: "${colorString(InterfaceColors.codeSeparator())}",
      |  scrollBarBackground: "${colorString(InterfaceColors.scrollBarBackground())}",
      |  scrollBarForeground: "${colorString(InterfaceColors.scrollBarForeground())}",
      |  scrollBarForegroundHover: "${colorString(InterfaceColors.scrollBarForegroundHover())}",
      |  caret: "${colorString(InterfaceColors.textAreaText())}",
      |  lineHighlight: "${colorString(InterfaceColors.codeLineHighlight())}",
      |  selection: "${colorString(InterfaceColors.codeSelection())}",
      |  selectionError: "${colorString(InterfaceColors.errorHighlight())}",
      |  default: "${colorString(InterfaceColors.defaultColor())}",
      |  comment: "${colorString(InterfaceColors.commentColor())}",
      |  constant: "${colorString(InterfaceColors.constantColor())}",
      |  keyword: "${colorString(InterfaceColors.keywordColor())}",
      |  command: "${colorString(InterfaceColors.commandColor())}",
      |  reporter: "${colorString(InterfaceColors.reporterColor())}"
      })""".stripMargin)
  }

  private class Bridge {
    def log(message: String): Unit = {
      println(message)
    }

    def textUpdated(overwriting: Boolean, canUndo: Boolean, canRedo: Boolean): Unit = {
      webEngine synchronized {
        refreshText = true
      }

      undoAction.setEnabled(canUndo)
      redoAction.setEnabled(canRedo)

      if (!overwriting) {
        EventQueue.invokeLater(() => {
          textListeners.foreach(_.textValueChanged(null))
        })
      }
    }

    def writeClipboard(text: String): Unit = {
      webEngine synchronized {
        ClipboardUtils.writeString(text)
      }
    }

    def readClipboard(): String = {
      webEngine synchronized {
        ClipboardUtils.readString()
      }
    }

    def jumpToDeclaration(): Unit = {
      EventQueue.invokeLater(() => {
        jumpToDeclarationAction.actionPerformed(null)
      })
    }
  }
}
