// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, Dialog, Dimension, Point, Window }
import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.{ JButton, JDialog, JPanel, WindowConstants }
import javax.swing.border.EmptyBorder

import org.nlogo.api.{ CompilerServices, Editable }
import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ BrowserLauncher, Button, ButtonPanel, Implicits, Transparent, Utils },
  BrowserLauncher.docPath,
  Implicits.thunk2action
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.WorldViewSettings

// An EditDialog contains an EditPanel, plus some buttons at the bottom (OK/Apply/Cancel).

// EditDialog is a trait because in EditDialogFactory we need to be able to call two different
// constructors of JDialog.  See
// http://stackoverflow.com/questions/3299776/in-scala-how-can-i-subclass-a-java-class-with-multiple-constructors
// - ST 2/18/10, 7/21/10

object EditDialog {
  // aiee! a mutable static global! for shame! - ST 2/18/10
  var lastLocation: Option[Point] = None
}

abstract class EditDialog(window: Window, target: Editable, useTooltips: Boolean, modal: Boolean,
                          compiler: CompilerServices, colorizer: Colorizer)
  extends JDialog(window, target.classDisplayName,
                  if (modal)
                    Dialog.ModalityType.APPLICATION_MODAL
                  else
                    Dialog.DEFAULT_MODALITY_TYPE) with ThemeSync {

  var canceled = false

  getContentPane.setLayout(new BorderLayout)

  private val mainPanel = new JPanel(new BorderLayout) with Transparent {
    setBorder(new EmptyBorder(5, 5, 5, 5))
  }

  getContentPane.add(mainPanel, BorderLayout.CENTER)

  // forget the fancy dialog in 3D for now.
  private val editPanel =
    if (target.isInstanceOf[WorldViewSettings])
      new WorldEditPanel(target, compiler, colorizer)
    else
      new EditPanel(target, compiler, colorizer, useTooltips)

  val okButton = new Button(I18N.gui.get("common.buttons.ok"), () => {
    if (editPanel.valid) {
      editPanel.apply()

      if (target.editFinished)
        bye()
    }
  })

  var sendEditFinishedOnCancel = false
  val applyButton = new Button(I18N.gui.get("common.buttons.apply"), () => {
    if (editPanel.valid) {
      sendEditFinishedOnCancel = true
      editPanel.apply()
      target.editFinished()
    }
  })

  val cancelButton = new Button(I18N.gui.get("common.buttons.cancel"), () => {
    cancel(target)
  })

  val helpButton = new Button(I18N.gui.get("common.buttons.help"), () => {
    val link = target.helpLink.getOrElse("")
    val splitLink = link.split("#")
    val (mainLink, anchor) =
      if (splitLink.length > 1) (splitLink(0), splitLink(1))
      else                      (splitLink.head, "")
    val path = docPath(mainLink)
    BrowserLauncher.openPath(this, path, anchor)
  })

  private val buttons: List[JButton] = List(
    Some(cancelButton),
    if (editPanel.liveUpdate) None else Some(applyButton),
    target.helpLink.map(_ => helpButton)
    Some(okButton)).flatten
  private val buttonPanel = ButtonPanel(buttons)
  mainPanel.add(buttonPanel, BorderLayout.SOUTH)
  mainPanel.add(editPanel, BorderLayout.CENTER)

  // this used to happen in the editPanel constructor itself however, with the new plotting dialog,
  // we decided we wanted the ability to hide code sections by default.  we cant do that without
  // calling pack on the parent, and we can't do that until we've added the edit view to something.
  // we add in the previous line. - josh 2/13/2010
  editPanel.init()

  syncTheme()

  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
  addWindowListener(
    new WindowAdapter() {
      override def windowClosing(e: WindowEvent) {
        if (sendEditFinishedOnCancel) {
          if(editPanel.valid) {
            editPanel.apply()
            if(target.editFinished) bye()
          }
        } else {
          abort()
        }
        }})

  Utils.addEscKeyAction(this, () => cancel(target))

  pack()
  getRootPane().setDefaultButton(okButton)
  EditDialog.lastLocation match {
    case Some(location) =>
      setLocation(location)
    case None =>
      Positioning.center(this, window)
      EditDialog.lastLocation = Some(getLocation)
  }
  editPanel.requestFocus()
  setResizable(editPanel.isResizable)
  setVisible(true)

  def abort() {
    cancel(target)
  }

  private def cancel(target: Editable) {
    editPanel.revert()
    if(!sendEditFinishedOnCancel || target.editFinished) {
      canceled = true
      bye()
    }
  }

  private def bye() {
    EditDialog.lastLocation = Some(getLocation)
    setVisible(false)
    dispose()
  }

  def limit(dim: Dimension) =
    new Dimension(
      dim.width max 400,
      dim.height min getGraphicsConfiguration.getBounds.height)

  def syncTheme() {
    getContentPane.setBackground(InterfaceColors.DIALOG_BACKGROUND)

    okButton.syncTheme()
    applyButton.syncTheme()
    helpButton.syncTheme()
    cancelButton.syncTheme()

    editPanel.syncTheme()
  }
}
