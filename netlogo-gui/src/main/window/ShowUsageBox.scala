package window

import java.awt.event.{KeyEvent, KeyListener, MouseEvent, MouseListener}
import javax.swing.table.{AbstractTableModel, DefaultTableModel}
import javax.swing.{JDialog, JFrame, JScrollPane, JTable}

import org.nlogo.api.CompilerServices
import org.nlogo.app.ProceduresMenuTarget
import org.nlogo.editor.EditorArea
import org.nlogo.parse.StructureParser

class ShowUsageBox (compilerServices: CompilerServices){
  val usageBox = new JDialog()
  val dataModel = new DefaultTableModel(){
    override def isCellEditable(row: Int, column: Int): Boolean = false
  }
  val usageTable = new JTable(dataModel)
  val scrollPane = new JScrollPane(usageTable)
  var editorArea = null
  usageBox.add(scrollPane)
  usageBox.setUndecorated(true)
  dataModel.setColumnCount(2)
  usageTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION)
  usageTable.getColumnModel.getColumn(0).setMaxWidth(30)
  usageTable.addMouseListener(new MouseListener(){
    override def mouseClicked(e: MouseEvent): Unit = {}

    override def mouseExited(e: MouseEvent): Unit = {}

    override def mouseEntered(e: MouseEvent): Unit = {}

    override def mousePressed(e: MouseEvent): Unit = {}

    override def mouseReleased(e: MouseEvent): Unit = {
      usageTable.getValueAt(usageTable.getSelectedRow, 1)
    }
  })
  usageTable.addKeyListener(new KeyListener(){
    override def keyPressed(e: KeyEvent): Unit = {
      e.getKeyCode match {
        case KeyEvent.VK_ESCAPE => usageBox.setVisible(false)
      }
    }

    override def keyTyped(e: KeyEvent): Unit = {}

    override def keyReleased(e: KeyEvent): Unit = {}
  })

  def init(editorArea: EditorArea): Unit ={

  }
  def showBox(me: MouseEvent, target: ProceduresMenuTarget, position: Int, editorArea: EditorArea): Unit = {
    dataModel.synchronized(dataModel.setRowCount(0))
    val token = compilerServices.getTokenAtPosition(target.getText, position)
    val tokens = compilerServices.getUsage(target.getText, compilerServices.getTokenAtPosition(target.getText, position))
    for(t <- tokens) {
      dataModel.addRow(Array[AnyRef](int2Integer(editorArea.offsetToLine(t.start)), editorArea.getLineText(t.start).trim))
    }
    usageBox.setVisible(true)
    usageBox.setLocation(me.getLocationOnScreen)
  }
}
