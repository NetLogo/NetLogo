// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.core.I18N

import java.awt.{ Color, Dimension, GridBagConstraints }
import java.awt.event.{ ActionEvent, ActionListener }

import javax.swing.{ ImageIcon, JButton, JLabel, JPanel }

abstract class RuntimeErrorDisplay(accessor: PropertyAccessor[Option[Exception]])
  extends PropertyEditor(accessor, handlesOwnErrors = true) with RuntimeErrorDisplayer {
    private var dismissed = false
    layoutErrorPanel()

    private def dismissError(): Unit = {
      setVisible(false)
      dismissed = true
    }

    private def displayError(): Unit = {
      setVisible(true)
      dismissed = false
    }

    override def changed() // abstract

    override def get: Option[Option[Exception]] = {
      if (dismissed) Some(None) else Some(accessor.get)
    }

    override def exceptionMessage: Option[String] = get.flatten.map(_.getMessage)

    override def actionPerformed(e: ActionEvent): Unit = { dismissError() }

    override def set(e: Option[Exception]): Unit = {
      e match {
        case None => dismissError()
        case Some(e) => displayError()
      }
    }

    override def getConstraints = {
      val c = super.getConstraints
      c.fill = GridBagConstraints.HORIZONTAL
      c.weightx = 1.0
      c.weighty = 0
      c.gridheight = if (dismissed) 0 else 1
      c
    }
  }


trait RuntimeErrorDisplayer extends JPanel with ActionListener {

  def exceptionMessage: Option[String]

  lazy val dismissButton = {
    implicit val i18nPrefix = I18N.Prefix("common.buttons")
    val button = new JButton(I18N.gui("dismiss"))
    button.setToolTipText(I18N.gui("dismiss"))
    button.addActionListener(this)
    button
  }

  lazy val errorLabel = {
    implicit val i18nPrefix = I18N.Prefix("edit.plot.error")
    val label = new JLabel(I18N.gui("runtimeError"))
    label.setIcon(new ImageIcon(getClass.getResource("/images/stop.gif")))
    label
  }

  protected def layoutErrorPanel(): Unit = {
    exceptionMessage.foreach { message =>
      add(errorLabel)
      add(new JLabel(message))
      add(dismissButton)

      setBackground(Color.yellow)
    }
  }

  override def actionPerformed(e: ActionEvent): Unit
}

class RuntimeErrorPanel(e: Exception, onDismiss: (RuntimeErrorPanel) => Unit = {_ => }) extends RuntimeErrorDisplayer {

  layoutErrorPanel()
  setMaximumSize(new Dimension(400, 100))

  override def exceptionMessage = Some(e.getMessage)

  override def actionPerformed(e: ActionEvent): Unit = {
    onDismiss(this)
    dismissButton.removeActionListener(this)
  }
}
