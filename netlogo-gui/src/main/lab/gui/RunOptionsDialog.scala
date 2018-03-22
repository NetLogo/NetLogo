// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.nlogo.api.{Editable, Property}
import org.nlogo.awt.UserCancelException
import org.nlogo.window.EditDialogFactoryInterface
import Supervisor.RunOptions
import collection.JavaConverters._
import java.util.prefs.Preferences

class RunOptionsDialog(parent: java.awt.Dialog,
                       dialogFactory: EditDialogFactoryInterface)
{
  object Prefs {
    private val prefs = Preferences.userNodeForPackage(RunOptionsDialog.this.getClass)
    def spreadsheet = prefs.getBoolean("spreadsheet", true)
    def table = prefs.getBoolean("table", false)
    def updateView = prefs.getBoolean("updateView", true)
    def updatePlotsAndMonitors = prefs.getBoolean("updatePlotsAndMonitors", true)
    def updateFrom(runOptions: RunOptions): Unit = {
      prefs.putBoolean("spreadsheet", runOptions.spreadsheet)
      prefs.putBoolean("table", runOptions.table)
      prefs.putBoolean("updateView", runOptions.updateView)
      prefs.putBoolean("updatePlotsAndMonitors", runOptions.updatePlotsAndMonitors)
    }
  }
  def get = {
    val editable = new EditableRunOptions
    if(dialogFactory.canceled(parent, editable))
      throw new UserCancelException
    val runOptions = editable.get
    Prefs.updateFrom(runOptions)
    runOptions
  }
  class EditableRunOptions extends Editable {
    var spreadsheet = Prefs.spreadsheet
    var table = Prefs.table
    var updateView = Prefs.updateView
    var updatePlotsAndMonitors = Prefs.updatePlotsAndMonitors
    var threadCount = Runtime.getRuntime.availableProcessors
    val classDisplayName = "Run options"
    val propertySet =
      List(
        Property("spreadsheet", Property.Boolean, "Spreadsheet output"),
        Property("table", Property.Boolean, "Table output"),
        Property("updateView", Property.Boolean, "Update view"),
        Property("updatePlotsAndMonitors", Property.Boolean, "Update plots and monitors"),
        Property("threadCount", Property.Integer, "Simultaneous runs in parallel",
                 "<html>If more than one, some runs happen invisibly in the background." +
                 "<br>Defaults to one per processor core.</html>")).asJava
    def get = RunOptions(threadCount, table, spreadsheet, updateView, updatePlotsAndMonitors)
    // boilerplate for Editable
    def helpLink = None
    def error(key:Object) = null
    def error(key:Object, e: Exception){}
    def anyErrors = false
    val sourceOffset = 0
    def editFinished = true
  }
}
