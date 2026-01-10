// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import java.awt.event.{ ActionEvent, ActionListener }
import javax.swing.{ Box, BoxLayout, JLabel, JPanel }
import javax.swing.border.EmptyBorder

import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.util.{ Success, Try }

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

  override def get: Try[Option[Exception]] = {
    if (dismissed) {
      Success(None)
    } else {
      Success(accessor.getter())
    }
  }

  override def exceptionMessage: Option[String] = get.fold(_ => None, _.map(_.getMessage))

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

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  setBorder(new EmptyBorder(6, 6, 6, 6))

  protected def layoutErrorPanel(): Unit = {
    exceptionMessage.foreach { message =>
      messageLabel.setText(message)

      add(errorLabel)
      add(Box.createHorizontalStrut(6))
      add(messageLabel)
      add(Box.createHorizontalStrut(6))
      add(dismissButton)
      add(Box.createHorizontalGlue)
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
