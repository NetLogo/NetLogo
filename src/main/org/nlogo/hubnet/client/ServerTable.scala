package org.nlogo.hubnet.client

import javax.swing.JTable
import javax.swing.SwingUtilities
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import org.nlogo.hubnet.protocol.DiscoveryMessage

object ServerTable {
  /**How often to check for expired server entries **/
  val EXPIRE_SERVER_FREQUENCY = 1000L
  /**How long a server entry stays listed once we're no longer hearing messages **/
  val SERVER_ENTRY_LIFETIME = 5000L
  /**Column names for the table **/
  val COLUMN_NAMES = Array("Name", "Activity", "Server", "Port")
  /**Font size for table entries **/
  val FONT_SIZE = 11.0F
}

/**
 * A JTable of the active servers. Part of the  { @link LoginDialog }.
 * Interfaces with the  { @link DiscoveryListener }.
 **/
class ServerTable extends JTable with Runnable with AnnouncementListener {

  import ServerTable._

  /**List of active servers **/
  private val activeServers = new TimedSet(SERVER_ENTRY_LIFETIME, 6)
  /**Expires inactive server entries **/
  private var expirationThread: Thread = null
  /**Current state of this table **/
  @volatile private var active = false
  /**Listens for multicast messages broadcast from hubnet servers **/
  private var discoveryListener: DiscoveryListener = null

  locally {
    setModel(new ServerTableModel())

    // Set selection mode
    setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION)
    setCellSelectionEnabled(false)
    setRowSelectionAllowed(true)
    setColumnSelectionAllowed(false)
    setIntercellSpacing(new java.awt.Dimension(0, 0))

    // Set the appearance of the table
    setFont(getFont().deriveFont(FONT_SIZE))
    setShowVerticalLines(false)
    putClientProperty("Quaqua.Table.style", "striped")

    // Set column sizes
    setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN)
    getColumn("Port").setMaxWidth(50)
    getColumn("Name").setPreferredWidth(50)
    getColumn("Activity").setPreferredWidth(50)
    getColumn("Server").setPreferredWidth(200)

    // We override the default cell renderer to get rid of pesky focus borders
    setDefaultRenderer(
      classOf[Object],
      new DefaultTableCellRenderer() {
        override def getTableCellRendererComponent(table: JTable, value: Object,
                                                   isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int) = {
          super.getTableCellRendererComponent(table, value, isSelected, false, row, col)
        }
      })
  }

  /**
   * Turns this table on or off. Toggles the DiscoveryListener
   * and the expiration thread.
   **/
  def setActive(active: Boolean) {
    if (active != this.active) {
      this.active = active
      if (active) {
        expirationThread = new ExpirationThread()
        discoveryListener = new DiscoveryListener()
        discoveryListener.setAnnouncementListener(this)
        expirationThread.start()
        discoveryListener.start()
      }
      else {
        discoveryListener.stopListening()
        discoveryListener = null
        expirationThread = null
        activeServers.clear()
      }
    }
  }

  /**
   * Handles server announcement events. From interface AnnoucementListener.
   **/
  def announcementReceived(m: DiscoveryMessage) {
    if (activeServers.add(m)) getModel.asInstanceOf[ServerTableModel].fireTableDataChanged()
  }

  /**
   * Expires inactive server entries. From interface runnable.
   * Executed on the AWT thread.
   **/
  def run() {
    if (activeServers.expire() > 0) getModel.asInstanceOf[ServerTableModel].fireTableDataChanged()
  }

  /**
   * Thread expires inactive server entries. Continues running
   * as long as this table is active.
   * Since it does the actual expiration on the AWT thread and
   * the underlying data structure (TimedSet) is synchronized
   * we should be totally threadsafe.
   **/
  private class ExpirationThread extends Thread {
    override def run() {
      while (active) {
        try {
          Thread.sleep(EXPIRE_SERVER_FREQUENCY)
          SwingUtilities.invokeLater(ServerTable.this)
        }
        catch {case ex: InterruptedException => org.nlogo.util.Exceptions.ignore(ex)}
      }
    }
  }

  /**
   * Wraps the TimedSet of the active servers in a table model.
   **/
  private class ServerTableModel extends AbstractTableModel {
    def getRowCount = activeServers.size
    def getColumnCount = 4
    override def getColumnName(column: Int) = COLUMN_NAMES(column)
    override def getValueAt(row: Int, column: Int) = {
      val m = activeServers.get(row).asInstanceOf[DiscoveryMessage]
      column match {
        case 0 => m.uniqueId
        case 1 => m.modelName
        case 2 => m.hostName
        case 3 => m.portNumber
        case _ => throw new IndexOutOfBoundsException("column " + column)
      }
    }
    override def isCellEditable(row: Int, column: Int) = false
  }
}
