package org.nlogo.review

import scala.language.existentials

import org.nlogo.mirror.IndexedNote

import javax.swing.table.AbstractTableModel

class NotesTableModel(val hasCurrentRun: HasCurrentRun, columns: Seq[Column])
  extends AbstractTableModel {

  private var _notes = scala.collection.mutable.ListBuffer[IndexedNote]()
  def notes = _notes.toList

  hasCurrentRun.afterRunChangePub.newSubscriber { event =>
    _notes.clear()
    _notes ++= event.newRun.toList.flatMap(_.indexedNotes)
  }

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
    }
  }
}
