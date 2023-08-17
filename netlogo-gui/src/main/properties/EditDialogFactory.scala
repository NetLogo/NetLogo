// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.api.{ CompilerServices, Editable }
import org.nlogo.editor.Colorizer

// see commentary in EditDialogFactoryInterface

class EditDialogFactory(_compiler: CompilerServices, _colorizer: Colorizer)
  extends org.nlogo.window.EditDialogFactoryInterface
{
  /*
  @param modal true if dialog is modal (blocking)
               false if dialog is not modal (non-blocking)
  */
  def canceled(frame: java.awt.Frame, _target: Editable, modal: Boolean) =
    (new javax.swing.JDialog(frame, _target.classDisplayName, modal)
       with EditDialog {
         override def window = frame
         override def target = _target
         override def compiler = _compiler
         override def colorizer = _colorizer
         override def getPreferredSize = limit(super.getPreferredSize)
       }).canceled
  def canceled(dialog: java.awt.Dialog, _target: Editable, modal: Boolean) =
    (new javax.swing.JDialog(dialog, _target.classDisplayName, modal)
       with EditDialog {
         override def window = dialog
         override def target = _target
         override def compiler = _compiler
         override def colorizer = _colorizer
         override def getPreferredSize = limit(super.getPreferredSize)
       }).canceled
}
