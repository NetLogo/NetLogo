// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dialog, Dimension, Window }
import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.{ AbstractAction, Icon, JComponent, JDialog, JLabel, JPanel, KeyStroke }

import org.nlogo.awt.LineBreaker
import org.nlogo.core.I18N
import org.nlogo.theme.InterfaceColors

import scala.collection.{ Seq => CollectionSeq }

object OptionPane {
  object Options {
    val Ok = Seq(I18N.gui.get("common.buttons.ok"))
    val OkCancel = Seq(I18N.gui.get("common.buttons.ok"), I18N.gui.get("common.buttons.cancel"))
    val YesNo = Seq(I18N.gui.get("common.buttons.yes"), I18N.gui.get("common.buttons.no"))
  }

  object Icons {
    val None: Icon = null
    val Info = Utils.iconScaledWithColor("/images/exclamation-circle.png", 30, 30, () => InterfaceColors.infoIcon())
    val Question = Utils.iconScaledWithColor("/images/question.png", 30, 30, () => InterfaceColors.infoIcon())
    val Warning = Utils.iconScaledWithColor("/images/exclamation-triangle.png", 30, 30,
                                            () => InterfaceColors.warningIcon())
    val Error = Utils.iconScaledWithColor("/images/exclamation-triangle.png", 30, 30,
                                          () => InterfaceColors.errorIcon())
  }
}

// like OptionDialog, but allows synchronization with theme (Isaac B 11/16/24)
class OptionPane(parent: Component, title: String, message: String, options: Seq[String], protected val icon: Icon)
  extends JDialog(parent match {
                    case w: Window => w
                    case _ => null
                  }, title, Dialog.ModalityType.APPLICATION_MODAL) with ZoomActions {

  WindowAutomator.automate(this)

  // this constructor makes it easier to access from Java (Isaac B 7/14/25)
  def this(parent: Component, title: String, message: String, options: CollectionSeq[String]) =
    this(parent, title, message, options.toSeq, OptionPane.Icons.None)

  private var selectedOption: Option[String] = None

  private val container = new BoxColumn with SyncZoom {
    setOpaque(true)
    setBackground(InterfaceColors.dialogBackground())

    override def zoom(oldZoom: Float): Unit = {
      super.zoom(oldZoom)

      pack()
    }
  }

  locally {
    setContentPane(container)

    addContents()

    val okButton = new DialogButton(true, options(0), selectAction(_))

    add(new ButtonPanel(okButton +: options.tail.map(new DialogButton(false, _, selectAction(_)))) {
      setBorder(new ZoomableBorder(0, 6, 6, 6))
    })

    container.syncZoom()

    packAndCenter()

    getRootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                   "OptionPaneCancel")

    getRootPane.getActionMap.put("OptionPaneCancel", new AbstractAction {
      override def actionPerformed(e: ActionEvent): Unit = {
        selectedOption = None

        setVisible(false)
      }
    })

    getRootPane.setDefaultButton(okButton)

    setResizable(false)
    setAlwaysOnTop(true)
    setVisible(true)
  }

  def getSelectedOption: Option[String] =
    selectedOption

  def getSelectedIndex: Int =
    selectedOption.map(options.indexOf).getOrElse(-1)

  protected def addContents(): Unit = {
    add(new BoxRow(Seq(
      new JLabel(icon),
      new JLabel(getWrappedMessage) {
        setForeground(InterfaceColors.dialogText())
      }
    ), 12) {
      setBorder(new ZoomableBorder(30, 30, 30, 30))
    })
  }

  protected def getWrappedMessage: String =
    LineBreaker.breakLines(message, getFontMetrics(new JLabel().getFont), 400).mkString("<html>", "<br>", "</html>")

  protected def packAndCenter(): Unit = {
    pack()

    Positioning.center(this, parent)
  }

  private def selectAction(text: String): Unit = {
    selectedOption = Option(text)

    setVisible(false)
  }
}

class InputOptionPane(parent: Component, title: String, message: String, startingInput: String = "")
  extends OptionPane(parent, title, message, OptionPane.Options.OkCancel, OptionPane.Icons.Question) {

  // lazy because addContents is called in super (Isaac B 11/16/24)
  private lazy val input = new TextField(0, startingInput) {
    override def getMinimumSize: Dimension =
      new Dimension(Utils.zoom(250), super.getMinimumSize.height)

    override def getPreferredSize: Dimension =
      getMinimumSize

    override def getMaximumSize: Dimension =
      getMinimumSize
  }

  def getInput: String = {
    if (getSelectedIndex == 0) {
      input.getText.trim
    } else {
      null
    }
  }

  override protected def addContents(): Unit = {
    add(new BoxRow(Seq(
      new JLabel(icon),
      new BoxColumn(Seq(
        new BoxRow(new JLabel(getWrappedMessage) {
          setForeground(InterfaceColors.dialogText())
        }, BoxAlign.Start),
        input
      ), 6)
    ), 12) {
      setBorder(new ZoomableBorder(30, 30, 30, 30))
    })

    input.requestFocus()
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
      dropdown.getSelectedItem.map(choices.indexOf).getOrElse(-1)
    } else {
      -1
    }
  }

  override protected def addContents(): Unit = {
    add(new BoxRow(Seq(
      new JLabel(icon),
      new BoxColumn(Seq(
        new JLabel(getWrappedMessage) {
          setForeground(InterfaceColors.dialogText())
        },
        dropdown
      ), 6)
    ), 12) {
      setBorder(new ZoomableBorder(30, 30, 30, 30))
    })

    dropdown.requestFocus()
    dropdown.addItemListener(_ => {
      packAndCenter()
    })
  }
}

class CustomOptionPane(parent: Component, title: String, contents: Component, options: Seq[String])
  extends OptionPane(parent, title, "", options) {

  override protected def addContents(): Unit = {
    add(new JPanel with Transparent {
      setBorder(new ZoomableBorder(30, 30, 30, 30))

      add(contents)
    })
  }
}
