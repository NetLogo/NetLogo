// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Dialog, Dimension, Window }
import java.awt.event.{ WindowAdapter, WindowEvent }
import java.net.URI
import javax.swing.{ JDialog, JPanel, WindowConstants }
import javax.swing.border.EmptyBorder

import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.swing.{ BrowserLauncher, ButtonPanel, DialogButton, Implicits, Transparent, Utils },
  BrowserLauncher.docPath, Implicits.thunk2action
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

// contains an EditPanel, plus some buttons at the bottom (OK/Apply/Help/Cancel).
class EditDialog(window: Window, target: Editable, editPanel: EditPanel, modal: Boolean)
  extends JDialog(window, target.classDisplayName,
                  if (modal)
                    Dialog.ModalityType.DOCUMENT_MODAL
                  else
                    Dialog.DEFAULT_MODALITY_TYPE) with ThemeSync {

  var canceled = false

  private val mainPanel = new JPanel(new BorderLayout) with Transparent

  val okButton = new DialogButton(true, I18N.gui.get("common.buttons.ok"), () => {
    if (editPanel.valid) {
      if (editPanel.changed)
        new Events.DirtyEvent(None).raise(window)

      editPanel.apply()

      if (target.editFinished())
        bye()
    }
  })

  var sendEditFinishedOnCancel = false
  val applyButton = new DialogButton(false, I18N.gui.get("common.buttons.apply"), () => {
    if (editPanel.valid) {
      sendEditFinishedOnCancel = true
      editPanel.apply()
      target.editFinished()
    }
  })

  val cancelButton = new DialogButton(false, I18N.gui.get("common.buttons.cancel"), () => {
    cancel(target)
  })

  val helpButton = new DialogButton(false, I18N.gui.get("common.buttons.help"), () => {
    val link = target.helpLink.getOrElse("")
    val splitLink = link.split("#")
    val (mainLink, anchor) =
      if (splitLink.length > 1) (splitLink(0), splitLink(1))
      else                      (splitLink.head, "")
    BrowserLauncher.tryOpenURI(this, new URI(s"https://docs.netlogo.org/7.0.1/$link"), docPath(mainLink),
                               anchor)
  })

  private val buttons = Seq(
    Some(okButton),
    if (!target.liveUpdate) Some(applyButton) else None,
    target.helpLink.map(_ => helpButton),
    Some(cancelButton)).flatten

  private val buttonPanel = new ButtonPanel(buttons) {
    setBorder(new EmptyBorder(0, 0, 6, 0))
  }

  getContentPane.setLayout(new BorderLayout)
  getContentPane.add(mainPanel, BorderLayout.CENTER)

  mainPanel.add(editPanel, BorderLayout.CENTER)
  mainPanel.add(buttonPanel, BorderLayout.SOUTH)

  syncTheme()

  editPanel.revert()

  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = {
      if (sendEditFinishedOnCancel) {
        if (editPanel.valid) {
          editPanel.apply()

          if (target.editFinished())
            bye()
        }
      } else {
        abort()
      }
    }
  })

  Utils.addEscKeyAction(this, () => cancel(target))

  pack()

  getRootPane.setDefaultButton(okButton)

  editPanel.requestFocus()

  Positioning.center(this, window)

  if (editPanel.isResizable) {
    setResizable(true)

    editPanel.setLayoutListener(() => setMinimumSize(mainPanel.getMinimumSize))
  } else {
    setResizable(false)
  }

  setVisible(true)

  def abort(): Unit = {
    cancel(target)
  }

  private def cancel(target: Editable): Unit = {
    editPanel.revert()

    if (!sendEditFinishedOnCancel || target.editFinished()) {
      canceled = true
      bye()
    }
  }

  private def bye(): Unit = {
    setVisible(false)
    dispose()
  }

  def limit(dim: Dimension): Dimension =
    new Dimension(
      dim.width max 400,
      dim.height min getGraphicsConfiguration.getBounds.height)

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    okButton.syncTheme()
    applyButton.syncTheme()
    helpButton.syncTheme()

    editPanel.syncTheme()
  }
}
