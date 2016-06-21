// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.{Color, Component, Dimension}
import java.awt.event._
import javax.swing._

import org.nlogo.api.CompilerServices
import org.nlogo.core.DefaultTokenMapper
import org.nlogo.window.SyntaxColors
import org.nlogo.editor.EditorArea

case class CodeCompletionPopup(compiler: CompilerServices) {
  // To control when the popup has to automatically show
  var isPopupEnabled = false
  // To store the last value user selected
  var lastSuggested = ""
  val autoSuggest = new AutoSuggest()
  val dlm = new DefaultListModel[String]()
  val suggestionDisplaylist = new JList[String](dlm)
  val scrollPane = new JScrollPane(suggestionDisplaylist)
  val window = new JDialog()
  window.setUndecorated(true)
  window.add(scrollPane)
  window.setSize(new Dimension(150, 200))
  var editorArea: Option[EditorArea] = None

  scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)

  def init(editorArea: EditorArea, autoSuggestDocumentListener: AutoSuggestDocumentListener): Unit = {
    if(this.editorArea.isDefined)
      return
    this.editorArea = Some(editorArea)
    for {eA <- this.editorArea} {
      suggestionDisplaylist.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
      suggestionDisplaylist.setLayoutOrientation(JList.VERTICAL)
      suggestionDisplaylist.setVisibleRowCount(10)
      suggestionDisplaylist.setCellRenderer(new SuggestionListRenderer(eA))

      suggestionDisplaylist.addKeyListener(new KeyListener {
        override def keyTyped(e: KeyEvent): Unit = {
          e.getKeyChar match {
            case java.awt.event.KeyEvent.VK_ESCAPE =>
              isPopupEnabled = false
              window.setVisible(false)
              eA.getDocument().removeDocumentListener(autoSuggestDocumentListener)
            case java.awt.event.KeyEvent.VK_BACK_SPACE =>
              // Trying to fix the weird behaviour of backspace
              val position = eA.getCaretPosition
              println("here")
              if (position != 0) {
                eA.setCaretPosition(position - 1)
                val doc = eA.getDocument.asInstanceOf[javax.swing.text.PlainDocument]
                doc.remove(position - 1, 1)
              }
            case java.awt.event.KeyEvent.VK_ENTER =>
              var suggestion = suggestionDisplaylist.getSelectedValue
              lastSuggested = suggestion
              val token = compiler.getTokenAtPosition(eA.getText(), eA.getCaretPosition)
              val position = eA.getCaretPosition
              val doc = eA.getDocument.asInstanceOf[javax.swing.text.PlainDocument]
              doc.replace(token.start, position - token.start, suggestion, null)
              eA.setCaretPosition(token.start + suggestion.length)
              doc.removeDocumentListener(autoSuggestDocumentListener)
              isPopupEnabled = false
            case _ =>
              e.setSource(eA)
              eA.dispatchEvent(e)
          }
        }

        override def keyPressed(e: KeyEvent): Unit = {}

        override def keyReleased(e: KeyEvent): Unit = {
        }
      })
    }
  }

  /**
    * Makes the suggestion box initially and displays it.
    * Should only be called when the user presses the shortcut to
    * enable the suggestion. To update the suggestions list call showPopUp()
    */
  def displayPopup(): Unit = {
    for{eA <- editorArea} {
      isPopupEnabled = true
      val token = compiler.getTokenAtPosition(eA.getText(), eA.getCaretPosition)
      val position = if (token != null) token.start else eA.getCaretPosition
      window.setLocation(eA.getLocationOnScreen.x + eA.modelToView(position).x,
        eA.getLocationOnScreen.y + eA.modelToView(position).y + eA.getFont.getSize)
      updatePopup()
    }
  }

  /**
    * Updates the list of the suggestion box and makes it visible.
    */
  def updatePopup(): Unit = {
    println("I was called")
    for{eA <- editorArea} {
      if (isPopupEnabled) {
        val token = compiler.getTokenAtPosition(eA.getText(), eA.getCaretPosition)
        var list = Seq[String]()
        // word only contains the current token till the cursor position
        val word = if (token != null) token.text.substring(0, eA.getCaretPosition - token.start) else null
        // popup not to be diplayed after user hits the enter key
        if (word != null && !token.text.equals(lastSuggested)) {
          list = autoSuggest.getSuggestions(word)
          dlm.removeAllElements()
          list.foreach(dlm.addElement(_))
          if (!list.isEmpty) {
            lastSuggested = ""
            window.validate()
            window.setVisible(true)
            // Required to keep the caret in the editorArea visible
            eA.getCaret.setVisible(true)
            suggestionDisplaylist.setSelectedIndex(0)
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
  def isVisible() = window.isVisible
}

/**
  * This is the renderer to render elements of the list displayed
  * in the suggestion box.
  *
  * @param editorArea
  */
class SuggestionListRenderer(editorArea: EditorArea) extends ListCellRenderer[String]{

  override def getListCellRendererComponent(list: JList[_ <: String], value: String, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
    val label = new JLabel(value.asInstanceOf[String])
    label.setForeground(if (DefaultTokenMapper.getCommand(value.asInstanceOf[String]).isEmpty) SyntaxColors.COMMAND_COLOR
    else SyntaxColors.REPORTER_COLOR)
    label.setBackground(if(isSelected || cellHasFocus) new Color(0xEEAEEE) else Color.white)
    label.setOpaque(true)
    label.setBorder(BorderFactory.createEmptyBorder())
    label.setFont(editorArea.getFont)
    label
  }
}