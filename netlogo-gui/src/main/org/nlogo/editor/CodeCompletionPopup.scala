package org.nlogo.editor

import java.awt.{Color, Component, Dimension, Window}
import java.awt.event._
import javax.swing._

import org.nlogo.api.CompilerServices
import org.nlogo.core.DefaultTokenMapper
import org.nlogo.window.AutoSuggest

case class CodeCompletionPopup(compiler: CompilerServices) {
  var isPopupEnabled = false
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
//  window.setAlwaysOnTop(true)

//  window.setType(javax.swing.JFrame.Type.POPUP)
  scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)

  def init(editorArea: EditorArea): Unit = {
    this.editorArea = editorArea
    suggestionDisplaylist.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
    suggestionDisplaylist.setLayoutOrientation(JList.VERTICAL)
    suggestionDisplaylist.setVisibleRowCount(10)
//    suggestionDisplaylist.setCellRenderer(new SuggestionListRenderer())

    suggestionDisplaylist.addKeyListener(new KeyListener {
      override def keyTyped(e: KeyEvent): Unit = {
        e.getKeyChar match {
          case java.awt.event.KeyEvent.VK_ESCAPE => isPopupEnabled = false
            window.setVisible(false)
          case java.awt.event.KeyEvent.VK_BACK_SPACE => val position = editorArea.getCaretPosition
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
                      doc.replace(position - token.text.length, token.text.length, suggestion, null)
                      editorArea.setCaretPosition(position - token.text.length + suggestion.length)
            isPopupEnabled = false
          case _ => e.setSource(editorArea)
            editorArea.dispatchEvent(e)
        }
      }

      override def keyPressed(e: KeyEvent): Unit = {}

      override def keyReleased(e: KeyEvent): Unit = {}
    })
  }
  def enablePopup(): Unit = {
    isPopupEnabled = true
    showPopup()
    val position = editorArea.getCaretPosition
    window.setLocation(editorArea.getLocationOnScreen.x + editorArea.modelToView(position + 1).x,
      editorArea.getLocationOnScreen.y + editorArea.modelToView(position).y + editorArea.getFont.getSize)
  }

  def showPopup(): Unit = {
    if(isPopupEnabled) {
      val token = compiler.getTokenAtPosition(editorArea.getText(), editorArea.getCaretPosition)
      var list = Seq[String]()
      if (token != null && !token.text.equals(lastSuggested)) {
        list = autoSuggest.getSuggestions(token.text)
        dlm.removeAllElements()
        var i = 0
        list.foreach(dlm.addElement(_))
        if (!list.isEmpty) {
          lastSuggested = ""
          window.validate()
          window.setVisible(true)
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

//class SuggestionListRenderer extends DefaultListCellRenderer {
//  override def getListCellRendererComponent(list: JList[_], value: Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
//    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
//    this.setForeground(if (DefaultTokenMapper.getCommand(value.asInstanceOf[String]) == null) Color.BLUE
//    else Color.PINK)
//    this
//  }
//}