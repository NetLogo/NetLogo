// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Font, GraphicsEnvironment }
import java.awt.event.{ KeyEvent, TextEvent, TextListener }
import java.awt.event.InputEvent.{ ALT_DOWN_MASK => AltKey, CTRL_DOWN_MASK => CtrlKey, SHIFT_DOWN_MASK => ShiftKey }
import javax.swing.{ Action, InputMap, JScrollPane, KeyStroke }
import javax.swing.text.{ DefaultEditorKit, TextAction }

import org.fife.ui.rtextarea.RTextAreaEditorKit

import org.nlogo.editor.KeyBinding._
import org.nlogo.swing.{ TextActions, UserAction }

object EditorConfiguration {
  private def os(s: String) =
    System.getProperty("os.name").startsWith(s)

  lazy val platformMonospacedFont =
    if (os("Mac"))
      "Menlo"
    else if (os("Windows"))
      GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames
        .find(_.equalsIgnoreCase("Consolas")).getOrElse("Monospaced")
    else "Monospaced"

  val defaultFont = new Font(platformMonospacedFont, Font.PLAIN, 12)

  private val emptyListener =
    new TextListener() { override def textValueChanged(e: TextEvent): Unit = { } }

  def defaultContextActions(colorizer: Colorizer): Seq[UserAction.MenuAction] =
    Seq(new MouseQuickHelpAction(colorizer))

  private val emptyMenu =
    new EditorMenu {
      def offerAction(action: UserAction.MenuAction): Unit = {}
    }

  def default(rows: Int, columns: Int, colorizer: Colorizer) =
    EditorConfiguration(rows, columns, defaultFont, emptyListener, colorizer, Map(), defaultContextActions(colorizer),
                        Seq(), false, false, false, false, emptyMenu, () => None)
}

case class EditorConfiguration(
  rows:                 Int,
  columns:              Int,
  font:                 Font,
  listener:             TextListener,
  colorizer:            Colorizer,
  /* additionalActions are added to the input map and added to
   * top-level menus if appropriate */
  additionalActions:    Map[KeyStroke, TextAction & UserAction.MenuAction],
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
  def addKeymap(key: KeyStroke, action: TextAction & UserAction.MenuAction) =
    copy(additionalActions = additionalActions + (key -> action))
  def withKeymap(keymap: Map[KeyStroke, TextAction & UserAction.MenuAction]) =
    copy(additionalActions = keymap)
  def withMenu(newMenu: EditorMenu) =
    copy(menu = newMenu)
  def withScrollPaneGetter(getter: () => Option[JScrollPane]) =
    copy(scrollPaneGetter = getter)

  def configureEditorArea(editor: EditorArea) = {

    editor.setEditorKit(new HighlightEditorKit(colorizer))

    val editorListener = new EditorListener(e => listener.textValueChanged(null))
    editorListener.install(editor)
    DocumentProperties.install(editor)

    val indenter = new DumbIndenter(editor)
    editor.setIndenter(indenter)

    if (highlightCurrentLine) {
      new LinePainter(editor)
    }

    editor.setFont(font)
    editor.setFocusTraversalKeysEnabled(enableFocusTraversal)

    if (enableFocusTraversal) {
      val focusTraversalListener = new FocusTraversalListener(editor)
      editor.addFocusListener(focusTraversalListener)
      editor.addMouseListener(focusTraversalListener)
      editor.getInputMap.put(keystroke(KeyEvent.VK_TAB),           new TransferFocusAction())
      editor.getInputMap.put(keystroke(KeyEvent.VK_TAB, ShiftKey), new TransferFocusBackwardAction())
    } else {
      editor.getInputMap.put(keystroke(KeyEvent.VK_TAB, CtrlKey),            new TransferFocusAction())
      editor.getInputMap.put(keystroke(KeyEvent.VK_TAB, CtrlKey | ShiftKey), new TransferFocusBackwardAction())
    }

    additionalActions.foreach {
      case (k, v) => editor.getInputMap.put(k, v)
    }

    (contextActions ++ menuActions).foreach {
      case e: InstallableAction => e.install(editor)
      case _ =>
    }

    TextActions.applyToComponent(editor)
  }

  def configureAdvancedEditorArea(editor: AbstractEditorArea) = {
    DocumentProperties.install(editor)

    val editorListener = new EditorListener(e => listener.textValueChanged(null))
    editorListener.install(editor)

    val indenter = new DumbIndenter(editor)
    editor.setIndenter(indenter)

    editor.setFont(font)

    (contextActions ++ menuActions).foreach {
      case e: InstallableAction => e.install(editor)
      case _ =>
    }

    additionalActions.foreach {
      case (k, v) => editor.getInputMap.put(k, v)
    }

    if (EditorConfiguration.os("Mac")) {
      Seq(
        (KeyEvent.VK_BACK_SPACE, AltKey)  -> RTextAreaEditorKit.rtaDeletePrevWordAction,
        // Unfortunately, `rtaDeleteNextWordAction` is not implemented by rta.
        // Leaving this here for posterity wondering why alt-delete doesn't
        // work on Macs. - BCH 11/7/2016
        //(KeyEvent.VK_DELETE,     AltKey)  -> RTextAreaEditorKit.rtaDeleteNextWordAction,
        (KeyEvent.VK_A,          CtrlKey) -> DefaultEditorKit.beginLineAction,
        (KeyEvent.VK_E,          CtrlKey) -> DefaultEditorKit.endLineAction,
        (KeyEvent.VK_K,          CtrlKey) -> RTextAreaEditorKit.rtaDeleteRestOfLineAction
      ).foreach { case ((key, mod), action) => editor.getInputMap().put(KeyStroke.getKeyStroke(key, mod), action)}
    }

    editor.getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, CtrlKey),
                           new TextActions.CorrectDeleteNextWordAction(editor))

    // there doesn't seem to be a way to directly remove an undesired action if it's in a parent map,
    // so recursively search through the parents to find the correct map to remove it from (Isaac B 4/13/25)
    def removeQuote(map: InputMap): Unit = {
      if (map.keys != null && map.keys.exists(_.getKeyChar == '"')) {
        map.remove(KeyStroke.getKeyStroke('"'))
      } else if (map.getParent != null) {
        removeQuote(map.getParent)
      }
    }

    removeQuote(editor.getInputMap)
  }

  def permanentActions: Seq[UserAction.MenuAction] = additionalActions.values.toSeq ++ menuActions

  def editorOnlyActions: Seq[Action] = Seq()
}
