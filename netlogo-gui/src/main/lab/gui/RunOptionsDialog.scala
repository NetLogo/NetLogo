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
    if (dialogFactory.canceled(parent, editable))
      throw new UserCancelException
    val runOptions = editable.get
    Prefs.updateFrom(runOptions)
    runOptions
  }
  class EditableRunOptions extends Editable with DummyErrorHandler {
    private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tools.behaviorSpace.runoptions")
    private var _spreadsheet = Prefs.spreadsheet
    private var _table = Prefs.table
    private var _stats = Prefs.stats
    private var _lists = Prefs.lists
    private var _updateView = Prefs.updateView
    private var _updatePlotsAndMonitors = Prefs.updatePlotsAndMonitors
    private var _threadCount = Prefs.updateThreadCount
    val classDisplayName = I18N.gui("title")

    def spreadsheet: String = _spreadsheet
    def setSpreadsheet(s: String): Unit = {
      _spreadsheet = s
    }

    def table: String = _table
    def setTable(s: String): Unit = {
      _table = s
    }

    def stats: String = _stats
    def setStats(s: String): Unit = {
      _stats = s
    }

    def lists: String = _lists
    def setLists(s: String): Unit = {
      _lists = s
    }

    def updateView: Boolean = _updateView
    def setUpdateView(b: Boolean): Unit = {
      _updateView = b
    }

    def updatePlotsAndMonitors: Boolean = _updatePlotsAndMonitors
    def setUpdatePlotsAndMonitors(b: Boolean): Unit = {
      _updatePlotsAndMonitors = b
    }

    def threadCount: Int = _threadCount
    def setThreadCount(i: Int): Unit = {
      _threadCount = i
    }

    override def editPanel: EditPanel = new RunOptionsEditPanel(this, spreadsheetFile, tableFile, statsFile, listsFile,
                                                                defaultProcessors.toString, totalProcessors.toString)

    def get = LabRunOptions(threadCount, table, spreadsheet, stats, lists, updateView, updatePlotsAndMonitors, false)
    // boilerplate for Editable
    def helpLink = None
    val sourceOffset = 0
    def editFinished() = true
  }
}
