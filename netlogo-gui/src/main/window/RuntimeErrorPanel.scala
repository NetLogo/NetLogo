// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import java.awt.event.{ ActionEvent, ActionListener }
import javax.swing.{ JLabel, JPanel }

import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class RuntimeErrorDisplay(accessor: PropertyAccessor[Option[Exception]])
  extends PropertyEditor(accessor, true) with RuntimeErrorDisplayer {

  private var dismissed = false

  setOpaque(true)
  layoutErrorPanel()

  private def dismissError(): Unit = {
    setVisible(false)
    dismissed = true
  }

  private def displayError(): Unit = {
    setVisible(true)
    dismissed = false
  }

  override def get: Option[Option[Exception]] = {
    if (dismissed) {
      Some(None)
    } else {
      Some(accessor.getter())
    }
  }

  override def exceptionMessage: Option[String] = get.flatten.map(_.getMessage)

  override def actionPerformed(e: ActionEvent): Unit = { dismissError() }

  override def set(e: Option[Exception]): Unit = {
    e match {
      case None => dismissError()
      case Some(e) => displayError()
    }
  }
}

trait RuntimeErrorDisplayer extends JPanel with ActionListener with ThemeSync {

  def exceptionMessage: Option[String]

  lazy val dismissButton = {
    implicit val i18nPrefix = I18N.Prefix("common.buttons")
    val button = new Button(I18N.gui("dismiss"), () => {})
    button.setToolTipText(I18N.gui("dismiss"))
    button.addActionListener(this)
    button
  }

  lazy val errorLabel = new JLabel(I18N.gui.get("edit.plot.error.runtimeError"))
  lazy val messageLabel = new JLabel

  protected def layoutErrorPanel(): Unit = {
    exceptionMessage.foreach { message =>
      messageLabel.setText(message)

      add(errorLabel)
      add(messageLabel)
      add(dismissButton)
    }
  }

  override def actionPerformed(e: ActionEvent): Unit

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.errorLabelBackground())

    errorLabel.setForeground(InterfaceColors.errorLabelText())
    messageLabel.setForeground(InterfaceColors.errorLabelText())

    errorLabel.setIcon(Utils.iconScaledWithColor("/images/error.png", 15, 15, InterfaceColors.errorLabelText()))

    dismissButton.syncTheme()
  }
}

class RuntimeErrorPanel(e: Exception, onDismiss: (RuntimeErrorPanel) => Unit = {_ => }) extends RuntimeErrorDisplayer {

  layoutErrorPanel()
  setMaximumSize(new Dimension(400, 100))

  syncTheme()

  override def exceptionMessage: Option[String] = Some(e.getMessage)

  override def actionPerformed(e: ActionEvent): Unit = {
    onDismiss(this)
    dismissButton.removeActionListener(this)
  }
}
