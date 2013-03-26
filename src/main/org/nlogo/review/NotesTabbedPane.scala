package org.nlogo.review

import java.awt.BorderLayout

import org.nlogo.swing.RichJButton

import javax.swing.{ AbstractCellEditor, JButton, JPanel, JScrollPane, JTabbedPane, JTable, JTextArea }
import javax.swing.table.{ AbstractTableModel, TableCellEditor, TableCellRenderer }
import scala.language.existentials

class NotesTabbedPane(tabState: ReviewTabState) extends JTabbedPane {
  val indexedNotes = new IndexedNotesTable(tabState)
  val generalNotes = new GeneralNotesTextArea(tabState)
  addTab("Indexed notes", new IndexedNotePanel(indexedNotes))
  addTab("General notes", new JScrollPane(generalNotes))
}

class IndexedNotePanel(table: IndexedNotesTable) extends JPanel {
  setLayout(new BorderLayout)
  add(new JScrollPane(table), BorderLayout.CENTER)
  val buttonPanel = new JPanel()
  buttonPanel.add(new AddNoteButton(table))
  add(buttonPanel, BorderLayout.SOUTH)
}

class GeneralNotesTextArea(tabState: ReviewTabState) extends JTextArea("") {
  setLineWrap(true)
  setRows(3)
  override def setText(text: String) {
    for { // TODO this should instead be listening to (not yet implemented) "run change" events
      run <- tabState.currentRun
      if getText != run.generalNotes
    } super.setText(text)
  }
}

class AddNoteButton(table: IndexedNotesTable)
  extends JButton(new ReviewAction("Add note", "add", () => table.addNote))
// TODO disable button when no current run

case class Note(frame: Int, ticks: Double, text: String = "") extends Ordered[Note] {
  def compare(that: Note) = frame.compareTo(that.frame)
}

case class Column(
  val name: String,
  val minWidth: Option[Int],
  val maxWidth: Option[Int],
  val editable: Boolean,
  val clazz: Class[_],
  val getValue: Note => AnyRef,
  val updatedValue: (AnyRef, Note) => Note)

class IndexedNotesTable(tabState: ReviewTabState) extends JTable { table =>

  val buttonsColumnName = "buttons"
  val columns = List(
    Column("Frame", Some(50), Some(50), false,
      classOf[java.lang.Integer],
      note => Int.box(note.frame),
      (value, note) => note.copy(frame = value.asInstanceOf[Int])),
    Column("Ticks", Some(50), Some(50), false,
      classOf[java.lang.Double],
      note => Double.box(note.ticks),
      (value, note) => note.copy(ticks = value.asInstanceOf[Double])),
    Column("Notes", Some(200), None, true,
      classOf[String], _.text,
      (value, note) => note.copy(text = value.asInstanceOf[String])),
    Column(buttonsColumnName, Some(105), Some(105), false,
      classOf[Unit],
      _ => Unit,
      (value, note) => note)
  )

  val model = new NotesTableModel(columns)

  def setTo(notes: List[Note]) {
    model.notes.clear();
    model.notes ++= notes
  }

  def scrollTo(frame: Int) {
    val rowIndex = notes.indexWhere(_.frame == frame)
    if (rowIndex != -1)
      table.scrollRectToVisible(table.getCellRect(rowIndex, 0, false))
  }

  locally {
    setModel(model)

    setRowHeight(getRowHeight + 20)
    setGridColor(java.awt.Color.DARK_GRAY)
    setShowGrid(true)
    setRowSelectionAllowed(false)

    for {
      column <- columns
      tablecolumn = getColumn(column.name)
    } {
      column.minWidth.foreach(tablecolumn.setMinWidth)
      column.maxWidth.foreach(tablecolumn.setMaxWidth)
    }

    val buttonsColumn = getColumn(buttonsColumnName)
    buttonsColumn.setCellRenderer(new ButtonCellEditor)
    buttonsColumn.setCellEditor(new ButtonCellEditor)
    buttonsColumn.setHeaderValue("")
  }

  def notes: List[Note] = model.notes.toList

  def addNote {
    // TODO figure out what's happening at frame 0
    // TODO maybe should not add if existing note at position?
    for {
      ticks <- tabState.currentTicks
      frame <- tabState.currentFrameIndex
    } model.addNote(Note(frame, ticks, ""))
  }

  // someone pressed the delete button in the notes row.
  def removeNote(index: Int) { model.removeNote(index) }

  def openAdvancedNoteEditor(editingNote: Note) {
    //      val p = new NoteEditorAdvanced(editingNote)
    //      new org.nlogo.swing.Popup(frame, I18N.gui("editing") + " " + editingNote.name, p, (), {
    //        p.getResult match {
    //          case Some(p) =>
    //            model.notes(getSelectedRow) = p
    //            table.removeEditor()
    //            table.repaint()
    //            true
    //          case _ => false
    //        }
    //      }, I18N.gui.get _).show()
  }

  // renders the delete and edit buttons for each column
  class ButtonCellEditor extends AbstractCellEditor with TableCellRenderer with TableCellEditor {
    val editButton = RichJButton(new javax.swing.ImageIcon(getClass.getResource("/images/edit.gif"))) {
      openAdvancedNoteEditor(model.notes(getSelectedRow))
    }
    val deleteButton = RichJButton(new javax.swing.ImageIcon(getClass.getResource("/images/delete.gif"))) {
      val index = getSelectedRow
      removeEditor()
      clearSelection()
      removeNote(index)
    }
    editButton.putClientProperty("JComponent.sizeVariant", "small")
    deleteButton.putClientProperty("JComponent.sizeVariant", "small")
    val buttonPanel = new JPanel {
      add(editButton)
      add(deleteButton)
    }
    def getTableCellRendererComponent(table: JTable, value: Object,
      isSelected: Boolean, hasFocus: Boolean,
      row: Int, col: Int) = buttonPanel
    def getTableCellEditorComponent(table: JTable, value: Object,
      isSelected: Boolean, row: Int, col: Int) = buttonPanel
    def getCellEditorValue = ""
  }

  class NotesTableModel(columns: Seq[Column]) extends AbstractTableModel {

    var notes = scala.collection.mutable.ListBuffer[Note]()

    override def getColumnCount = columns.length
    override def getRowCount = notes.length
    override def getColumnName(col: Int) = columns(col).name
    override def isCellEditable(row: Int, col: Int) = columns(col).editable
    override def getValueAt(row: Int, col: Int) = columns(col).getValue(notes(row))
    override def getColumnClass(col: Int): Class[_] = columns(col).clazz

    override def setValueAt(value: Object, row: Int, col: Int) {
      if (row < notes.size) {
        val note = notes(row)
        notes(row) = columns(col).updatedValue(value, note)
        fireTableCellUpdated(row, col)
      }
    }

    def addNote(n: Note) {
      notes += n;
      notes = notes.sorted
      fireTableDataChanged()
    }

    def removeNote(index: Int) {
      if (index != -1) {
        notes.remove(index)
        fireTableRowsDeleted(index, index)
        removeEditor()
        revalidate()
        repaint()
      }
    }
  }
}
