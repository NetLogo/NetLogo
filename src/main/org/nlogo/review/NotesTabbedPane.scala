package org.nlogo.review

import scala.language.existentials
import java.awt.BorderLayout
import org.nlogo.mirror.IndexedNote
import org.nlogo.swing.Implicits.thunk2action
import org.nlogo.swing.RichJButton
import javax.swing.{
  AbstractCellEditor,
  JButton,
  JDialog,
  JFrame,
  JPanel,
  JScrollPane,
  JTabbedPane,
  JTable,
  JTextArea,
  SwingUtilities
}
import javax.swing.table.{
  AbstractTableModel,
  TableCellEditor,
  TableCellRenderer
}

class NotesTabbedPane(tabState: ReviewTabState) extends JTabbedPane {
  val indexedNotesTable = new IndexedNotesTable(tabState)
  val addNoteButton = new AddNoteButton(tabState, indexedNotesTable)
  val indexedNotesPanel = new IndexedNotesPanel(indexedNotesTable, addNoteButton)
  val generalNotes = new GeneralNotesTextArea(tabState)
  addTab("Indexed notes", indexedNotesPanel)
  addTab("General notes", new JScrollPane(generalNotes))
}

class IndexedNotesPanel(indexedNotesTable: IndexedNotesTable, addNoteButton: AddNoteButton)
  extends JPanel {
  setLayout(new BorderLayout)
  add(new JScrollPane(indexedNotesTable), BorderLayout.CENTER)
  val buttonPanel = new JPanel()
  buttonPanel.add(addNoteButton)
  add(buttonPanel, BorderLayout.WEST)
}

class GeneralNotesTextArea(val hasCurrentRun: HasCurrentRun)
  extends JTextArea("")
  with EnabledWithCurrentRun {
  setLineWrap(true)
  setRows(3)

  override def notify(pub: HasCurrentRun#Pub, event: CurrentRunChangeEvent) {
    super.notify(pub, event) // enabled with current run
    setText(event.newRun.map(_.generalNotes).getOrElse(""))
  }
}

class AddNoteButton(val hasCurrentRun: HasCurrentRun, table: IndexedNotesTable)
  extends JButton(new ReviewAction("Add note", "add", () => table.addNote))
  with EnabledWithCurrentRun

case class Column(
  val name: String,
  val minWidth: Option[Int],
  val maxWidth: Option[Int],
  val editable: Boolean,
  val clazz: Class[_],
  val getValue: IndexedNote => AnyRef,
  val updatedValue: (AnyRef, IndexedNote) => IndexedNote)

class IndexedNotesTable(tabState: ReviewTabState) extends JTable { table =>

  putClientProperty("terminateEditOnFocusLost", Boolean.box(true))

  val buttonsColumnName = "buttons"
  val notesColumnName = "Notes"
  val columns = List(
    Column("Frame", Some(50), Some(50), false,
      classOf[java.lang.Integer],
      note => Int.box(note.frame),
      (value, note) => note.copy(frame = value.asInstanceOf[Int])),
    Column("Ticks", Some(50), Some(50), false,
      classOf[java.lang.Double],
      note => Double.box(note.ticks),
      (value, note) => note.copy(ticks = value.asInstanceOf[Double])),
    Column(notesColumnName, Some(200), None, true,
      classOf[String], _.text,
      (value, note) => note.copy(text = value.asInstanceOf[String])),
    Column(buttonsColumnName, Some(105), Some(105), true,
      classOf[Unit],
      _ => Unit,
      (value, note) => note)
  )

  val model = new NotesTableModel(tabState, columns)

  def scrollTo(frame: Int) {
    val rowIndex = model.notes.indexWhere(_.frame == frame)
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
    val buttonCellEditor = new ButtonCellEditor
    buttonsColumn.setCellRenderer(buttonCellEditor)
    buttonsColumn.setCellEditor(buttonCellEditor)
    buttonsColumn.setHeaderValue("")

  }

  def addNote {
    // TODO figure out what's happening at frame 0
    for {
      ticks <- tabState.currentTicks
      frame <- tabState.currentFrameIndex
    } {
      if (!model.notes.exists(_.frame == frame))
        model.addNote(IndexedNote(frame, ticks, ""))
      scrollTo(frame)
      val row = model.notes.indexWhere(_.frame == frame)
      val col = columns.indexWhere(_.name == notesColumnName)
      editCellAt(row, col)
      transferFocus()
    }
  }

