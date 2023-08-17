// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.nlogo.api.{Editable, Property, RunOptions}
import org.nlogo.awt.UserCancelException
import org.nlogo.core.{ I18N }
import org.nlogo.window.EditDialogFactoryInterface

import java.io.File
import java.util.prefs.Preferences

class RunOptionsDialog(parent: java.awt.Dialog,
                       dialogFactory: EditDialogFactoryInterface,
                       filePrefix: String)
{
  val userHome = System.getProperty("user.home")
  val spreadsheetFile = s"$filePrefix-spreadsheet.csv"
  val tableFile = s"$filePrefix-table.csv"

  object Prefs {
    private val prefs = Preferences.userNodeForPackage(RunOptionsDialog.this.getClass)
    // to match the behavior we had before (just boolean spreadsheet/table), if we had a
    // value stored in Preferences we want to use the setting again, but we want the
    // file path to be the suggested path since the stored one could be for another model
    // or experiement.  So just use the suggested one and assume that any string value
    // means they want the same output type.
    // -Jeremy B February 2022
    val emptyVals = Set(null, "", "false")

    def append(pathString: String, fileName: String) =
      (new File(pathString)).toPath.resolve(fileName).toString

    def parentDirectory(pathString: String) =
      if (pathString == null || pathString.trim == "") {
        ""
      } else {
        (new File(pathString)).toPath.getParent.toString
      }

    def replaceTrue(v: String) =
      if (v == "true") { "" } else { v }

    def spreadsheet = {
      val ss = prefs.get("spreadsheet", "")
      if (emptyVals.contains(ss)) { "" } else {
        append(replaceTrue(ss), spreadsheetFile)
      }
    }
    def table = {
      val t = prefs.get("table", "")
      if (emptyVals.contains(t)) { "" } else {
        append(replaceTrue(t), tableFile)
      }
    }
    def updateView = prefs.getBoolean("updateView", true)
    def updatePlotsAndMonitors = prefs.getBoolean("updatePlotsAndMonitors", true)
    def updateThreadCount = prefs.getInt("threadCount", Runtime.getRuntime.availableProcessors)
    def updateFrom(runOptions: RunOptions): Unit = {
      prefs.put("spreadsheet", parentDirectory(runOptions.spreadsheet))
      prefs.put("table", parentDirectory(runOptions.table))
      prefs.putBoolean("updateView", runOptions.updateView)
      prefs.putBoolean("updatePlotsAndMonitors", runOptions.updatePlotsAndMonitors)
      prefs.putInt("threadCount", runOptions.threadCount)
    }
  }
  def get = {
    val editable = new EditableRunOptions
    if (dialogFactory.canceled(parent, editable))
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
    var threadCount = Prefs.updateThreadCount
    val classDisplayName = "Run options"

    private implicit val i18nPrefix = I18N.Prefix("tools.behaviorSpace.runoptions")
    val propertySet = {
      import scala.collection.JavaConverters._
      List(
        Property("spreadsheet", Property.FilePath(spreadsheetFile), I18N.gui("spreadsheet")),
        Property("table", Property.FilePath(tableFile), I18N.gui("table")),
        Property("updateView", Property.Boolean, I18N.gui("updateview")),
        Property("updatePlotsAndMonitors", Property.Boolean, I18N.gui("updateplotsandmonitors")),
        Property("threadCount", Property.Integer, I18N.gui("simultaneousruns"),
                 "<html>" + I18N.gui("simultaneousruns.info") + "</html>")).asJava
    }
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
