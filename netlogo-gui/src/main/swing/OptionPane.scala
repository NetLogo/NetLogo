// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dialog, GridBagConstraints, GridBagLayout, Insets, Window }
import javax.swing.{ Icon, JDialog, JLabel, JPanel }

import org.nlogo.awt.LineBreaker
import org.nlogo.core.I18N
import org.nlogo.theme.InterfaceColors

object OptionPane {
  object Options {
    val Ok = Seq(I18N.gui.get("common.buttons.ok"))
    val OkCancel = Seq(I18N.gui.get("common.buttons.ok"), I18N.gui.get("common.buttons.cancel"))
    val YesNo = Seq(I18N.gui.get("common.buttons.yes"), I18N.gui.get("common.buttons.no"))
  }

  object Icons {
    val None: Icon = null
    val Info = Utils.iconScaledWithColor("/images/exclamation-circle.png", 30, 30, InterfaceColors.infoIcon)
    val Question = Utils.iconScaledWithColor("/images/question.png", 30, 30, InterfaceColors.infoIcon)
    val Warning = Utils.iconScaledWithColor("/images/exclamation-triangle.png", 30, 30, InterfaceColors.warningIcon)
    val Error = Utils.iconScaledWithColor("/images/exclamation-triangle.png", 30, 30, InterfaceColors.errorIcon)
  }
}

// like OptionDialog, but allows synchronization with theme (Isaac B 11/16/24)
class OptionPane(parent: Component, title: String, message: String, options: Seq[String],
                 protected val icon: Icon = OptionPane.Icons.None) extends JDialog(parent match {
                   case w: Window => w
                   case _ => null
                 }, title, Dialog.ModalityType.APPLICATION_MODAL) {

  private var selectedOption: Option[String] = None

  getContentPane.setBackground(InterfaceColors.dialogBackground)
  getContentPane.setLayout(new GridBagLayout)

  addContents()

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.NONE
    c.insets = new Insets(0, 6, 6, 6)

    add(new ButtonPanel(
      options.map(option => {
        new Button(option, () => {
          selectedOption = Option(option)

          OptionPane.this.setVisible(false)
        })
      })
    ), c)
  }

  packAndCenter()

  setResizable(false)
  setVisible(true)

  def getSelectedOption: Option[String] =
    selectedOption

  def getSelectedIndex: Int =
    selectedOption.map(options.indexOf).getOrElse(-1)

  protected def addContents(): Unit = {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.BOTH
    c.weightx = 1
    c.weighty = 1
    c.insets = new Insets(30, 30, 30, 30)

    add(new JPanel(new GridBagLayout) with Transparent {
      locally {
        val c = new GridBagConstraints

        c.insets = new Insets(0, 0, 0, 12)

        add(new JLabel(icon), c)

        c.insets = new Insets(0, 0, 0, 0)

        add(new JLabel(getWrappedMessage) {
          setForeground(InterfaceColors.dialogText)
        }, c)
      }
    }, c)
  }

  protected def getWrappedMessage: String =
    LineBreaker.breakLines(message, getFontMetrics(new JLabel().getFont), 400).mkString("<html>", "<br>", "</html>")

  protected def packAndCenter(): Unit = {
    pack()

    Positioning.center(this, parent)
  }
}

class InputOptionPane(parent: Component, title: String, message: String, startingInput: String = "")
  extends OptionPane(parent, title, message, OptionPane.Options.OkCancel, OptionPane.Icons.Question) {

  // lazy because addContents is called in super (Isaac B 11/16/24)
  private lazy val input = new TextField(0, startingInput)

  def getInput: String = {
    if (getSelectedIndex == 0) {
      input.getText.trim
    } else {
      null
    }
  }

  override protected def addContents(): Unit = {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.BOTH
    c.weightx = 1
    c.weighty = 1
    c.insets = new Insets(30, 30, 30, 30)

    add(new JPanel(new GridBagLayout) with Transparent {
      locally {
        val c = new GridBagConstraints

        c.gridheight = 2
        c.insets = new Insets(0, 0, 0, 12)

        add(new JLabel(icon), c)

        c.gridx = 1
        c.gridheight = 1
        c.anchor = GridBagConstraints.WEST
        c.insets = new Insets(0, 0, 6, 0)

        add(new JLabel(getWrappedMessage) {
          setForeground(InterfaceColors.dialogText)
        }, c)

        c.insets = new Insets(0, 0, 0, 0)

        add(input, c)

        input.requestFocus()
      }
    }, c)
  }
}

class DropdownOptionPane[T](parent: Component, title: String, message: String, choices: Seq[T])
  extends OptionPane(parent, title, message, OptionPane.Options.OkCancel, OptionPane.Icons.Question) {

  // lazy because addContents is called in super (Isaac B 11/16/24)
  private lazy val dropdown = new ComboBox[T](choices)

  def getSelectedChoice: Option[T] = {
    if (getSelectedIndex == 0) {
      dropdown.getSelectedItem
    } else {
      None
    }
  }

  def getChoiceIndex: Int = {
    if (getSelectedIndex == 0) {
      choices.indexOf(dropdown.getSelectedItem)
    } else {
      -1
    }
  }

  override protected def addContents(): Unit = {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.BOTH
    c.weightx = 1
    c.weighty = 1
    c.insets = new Insets(30, 30, 30, 30)

    add(new JPanel(new GridBagLayout) with Transparent {
      locally {
        val c = new GridBagConstraints

        c.gridheight = 2
        c.insets = new Insets(0, 0, 0, 12)

        add(new JLabel(icon), c)

        c.gridx = 1
        c.gridheight = 1
        c.anchor = GridBagConstraints.WEST
        c.insets = new Insets(0, 0, 6, 0)

        add(new JLabel(getWrappedMessage) {
          setForeground(InterfaceColors.dialogText)
        }, c)

        c.insets = new Insets(0, 0, 0, 0)

        add(dropdown, c)

        dropdown.requestFocus()
      }
    }, c)

    dropdown.addItemListener(_ => packAndCenter)
  }
}

class CustomOptionPane(parent: Component, title: String, contents: Component, options: Seq[String])
  extends OptionPane(parent, title, "", options) {

  override protected def addContents(): Unit = {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.BOTH
    c.weightx = 1
    c.weighty = 1
    c.insets = new Insets(30, 30, 30, 30)

    add(contents, c)
  }
}
