// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import javax.swing.event.{ListSelectionEvent, ListSelectionListener}
import javax.swing.border.EmptyBorder
import java.net.{ Inet4Address, InetAddress, NetworkInterface, UnknownHostException }
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

  locally {
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE)
    getContentPane.setLayout(new BorderLayout())
    getContentPane.add(new ServerOptionsPanel(HubNetUtils.viewMirroring, HubNetUtils.plotMirroring), BorderLayout.CENTER)
    getContentPane.add(clientsPanel, BorderLayout.EAST)
    getContentPane.add(messagePanel, BorderLayout.SOUTH)
    pack()
    org.nlogo.awt.Positioning.moveNextTo(this, frame)
    setVisible(true)
  }

  def setViewMirroring(mirror: Boolean) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    if (mirror != HubNetUtils.viewMirroring) {
      HubNetUtils.viewMirroring = mirror
      server.setViewEnabled(mirror)
    }
  }

  def setPlotMirroring(mirror: Boolean) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    HubNetUtils.plotMirroring = mirror;
    if (mirror) server.plotManager.broadcastPlots()
  }

  // Kicks a client and notifies it.
  def kickClient(clientId: String) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    server.removeClient(clientId, true, I18N.gui.get("menu.tools.hubnetControlCenter.removedViaControlCenter"))
    clientsPanel.removeClientEntry(clientId)
  }

  def kickAllClients() {server.removeAllClients()}
  def reloadClientInterface() {server.reloadClientInterface()}
  def broadcastMessage(text: String) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    server.broadcast(text)
  }

  def addClient(clientId: String, remoteAddress: String) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    clientsPanel.addClientEntry(clientId)
    messagePanel.logMessage( I18N.gui.getN("menu.tools.hubnetControlCenter.messagePanel.clientJoined" , clientId, remoteAddress) + "\n")
  }

  def clientDisconnect(clientId: String) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    messagePanel.logMessage(I18N.gui.getN("menu.tools.hubnetControlCenter.messagePanel.clientDisconnected" , clientId) + "\n")
    clientsPanel.removeClientEntry(clientId)
  }

  def logMessage(message: String) {messagePanel.logMessage(message)}
  def launchNewClient() {server.connection.newClient(false, 0)}

  /**
   * Panel in HubNet Control Center displays client list
   */
  class ClientsPanel(initialClientEntries: Iterable[String]) extends JPanel with ActionListener with ListSelectionListener {
    private val listData = new DefaultListModel[String]()
    private val clientsList = new JList(listData) {
      putClientProperty("Quaqua.List.style", "striped")
      setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
      addListSelectionListener(ClientsPanel.this)
      setPrototypeCellValue(I18N.gui.get("menu.tools.hubnetControlCenter.clientName"))
    }
    private val kickButton = new JButton( I18N.gui.get("menu.tools.hubnetControlCenter.kick")) {addActionListener(ClientsPanel.this); setEnabled(false)}
    private val newClientButton = new JButton(I18N.gui.get("menu.tools.hubnetControlCenter.local")) {addActionListener(ClientsPanel.this)}
    private val reloadButton = new JButton(I18N.gui.get("menu.tools.hubnetControlCenter.reset")) {addActionListener(ClientsPanel.this)}

    locally {
      setBorder(new EmptyBorder(12, 12, 12, 12))
      setLayout(new BorderLayout(0, 4))
      add(new JLabel(I18N.gui.get("menu.tools.hubnetControlCenter.clients")) {setAlignmentY(0)}, BorderLayout.NORTH)
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
      import scala.collection.JavaConverters._
      if (evt.getSource == kickButton) {
        val clientIds = clientsList.getSelectedValuesList().asScala
        for (j <- 0 until clientIds.size) {kickClient(clientIds(j).toString)}
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
    private val dateFormatter = new SimpleDateFormat(I18N.gui.get("menu.tools.hubnetControlCenter.dateFormat"))

    locally {
      val broadcastButton = new JButton(I18N.gui.get("menu.tools.hubnetControlCenter.broadcastMessage")) {addActionListener(MessagePanel.this)}
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
      org.nlogo.awt.EventQueue.invokeLater(() => inputField.requestFocus())
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

    private val mirrorViewCheckBox = new JCheckBox(I18N.gui.get("menu.tools.hubnetControlCenter.mirrorViewOn2dClients"), mirrorView) {
      addItemListener(ServerOptionsPanel.this)
    }
    private val mirrorPlotsCheckBox = new JCheckBox(I18N.gui.get("menu.tools.hubnetControlCenter.mirrorPlotsOnClients"), mirrorPlots) {
      addItemListener(ServerOptionsPanel.this)
    }

    locally {
      setBorder(new EmptyBorder(12, 12, 12, 12))
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(new TextFieldBox(SwingConstants.RIGHT, null, new JLabel().getFont.deriveFont(Font.BOLD)) {
        addField(I18N.gui.get("menu.tools.hubnetControlCenter.name"), new SelectableJLabel(serverId))
        addField(I18N.gui.get("menu.tools.hubnetControlCenter.activity"), new SelectableJLabel(activityName))
        add(Box.createVerticalStrut(12))
        val serverIP = findLocalHostAddress()
        addField(I18N.gui.get("menu.tools.hubnetControlCenter.serverAddress"), new SelectableJLabel(serverIP))
        addField(I18N.gui.get("menu.tools.hubnetControlCenter.portNumber"), new SelectableJLabel(server.port.toString))
      })
      add(Box.createVerticalStrut(30))
      add(new JLabel(I18N.gui.get("menu.tools.hubnetControlCenter.settings")))
      add(Box.createVerticalStrut(4))
      add(mirrorViewCheckBox)
      add(mirrorPlotsCheckBox)
      add(Box.createVerticalGlue())
    }

    private def findLocalHostAddress(): String =
      try
        if (!InetAddress.getLocalHost.isLoopbackAddress)
          InetAddress.getLocalHost.getHostAddress
        else {
          import scala.collection.JavaConverters._
          NetworkInterface.getNetworkInterfaces.asScala.toSeq flatMap {
            _.getInetAddresses.asScala.toSeq
          } collectFirst {
            case addr: Inet4Address if (!addr.isLoopbackAddress) => addr.getHostAddress
          } getOrElse (throw new UnknownHostException)
        }
      catch {
        case _: UnknownHostException => I18N.gui.get("menu.tools.hubnetControlCenter.unknown")
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
