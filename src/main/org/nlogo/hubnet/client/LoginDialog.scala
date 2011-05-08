package org.nlogo.hubnet.client

import org.nlogo.swing.{NonemptyTextFieldButtonEnabler, TextField, TextFieldBox}
import org.nlogo.swing.Implicits._
import javax.swing._
import event.{DocumentEvent, ListSelectionEvent, DocumentListener, ListSelectionListener}
import java.awt.event.{ActionEvent, MouseEvent, MouseAdapter, ActionListener}
import java.awt.{BorderLayout, FlowLayout, Dimension, Frame}

/**
 * The HubNet client login graphical interface.
 **/
class LoginDialog(parent: Frame, userid: String, server: String, port: Int, getServer: Boolean)
        extends JDialog(parent, "HubNet", true)
                with ListSelectionListener with ActionListener with DocumentListener {

  final val nameField = new TextField(14) {setText(userid)}
  private val serverField = new TextField(26) {setText(server)}
  private val portField = new TextField(4) {
    setText(port.toString)
    getDocument.addDocumentListener(LoginDialog.this)
  }

  def getUserName = nameField.getText
  def getServer = serverField.getText
  def getPort = portField.getText.toInt

  private val enterButton = new JButton("Enter") {addActionListener(LoginDialog.this)}
  private val serverTable = new ServerTable()
  private var isServerTableSelectingValue = false

  // this is all really crazy... - JC 8/21/10
  locally {
    setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE)

    // Layout the main components
    setLayout(new BorderLayout())

    val centerPanel = new JPanel() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(new TextFieldBox() {
        addField("User name:", nameField)
        if (getServer) {
          add(Box.createVerticalStrut(12))
          addField("Server:", serverField)
        }
        addField("Port:", portField)
      })
    }
    add(centerPanel, java.awt.BorderLayout.CENTER)

    val buttonEnabler = new NonemptyTextFieldButtonEnabler(enterButton)

    if (getServer) {
      centerPanel.add(Box.createVerticalStrut(12))
      centerPanel.add(Box.createVerticalStrut(2))

      // Set up server table
      val serverTablePane = new JScrollPane(serverTable) {
        setPreferredSize(new Dimension(100, 88))
        setVisible(false)
      }
      centerPanel.add(serverTablePane)

      // Register event handlers
      serverTable.getSelectionModel().addListSelectionListener(this)
      serverField.getDocument().addDocumentListener(this)
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
    else {
      centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    }

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
    if (!e.getValueIsAdjusting()) {
      val i = serverTable.getSelectionModel().getMinSelectionIndex()
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
    try {
      // make sure port is a number
      portField.getText.toInt
      dispose()
    }
    catch {
      case nfex: NumberFormatException =>
        JOptionPane.showMessageDialog(this, "Invalid port number", "Login Failed", JOptionPane.INFORMATION_MESSAGE)
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
      val i = serverTable.getSelectionModel().getMinSelectionIndex()
      if (i > -1) {
        val server = serverTable.getValueAt(i, 2).toString
        val port = serverTable.getValueAt(i, 3).toString
        if (!(port==portField.getText && server==serverField.getText)) serverTable.clearSelection()
      }
    }
  }

  def doLogin () { setVisible (true) }

  /**
   * Overrides <code>component.setVisible(...)</code> to make sure
   * server table threads are off when this is not visible.
   **/
  override def setVisible(visible: Boolean) {
    if (serverTable != null) serverTable.setActive(visible)
    super.setVisible(visible)
  }
}
