// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import java.awt.{ BorderLayout, Dimension, FlowLayout, Frame }
import java.awt.event.{ ActionEvent, ActionListener, MouseAdapter, MouseEvent }
import java.net.{ InetAddress, NetworkInterface }
import javax.swing.{ Box, BoxLayout, JDialog, JPanel, JTable, SwingUtilities }
import javax.swing.event.{ DocumentEvent, DocumentListener, ListSelectionEvent, ListSelectionListener }
import javax.swing.table.{ AbstractTableModel, DefaultTableCellRenderer }

import org.nlogo.hubnet.connection.NetworkUtils
import org.nlogo.swing.{ Button, ComboBox, NonemptyTextFieldButtonEnabler, ScrollPane, TextField, TextFieldBox,
                         Transparent }
import org.nlogo.theme.InterfaceColors

abstract class LoginCallback{
  def apply(user:String, host:String, port:Int)
}

/**
 * The HubNet client login graphical interface.
 **/
class LoginDialog(parent: Frame, defaultUserId: String, defaultServerName: String, defaultPort: Int)
  extends JDialog(parent, "HubNet", true) with ListSelectionListener with DocumentListener {

  private val nameField = new TextField(defaultUserId, 14)
  private val serverField = new TextField(defaultServerName, 26)
  private val portField = new TextField(defaultPort.toString, 4) {
    getDocument.addDocumentListener(LoginDialog.this)
  }

  def username = nameField.getText
  def server = serverField.getText
  def port = portField.getText.toInt

  private val enterButton = new Button("Enter", () => {
    try this.loginCallback(username, server, port)
    catch {
      case nfex: NumberFormatException =>
        // we run this later on the swing thread so as not
        // to interfere with a concurrent keyboard event
        SwingUtilities.invokeLater(() => portField.requestFocus())
    }
  })

  new NonemptyTextFieldButtonEnabler(enterButton, List(serverField, nameField, portField))

  private val interfaceChooser = new InterfaceComboBox
  private val serverTable = new ServerTable(interfaceChooser.selectedNetworkAddress)
  interfaceChooser.addItemListener(_ => serverTable.actionPerformed(null))
  private var isServerTableSelectingValue = false

  // this is all really crazy... - JC 8/21/10
  locally {
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
    setBackground(InterfaceColors.DIALOG_BACKGROUND)

    // Layout the main components
    setLayout(new BorderLayout)

    val centerPanel = new JPanel with Transparent {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(new TextFieldBox {
        addField("User name:", nameField)
        add(Box.createVerticalStrut(12))
        addField("Server:", serverField)
        addField("Port:", portField)
      })
    }

    add(centerPanel, BorderLayout.CENTER)

    centerPanel.add(Box.createVerticalStrut(12))
    centerPanel.add(Box.createVerticalStrut(2))

    // Set up server table
    val serverTablePane = new ScrollPane(serverTable) {
      setPreferredSize(new Dimension(100, 88))
      setVisible(false)

      setBackground(InterfaceColors.DIALOG_BACKGROUND)
    }
    centerPanel.add(serverTablePane)
    if (interfaceChooser.itemCount > 1) {
      centerPanel.add(interfaceChooser)
    }

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

    serverTablePane.setVisible(true)
    serverTable.setActive(true)
    centerPanel.add(Box.createVerticalGlue())

    add(new JPanel(new FlowLayout(FlowLayout.RIGHT)) with Transparent {
      add(enterButton)
    }, BorderLayout.SOUTH)

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
  class ServerTable(private var interfaceAddress: Option[InetAddress]) extends JTable with Runnable with AnnouncementListener with ActionListener {

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
          discoveryListener = new DiscoveryListener(interfaceAddress)
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
     * Controls the interface that the DiscoveryListener is listening on
     **/
    def setInterfaceAddress(address: Option[InetAddress]): Unit = {
      interfaceAddress = address
      if (discoveryListener != null)
        discoveryListener.interfaceAddress = address
    }

    /**
     * Keeps DiscoveryListener in sync with target address
     **/
    def actionPerformed(e: ActionEvent): Unit = {
      e.getSource match {
        case i: InterfaceComboBox => setInterfaceAddress(i.selectedNetworkAddress)
        case _ =>
      }
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
          catch {case ex: InterruptedException => org.nlogo.api.Exceptions.ignore(ex)}
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

  object InterfaceComboBox {
    def choiceToString(choice: (NetworkInterface, InetAddress)) =
      choice match {
        case (ni: NetworkInterface, a: InetAddress) => s"${ni.getName}: ${a.toString}"
      }

    private def choices: Seq[(NetworkInterface, InetAddress)] =
      NetworkUtils.findViableInterfaces
  }

  class InterfaceComboBox(val choices: Seq[(NetworkInterface, InetAddress)] = InterfaceComboBox.choices)
    extends ComboBox(choices.map(InterfaceComboBox.choiceToString).toList) {

    import InterfaceComboBox._
    val choiceMap = choices.map(c => choiceToString(c) -> c).toMap

    setMaximumSize(new Dimension(250, 80))
    setAlignmentX(0.0f)

    def selectedNetworkAddress = choiceMap.get(getSelectedItem).map(_._2)
  }
}
