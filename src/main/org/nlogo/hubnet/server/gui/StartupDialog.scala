// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import org.nlogo.swing.NonemptyTextFieldButtonEnabler
import org.nlogo.swing.{TextField,TextFieldBox}
import org.nlogo.swing.Implicits._
import javax.swing._
import java.awt.{BorderLayout, FlowLayout, Frame}

class StartupDialog(parent: Frame) extends JDialog(parent, true) {
  private val nameField = new TextField(14) {
    setText(System.getProperty("user.name", ""))
  }
  private val discoveryCheckBox = new JCheckBox("Broadcast server location") {
    setSelected(true)
  }

  override def getName = nameField.getText
  def isDiscoverySelected = discoveryCheckBox.isSelected

  locally {
    val okButton = new JButton("Start"){ addActionListener(() => StartupDialog.this.setVisible(false)) }
    nameField.addActionListener(() => okButton.doClick())

    val content = new JPanel(){
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      setBorder(new border.EmptyBorder(8, 8, 8, 8))
      add(Box.createVerticalStrut(12))
      add(new TextFieldBox(){addField("Session name:", nameField)})
      add(Box.createVerticalStrut(12))
      add(discoveryCheckBox)
      add(Box.createVerticalStrut(12))
      add(new JPanel(new FlowLayout(FlowLayout.RIGHT)){add(okButton)}, BorderLayout.SOUTH)
    }

    // does this work via some magic side effect? or can it just be removed? JC - 8/21/10
    val buttonEnabler = new NonemptyTextFieldButtonEnabler(okButton){addRequiredField(nameField)}

    getRootPane.setDefaultButton(okButton)
    setContentPane(content)
    setTitle("Start HubNet Activity")
    setResizable(false)
    pack()
    org.nlogo.awt.Positioning.center(this, parent)
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
  }
}
