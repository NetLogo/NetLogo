// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.util.function.Consumer
import org.nlogo.api.{ CompilerServices, Editable }
import org.nlogo.editor.Colorizer

// see commentary in EditDialogFactoryInterface

class EditDialogFactory(_compiler: CompilerServices, _colorizer: Colorizer)
  extends org.nlogo.window.EditDialogFactoryInterface
{
  var dialog: EditDialog = null

  def canceled(frame: java.awt.Frame, _target: Editable) = {
    (new javax.swing.JDialog(frame, _target.classDisplayName, true)
       with EditDialog {
         override def window = frame
         override def target = _target
         override def compiler = _compiler
         override def colorizer = _colorizer
         override def getPreferredSize = limit(super.getPreferredSize)
       }).canceled
  }
  def canceled(dialog: java.awt.Dialog, _target: Editable) = {
    (new javax.swing.JDialog(dialog, _target.classDisplayName, true)
       with EditDialog {
         override def window = dialog
         override def target = _target
         override def compiler = _compiler
         override def colorizer = _colorizer
         override def getPreferredSize = limit(super.getPreferredSize)
       }).canceled
  }

  def create(frame: java.awt.Frame, _target: Editable, finish: Consumer[java.lang.Boolean]) = {
    dialog = new javax.swing.JDialog(frame, _target.classDisplayName, false)
               with EditDialog {
                 override def window = frame
                 override def target = _target
                 override def compiler = _compiler
                 override def colorizer = _colorizer
                 override def getPreferredSize = limit(super.getPreferredSize)
               }
    dialog.addWindowListener(new java.awt.event.WindowAdapter {
      override def windowClosed(e: java.awt.event.WindowEvent) {
        finish.accept(dialog != null && !dialog.canceled)
      }
    })
  }

  def create(_dialog: java.awt.Dialog, _target: Editable, finish: Consumer[java.lang.Boolean]) = {
    dialog = new javax.swing.JDialog(_dialog, _target.classDisplayName, false)
                    with EditDialog {
                      override def window = _dialog
                      override def target = _target
                      override def compiler = _compiler
                      override def colorizer = _colorizer
                      override def getPreferredSize = limit(super.getPreferredSize)
                    }
    dialog.addWindowListener(new java.awt.event.WindowAdapter {
      override def windowClosed(e: java.awt.event.WindowEvent) {
        finish.accept(dialog != null && !dialog.canceled)
      }
    })
  }
  
  def getDialog() = dialog
  def clearDialog() = {
    if (dialog != null)
    {
      dialog.abort()
      dialog = null
    }
  }
}
