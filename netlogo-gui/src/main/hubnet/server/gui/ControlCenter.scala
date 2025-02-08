// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import java.awt.{ BorderLayout, Component, Dimension, Font, Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ WindowAdapter, WindowEvent }
import java.net.{ Inet4Address, InetAddress, NetworkInterface, UnknownHostException }
import java.text.SimpleDateFormat
import javax.swing.{ Box, BoxLayout, DefaultListModel, JFrame, JLabel, JList, JPanel, ListCellRenderer,
                     ListSelectionModel, SwingConstants }
import javax.swing.border.{ EmptyBorder, LineBorder }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }

import org.nlogo.awt.{ EventQueue, Positioning }
import org.nlogo.core.I18N
import org.nlogo.hubnet.server.{ ConnectionManager, HubNetUtils }
import org.nlogo.swing.{ Button, CheckBox, NonemptyTextFieldButtonEnabler, ScrollPane, SelectableJLabel, TextArea,
                         TextField, TextFieldBox, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.ClientAppInterface

import scala.collection.mutable.Set

/**
 * The Control Center window allows the user to interact with
 * the HubNet Server.<p>
 * <i>Thread policy: every method (except the constructor and
 * methods only called by the constructor), must be
 * executed on the event thread.</i>
 */
class ControlCenter(server: ConnectionManager, frame: Frame, serverId: String, activityName: String, address: Option[InetAddress])
  extends JFrame(I18N.gui.get("menu.tools.hubNetControlCenter")) with ThemeSync {

  private val serverPanel = new ServerOptionsPanel(HubNetUtils.viewMirroring, HubNetUtils.plotMirroring)
  private val clientsPanel = new ClientsPanel(server.clients.keys)
  private val messagePanel = new MessagePanel

  private val clientWindows = Set[ClientAppInterface]()

  setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE)
  getContentPane.setLayout(new BorderLayout())
  getContentPane.add(serverPanel, BorderLayout.CENTER)
  getContentPane.add(clientsPanel, BorderLayout.EAST)
  getContentPane.add(messagePanel, BorderLayout.SOUTH)
  pack()
  Positioning.moveNextTo(this, frame)
  setVisible(true)

  def setViewMirroring(mirror: Boolean): Unit = {
    EventQueue.mustBeEventDispatchThread()
    if (mirror != HubNetUtils.viewMirroring) {
      HubNetUtils.viewMirroring = mirror
      server.setViewEnabled(mirror)
    }
  }

  def setPlotMirroring(mirror: Boolean): Unit = {
    EventQueue.mustBeEventDispatchThread()
    HubNetUtils.plotMirroring = mirror;
    if (mirror) {
      server.plotManager.broadcastPlots()
      server.plotManager.initPlotListeners()
    }
  }

  // Kicks a client and notifies it.
  def kickClient(clientId: String): Unit = {
    EventQueue.mustBeEventDispatchThread()
    server.removeClient(clientId, true, I18N.gui.get("menu.tools.hubnetControlCenter.removedViaControlCenter"))
    clientsPanel.removeClientEntry(clientId)
  }

  def kickAllClients(): Unit = {server.removeAllClients()}
  def reloadClientInterface(): Unit = {server.reloadClientInterface()}
  def broadcastMessage(text: String): Unit = {
    EventQueue.mustBeEventDispatchThread()
    server.broadcast(text)
  }

  def addClient(clientId: String, remoteAddress: String): Unit = {
    EventQueue.mustBeEventDispatchThread()
    clientsPanel.addClientEntry(clientId)
    messagePanel.logMessage(I18N.gui.getN("menu.tools.hubnetControlCenter.messagePanel.clientJoined", clientId, remoteAddress) + "\n")
  }

  def clientDisconnect(clientId: String): Unit = {
    EventQueue.mustBeEventDispatchThread()
    messagePanel.logMessage(I18N.gui.getN("menu.tools.hubnetControlCenter.messagePanel.clientDisconnected", clientId) + "\n")
    clientsPanel.removeClientEntry(clientId)
  }

  def logMessage(message: String): Unit = {
    messagePanel.logMessage(message)
  }

  def launchNewClient(): Unit = {
    val window = server.connection.newClient(false, 0).asInstanceOf[ClientAppInterface]

    window.addWindowListener(new WindowAdapter {
      override def windowClosed(e: WindowEvent): Unit = {
        clientWindows -= window
      }
    })

    clientWindows += window
  }

  def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.DIALOG_BACKGROUND)

    serverPanel.syncTheme()
    clientsPanel.syncTheme()
    messagePanel.syncTheme()

    clientWindows.foreach(_.syncTheme())
  }

  /**
   * Panel in HubNet Control Center displays client list
   */
  class ClientsPanel(initialClientEntries: Iterable[String]) extends JPanel with Transparent with ListSelectionListener
                                                             with ThemeSync {

    private class ClientCellRenderer extends JPanel(new GridBagLayout) with ListCellRenderer[String] {
      private val label = new JLabel

      locally {
        val c = new GridBagConstraints

        c.anchor = GridBagConstraints.WEST
        c.fill = GridBagConstraints.HORIZONTAL
        c.weightx = 1
        c.insets = new Insets(3, 3, 3, 3)

        add(label, c)
      }

      def getListCellRendererComponent(list: JList[_ <: String], value: String, index: Int, isSelected: Boolean,
                                       hasFocus: Boolean): Component = {
        label.setText(value)

        if (isSelected) {
          setBackground(InterfaceColors.DIALOG_BACKGROUND_SELECTED)

          label.setForeground(InterfaceColors.DIALOG_TEXT_SELECTED)
        } else {
          setBackground(InterfaceColors.DIALOG_BACKGROUND)

          label.setForeground(InterfaceColors.DIALOG_TEXT)
        }

        this
      }
    }

    private val listData = new DefaultListModel[String]()
    private val clientsList = new JList(listData) {
      setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
      addListSelectionListener(ClientsPanel.this)
      setPrototypeCellValue(I18N.gui.get("menu.tools.hubnetControlCenter.clientName"))
      setCellRenderer(new ClientCellRenderer)
    }

    private val clientsLabel = new JLabel(I18N.gui.get("menu.tools.hubnetControlCenter.clients")) {
      setAlignmentY(0)
    }

    private val scrollPane = new ScrollPane(clientsList) with Transparent

    private val kickButton = new Button(I18N.gui.get("menu.tools.hubnetControlCenter.kick"), () => {
      import scala.collection.JavaConverters._

      clientsList.getSelectedValuesList.asScala.foreach(client => kickClient(client.toString))
    }) {
      setEnabled(false)
    }

    private val newClientButton = new Button(I18N.gui.get("menu.tools.hubnetControlCenter.local"), launchNewClient)

    private val reloadButton = new Button(I18N.gui.get("menu.tools.hubnetControlCenter.reset"), () => {
      kickAllClients()
      reloadClientInterface()
    })

    locally {
      setBorder(new EmptyBorder(12, 12, 12, 12))
      setLayout(new BorderLayout(0, 4))
      add(clientsLabel, BorderLayout.NORTH)
      add(scrollPane, BorderLayout.CENTER)
      add(new JPanel(new GridBagLayout) with Transparent {
        val c = new GridBagConstraints
        c.fill = GridBagConstraints.BOTH
        add(kickButton, c)
        c.gridwidth = GridBagConstraints.REMAINDER
        add(newClientButton, c)
        add(reloadButton, c)
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
    def valueChanged(evt: ListSelectionEvent): Unit = {
      if (!evt.getValueIsAdjusting()) kickButton.setEnabled(clientsList.getMinSelectionIndex() > -1)
    }

    def addClientEntry(clientId: String): Unit = {listData.addElement(clientId)}
    def removeClientEntry(clientId: String): Unit = {listData.removeElement(clientId)}
    def setClientList(clientNames: List[String]): Unit = {
      listData.clear()
      clientNames.foreach(listData.addElement)
    }

    def syncTheme(): Unit = {
      kickButton.syncTheme()
      newClientButton.syncTheme()
      reloadButton.syncTheme()

      clientsLabel.setForeground(InterfaceColors.DIALOG_TEXT)
      scrollPane.setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
      clientsList.setBackground(InterfaceColors.DIALOG_BACKGROUND)
    }
  }

  /**
   * Panel in HubNet Control Center displays
   * and sends broadcast messages.
   */
  class MessagePanel extends JPanel with Transparent with ThemeSync {
    private val inputField = new TextField {
      addActionListener(_ => beginBroadcast)
    }

    private val messageTextArea = new TextArea(4, 0) {
      setEditable(false)
    }

    private val scrollPane = new ScrollPane(messageTextArea) {
      setPreferredSize(new Dimension(10, 70))
    }

    // Format for message timestamp
    private val dateFormatter = new SimpleDateFormat(I18N.gui.get("menu.tools.hubnetControlCenter.dateFormat"))

    private val broadcastButton = new Button(I18N.gui.get("menu.tools.hubnetControlCenter.broadcastMessage"), beginBroadcast)

    private[gui] val buttonEnabler = new NonemptyTextFieldButtonEnabler(broadcastButton, List(inputField))

    locally {
      setBorder(new EmptyBorder(12, 12, 12, 12))
      setLayout(new BorderLayout(4, 4))
      add(new JPanel(new BorderLayout(8, 8)) with Transparent {
        add(inputField, BorderLayout.CENTER)
        add(broadcastButton, BorderLayout.EAST)
      }, BorderLayout.SOUTH)
      add(scrollPane, BorderLayout.NORTH)
      EventQueue.invokeLater(() => inputField.requestFocus())
    }

    /**
     * Broadcasts the message and appends it to the message log.
     * Called when the button is clicked on return is pressed.
     */
    def beginBroadcast(): Unit = {
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
    def logMessage(message: String): Unit = {
      val currentTime = dateFormatter.format(new java.util.Date())
      val newMessage = "" + currentTime + "   " + message
      // we use setText instead of append to ensure scrolling
      messageTextArea.setText(
        messageTextArea.getText() + (if (newMessage.endsWith("\n")) newMessage else newMessage) + "\n")
    }

    def syncTheme(): Unit = {
      inputField.syncTheme()
      messageTextArea.syncTheme()

      scrollPane.setBorder(new LineBorder(InterfaceColors.TEXT_AREA_BORDER_NONEDITABLE))
      scrollPane.setBackground(InterfaceColors.TEXT_AREA_BACKGROUND)
    }
  }

  /**
   * Panel in HubNet Control Center displays server info  and server options.
   */
  class ServerOptionsPanel(mirrorView: Boolean, mirrorPlots: Boolean) extends JPanel with Transparent with ThemeSync {
    private val mirrorViewCheckBox: CheckBox =
      new CheckBox(I18N.gui.get("menu.tools.hubnetControlCenter.mirrorViewOn2dClients"),
                   () => setViewMirroring(mirrorViewCheckBox.isSelected)) {
        setSelected(mirrorView)
      }

    private val mirrorPlotsCheckBox: CheckBox =
      new CheckBox(I18N.gui.get("menu.tools.hubnetControlCenter.mirrorPlotsOnClients"),
                   () => setPlotMirroring(mirrorViewCheckBox.isSelected)) {
        setSelected(mirrorPlots)
      }

    private val idLabel = new SelectableJLabel(serverId)
    private val activityLabel = new SelectableJLabel(activityName)
    private val addressLabel = new SelectableJLabel(address.map(_.toString.drop(1)).getOrElse(findLocalHostAddress()))
    private val portLabel = new SelectableJLabel(server.port.toString)

    private val fields = new TextFieldBox(SwingConstants.RIGHT, None, Some(new JLabel().getFont.deriveFont(Font.BOLD))) {
      addField(I18N.gui.get("menu.tools.hubnetControlCenter.name"), idLabel)
      addField(I18N.gui.get("menu.tools.hubnetControlCenter.activity"), activityLabel)
      add(Box.createVerticalStrut(12))
      addField(I18N.gui.get("menu.tools.hubnetControlCenter.serverAddress"), addressLabel)
      addField(I18N.gui.get("menu.tools.hubnetControlCenter.portNumber"), portLabel)
    }

    private val settingsLabel = new JLabel(I18N.gui.get("menu.tools.hubnetControlCenter.settings"))

    locally {
      setBorder(new EmptyBorder(12, 12, 12, 12))
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(fields)
      add(Box.createVerticalStrut(30))
      add(settingsLabel)
      add(Box.createVerticalStrut(4))
      add(mirrorViewCheckBox)
      add(mirrorPlotsCheckBox)
      add(Box.createVerticalGlue())
    }

    private def findLocalHostAddress(): String =
      try
        if (!InetAddress.getLocalHost.isLoopbackAddress) {
          InetAddress.getLocalHost.getHostAddress
        } else {
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

    def syncTheme(): Unit = {
      mirrorViewCheckBox.setForeground(InterfaceColors.DIALOG_TEXT)
      mirrorPlotsCheckBox.setForeground(InterfaceColors.DIALOG_TEXT)
      settingsLabel.setForeground(InterfaceColors.DIALOG_TEXT)
      idLabel.setForeground(InterfaceColors.DIALOG_TEXT)
      activityLabel.setForeground(InterfaceColors.DIALOG_TEXT)
      addressLabel.setForeground(InterfaceColors.DIALOG_TEXT)
      portLabel.setForeground(InterfaceColors.DIALOG_TEXT)

      fields.syncTheme()
    }
  }
}
