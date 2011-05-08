package org.nlogo.hubnet.server.gui

import javax.swing.event.{ListSelectionEvent, ListSelectionListener}
import javax.swing.border.EmptyBorder
import java.net.{SocketAddress, UnknownHostException, InetAddress}
import java.text.SimpleDateFormat
import org.nlogo.swing.{SelectableJLabel, TextFieldBox, NonemptyTextFieldButtonEnabler}
import java.awt.event.{ItemEvent, ItemListener, ActionEvent, ActionListener}
import javax.swing.{Box, SwingConstants, BoxLayout, JCheckBox, JTextArea, JTextField,
  JScrollPane, JLabel, JButton, ListSelectionModel, JList, DefaultListModel, JPanel, JFrame}
import java.awt.{Font, BorderLayout, Color, Dimension, Frame, GridBagConstraints, GridBagLayout}
import org.nlogo.api.I18N
import org.nlogo.hubnet.server.{HubNetUtils, ConnectionManager}

/**
 * The Control Center window allows the user to interact with 
 * the HubNet Server.<p>
 * <i>Thread policy: every method (except the constructor and 
 * methods only called by the constructor), must be 
 * executed on the event thread.</i>
 */
class ControlCenter(server: ConnectionManager, frame: Frame, serverId: String, activityName: String)
        extends JFrame(I18N.gui.get("menu.tools.hubNetControlCenter")) {
  private val clientsPanel: ClientsPanel = new ClientsPanel(server.clients.keys)
  private val messagePanel: MessagePanel = new MessagePanel()

  import org.nlogo.swing.Implicits._
  import org.nlogo.awt.Utils.invokeLater

  locally {
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE)
    getContentPane.setLayout(new BorderLayout())
    getContentPane.add(new ServerOptionsPanel(HubNetUtils.viewMirroring, HubNetUtils.plotMirroring), BorderLayout.CENTER)
    getContentPane.add(clientsPanel, BorderLayout.EAST)
    getContentPane.add(messagePanel, BorderLayout.SOUTH)
    pack()
    org.nlogo.awt.Utils.moveNextTo(this, frame)
    setVisible(true)
  }

  def setViewMirroring(mirror: Boolean) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread()
    if (mirror != HubNetUtils.viewMirroring) {
      HubNetUtils.viewMirroring = mirror
      server.setViewEnabled(mirror)
    }
  }

  def setPlotMirroring(mirror: Boolean) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread()
    HubNetUtils.plotMirroring = mirror;
    if (mirror) server.plotManager.broadcastPlots()
  }

  // Kicks a client and notifies it.
  def kickClient(clientId: String) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread()
    server.removeClient(clientId, true, "Removed via Control Center.")
    clientsPanel.removeClientEntry(clientId)
  }

  def kickAllClients() {server.removeAllClients()}
  def reloadClientInterface() {server.reloadClientInterface()}
  def broadcastMessage(text: String) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread()
    server.broadcast(text)
  }

  def addClient(clientId: String, remoteAddress: String) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread()
    clientsPanel.addClientEntry(clientId)
    messagePanel.logMessage("\'" + clientId + "\' joined from: " + remoteAddress + "\n")
  }

  def clientDisconnect(clientId: String) {
    org.nlogo.awt.Utils.mustBeEventDispatchThread()
    messagePanel.logMessage("\'" + clientId + "\' disconnected.\n")
    clientsPanel.removeClientEntry(clientId)
  }

  def logMessage(message: String) {messagePanel.logMessage(message)}
  def launchNewClient() {server.connection.newClient(false, 0)}
 
  /**
   * Panel in HubNet Control Center displays client list
   */
  class ClientsPanel(initialClientEntries: Iterable[String]) extends JPanel with ActionListener with ListSelectionListener {
    private val listData = new DefaultListModel()
    private val clientsList = new JList(listData) {
      putClientProperty("Quaqua.List.style", "striped")
      setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
      addListSelectionListener(ClientsPanel.this)
      setPrototypeCellValue("CLIENT NAME")
    }
    private val kickButton = new JButton("Kick") {addActionListener(ClientsPanel.this); setEnabled(false)}
    private val newClientButton = new JButton("Local") {addActionListener(ClientsPanel.this)}
    private val reloadButton = new JButton("Reset") {addActionListener(ClientsPanel.this)}

    locally {
      setBorder(new EmptyBorder(12, 12, 12, 12))
      setLayout(new BorderLayout(0, 4))
      add(new JLabel("Clients:") {setAlignmentY(0)}, BorderLayout.NORTH)
      add(new JScrollPane(clientsList), BorderLayout.CENTER)
      val gridbag = new GridBagLayout()
      add(new JPanel(gridbag) {
        val c = new GridBagConstraints()
        c.fill = GridBagConstraints.BOTH
        gridbag.setConstraints(kickButton, c)
        add(kickButton)
        c.gridwidth = GridBagConstraints.REMAINDER
        gridbag.setConstraints(newClientButton, c)
        add(newClientButton)
        gridbag.setConstraints(reloadButton, c)
        add(reloadButton)
      }, BorderLayout.SOUTH)

      kickButton.setAlignmentY(1)
      newClientButton.setAlignmentY(1)

      // Add initial client entries
      initialClientEntries.foreach(addClientEntry)
    }

    /**
     * Enables/disables the kick button.
     * Called when the list selection changes.
     * From interface ListSelectionListener.
     */
    def valueChanged(evt: ListSelectionEvent) {
      if (!evt.getValueIsAdjusting()) kickButton.setEnabled(clientsList.getMinSelectionIndex() > -1)
    }

    /**
     * Kicks a client.
     * From interface ActionListener.
     */
    def actionPerformed(evt: ActionEvent) {
      if (evt.getSource == kickButton) {
        val clientIds = clientsList.getSelectedValues()
        for (j <- 0 until clientIds.length) {kickClient(clientIds(j).toString)}
      }
      else if (evt.getSource == newClientButton) {launchNewClient()}
      else if (evt.getSource == reloadButton) {
        kickAllClients()
        reloadClientInterface()
      }
    }

    def addClientEntry(clientId: String) {listData.addElement(clientId)}
    def removeClientEntry(clientId: String) {listData.removeElement(clientId)}
    def setClientList(clientNames: List[String]) {
      listData.clear()
      clientNames.foreach(listData.addElement)
    }
  }

  /**
   * Panel in HubNet Control Center displays
   * and sends broadcast messages.
   */
  class MessagePanel extends JPanel with ActionListener {
    private val inputField = new JTextField() {addActionListener(MessagePanel.this)}
    private val messageTextArea = new JTextArea() {
      setEditable(false)
      setForeground(Color.darkGray)
      setRows(4)
    }

    // Format for message timestamp
    private val dateFormatter = new SimpleDateFormat("h:mm:ss")

    locally {
      val broadcastButton = new JButton("Broadcast Message") {addActionListener(MessagePanel.this)}
      val buttonEnabler = new NonemptyTextFieldButtonEnabler(broadcastButton) {
        addRequiredField(inputField)
      }
      setBorder(new EmptyBorder(12, 12, 12, 12))
      setLayout(new BorderLayout(4, 4))
      add(new JPanel(new BorderLayout(8, 8)) {
        add(inputField, BorderLayout.CENTER)
        add(broadcastButton, BorderLayout.EAST)
      }, BorderLayout.SOUTH)
      add(new JScrollPane(messageTextArea) {setPreferredSize(new Dimension(10, 70))}, BorderLayout.NORTH)
      org.nlogo.awt.Utils.invokeLater(() => inputField.requestFocus())
    }

    /**
     * Broadcasts the message and appends it to the message log.
     * Called when the button is clicked on return is pressed.
     */
    def actionPerformed(evt: ActionEvent) {
      val message = inputField.getText
      if (!message.isEmpty) {
        logMessage("<Leader> " + message + "\n")
        val currentTime = dateFormatter.format(new java.util.Date())
        broadcastMessage("" + currentTime + "   <Leader> " + message)
        inputField.setText("")
      }
    }

    /**
     * Appends a message to the message log.
     */
    def logMessage(message: String) {
      val currentTime = dateFormatter.format(new java.util.Date())
      val newMessage = "" + currentTime + "   " + message
      // we use setText instead of append to ensure scrolling
      messageTextArea.setText(
        messageTextArea.getText() + (if (newMessage.endsWith("\n")) newMessage else newMessage) + "\n")
    }
  }

  /**
   * Panel in HubNet Control Center displays server info  and server options.
   */
  class ServerOptionsPanel(mirrorView: Boolean, mirrorPlots: Boolean) extends JPanel with ItemListener {

    private val mirrorViewCheckBox = new JCheckBox("Mirror 2D view on clients", mirrorView) {
      addItemListener(ServerOptionsPanel.this)
    }
    private val mirrorPlotsCheckBox = new JCheckBox("Mirror plots on clients (experimental)", mirrorPlots) {
      addItemListener(ServerOptionsPanel.this)
    }

    locally {
      setBorder(new EmptyBorder(12, 12, 12, 12))
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(new TextFieldBox(SwingConstants.RIGHT, null, new JLabel().getFont.deriveFont(Font.BOLD)) {
        addField("Name:", new SelectableJLabel(serverId))
        addField("Activity:", new SelectableJLabel(activityName))
        add(Box.createVerticalStrut(12))
        val serverIP = try InetAddress.getLocalHost.getHostAddress catch {case e: UnknownHostException => "UNKNOWN"}
        addField("Server address:", new SelectableJLabel(serverIP))
        addField("Port number:", new SelectableJLabel(server.port.toString))
      })
      add(Box.createVerticalStrut(30))
      add(new JLabel("Settings:"))
      add(Box.createVerticalStrut(4))
      add(mirrorViewCheckBox)
      add(mirrorPlotsCheckBox)
      add(Box.createVerticalGlue())
    }

    /**
     * Updates server options.
     * Called when a checkbox is clicked.
     * From interface java.awt.event.ItemListener.
     */
    def itemStateChanged(e: ItemEvent) {
      setViewMirroring(mirrorViewCheckBox.isSelected)
      setPlotMirroring(mirrorPlotsCheckBox.isSelected)
    }
  }
}
