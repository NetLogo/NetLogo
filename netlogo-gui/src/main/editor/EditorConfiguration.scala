// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Font, GraphicsEnvironment }
import java.awt.event.{ KeyEvent, TextEvent, TextListener }
import java.awt.event.InputEvent.{ CTRL_DOWN_MASK => CtrlKey, SHIFT_DOWN_MASK => ShiftKey }
import javax.swing.{ JScrollPane, KeyStroke }

import org.nlogo.api.CompilerServices
import org.nlogo.core.NetLogoPreferences
import org.nlogo.editor.KeyBinding._
import org.nlogo.swing.{ TextActions, UserAction }

object EditorConfiguration {
  private def os(s: String) =
    System.getProperty("os.name").startsWith(s)

  private lazy val availableFonts: Array[String] =
    GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames

  private lazy val platformMonospacedFont: String = {
    if (os("Mac")) {
      "Menlo"
    } else if (os("Windows")) {
      availableFonts.find(_.equalsIgnoreCase("Consolas")).getOrElse("Monospaced")
    } else {
      "Monospaced"
    }
  }

  private lazy val defaultFont = new Font(platformMonospacedFont, Font.PLAIN, 12)

  private var defaultCodeFont: Option[Font] = {
    Option(NetLogoPreferences.get("codeFont", null))
      .filter(font => availableFonts.exists(_.toLowerCase == font.toLowerCase))
      .map(new Font(_, Font.PLAIN, 12))
  }

  private val emptyListener =
    new TextListener() { override def textValueChanged(e: TextEvent): Unit = { } }

  def defaultContextActions(colorizer: Colorizer): Seq[UserAction.MenuAction] =
    Seq(new MouseQuickHelpAction(colorizer))

  private val emptyMenu =
    new EditorMenu {
      def offerAction(action: UserAction.MenuAction): Unit = {}
    }

  def default(rows: Int, columns: Int, compiler: CompilerServices, colorizer: Colorizer) =
    EditorConfiguration(rows, columns, getCodeFont, emptyListener, compiler, colorizer, Map(),
                        defaultContextActions(colorizer), Seq(), false, false, false, false, emptyMenu, () => None)

  def getMonospacedFont: Font =
    defaultFont

  def getCodeFont: Font =
    defaultCodeFont.getOrElse(defaultFont)

  def setCodeFont(font: Option[Font]): Unit = {
    defaultCodeFont = font
  }
}

case class EditorConfiguration(
  rows:                 Int,
  columns:              Int,
  font:                 Font,
  listener:             TextListener,
  compiler:             CompilerServices,
  colorizer:            Colorizer,
  /* additionalActions are added to the input map and added to
   * top-level menus if appropriate */
  additionalActions:    Map[KeyStroke, UserAction.MenuAction],
  /* contextActions are presented in the right-click context menu */
  contextActions:       Seq[UserAction.MenuAction],
  /* menuActions are made available to top-level menus, but not otherwise available */
  menuActions:          Seq[UserAction.MenuAction],
  enableFocusTraversal: Boolean,
  highlightCurrentLine: Boolean,
  showLineNumbers:      Boolean,
  is3Dlanguage:         Boolean,
  menu:                 EditorMenu,
  scrollPaneGetter:     () => Option[JScrollPane]) {

  def withFont(font: Font) =
    copy(font = font)
  def withListener(listener: TextListener) =
    copy(listener = listener)
  def withFocusTraversalEnabled(isEnabled: Boolean) =
    copy(enableFocusTraversal = isEnabled)
  def withCurrentLineHighlighted(isHighlighted: Boolean) =
    copy(highlightCurrentLine = isHighlighted)
  def withLineNumbers(show: Boolean) =
    copy(showLineNumbers = show)
  def withContextActions(actions: Seq[UserAction.MenuAction]) =
    copy(contextActions = contextActions ++ actions)
  def withMenuActions(actions: Seq[UserAction.MenuAction]) =
    copy(menuActions = menuActions ++ actions)
  def forThreeDLanguage(is3D: Boolean) =
    copy(is3Dlanguage = is3D)
  def addKeymap(key: KeyStroke, action: UserAction.MenuAction) =
    copy(additionalActions = additionalActions + (key -> action))
  def withKeymap(keymap: Map[KeyStroke, UserAction.MenuAction]) =
    copy(additionalActions = keymap)
  def withMenu(newMenu: EditorMenu) =
    copy(menu = newMenu)
  def withScrollPaneGetter(getter: () => Option[JScrollPane]) =
    copy(scrollPaneGetter = getter)

  def configureEditorArea(editor: EditorArea) = {

    editor.setEditorKit(new HighlightEditorKit(colorizer))
    editor.addTextListener(listener)

    DocumentProperties.install(editor)

    editor.setIndenter(new DumbIndenter(editor))

    if (highlightCurrentLine) {
      new LinePainter(editor)
    }

    editor.setFont(font)
    editor.setFocusTraversalKeysEnabled(enableFocusTraversal)

    val focusTraversalListener = new FocusTraversalListener(editor)

    editor.addFocusListener(focusTraversalListener)
    editor.addMouseListener(focusTraversalListener)

    if (enableFocusTraversal) {
      editor.getInputMap.put(keystroke(KeyEvent.VK_TAB), new TransferFocusAction())
      editor.getInputMap.put(keystroke(KeyEvent.VK_TAB, ShiftKey), new TransferFocusBackwardAction())
    } else {
      editor.getInputMap.put(keystroke(KeyEvent.VK_TAB, CtrlKey), new TransferFocusAction())
      editor.getInputMap.put(keystroke(KeyEvent.VK_TAB, CtrlKey | ShiftKey), new TransferFocusBackwardAction())
    }

    additionalActions.foreach {
      case (k, v) => editor.getInputMap.put(k, v)
    }

    (contextActions ++ menuActions ++ additionalActions.values).foreach {
      case e: InstallableAction => e.install(editor)
      case _ =>
    }

    TextActions.applyToComponent(editor)
  }

  def configureAdvancedEditorArea(editor: AdvancedEditorArea) = {
    editor.addTextListener(listener)
    editor.setIndenter(false)
    editor.setFont(font)

    (contextActions ++ menuActions ++ additionalActions.values).foreach {
      case e: InstallableAction => e.install(editor)
      case _ =>
    }

    additionalActions.foreach {
      case (k, v) => editor.getInputMap.put(k, v)
    }
  }

  def getAdditionalActions: Seq[UserAction.MenuAction] = additionalActions.values.toSeq

  def permanentActions: Seq[UserAction.MenuAction] = getAdditionalActions ++ menuActions
}
