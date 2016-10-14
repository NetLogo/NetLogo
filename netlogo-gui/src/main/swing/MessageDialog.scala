// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, Component, Frame }
import java.awt.event.{ ActionEvent, WindowAdapter, WindowEvent }
import java.util.{ List => JList, ArrayList }
import javax.swing.{ AbstractAction, BorderFactory, JButton, JComponent, JDialog,
  JScrollPane, JTextArea }

import org.nlogo.awt.{ Hierarchy, Positioning }

object MessageDialog {
  private val DefaultRows    = 15
  private val DefaultColumns = 60
}

import MessageDialog._

class MessageDialog(owner: Component, dismissName: String = "Dismiss") extends JDialog(Hierarchy.getFrame(owner)) {
  private def parentFrame: Frame = Hierarchy.getFrame(owner)
  protected val textArea = new JTextArea(DefaultRows, DefaultColumns)

  private var firstShow = true

  val dismissAction =
    new AbstractAction(dismissName) {
      def actionPerformed(e: ActionEvent) {
        setVisible(false)
      }
    }

  val buttonPanel = ButtonPanel(makeButtons: _*)

  locally {
    addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = {
        setVisible(false)
      }
    })
    getContentPane.setLayout(new BorderLayout());
    textArea.setDragEnabled(false)
    textArea.setLineWrap(true)
    textArea.setWrapStyleWord(true)
    textArea.setEditable(false)
    textArea.setBorder(BorderFactory.createEmptyBorder(3, 5, 0, 5))
    getContentPane.add(new JScrollPane(textArea), BorderLayout.CENTER)
    getContentPane.add(buttonPanel, BorderLayout.SOUTH)
    pack()
  }

  def makeButtons(): Seq[JComponent] = {
    val dismissButton = new JButton(dismissAction)
    getRootPane.setDefaultButton(dismissButton)
    Utils.addEscKeyAction(this, dismissAction)
    Seq(dismissButton)
  }

  protected def doShow(title: String, message: String, rows: Int, columns: Int): Unit = {
    setTitle(title)
    textArea.setRows(rows)
    textArea.setColumns(columns)
    textArea.setText(message)
    textArea.setCaretPosition(0)
    pack()
    if (firstShow) {
      firstShow = false
      Positioning.center(this, parentFrame)
    }
    setVisible(true)
  }
}
