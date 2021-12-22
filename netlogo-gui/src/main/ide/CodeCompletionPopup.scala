// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.{Color, Component, Dimension, Font, GraphicsEnvironment}
import java.awt.event._
import javax.swing._
import javax.swing.event.DocumentEvent
import javax.swing.text.JTextComponent

import org.nlogo.awt.Fonts
import org.nlogo.core.{Dialect, Femto, NetLogoCore, Token, TokenType, TokenizerInterface}
import org.nlogo.nvm.ExtensionManager
import org.nlogo.window.SyntaxColors

object CodeCompletionPopup {
  def apply() =
    new CodeCompletionPopup(AutoSuggest(NetLogoCore, None))
  def apply(dialect: Dialect) =
    new CodeCompletionPopup(AutoSuggest(dialect, None),
      new SuggestionListRenderer(dialect, None))
  def apply(dialect: Dialect, extensionManager: ExtensionManager) =
    new CodeCompletionPopup(AutoSuggest(dialect, Some(extensionManager)),
      new SuggestionListRenderer(dialect, Some(extensionManager)))
}

case class CodeCompletionPopup(autoSuggest: AutoSuggest,
  listRenderer: SuggestionListRenderer = new SuggestionListRenderer(NetLogoCore, None)) {
  // To control when the popup has to automatically show
  var isPopupEnabled = false
  // To store the last value user selected
  var lastSuggested = ""
  val dlm = new DefaultListModel[String]()
  val suggestionDisplaylist = new JList[String](dlm)
  val scrollPane = new JScrollPane(suggestionDisplaylist)
  val window = new JDialog()
  window.setUndecorated(true)
  window.add(scrollPane)
  window.setMinimumSize(new Dimension(150, 210))
  var editorArea: Option[JTextComponent] = None

  scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)

  def init(editorArea: JTextComponent, autoSuggestDocumentListener: AutoSuggestDocumentListener): Unit = {
    if(this.editorArea.isDefined)
      return
    this.editorArea = Some(editorArea)
    for {eA <- this.editorArea} {
      suggestionDisplaylist.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
      suggestionDisplaylist.setLayoutOrientation(JList.VERTICAL)
      suggestionDisplaylist.setVisibleRowCount(10)
      listRenderer.font = eA.getFont
      suggestionDisplaylist.setCellRenderer(listRenderer)

      suggestionDisplaylist.addMouseListener(new MouseAdapter {
        override def mouseClicked(e: MouseEvent): Unit = {
          autoCompleteSuggestion(eA, autoSuggestDocumentListener)
        }
      })

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
              autoCompleteSuggestion(eA, autoSuggestDocumentListener)
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
    * Removes the currently typed token and replaces it with the selected element from
    * the list of suggestions
    *
    * @param eA
    * @param autoSuggestDocumentListener
    */
  def autoCompleteSuggestion(eA: JTextComponent, autoSuggestDocumentListener: AutoSuggestDocumentListener) {
    isPopupEnabled = false
    val suggestion = suggestionDisplaylist.getSelectedValue
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
  }

  /**
    * Makes the suggestion box initially and displays it.
    * Should only be called when the user presses the shortcut to
    * enable the suggestion. To update the suggestions list call showPopUp()
    */
  def displayPopup(): Unit = {
    for{eA <- editorArea} {
      isPopupEnabled = true
      autoSuggest.refresh()
      val tokenOption = getTokenTillPosition(eA.getText(), eA.getCaretPosition)
      val position = tokenOption.map(_.start).getOrElse(eA.getCaretPosition)
      fireUpdatePopup(None)
      placeWindowOnScreen(eA, position)
    }
  }

  /**
    * Sets the location at which the window will be displayed
    * @param eA
    * @param position
    */
  def placeWindowOnScreen(eA: JTextComponent, position: Int): Unit = {
    val screenHeight = GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice.getDisplayMode.getHeight
    if (window.getSize.height + eA.getLocationOnScreen.y + eA.modelToView2D(position).getY +
      eA.getFont.getSize > screenHeight) {
      window.setLocation(eA.getLocationOnScreen.x + eA.modelToView2D(position).getX.toInt,
        (eA.getLocationOnScreen.y + eA.modelToView2D(position).getY - window.getSize.getHeight).toInt)
    } else {
      window.setLocation(eA.getLocationOnScreen.x + eA.modelToView2D(position).getX.toInt,
        eA.getLocationOnScreen.y + eA.modelToView2D(position).getY.toInt + eA.getFont.getSize)
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
      dlm.removeAllElements()
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
              list.foreach(dlm.addElement(_))
              if (!list.isEmpty) {
                lastSuggested = ""
                window.validate()
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
            isPopupEnabled = false
        }
      }
      window.setVisible(!dlm.isEmpty)
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
class SuggestionListRenderer(dialect: Dialect, extensionManager: Option[ExtensionManager]) extends ListCellRenderer[String]{

  var font: Font = Fonts.monospacedFont

  override def getListCellRendererComponent(list: JList[_ <: String], value: String, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
    val label = new JLabel(value)

    val fgColor =
      if (dialect.tokenMapper.getCommand(value).nonEmpty ||
        extensionManager.flatMap(_.cachedType(value)).contains(TokenType.Command))
        SyntaxColors.COMMAND_COLOR
      else
        SyntaxColors.REPORTER_COLOR
    label.setForeground(fgColor)
    label.setBackground(if(isSelected || cellHasFocus) new Color(0xEEAEEE) else Color.white)
    label.setOpaque(true)
    label.setBorder(BorderFactory.createEmptyBorder())
    label.setFont(font)
    label
  }
}
