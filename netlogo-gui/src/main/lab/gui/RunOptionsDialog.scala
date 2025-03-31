// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.Window

import org.nlogo.api.{ LabDefaultValues, LabRunOptions }
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.window.{ DummyErrorHandler, Editable, EditDialogFactory, EditPanel }

import java.io.File
import java.util.prefs.Preferences

class RunOptionsDialog(parent: Window, dialogFactory: EditDialogFactory, filePrefix: String) {
  val userHome = System.getProperty("user.home")
  val spreadsheetFile = s"$filePrefix-spreadsheet.csv"
  val tableFile = s"$filePrefix-table.csv"
  val statsFile = s"$filePrefix-stats.csv"
  val listsFile = s"$filePrefix-lists.csv"
  val totalProcessors = Runtime.getRuntime.availableProcessors
  val defaultProcessors = LabDefaultValues.getDefaultThreads

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
    def stats = {
      val t = prefs.get("stats", "")
      if (emptyVals.contains(t)) { "" } else {
        append(replaceTrue(t), statsFile)
      }
    }
    def lists = {
      val l = prefs.get("lists", "")
      if (emptyVals.contains(l)) { "" } else {
        append(replaceTrue(l), listsFile)
      }
    }
    def updateView = prefs.getBoolean("updateView", true)
    def updatePlotsAndMonitors = prefs.getBoolean("updatePlotsAndMonitors", true)
    def updateThreadCount = prefs.getInt("threadCount", defaultProcessors)
    def updateFrom(runOptions: LabRunOptions): Unit = {
      prefs.put("spreadsheet", parentDirectory(runOptions.spreadsheet))
      prefs.put("table", parentDirectory(runOptions.table))
      prefs.put("stats", parentDirectory(runOptions.stats))
      prefs.put("lists", parentDirectory(runOptions.lists))
      prefs.putBoolean("updateView", runOptions.updateView)
      prefs.putBoolean("updatePlotsAndMonitors", runOptions.updatePlotsAndMonitors)
      prefs.putInt("threadCount", runOptions.threadCount)
    }
  }
  def get = {
    val editable = new EditableRunOptions
    if (dialogFactory.canceled(parent, editable, false))
      throw new UserCancelException
    val runOptions = editable.get
    Prefs.updateFrom(runOptions)
    runOptions
  }
  class EditableRunOptions extends Editable with DummyErrorHandler {
    private implicit val i18nPrefix = I18N.Prefix("tools.behaviorSpace.runoptions")
    var spreadsheet = Prefs.spreadsheet
    var table = Prefs.table
    var stats = Prefs.stats
    var lists = Prefs.lists
    var updateView = Prefs.updateView
    var updatePlotsAndMonitors = Prefs.updatePlotsAndMonitors
    var threadCount = Prefs.updateThreadCount
    val classDisplayName = I18N.gui("title")

    // val propertySet = {
    //   Seq(
    //     Property("spreadsheet", Property.FilePath(spreadsheetFile), I18N.gui("spreadsheet")),
    //     Property("table", Property.FilePath(tableFile), I18N.gui("table")),
    //     Property("stats", Property.FilePath(statsFile), I18N.gui("stats")),
    //     Property("lists", Property.FilePath(listsFile), I18N.gui("lists")),
    //     Property("updateView", Property.Boolean, I18N.gui("updateview")),
    //     Property("updatePlotsAndMonitors", Property.Boolean, I18N.gui("updateplotsandmonitors"),
    //              "<html>" + I18N.gui("updateplotsandmonitors.info") + "</html>"),
    //     Property("threadCount", Property.Integer, I18N.gui("simultaneousruns"),
    //              "<html>" + I18N.gui("simultaneousruns.info",
    //                             defaultProcessors.toString,
    //                             (totalProcessors.toString))
    //             + "</html>"))
    // }

    override def editPanel: EditPanel =
      null

    def get = LabRunOptions(threadCount, table, spreadsheet, stats, lists, updateView, updatePlotsAndMonitors, false)
    // boilerplate for Editable
    def helpLink = None
    val sourceOffset = 0
    def editFinished = true
  }
}
