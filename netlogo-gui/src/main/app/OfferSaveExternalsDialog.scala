// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Component
import javax.swing.{BoxLayout, JLabel, JPanel, JScrollPane, JTable}
import javax.swing.table.DefaultTableModel

import org.nlogo.app.codetab.TemporaryCodeTab
import org.nlogo.app.common.TabsInterface.Filename
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.OptionDialog

object OfferSaveExternalsDialog {
  def offer(dirtyExternalFiles: Set[TemporaryCodeTab], parent: Component) = {
    implicit val i18nPrefix = I18N.Prefix("file.save.offer.external")

    if (dirtyExternalFiles.nonEmpty) {
      val panel = new JPanel
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
      panel.add(new JLabel(I18N.gui("filesChanged")))
      val tableModel = new SaveTableModel(dirtyExternalFiles)
      val table = new JTable(tableModel)
      table.setShowGrid(false)
      table.getTableHeader.setReorderingAllowed(false)
      table.getTableHeader.setResizingAllowed(false)
      table.setRowSelectionAllowed(false)
      table.getColumnModel.getColumn(0)
        .setMaxWidth(panel.getFontMetrics(panel.getFont).stringWidth(I18N.gui("shouldSave")) * 2)
      table.validate()
      panel.add(new JScrollPane(table))
      val options = Array(I18N.gui("saveSelected"), I18N.gui("discardAll"), I18N.gui.get("common.buttons.cancel"))
      OptionDialog.showCustom(parent, I18N.gui.get("common.netlogo"), panel, options) match {
        case 0 =>
          tableModel.files
            .filter (_ (0).asInstanceOf[Boolean])
            .foreach { row =>
              dirtyExternalFiles.find(_.filename == row(1).asInstanceOf[Filename]) foreach (_.save(false))
            }
        case 1 =>
        case _ => throw new UserCancelException
      }
    }
  }

  private class SaveTableModel(dirtyExternalFiles: Set[TemporaryCodeTab]) extends DefaultTableModel {
    implicit val i18nPrefix = I18N.Prefix("file.save.offer.external")
    val files = (dirtyExternalFiles map (tab => Array[AnyRef](true: java.lang.Boolean, tab.filename))).toArray
    override def getValueAt(row: Int, col: Int) = col match {
      case 0 => files(row)(col)
      case 1 => files(row)(col).asInstanceOf[Filename].merge
    }
    override def setValueAt(value: AnyRef, row: Int, col: Int) = files(row)(col) = value
    override def getRowCount: Int = dirtyExternalFiles.size
    override def getColumnCount: Int = 2
    override def getColumnName(i: Int): String = i match {
      case 0 => I18N.gui("shouldSave")
      case 1 => I18N.gui("filename")
    }
    override def getColumnClass(i: Int): Class[_] = i match {
      case 0 => classOf[java.lang.Boolean]
      case 1 => classOf[String]
    }
    override def isCellEditable(row: Int, col: Int) = col == 0
  }
}
