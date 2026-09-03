// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BorderLayout, Component, Frame }
import java.awt.event.{ ActionEvent, WindowAdapter, WindowEvent }
import javax.swing.{ AbstractAction, JComponent, JDialog, JPanel }
import javax.swing.border.LineBorder

import org.nlogo.awt.Hierarchy
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

object MessageDialog {
  private val DefaultRows    = 15
  private val DefaultColumns = 60
}

import MessageDialog._

class MessageDialog(owner: Component, dismissName: String = "Dismiss")
  extends JDialog(Hierarchy.getFrame(owner)) with ZoomActions with ThemeSync {

  WindowAutomator.automate(this)

  private def parentFrame: Frame = Hierarchy.getFrame(owner)

  protected val textArea = new TextArea(DefaultRows, DefaultColumns) {
    setDragEnabled(false)
    setLineWrap(true)
    setWrapStyleWord(true)
    setEditable(false)
    setBorder(new ZoomableBorder(3, 5, 0, 5))
  }

  val dismissAction =
    new AbstractAction(dismissName) {
      def actionPerformed(e: ActionEvent): Unit = {
        setVisible(false)
      }
    }

  private val buttonPanel = new ButtonPanel(makeButtons()) {
    setBorder(new ZoomableBorder(6, 6, 6, 6))
  }

  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = {
      setVisible(false)
    }
  })

  private val scrollPane = new ScrollPane(textArea)

  private val contents = new JPanel with Transparent with Zoomable {
    setLayout(new BorderLayout)

    add(scrollPane, BorderLayout.CENTER)
    add(buttonPanel, BorderLayout.SOUTH)

    override def zoomComponent(): Unit = {
      pack()
    }
  }

  add(contents)

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
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    textArea.syncTheme()

    scrollPane.setBorder(new LineBorder(InterfaceColors.textAreaBorderNoneditable()))
    scrollPane.setBackground(InterfaceColors.textAreaBackground())

    buttonPanel.getComponents.foreach(_ match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    })
  }
}
