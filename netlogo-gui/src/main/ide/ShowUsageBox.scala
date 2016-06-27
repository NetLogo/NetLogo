// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.Color
import java.awt.event._
import javax.swing.table.{DefaultTableCellRenderer, DefaultTableModel}
import javax.swing.{JDialog, JScrollPane, JTable}

import org.nlogo.api.CompilerServices
import org.nlogo.core.{Femto, Token, TokenizerInterface}
import org.nlogo.editor.EditorArea

import scala.util.parsing.combinator.token.Tokens

class ShowUsageBox (){
  val usageBox = new JDialog()
  val dataModel = new DefaultTableModel(){
    override def isCellEditable(row: Int, column: Int): Boolean = false
    override def getColumnClass(columnIndex: Int): Class[_] = {
      if(columnIndex == 0) classOf[Token] else super.getColumnClass(columnIndex)
    }
  }
  val usageTable = new JTable(dataModel)
  val scrollPane = new JScrollPane(usageTable)
  var editorArea: EditorArea = null
  usageBox.add(scrollPane)
  usageBox.setUndecorated(true)
  usageTable.setDefaultRenderer(classOf[Token], new LineNumberRenderer())

  def init(editorArea: EditorArea): Unit = {
    if(this.editorArea != null){
      return
    }
    this.editorArea = editorArea
    usageBox.addWindowFocusListener(new WindowFocusListener {
      override def windowLostFocus(e: WindowEvent): Unit = {
        usageBox.setVisible(false)
        dataModel.synchronized(dataModel.setRowCount(0))
      }

      override def windowGainedFocus(e: WindowEvent): Unit = {}
    })
    dataModel.setColumnCount(2)
    usageTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION)
    usageTable.getColumnModel.getColumn(0).setMaxWidth(40)
    usageTable.getColumnModel.setColumnSelectionAllowed(false)
    usageTable.getTableHeader().setReorderingAllowed(false)
    usageTable.setTableHeader(null)
    usageTable.setRowMargin(3)
    usageTable.setRowHeight(20)
    usageTable.getColumnModel.getColumn(1).setMinWidth(200)
    usageTable.getColumnModel.getColumn(0).setMinWidth(40)
    usageTable.setFont(editorArea.getFont)

    usageTable.addMouseListener(new MouseAdapter() {
      override def mouseClicked(e: MouseEvent): Unit = {
        usageBox.setVisible(false)
        val token = usageTable.getValueAt(usageTable.getSelectedRow, 0).asInstanceOf[Token]
        dataModel.synchronized(dataModel.setRowCount(0))
        editorArea.setCaretPosition(token.start)
      }
    })

    usageTable.addKeyListener(new KeyListener(){
      override def keyPressed(e: KeyEvent): Unit = {
        e.getKeyCode match {
          case KeyEvent.VK_ESCAPE => usageBox.setVisible(false)
          case _ => e.setSource(editorArea)
            editorArea.dispatchEvent(e)
        }
      }

      override def keyTyped(e: KeyEvent): Unit = {}

      override def keyReleased(e: KeyEvent): Unit = {}
    })
  }

  def showBox(me: MouseEvent, position: Int): Unit = {
    val tokenOption = findTokenContainingPosition(editorArea.getText(), position)
    for {token <- tokenOption} {
      val tokens = getUsage(editorArea.getText(), token)
      dataModel.setRowCount(0)
      for (t <- tokens) {
        dataModel.addRow(Array[AnyRef](t, editorArea.getLineText(t.start).trim))
      }
      if(dataModel.getRowCount != 0) {
        usageTable.setPreferredScrollableViewportSize(usageTable.getPreferredSize())
        usageTable.setFillsViewportHeight(true)
        usageBox.setSize(usageTable.getPreferredSize)
        usageTable.validate()
        usageBox.setLocation(me.getLocationOnScreen)
        usageBox.setVisible(true)
      }
    }
  }

  def getUsage(source: String, token: Token): Iterator[Token] = {
    val iterator = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer").tokenizeString(source)
    iterator.filter(_.text.equalsIgnoreCase(token.text))
  }

  def findTokenContainingPosition(source: String, position: Int): Option[Token] = {
    val iterator = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer").tokenizeString(source)
    iterator.find(p => p.start < position && p.end >= position)
  }
  class LineNumberRenderer extends DefaultTableCellRenderer {
    override def setValue(value: AnyRef) = {
      setText(editorArea.offsetToLine(value.asInstanceOf[Token].start).toString)
    }
  }
}
