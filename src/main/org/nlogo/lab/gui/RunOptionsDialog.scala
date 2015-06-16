// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.nlogo.api.{Editable, Property}
import org.nlogo.awt.UserCancelException
import org.nlogo.window.EditDialogFactoryInterface
import Supervisor.RunOptions
import collection.JavaConverters._

class RunOptionsDialog(parent: java.awt.Dialog,
                       dialogFactory: EditDialogFactoryInterface)
{
  def get = {
    val editable = new EditableRunOptions
    if(dialogFactory.canceled(parent, editable))
      throw new UserCancelException
    editable.get
  }
  class EditableRunOptions extends Editable {
    var spreadsheet = true
    var table = false
    var threadCount = Runtime.getRuntime.availableProcessors
    val classDisplayName = "Run options"
    val propertySet =
      List(
        Property("spreadsheet", Property.Boolean, "Spreadsheet output"),
        Property("table", Property.Boolean, "Table output"),
        Property("threadCount", Property.Integer, "Simultaneous runs in parallel",
                 "<html>If more than one, some runs happen invisibly in the background." +
                 "<br>Defaults to one per processor core.</html>")).asJava
    def get = RunOptions(threadCount, table, spreadsheet)
    // boilerplate for Editable
    def helpLink = None
    def error(key:Object) = null
    def error(key:Object, e: Exception) = {}
    def anyErrors = false
    val sourceOffset = 0
    def editFinished = true
  }
}
