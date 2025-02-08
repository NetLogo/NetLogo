// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dialog, GridBagConstraints, GridBagLayout, Insets, Window }
import javax.swing.{ Icon, JDialog, JLabel, JPanel }

import org.nlogo.awt.LineBreaker
import org.nlogo.core.I18N
import org.nlogo.theme.InterfaceColors

import scala.collection.JavaConverters._

object OptionPane {
  object Options {
    val OK = Seq(I18N.gui.get("common.buttons.ok"))
    val OK_CANCEL = Seq(I18N.gui.get("common.buttons.ok"), I18N.gui.get("common.buttons.cancel"))
    val YES_NO = Seq(I18N.gui.get("common.buttons.yes"), I18N.gui.get("common.buttons.no"))
  }

  object Icons {
    val NONE: Icon = null
    val INFO = Utils.iconScaledWithColor("/images/exclamation-circle.png", 30, 30, InterfaceColors.INFO_ICON)
    val QUESTION = Utils.iconScaledWithColor("/images/question.png", 30, 30, InterfaceColors.INFO_ICON)
    val WARNING = Utils.iconScaledWithColor("/images/exclamation-triangle.png", 30, 30, InterfaceColors.WARNING_ICON)
    val ERROR = Utils.iconScaledWithColor("/images/exclamation-triangle.png", 30, 30, InterfaceColors.ERROR_ICON)
  }
}

// like OptionDialog, but allows synchronization with theme (Isaac B 11/16/24)
class OptionPane(parent: Component, title: String, message: String, options: Seq[String],
                 protected val icon: Icon = OptionPane.Icons.NONE) extends JDialog(parent match {
                   case w: Window => w
                   case _ => null
                 }, title, Dialog.ModalityType.APPLICATION_MODAL) {

  private var selectedOption: String = null

  getContentPane.setBackground(InterfaceColors.DIALOG_BACKGROUND)
  getContentPane.setLayout(new GridBagLayout)

  addContents()

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.EAST
    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.weighty = 0
    c.insets = new Insets(0, 6, 6, 6)

    add(new JPanel(new GridBagLayout) with Transparent {
      locally {
        val c = new GridBagConstraints

        c.anchor = GridBagConstraints.EAST
        c.insets = new Insets(0, 6, 0, 0)

        options.foreach(option => {
          add(new Button(option, () => {
            selectedOption = option

            OptionPane.this.setVisible(false)
          }), c)
        })
      }
    }, c)
  }

  packAndCenter()

  setResizable(false)
  setVisible(true)

  def getSelectedOption: String =
    selectedOption

  def getSelectedIndex: Int =
    options.indexOf(selectedOption)

  protected def addContents() {
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
          setForeground(InterfaceColors.DIALOG_TEXT)
        }, c)
      }
    }, c)
  }

  protected def getWrappedMessage: String =
    LineBreaker.breakLines(message, getFontMetrics(new JLabel().getFont), 400).asScala.mkString("<html>", "<br>", "</html>")

  protected def packAndCenter() {
    pack()

    Positioning.center(this, parent)
  }
}

class InputOptionPane(parent: Component, title: String, message: String, startingInput: String = "")
  extends OptionPane(parent, title, message, OptionPane.Options.OK_CANCEL, OptionPane.Icons.QUESTION) {

  // lazy because addContents is called in super (Isaac B 11/16/24)
  private lazy val input = new TextField(0, startingInput)

  def getInput: String = {
    if (getSelectedIndex == 0)
      input.getText.trim
    else
      null
  }

  override protected def addContents() {
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
          setForeground(InterfaceColors.DIALOG_TEXT)
        }, c)

        c.insets = new Insets(0, 0, 0, 0)

        add(input, c)
      }
    }, c)
  }
}

class DropdownOptionPane[T >: Null](parent: Component, title: String, message: String, choices: Seq[T])
  extends OptionPane(parent, title, message, OptionPane.Options.OK_CANCEL, OptionPane.Icons.QUESTION) {

  // lazy because addContents is called in super (Isaac B 11/16/24)
  private lazy val dropdown = new ComboBox(choices)

  def getSelectedChoice: T = {
    if (getSelectedIndex == 0)
      dropdown.getSelectedItem
    else
      null
  }

  def getChoiceIndex: Int = {
    if (getSelectedIndex == 0)
      choices.indexOf(dropdown.getSelectedItem)
    else
      -1
  }

  override protected def addContents() {
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
          setForeground(InterfaceColors.DIALOG_TEXT)
        }, c)

        c.insets = new Insets(0, 0, 0, 0)

        add(dropdown, c)
      }
    }, c)

    dropdown.addItemListener(_ => packAndCenter)
  }
}

class CustomOptionPane(parent: Component, title: String, contents: Component, options: Seq[String])
  extends OptionPane(parent, title, "", options) {

  override protected def addContents() {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.BOTH
    c.weightx = 1
    c.weighty = 1
    c.insets = new Insets(30, 30, 30, 30)

    add(contents, c)
  }
}
