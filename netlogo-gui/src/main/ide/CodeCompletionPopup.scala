// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.{Color, Component, Dimension}
import java.awt.event._
import javax.swing._
import javax.swing.event.DocumentEvent

import org.nlogo.core.{DefaultTokenMapper, Femto, Token, TokenizerInterface}
import org.nlogo.window.SyntaxColors
import org.nlogo.editor.EditorArea

case class CodeCompletionPopup() {
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
              if (position != 0) {
                eA.setCaretPosition(position - 1)
                val doc = eA.getDocument.asInstanceOf[javax.swing.text.PlainDocument]
                doc.remove(position - 1, 1)
              }
            case java.awt.event.KeyEvent.VK_ENTER =>
              isPopupEnabled = false
              var suggestion = suggestionDisplaylist.getSelectedValue
              lastSuggested = suggestion
              val tokenOption = getTokenTillPosition(eA.getText(), eA.getCaretPosition)
              for(token <- tokenOption) {
                val position = eA.getCaretPosition
                val doc = eA.getDocument.asInstanceOf[javax.swing.text.PlainDocument]
                doc.removeDocumentListener(autoSuggestDocumentListener)
                doc.replace(token.start, position - token.start, suggestion, null)
                eA.setCaretPosition(token.start + suggestion.length)
                window.setVisible(false)
              }
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
      val tokenOption = getTokenTillPosition(eA.getText(), eA.getCaretPosition)
      val position = tokenOption.map(_.start).getOrElse(eA.getCaretPosition)
      window.setLocation(eA.getLocationOnScreen.x + eA.modelToView(position).x,
        eA.getLocationOnScreen.y + eA.modelToView(position).y + eA.getFont.getSize)
      fireUpdatePopup(None)
    }
  }

  /**
    * Should be called to update the suggestion box
    *
    * @param docEventOption
    */
  def fireUpdatePopup(docEventOption: Option[DocumentEvent]): Unit = {
    for{eA <- editorArea} {
      var position = eA.getCaretPosition
      for {docEvent <- docEventOption} {
        docEvent.getType match {
          // Trying to fix the behavior of getCaretPosition which is not returning the correct position
          case DocumentEvent.EventType.INSERT => position += 1
          case _ =>
        }
      }
      updatePopup(position)
    }
  }

  /**
    * Updates the list of the suggestion box and makes it visible.
    */
  def updatePopup(position: Int): Unit = {
    for{eA <- editorArea} {
      if (isPopupEnabled ) {
        val tokenOption = getTokenTillPosition(eA.getText(), position)
        var list = Seq[String]()
        tokenOption match {
          case Some(token) =>
            // word only contains the current token till the cursor position
            val word = token.text.substring(0, position - token.start)
            // popup not to be diplayed after user hits the enter key
            if (!token.text.equals(lastSuggested)) {
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
          case None => window.setVisible(false)
        }
      }
    }
  }

  def getTokenTillPosition(source: String, position: Int): Option[Token] = {
    val iterator = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer").tokenizeString(source)
    iterator.find(p => p.start < position && p.end >= position)
  }
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
    label.setForeground(if (DefaultTokenMapper.getCommand(value.asInstanceOf[String]).isEmpty) SyntaxColors.REPORTER_COLOR
    else SyntaxColors.COMMAND_COLOR)
    label.setBackground(if(isSelected || cellHasFocus) new Color(0xEEAEEE) else Color.white)
    label.setOpaque(true)
    label.setBorder(BorderFactory.createEmptyBorder())
    label.setFont(editorArea.getFont)
    label
  }
}