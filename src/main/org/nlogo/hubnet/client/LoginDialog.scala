// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import java.awt.event.{ActionEvent, MouseEvent, MouseAdapter, ActionListener}
import java.awt.{BorderLayout, FlowLayout, Dimension, Frame}
import javax.swing.{BorderFactory, Box, BoxLayout, JPanel, JButton, JDialog, JScrollPane, JTable, SwingUtilities}
import javax.swing.event.{DocumentEvent, ListSelectionEvent, DocumentListener, ListSelectionListener}
import javax.swing.table.{AbstractTableModel, DefaultTableCellRenderer}
import org.nlogo.swing.{NonemptyTextFieldButtonEnabler, TextField, TextFieldBox}
import org.nlogo.swing.Implicits._

abstract class LoginCallback{
  def apply(user:String, host:String, port:Int)
}

/**
 * The HubNet client login graphical interface.
 **/
class LoginDialog(parent: Frame, defaultUserId: String, defaultServerName: String,
                  defaultPort: Int, inApplet: Boolean)
        extends JDialog(parent, "HubNet", true)
        with ListSelectionListener with ActionListener with DocumentListener {

  final val nameField = new TextField(14) {setText(defaultUserId)}
  private val serverField = new TextField(26) {setText(defaultServerName)}
  private val portField = new TextField(4) {
    setText(defaultPort.toString)
    getDocument.addDocumentListener(LoginDialog.this)
  }

  def username = nameField.getText
  def server = serverField.getText
  def port = portField.getText.toInt

  private val enterButton = new JButton("Enter") {addActionListener(LoginDialog.this)}
  private val serverTable = new ServerTable()
  private var isServerTableSelectingValue = false

  // this is all really crazy... - JC 8/21/10
  locally {
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)

    // Layout the main components
    setLayout(new BorderLayout())

    val centerPanel = new JPanel() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(new TextFieldBox() {
        addField("User name:", nameField)
        if (! inApplet) {
          add(Box.createVerticalStrut(12))
          addField("Server:", serverField)
        }
        addField("Port:", portField)
      })
    }
    add(centerPanel, java.awt.BorderLayout.CENTER)

    val buttonEnabler = new NonemptyTextFieldButtonEnabler(enterButton)

    // we don't show the server field or table in the applet
    // because it can only connect to one place.
    if (! inApplet) {
      centerPanel.add(Box.createVerticalStrut(12))
      centerPanel.add(Box.createVerticalStrut(2))

      // Set up server table
      val serverTablePane = new JScrollPane(serverTable) {
        setPreferredSize(new Dimension(100, 88))
        setVisible(false)
      }
      centerPanel.add(serverTablePane)

      // Register event handlers
      serverTable.getSelectionModel.addListSelectionListener(this)
      serverField.getDocument.addDocumentListener(this)
      // Allow double clicks on server entries
      serverTable.addMouseListener(new MouseAdapter() {
        override def mouseClicked(e: MouseEvent) {
          if (e.getClickCount == 2)
            if (enterButton.isEnabled) enterButton.doClick()
            else nameField.requestFocus()
        }
      })

      buttonEnabler.addRequiredField(serverField)
      serverTablePane.setVisible(true)
      serverTable.setActive(true)
      centerPanel.add(Box.createVerticalGlue())
    }
    else centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))

    add(new JPanel(new FlowLayout(FlowLayout.RIGHT)) {add(enterButton)}, java.awt.BorderLayout.SOUTH)

    buttonEnabler.addRequiredField(nameField)
    buttonEnabler.addRequiredField(portField)

    pack()
  }

  /**
   * Sets the default button of the root pane and requests focus
   * for the first text field. Called by swing
   * when this panel gets a parent component.
   **/
  override def addNotify() {
    super.addNotify()
    getRootPane.setDefaultButton(enterButton)
    // requestFocus doesn't seem to work here unless
    // we toss it on the swing event queue
    SwingUtilities.invokeLater(() => nameField.requestFocus())
  }

  /**
   * Handles list selection events from the server table.
   * When a row of the table is selected, puts the values
   * of the selected server entry into the text Fields.
   * From interface ListSelectionListener.
   **/
  def valueChanged(e: ListSelectionEvent) {
    if (!e.getValueIsAdjusting) {
      val i = serverTable.getSelectionModel.getMinSelectionIndex
      if (i > -1) {
        isServerTableSelectingValue = true
        val server = serverTable.getValueAt(i, 2).toString
        serverField.setText(server)
        val port = serverTable.getValueAt(i, 3).toString
        portField.setText(port)
        isServerTableSelectingValue = false
      }
    }
  }

  /**
   * Handles action events from the enter button.
   * Logs in to the server.
   * From interface ActionListener.
   **/
  def actionPerformed(e: ActionEvent) {
    try this.loginCallback(username, server, port)
    catch {
      case nfex: NumberFormatException =>
        // we run this later on the swing thread so as not
        // to interfere with a concurrent keyboard event
        SwingUtilities.invokeLater(() => portField.requestFocus())
    }
  }

  // Clears selections in the server table.
  // Called when the server or port field is edited.
  def changedUpdate(e: DocumentEvent) {fieldChangeUpdate()}
  def insertUpdate(e:DocumentEvent){ fieldChangeUpdate() }
  def removeUpdate(e:DocumentEvent){ fieldChangeUpdate() }

  // Clears the selection in the server table if the entry has been changed.
  private def fieldChangeUpdate() {
    if (!isServerTableSelectingValue) {
      val i = serverTable.getSelectionModel.getMinSelectionIndex
      if (i > -1) {
        val server = serverTable.getValueAt(i, 2).toString
        val port = serverTable.getValueAt(i, 3).toString
        if (!(port==portField.getText && server==serverField.getText)) serverTable.clearSelection()
      }
    }
  }

  private var loginCallback: LoginCallback = null
  def go(callback:LoginCallback) {
    this.loginCallback = callback
    setVisible (true)
  }

  /**
   * Overrides <code>component.setVisible(...)</code> to make sure
   * server table threads are off when this is not visible.
   **/
  override def setVisible(visible: Boolean) {
    if (serverTable != null) serverTable.setActive(visible)
    super.setVisible(visible)
  }

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
    import org.nlogo.hubnet.protocol.DiscoveryMessage

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
      setFont(getFont.deriveFont(FONT_SIZE))
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
}
