// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, Component, Frame }
import java.awt.event.{ ActionEvent, WindowAdapter, WindowEvent }
import javax.swing.{ AbstractAction, BorderFactory, JComponent, JDialog }
import javax.swing.border.{ EmptyBorder, LineBorder }

import org.nlogo.awt.Hierarchy
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

object MessageDialog {
  private val DefaultRows    = 15
  private val DefaultColumns = 60
}

import MessageDialog._

class MessageDialog(owner: Component, dismissName: String = "Dismiss") extends JDialog(Hierarchy.getFrame(owner))
                                                                       with ThemeSync {
  private def parentFrame: Frame = Hierarchy.getFrame(owner)

  protected val textArea = new TextArea(DefaultRows, DefaultColumns) {
    setDragEnabled(false)
    setLineWrap(true)
    setWrapStyleWord(true)
    setEditable(false)
    setBorder(BorderFactory.createEmptyBorder(3, 5, 0, 5))
  }

  val dismissAction =
    new AbstractAction(dismissName) {
      def actionPerformed(e: ActionEvent) {
        setVisible(false)
      }
    }

  private val buttonPanel = new ButtonPanel(makeButtons)

  buttonPanel.setBorder(new EmptyBorder(6, 6, 6, 6))

  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = {
      setVisible(false)
    }
  })

  private val scrollPane = new ScrollPane(textArea)

  getContentPane.setLayout(new BorderLayout())
  getContentPane.add(scrollPane, BorderLayout.CENTER)
  getContentPane.add(buttonPanel, BorderLayout.SOUTH)

  pack()

  def makeButtons(): Seq[JComponent] = {
    val dismissButton = new DialogButton(true, dismissAction)
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
    syncTheme()
    Positioning.center(this, parentFrame)
    setVisible(true)
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground)

    textArea.syncTheme()

    scrollPane.setBorder(new LineBorder(InterfaceColors.textAreaBorderNoneditable))
    scrollPane.setBackground(InterfaceColors.textAreaBackground)

    buttonPanel.getComponents.foreach(_ match {
      case ts: ThemeSync => ts.syncTheme()
    })
  }
}
