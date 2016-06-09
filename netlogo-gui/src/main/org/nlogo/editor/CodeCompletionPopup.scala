// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{Color, Component, Dimension}
import java.awt.event._
import javax.swing._

import org.nlogo.api.CompilerServices
import org.nlogo.core.DefaultTokenMapper
import org.nlogo.window.AutoSuggest

case class CodeCompletionPopup(compiler: CompilerServices) {
  // To control when the popup has to automatically show
  var isPopupEnabled = false
  // To store the last value user selected
  var lastSuggested = ""
  var editorArea: EditorArea = null
  val autoSuggest = new AutoSuggest()
  val dlm = new DefaultListModel[String]()
  val suggestionDisplaylist = new JList[String](dlm)
  val scrollPane = new JScrollPane(suggestionDisplaylist)
  val window = new JDialog()
  window.setUndecorated(true)
  window.add(scrollPane)
  window.setSize(new Dimension(150, 200))

  scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)

  def init(editorArea: EditorArea): Unit = {
    this.editorArea = editorArea
    suggestionDisplaylist.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
    suggestionDisplaylist.setLayoutOrientation(JList.VERTICAL)
    suggestionDisplaylist.setVisibleRowCount(10)
    suggestionDisplaylist.setCellRenderer(new SuggestionListRenderer(editorArea))

    suggestionDisplaylist.addKeyListener(new KeyListener {
      override def keyTyped(e: KeyEvent): Unit = {
        e.getKeyChar match {
          case java.awt.event.KeyEvent.VK_ESCAPE =>
            isPopupEnabled = false
            window.setVisible(false)
          case java.awt.event.KeyEvent.VK_BACK_SPACE =>
            // Trying to fix the weird behaviour of backspace
            val position = editorArea.getCaretPosition
            if (position != 0) {
              editorArea.setCaretPosition(position - 1)
              val doc = editorArea.getDocument.asInstanceOf[javax.swing.text.PlainDocument]
              doc.remove(position - 1, 1)
            }
          case java.awt.event.KeyEvent.VK_ENTER =>
            val suggestion = suggestionDisplaylist.getSelectedValue
            lastSuggested = suggestion
            val token = compiler.getTokenAtPosition(editorArea.getText(), editorArea.getCaretPosition)
            val position = editorArea.getCaretPosition
            val doc = editorArea.getDocument.asInstanceOf[javax.swing.text.PlainDocument]
            doc.replace(token.start, position - token.start, suggestion, null)
            editorArea.setCaretPosition(token.start + suggestion.length)
            isPopupEnabled = false
          case _ =>
            e.setSource(editorArea)
            editorArea.dispatchEvent(e)
        }
      }

      override def keyPressed(e: KeyEvent): Unit = {}

      override def keyReleased(e: KeyEvent): Unit = {}
    })
  }

  /**
    * Makes the suggestion box initially and displays it.
    * Should only be called when the user presses the shortcut to
    * enable the suggestion. To update the suggestions list call showPopUp()
    */
  def enablePopup(): Unit = {
    isPopupEnabled = true
    showPopup()
    val position = editorArea.getCaretPosition
    window.setLocation(editorArea.getLocationOnScreen.x + editorArea.modelToView(position + 1).x,
      editorArea.getLocationOnScreen.y + editorArea.modelToView(position).y + editorArea.getFont.getSize)
  }

  /**
    * Updates the list of the suggestion box and makes it visible.
    */
  def showPopup(): Unit = {
    if(isPopupEnabled) {
      val token = compiler.getTokenAtPosition(editorArea.getText(), editorArea.getCaretPosition)
      var list = Seq[String]()
      // word only containd the current token till the cursor position
      val word = if(token != null) token.text.substring(0, editorArea.getCaretPosition - token.start) else null
      // popup not to be diplayed after user hits the enter key
      if (word != null && !token.text.equals(lastSuggested)) {
        list = autoSuggest.getSuggestions(word)
        dlm.removeAllElements()
        var i = 0
        list.foreach(dlm.addElement(_))
        if (!list.isEmpty) {
          lastSuggested = ""
          window.validate()
          window.setVisible(true)
          // Required to keep the caret in the editorArea visible
          editorArea.getCaret.setVisible(true)
        } else {
          isPopupEnabled = false
        }
      }
      if (list.isEmpty || token == null) {
        window.setVisible(false)
      }
    }
  }
}

/**
  * This is the renderer to render elements of the list displayed
  * in the suggestion box.
  * @param editorArea
  */
class SuggestionListRenderer(editorArea: EditorArea) extends ListCellRenderer[String]{

  override def getListCellRendererComponent(list: JList[_ <: String], value: String, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
    val label = new JLabel(value.asInstanceOf[String])
    label.setForeground(if (DefaultTokenMapper.getCommand(value.asInstanceOf[String]).isEmpty) Color.BLUE
    else new Color(0x551A8B))
    label.setBackground(if(isSelected || cellHasFocus) new Color(0xEEAEEE) else Color.white)
    label.setOpaque(true)
    label.setBorder(BorderFactory.createEmptyBorder())
    label.setFont(editorArea.getFont)
    label
  }
}