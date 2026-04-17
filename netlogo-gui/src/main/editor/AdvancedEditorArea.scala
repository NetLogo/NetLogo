// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Color, EventQueue, Font }
import java.awt.event.{ ActionEvent, KeyEvent, MouseAdapter, MouseEvent, TextListener }
import java.util.Base64

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

import org.nlogo.core.I18N
import org.nlogo.swing.{ ClipboardUtils, ScrollableTextComponent, UserAction },
  UserAction.{ EditCategory, EditClipboardGroup, EditFoldGroup, EditFoldSubcategory, EditFormatGroup,
               EditSelectionGroup, EditUndoGroup, KeyBindings, MenuAction }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.collection.mutable.Buffer

class AdvancedEditorArea(configuration: EditorConfiguration)
  extends JFXPanel with AbstractEditorArea with ScrollableTextComponent with ThemeSync {

  private val bridge = new Bridge

  private var webEngine: Option[WebEngine] = None

  private var currentText = ""

  private var refreshText = false

  private lazy val textListeners = Buffer[TextListener]()

  private val undoAction: MenuAction = {
    new AbstractAction(I18N.gui.get("menu.edit.undo")) with MenuAction {
      category = EditCategory
      group = EditUndoGroup
      accelerator = KeyBindings.keystroke('Z', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.undo()")
      }
    }
  }

  private val redoAction: MenuAction = {
    new AbstractAction(I18N.gui.get("menu.edit.redo")) with MenuAction {
      category = EditCategory
      group = EditUndoGroup
      accelerator = KeyBindings.keystroke('Y', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.redo()")
      }
    }
  }

  private val copyAction: MenuAction = {
    new AbstractAction(I18N.gui.get("menu.edit.copy")) with MenuAction {
      category = EditCategory
      group = EditClipboardGroup
      accelerator = KeyBindings.keystroke('C', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.copy(window.view)")
      }
    }
  }

  private val cutAction: MenuAction = {
    new AbstractAction(I18N.gui.get("menu.edit.cut")) with MenuAction {
      category = EditCategory
      group = EditClipboardGroup
      accelerator = KeyBindings.keystroke('X', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.cut(window.view)")
      }
    }
  }

  private val pasteAction: MenuAction = {
    new AbstractAction(I18N.gui.get("menu.edit.paste")) with MenuAction {
      category = EditCategory
      group = EditClipboardGroup
      accelerator = KeyBindings.keystroke('V', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.paste(window.view)")
      }
    }
  }

  private val deleteAction: MenuAction = {
    new AbstractAction(I18N.gui.get("menu.edit.delete")) with MenuAction {
      category = EditCategory
      group = EditClipboardGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_DELETE)

      override def actionPerformed(e: ActionEvent): Unit = {
        replaceSelection("")
      }
    }
  }

  private val selectAllAction: MenuAction = {
    new AbstractAction(I18N.gui.get("menu.edit.selectAll")) with MenuAction {
      category = EditCategory
      group = EditSelectionGroup
      accelerator = KeyBindings.keystroke('A', withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.selectAll()")
      }
    }
  }

  private val toggleCommentsAction: MenuAction = {
    new AbstractAction(I18N.gui.get("menu.edit.comment") + " / " + I18N.gui.get("menu.edit.uncomment"))
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
    new AbstractAction(I18N.gui.get("menu.edit.shiftLeft")) with MenuAction {
      category = EditCategory
      group = EditFormatGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_OPEN_BRACKET, withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.shiftLeft()")
      }
    }
  }

  private val shiftRightAction: MenuAction = {
    new AbstractAction(I18N.gui.get("menu.edit.shiftRight")) with MenuAction {
      category = EditCategory
      group = EditFormatGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_CLOSE_BRACKET, withMenu = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.shiftRight()")
      }
    }
  }

  private val foldSelectedAction: MenuAction = {
    new AbstractAction(I18N.gui.get("menu.edit.foldSelected")) with MenuAction {
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
    new AbstractAction(I18N.gui.get("menu.edit.unfoldSelected")) with MenuAction {
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
    new AbstractAction(I18N.gui.get("menu.edit.foldAll")) with MenuAction {
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
    new AbstractAction(I18N.gui.get("menu.edit.unfoldAll")) with MenuAction {
      category = EditCategory
      subcategory = EditFoldSubcategory
      group = EditFoldGroup
      accelerator = KeyBindings.keystroke(KeyEvent.VK_CLOSE_BRACKET, withMenu = true, withAlt = true)

      override def actionPerformed(e: ActionEvent): Unit = {
        runInWeb("window.unfoldAll()")
      }
    }
  }

  val permanentMenuActions: Seq[MenuAction] = {
    Seq(
      copyAction,
      cutAction,
      pasteAction,
      deleteAction,
      selectAllAction
    )
  }

  val activeMenuActions: Seq[MenuAction] = {
    Seq(
      undoAction,
      redoAction,
      toggleCommentsAction,
      shiftLeftAction,
      shiftRightAction,
      new JumpToDeclarationAction(this),
      foldSelectedAction,
      unfoldSelectedAction,
      foldAllAction,
      unfoldAllAction
    )
  }

  private val menuKeyMask: Int = getToolkit.getMenuShortcutKeyMaskEx

  configuration.configureAdvancedEditorArea(this)

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      selectNormal()
    }
  })

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
        }
      }
    })

    webView.addEventFilter(ScrollEvent.SCROLL, event => {
      webEngine.foreach(_.executeScript(
        s"window.scrollBy(${-event.getDeltaX}, ${-event.getDeltaY})"
      ))

      event.consume()
    })

    setScene(new Scene(webView))

    engine.load(getClass.getResource("/codetab/index.html").toExternalForm)
  })

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
    runInWeb("window.setNormalSelection()")
  }

  override def selectError(start: Int, end: Int): Unit = {
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

  private def runInWeb(function: String): Unit = {
    Platform.runLater(() => {
      webEngine.foreach(_.executeScript(function))
    })
  }

  private def getWebValue[T](function: String, default: T): T = {
    webEngine synchronized {
      var value: Option[T] = None

      Platform.runLater(() => {
        val result = webEngine.fold(default)(_.executeScript(function).asInstanceOf[T])

        webEngine synchronized {
          value = Some(result)
        }
      })

      while (value.isEmpty)
        webEngine.wait(10)

      value.getOrElse(default)
    }
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
  }
}