  // someone pressed the delete button in the notes row.
  def removeNote(index: Int) { model.removeNote(index) }

  def editNote(oldNote: IndexedNote): IndexedNote = {
    var newNote = oldNote
    val parent = SwingUtilities.getAncestorOfClass(classOf[JFrame], this).asInstanceOf[JFrame]
    val dialog = new JDialog(parent,
      s"Editing Note at frame ${oldNote.frame}, ticks: ${oldNote.ticks}",
      true // modal
    )
    val textArea = new JTextArea(oldNote.text)
    textArea.setLineWrap(true)
    textArea.setWrapStyleWord(true)
    val okButton = new JButton("OK")
    okButton.addActionListener(() => {
      newNote = oldNote.copy(text = textArea.getText)
      dialog.dispose()
    })
    val cancelButton = new JButton("Cancel")
    cancelButton.addActionListener(() => dialog.dispose())
    val buttonPanel = new JPanel()
    Seq(okButton, cancelButton).foreach(buttonPanel.add)
    dialog.setLayout(new BorderLayout)
    dialog.add(new JScrollPane(textArea), BorderLayout.CENTER)
    dialog.add(buttonPanel, BorderLayout.SOUTH)
    dialog.setSize(500, 300)
    dialog.setLocationRelativeTo(parent)
    dialog.setVisible(true)
    newNote
  }

  // renders the delete and edit buttons for each column
  class ButtonCellEditor extends AbstractCellEditor with TableCellRenderer with TableCellEditor {
    val editButton = RichJButton(new javax.swing.ImageIcon(getClass.getResource("/images/edit.gif"))) {
      val oldNote = model.notes(getSelectedRow)
      val newNote = editNote(oldNote)
      if (newNote != oldNote) {
        model.removeNote(getSelectedRow)
        model.addNote(newNote)
      }
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
    override def getTableCellRendererComponent(table: JTable, value: Object,
      isSelected: Boolean, hasFocus: Boolean,
      row: Int, col: Int) = buttonPanel
    override def getTableCellEditorComponent(table: JTable, value: Object,
      isSelected: Boolean, row: Int, col: Int) = buttonPanel
    override def getCellEditorValue = ""
  }

  // TODO: the model should probably not be an inner class
  class NotesTableModel(val hasCurrentRun: HasCurrentRun, columns: Seq[Column])
    extends AbstractTableModel
    with HasCurrentRun#Sub {
    hasCurrentRun.subscribe(this)

    override def notify(pub: HasCurrentRun#Pub, event: CurrentRunChangeEvent) {
      event match {
        case BeforeCurrentRunChangeEvent(_, _) if (isEditing) =>
          getCellEditor.stopCellEditing()
        case _ =>
      }
      _notes.clear()
      _notes ++= event.newRun.toList.flatMap(_.indexedNotes)
      removeEditor()
      revalidate()
      repaint()
    }

    private var _notes = scala.collection.mutable.ListBuffer[IndexedNote]()
    def notes = _notes.toList

    override def getColumnCount = columns.length
    override def getRowCount = _notes.length
    override def getColumnName(col: Int) = columns(col).name
    override def isCellEditable(row: Int, col: Int) = columns(col).editable
    override def getValueAt(row: Int, col: Int) = columns(col).getValue(notes(row))
    override def getColumnClass(col: Int): Class[_] = columns(col).clazz

    override def setValueAt(value: Object, row: Int, col: Int) {
      if (row < _notes.size) {
        val note = notes(row)
        _notes(row) = columns(col).updatedValue(value, note)
        fireTableCellUpdated(row, col)
      }
    }

    def addNote(n: IndexedNote) {
      _notes += n;
      _notes = _notes.sorted
      fireTableDataChanged()
    }

    def removeNote(index: Int) {
      if (index != -1) {
        _notes.remove(index)
        fireTableRowsDeleted(index, index)
        removeEditor()
        revalidate()
        repaint()
      }
    }
  }
}
