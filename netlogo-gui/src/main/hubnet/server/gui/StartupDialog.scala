// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import java.awt.{ BorderLayout, Dimension, FlowLayout, Frame }
import java.net.{ InetAddress, NetworkInterface }
import javax.swing.{ Box, BoxLayout, JDialog, JLabel, JPanel, WindowConstants }
import javax.swing.border.EmptyBorder

import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.swing.NonemptyTextFieldButtonEnabler
import org.nlogo.swing.{ Button, CheckBox, ComboBox, TextField, TextFieldBox, Transparent }
import org.nlogo.theme.InterfaceColors

class StartupDialog(parent: Frame, choices: Seq[(NetworkInterface, InetAddress)],
                    preferredNetworkConnection: Option[(NetworkInterface, InetAddress)])
  extends JDialog(parent, I18N.gui.get("edit.hubnet.startActivity"), true) {

  private val nameField = new TextField(System.getProperty("user.name", ""), 14)

  private val discoveryCheckBox = new CheckBox("Broadcast server location") {
    setForeground(InterfaceColors.DIALOG_TEXT)
    setSelected(true)
  }

  override def getName = nameField.getText
  def isDiscoverySelected = discoveryCheckBox.isSelected

  private val okButton = new Button("Start", () => StartupDialog.this.setVisible(false))

  nameField.addActionListener(_ => okButton.doClick())

  private def choiceToString(choice: (NetworkInterface, InetAddress)) =
    choice match {
      case (ni: NetworkInterface, a: InetAddress) => s"${ni.getName}: ${a.toString}"
    }

  private val networkChoices: Map[String, (NetworkInterface, InetAddress)] =
    choices.map(c => choiceToString(c) -> c).toMap

  private val networkSelection = new ComboBox(networkChoices.keys.toSeq.sorted.toList) {
    setMaximumSize(new Dimension(250, 80))
    setAlignmentX(0.0f)
  }

  preferredNetworkConnection.foreach { c =>
    networkSelection.setSelectedItem(choiceToString(c))
  }

  def selectedNetwork: Option[(NetworkInterface, InetAddress)] =
    networkChoices.get(networkSelection.getSelectedItem)

    // does this work via some magic side effect? or can it just be removed? JC - 8/21/10
  private[gui] val buttonEnabler = new NonemptyTextFieldButtonEnabler(okButton, List(nameField))

  locally {
    val content = new JPanel {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      setBorder(new EmptyBorder(8, 8, 8, 8))
      setBackground(InterfaceColors.DIALOG_BACKGROUND)
      add(Box.createVerticalStrut(12))
      add(new TextFieldBox {
        addField("Session name:", nameField)
        syncTheme()
      })
      add(Box.createVerticalStrut(12))
      add(discoveryCheckBox)
      if (networkChoices.size > 1) {
        add(Box.createVerticalStrut(12))
        add(new JLabel("Broadcast network connection on:") {
          setForeground(InterfaceColors.DIALOG_TEXT)
        })
        add(Box.createVerticalStrut(4))
        add(networkSelection)
      }
      add(Box.createVerticalStrut(12))
      add(new JPanel(new FlowLayout(FlowLayout.RIGHT)) with Transparent {
        add(okButton)
      }, BorderLayout.SOUTH)
    }

    getRootPane.setDefaultButton(okButton)
    setContentPane(content)
    setResizable(false)
    pack()
    Positioning.center(this, parent)
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
  }
}
