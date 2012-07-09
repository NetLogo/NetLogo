// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.api.Editable
import org.nlogo.api.{CompilerServices, TokenType}
import org.nlogo.editor.Colorizer

// see commentary in EditDialogFactoryInterface

class EditDialogFactory(_compiler: CompilerServices, _colorizer: Colorizer[TokenType])
  extends org.nlogo.window.EditDialogFactoryInterface
{
  def canceled(frame: java.awt.Frame, _target: Editable) =
    (new javax.swing.JDialog(frame, _target.classDisplayName, true) // true = modal
       with EditDialog {
         override def window = frame
         override def target = _target
         override def compiler = _compiler
         override def colorizer = _colorizer
         override def getPreferredSize = limit(super.getPreferredSize)
       }).canceled
  def canceled(dialog: java.awt.Dialog, _target: Editable) =
    (new javax.swing.JDialog(dialog, _target.classDisplayName, true) // true = modal
       with EditDialog {
         override def window = dialog
         override def target = _target
         override def compiler = _compiler
         override def colorizer = _colorizer
         override def getPreferredSize = limit(super.getPreferredSize)
       }).canceled
}
