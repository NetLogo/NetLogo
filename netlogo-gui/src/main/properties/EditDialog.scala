// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

// An EditDialog contains an EditPanel, plus some buttons at the bottom (OK/Apply/Cancel).

import org.nlogo.core.{ I18N, TokenType }
import org.nlogo.editor.Colorizer
import org.nlogo.swing.Implicits._
import javax.swing.JButton
import org.nlogo.swing.{BrowserLauncher, ButtonPanel, RichJButton}
import org.nlogo.api.CompilerServices

// EditDialog is a trait because in EditDialogFactory we need to be able to call two different
// constructors of JDialog.  See
// http://stackoverflow.com/questions/3299776/in-scala-how-can-i-subclass-a-java-class-with-multiple-constructors
// - ST 2/18/10, 7/21/10

object EditDialog {
  // aiee! a mutable static global! for shame! - ST 2/18/10
  var lastLocation: Option[java.awt.Point] = None
}

trait EditDialog extends javax.swing.JDialog {

  // these would all be constructor parameters, except traits don't allow them, so we have to make
  // them abstract instead - ST 2/18/10
  def window: java.awt.Window
  def target: org.nlogo.api.Editable
  def compiler: CompilerServices
  def colorizer: Colorizer
  var canceled = false

  getContentPane.setBackground(java.awt.Color.LIGHT_GRAY)
  getContentPane.setLayout(new java.awt.BorderLayout)
  private val mainPanel = new javax.swing.JPanel
  mainPanel.setBorder(
    javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5))
  mainPanel.setLayout(new java.awt.BorderLayout())
  getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER)

  // forget the fancy dialog in 3D for now.
  private val editPanel =
    if(target.isInstanceOf[org.nlogo.window.WorldViewSettings])
      new WorldEditPanel(target, compiler, colorizer)
    else new EditPanel(target, compiler, colorizer)

  val okButton = new javax.swing.JButton(I18N.gui.get("common.buttons.ok"))
  okButton.addActionListener{() =>
    if(editPanel.valid) {
      editPanel.apply()
      if(target.editFinished)
        bye()
    }}

  var sendEditFinishedOnCancel = false
  val applyButton = RichJButton(I18N.gui.get("common.buttons.apply")){
    if(editPanel.valid) {
      sendEditFinishedOnCancel = true
      editPanel.apply()
      target.editFinished()
    }
  }

  val cancelButton = RichJButton(I18N.gui.get("common.buttons.cancel")){ cancel(target) }
  val helpButton = RichJButton(I18N.gui.get("common.buttons.help")){
    val link = target.helpLink.getOrElse("")
    val mainLink = if(link.contains('#')) link.takeWhile(_!='#') else link
    val anchor = if(link.contains('#')) link.dropWhile(_!='#') else ""
    BrowserLauncher.openURL(this, mainLink, anchor, true)
  }

  private val buttons: List[JButton] = List(
    Some(okButton),
    if (editPanel.liveUpdate) None else Some(applyButton),
    target.helpLink.map(_ => helpButton),
    Some(cancelButton)).flatten
  private val buttonPanel = ButtonPanel(buttons)
  mainPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH)
  mainPanel.add(editPanel, java.awt.BorderLayout.CENTER)

  // this used to happen in the editPanel constructor itself however, with the new plotting dialog,
  // we decided we wanted the ability to hide code sections by default.  we cant do that without
  // calling pack on the parent, and we can't do that until we've added the edit view to something.
  // we add in the previous line. - josh 2/13/2010
  editPanel.init()

  setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
  addWindowListener(
    new java.awt.event.WindowAdapter() {
      override def windowClosing(e: java.awt.event.WindowEvent) {
        if(editPanel.valid) {
          editPanel.apply()
          if(target.editFinished) bye()
        }}})

  org.nlogo.swing.Utils.addEscKeyAction(this, () => cancel(target))

  pack()
  getRootPane().setDefaultButton(okButton)
  EditDialog.lastLocation match {
    case Some(location) =>
      setLocation(location)
    case None =>
      org.nlogo.awt.Positioning.center(this, window)
      EditDialog.lastLocation = Some(getLocation)
  }
  editPanel.requestFocus()
  setResizable(editPanel.isResizable)
  setVisible(true)

  private def cancel(target: org.nlogo.api.Editable) {
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

  def limit(dim: java.awt.Dimension) =
    new java.awt.Dimension(
      dim.width max 400,
      dim.height min getGraphicsConfiguration.getBounds.height)

}
