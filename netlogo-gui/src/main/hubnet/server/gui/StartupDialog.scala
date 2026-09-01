// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import java.awt.{ Dimension, Frame }
import java.net.{ InetAddress, NetworkInterface }
import javax.swing.{ JDialog, JLabel, WindowConstants }

import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.swing.NonemptyTextFieldButtonEnabler
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, CheckBox, ComboBox, DialogButton, SyncZoom, TextField,
                         TextFieldBox, WindowAutomator, ZoomableBorder, ZoomActions }
import org.nlogo.theme.InterfaceColors

class StartupDialog(parent: Frame, choices: Seq[(NetworkInterface, InetAddress)],
                    preferredNetworkConnection: Option[(NetworkInterface, InetAddress)])
  extends JDialog(parent, I18N.gui.get("edit.hubnet.startActivity"), true) with ZoomActions {

  WindowAutomator.automate(this)

  private val nameField = new TextField(14, System.getProperty("user.name", ""))

  private val discoveryCheckBox = new CheckBox("Broadcast server location") {
    setForeground(InterfaceColors.dialogText())
    setSelected(true)
  }

  override def getName = nameField.getText
  def isDiscoverySelected = discoveryCheckBox.isSelected

  private val okButton = new DialogButton(true, "Start", () => StartupDialog.this.setVisible(false))

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
    networkChoices.get(networkSelection.getSelectedItem.orNull)

    // does this work via some magic side effect? or can it just be removed? JC - 8/21/10
  private[gui] val buttonEnabler = new NonemptyTextFieldButtonEnabler(okButton, List(nameField))

  private val contents = new BoxColumn(12) with SyncZoom {
    setOpaque(true)
    setBackground(InterfaceColors.dialogBackground())
    setBorder(new ZoomableBorder(8, 8, 8, 8))

    add(new TextFieldBox {
      addField("Session name:", nameField)
      syncTheme()
    })

    add(new BoxRow(discoveryCheckBox, BoxAlign.Start))

    if (networkChoices.size > 1) {
      add(new BoxRow(new JLabel("Broadcast network connection on:") {
        setForeground(InterfaceColors.dialogText())
      }, BoxAlign.Start))

      add(new BoxRow(networkSelection, BoxAlign.Start))
    }

    add(new BoxRow(okButton, BoxAlign.End))

    override def zoom(oldZoom: Float): Unit = {
      super.zoom(oldZoom)

      pack()
    }
  }

  getRootPane.setDefaultButton(okButton)

  setContentPane(contents)

  contents.syncZoom()

  setResizable(false)
  pack()
  Positioning.center(this, parent)
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
}
