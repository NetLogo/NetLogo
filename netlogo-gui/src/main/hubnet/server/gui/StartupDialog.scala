// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import org.nlogo.swing.NonemptyTextFieldButtonEnabler
import org.nlogo.swing.{TextField,TextFieldBox}
import org.nlogo.swing.Implicits._
import javax.swing._
import java.awt.{ BorderLayout, FlowLayout, Frame }
import java.net.{ NetworkInterface, InetAddress }

class StartupDialog(parent: Frame,
  choices: Seq[(NetworkInterface, InetAddress)],
  preferredNetworkConnection: Option[(NetworkInterface, InetAddress)]) extends JDialog(parent, true) {

  private val nameField = new TextField(14) {
    setText(System.getProperty("user.name", ""))
  }
  private val discoveryCheckBox = new JCheckBox("Broadcast server location") {
    setSelected(true)
  }

  override def getName = nameField.getText
  def isDiscoverySelected = discoveryCheckBox.isSelected

  private val okButton = new JButton("Start") { addActionListener(_ => StartupDialog.this.setVisible(false)) }
  nameField.addActionListener(_ => okButton.doClick())

  private def choiceToString(choice: (NetworkInterface, InetAddress)) =
    choice match {
      case (ni: NetworkInterface, a: InetAddress) => s"${ni.getName}: ${a.toString}"
    }

  private val networkChoices: Map[String, (NetworkInterface, InetAddress)] =
    choices.map(c => choiceToString(c) -> c).toMap

  private val networkSelection = {
    val cb = new JComboBox[String](networkChoices.keys.toSeq.sorted.toArray[String])
    // 15 characters for IP, 10 characters for name
    cb.setPrototypeDisplayValue("1234567890123456789012345")
    cb.setMaximumSize(new java.awt.Dimension(250, 80))
    cb.setAlignmentX(0.0f)
    cb
  }

  preferredNetworkConnection.foreach { c =>
    networkSelection.setSelectedItem(choiceToString(c))
  }

  def selectedNetwork: Option[(NetworkInterface, InetAddress)] =
    if (networkSelection.isEmpty) None
    else networkChoices.get(networkSelection.getItemAt(networkSelection.getSelectedIndex))

    // does this work via some magic side effect? or can it just be removed? JC - 8/21/10
  private[gui] val buttonEnabler = new NonemptyTextFieldButtonEnabler(okButton){addRequiredField(nameField)}

  locally {
    val content = new JPanel(){
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      setBorder(new border.EmptyBorder(8, 8, 8, 8))
      add(Box.createVerticalStrut(12))
      add(new TextFieldBox(){addField("Session name:", nameField)})
      add(Box.createVerticalStrut(12))
      add(discoveryCheckBox)
      if (networkChoices.size > 1) {
        add(Box.createVerticalStrut(12))
        add(new JLabel("Broadcast network connection on:"))
        add(Box.createVerticalStrut(4))
        add(networkSelection)
      }
      add(Box.createVerticalStrut(12))
      add(new JPanel(new FlowLayout(FlowLayout.RIGHT)){add(okButton)}, BorderLayout.SOUTH)
    }

    getRootPane.setDefaultButton(okButton)
    setContentPane(content)
    setTitle("Start HubNet Activity")
    setResizable(false)
    pack()
    org.nlogo.awt.Positioning.center(this, parent)
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
  }
}
