// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.{Color, Component, Cursor}
import java.awt.event._
import javax.swing.table.{DefaultTableCellRenderer, DefaultTableModel, TableCellRenderer}
import javax.swing._

import org.nlogo.core.{Femto, Token, TokenType, TokenizerInterface}
import org.nlogo.editor.{EditorArea, HighlightEditorKit}

class ShowUsageBox(editorArea: EditorArea) {
  val usageBox = new JDialog()
  val dataModel = new DefaultTableModel(){
    override def isCellEditable(row: Int, column: Int): Boolean = false
    override def getColumnClass(columnIndex: Int): Class[_] = {
      if(columnIndex == 0) classOf[Token] else classOf[String]
    }
  }
  val usageTable = new JTable(dataModel)
  val scrollPane = new JScrollPane(usageTable)
  usageBox.add(scrollPane)
  usageBox.setUndecorated(true)
  usageTable.setDefaultRenderer(classOf[Token], new LineNumberRenderer())
  usageTable.setDefaultRenderer(classOf[String], new LineRenderer(None))

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
      editorArea.select(token.start, token.end)
    }
  })

  usageTable.addKeyListener(new KeyAdapter(){
    override def keyPressed(e: KeyEvent): Unit = {
      e.getKeyCode match {
        case KeyEvent.VK_ESCAPE => usageBox.setVisible(false)
        case _ => e.setSource(editorArea)
          editorArea.dispatchEvent(e)
      }
    }
  })

  usageTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))

  def showBox(me: MouseEvent, position: Int): Unit = {
    val tokenOption = findTokenContainingPosition(editorArea.getText(), position)
    for {token <- tokenOption} {
      if(token.tpe == TokenType.Ident || token.tpe == TokenType.Command || token.tpe == TokenType.Reporter) {
        val tokens = getUsage(editorArea.getText(), token)
        dataModel.setRowCount(0)
        for (t <- tokens) {
          dataModel.addRow(Array[AnyRef](t, editorArea.getLineText(t.start).trim))
        }
        if (dataModel.getRowCount != 0) {
          usageTable.setDefaultRenderer(classOf[String], new LineRenderer(Some(token.text)))
          usageTable.setPreferredScrollableViewportSize(usageTable.getPreferredSize())
          usageTable.setFillsViewportHeight(true)
          usageBox.setSize(usageTable.getPreferredSize)
          usageTable.validate()
          usageBox.setLocation(me.getLocationOnScreen)
          usageBox.setVisible(true)
        }
      }
    }
  }

  def getUsage(source: String, token: Token): Iterator[Token] = {
    val iterator = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer").tokenizeString(source)
    iterator.filter(t => t.text.equalsIgnoreCase(token.text) && t.start != token.start)
  }

  def findTokenContainingPosition(source: String, position: Int): Option[Token] = {
    val iterator = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer").tokenizeString(source)
    iterator.find(p => p.start < position && p.end >= position)
  }

  class LineRenderer(boldedString: Option[String]) extends TableCellRenderer {
    override def getTableCellRendererComponent(table: JTable, value: AnyRef, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {
      val pane  = new JEditorPane()
      pane.setOpaque(true)
      pane.setBorder(BorderFactory.createEmptyBorder(1,0,0,0))
      val editorKit = boldedString match {
        case None => new HighlightEditorKit(null, editorArea.colorizer)
        case Some(selectedString) => new BoldEditorKit(selectedString)
      }
      pane.setEditorKit(editorKit)
      pane.setText(value.asInstanceOf[String])
      val alternate = UIManager.getColor("Table.alternateRowColor")
      val defaults = new UIDefaults()
      if (row%2 == 0){
        defaults.put("EditorPane[Enabled].backgroundPainter", Color.WHITE)
        pane.setBackground(Color.WHITE)
      }
      else {
        defaults.put("EditorPane[Enabled].backgroundPainter", alternate)
        pane.setBackground(alternate)
      }
      if(isSelected) {
        pane.setBackground(usageTable.getSelectionBackground)
        pane.setForeground(usageTable.getSelectionForeground)
      }
      pane.putClientProperty("Nimbus.Overrides", defaults)
      pane.putClientProperty("Nimbus.Overrides.InheritDefaults", true)
      pane
    }
  }
  class LineNumberRenderer extends DefaultTableCellRenderer {
    override def setValue(value: AnyRef) = {
      setText(editorArea.offsetToLine(value.asInstanceOf[Token].start).toString)
    }
  }
  import org.nlogo.editor.{ HighlightEditorKit, HighlightView }
  class BoldEditorKit(selectedString: String) extends HighlightEditorKit(null, editorArea.colorizer) {
    import scala.collection.immutable.Range
    class BoldView(elem: javax.swing.text.Element) extends HighlightView(this.pane, elem, this.colorizer) {
      var boldingRange = Option.empty[Range]
      override def studyLine(lineIndex: Int): Unit = {
        super.studyLine(lineIndex)
        val elem = getElement.getElement(lineIndex)
        val lineText = getDocument.getText(elem.getStartOffset, elem.getEndOffset - elem.getStartOffset max 0)
        val beginningIndex = lineText.toUpperCase.indexOf(selectedString.toUpperCase)
        if (beginningIndex != -1)
          boldingRange = Some(beginningIndex to (beginningIndex + selectedString.length))
      }

      override def drawText(g: java.awt.Graphics, x: Int, y: Int, p0: Int, p1: Int, isSelected: Boolean): Int = {
        boldingRange match {
          case Some(range) =>
            val midX = super.drawText(g, x, y, p0, range.start, isSelected)
            val originalFont = g.getFont
            val boldFont = originalFont.deriveFont(java.awt.Font.BOLD)
            g.setFont(boldFont)
            val endX = super.drawText(g, midX, y, range.start, range.end, isSelected)
            g.setFont(originalFont)
            super.drawText(g, endX, y, range.end, p1, isSelected)
          case None => super.drawText(g, x, y, p0, p1, isSelected)
        }
      }
    }

    override def create(elem: javax.swing.text.Element): javax.swing.text.View = new BoldView(elem)
  }
}
