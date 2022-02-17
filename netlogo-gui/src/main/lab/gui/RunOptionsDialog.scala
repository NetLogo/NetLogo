// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.nlogo.api.{Editable, Property}
import org.nlogo.awt.UserCancelException
import org.nlogo.window.EditDialogFactoryInterface
import Supervisor.RunOptions
import java.util.prefs.Preferences

class RunOptionsDialog(parent: java.awt.Dialog,
                       dialogFactory: EditDialogFactoryInterface,
                       filePrefix: String)
{
  println("RunOptionsDialog()")
  object Prefs {
    private val prefs = Preferences.userNodeForPackage(RunOptionsDialog.this.getClass)
    // to match the behavior we had before (just boolean spreadsheet/table), if we had a
    // value stored in Preferences we want to use the setting again, but we want the
    // file path to be the suggested path since the stored one could be for another model
    // or experiement.  So just use the suggested one and assume that any string value
    // means they want the same output type.
    // -Jeremy B February 2022
    val emptyVals = Set(null, "", "true", "false")
    val userHome = System.getProperty("user.home")
    val filePath = userHome + java.io.File.separator + filePrefix
    def spreadsheet = {
      val ss = prefs.get("spreadsheet", "")
      if (emptyVals.contains(ss)) { "" } else { s"$filePath-spreadsheet.csv" }
    }
    def table = {
      val t = prefs.get("table", "")
      if (emptyVals.contains(t)) { "" } else { s"$filePath-table.csv" }
    }
    def updateView = prefs.getBoolean("updateView", true)
    def updatePlotsAndMonitors = prefs.getBoolean("updatePlotsAndMonitors", true)
    def updateFrom(runOptions: RunOptions): Unit = {
      println("Prefs.updateFrom()")
      prefs.put("spreadsheet", runOptions.spreadsheet)
      prefs.put("table", runOptions.table)
      prefs.putBoolean("updateView", runOptions.updateView)
      prefs.putBoolean("updatePlotsAndMonitors", runOptions.updatePlotsAndMonitors)
      println("Prefs.updateFrom() complete.")
    }
  }
  def get = {
    println("RunOptionsDialog.get()")
    val editable = new EditableRunOptions
    if (dialogFactory.canceled(parent, editable)) {
      println("EditableRunOptions dialog canceled.")
      throw new UserCancelException
    }
    val runOptions = editable.get
    println(s"Run options retrieved: ${runOptions}")
    Prefs.updateFrom(runOptions)
    println(s"RunOptionsDialog.get() complete, double checking runOptions: ${runOptions}.")
    runOptions
  }
  class EditableRunOptions extends Editable {
    println("EditableRunOptions()")
    var spreadsheet = Prefs.spreadsheet
    var table = Prefs.table
    var updateView = Prefs.updateView
    var updatePlotsAndMonitors = Prefs.updatePlotsAndMonitors
    var threadCount = Runtime.getRuntime.availableProcessors
    val classDisplayName = "Run options"
    val propertySet = {
      import scala.collection.JavaConverters._
      List(
        Property("spreadsheet", Property.FilePath(spreadsheet), "Spreadsheet output"),
        Property("table", Property.FilePath(table), "Table output"),
        Property("updateView", Property.Boolean, "Update view"),
        Property("updatePlotsAndMonitors", Property.Boolean, "Update plots and monitors"),
        Property("threadCount", Property.Integer, "Simultaneous runs in parallel",
                 "<html>If more than one, some runs happen invisibly in the background." +
                 "<br>Defaults to one per processor core.</html>")).asJava
    }
    def get = RunOptions(threadCount, table, spreadsheet, updateView, updatePlotsAndMonitors)
    // boilerplate for Editable
    def helpLink = None
    def error(key:Object) = null
    def error(key:Object, e: Exception) {
      println(s"EditableRunOptions.error(key: ${key}, e: ${e})")
      e.printStackTrace
    }
    def anyErrors = false
    val sourceOffset = 0
    def editFinished = true
    println("EditableRunOptions() complete.")
  }
  println("RunOptionsDialog() complete.")
}
