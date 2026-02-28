// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.Window
import java.nio.file.Path

import org.nlogo.api.{ LabDefaultValues, LabProtocol, Options }
import org.nlogo.awt.UserCancelException
import org.nlogo.core.{ I18N, NetLogoPreferences }
import org.nlogo.window.{ DummyErrorHandler, Editable, EditDialogFactory, EditPanel }

import java.io.File

class RunOptionsDialog(parent: Window, dialogFactory: EditDialogFactory, currentDirectory: Option[Path],
                       filePrefix: String, protocol: LabProtocol) {
  val spreadsheetFile = s"$filePrefix-spreadsheet.csv"
  val tableFile = s"$filePrefix-table.csv"
  val statsFile = s"$filePrefix-stats.csv"
  val listsFile = s"$filePrefix-lists.csv"
  val totalProcessors = Runtime.getRuntime.availableProcessors
  val defaultProcessors = LabDefaultValues.getDefaultThreads
  val mirrorHeadlessOutput = LabDefaultValues.getDefaultMirrorHeadlessOutput

  object Prefs {
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
      val ss = NetLogoPreferences.get("spreadsheet", "")
      if (emptyVals.contains(ss)) { "" } else {
        append(replaceTrue(ss), spreadsheetFile)
      }
    }
    def table = {
      val t = NetLogoPreferences.get("table", "")
      if (emptyVals.contains(t)) { "" } else {
        append(replaceTrue(t), tableFile)
      }
    }
    def stats = {
      val t = NetLogoPreferences.get("stats", "")
      if (emptyVals.contains(t)) { "" } else {
        append(replaceTrue(t), statsFile)
      }
    }
    def lists = {
      val l = NetLogoPreferences.get("lists", "")
      if (emptyVals.contains(l)) { "" } else {
        append(replaceTrue(l), listsFile)
      }
    }
    def updateView = NetLogoPreferences.getBoolean("updateView", true)
    def updatePlotsAndMonitors = NetLogoPreferences.getBoolean("updatePlotsAndMonitors", true)
    def threadCount = NetLogoPreferences.getInt("threadCount", defaultProcessors)
    def mirrorHeadlessOutput = NetLogoPreferences.getBoolean("mirrorHeadlessOutput", false)
    def errorBehavior = NetLogoPreferences.get("errorBehavior", LabProtocol.AbortRun.key)
    def updateFrom(protocol: LabProtocol): Unit = {
      NetLogoPreferences.put("spreadsheet", parentDirectory(protocol.spreadsheet))
      NetLogoPreferences.put("table", parentDirectory(protocol.table))
      NetLogoPreferences.put("stats", parentDirectory(protocol.stats))
      NetLogoPreferences.put("lists", parentDirectory(protocol.lists))
      NetLogoPreferences.putBoolean("updateView", protocol.updateView)
      NetLogoPreferences.putBoolean("updatePlotsAndMonitors", protocol.updatePlotsAndMonitors)
      NetLogoPreferences.putInt("threadCount", protocol.threadCount)
      NetLogoPreferences.putBoolean("mirrorHeadlessOutput", protocol.mirrorHeadlessOutput)
      NetLogoPreferences.put("errorBehavior", protocol.errorBehavior.key)
    }
  }

  def run(): Unit = {
    val editable = new EditableRunOptions
    if (dialogFactory.canceled(parent, editable))
      throw new UserCancelException
    Prefs.updateFrom(protocol)
  }

  class EditableRunOptions extends Editable with DummyErrorHandler {
    private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tools.behaviorSpace.runoptions")

    val classDisplayName = I18N.gui("title")

    var errorBehaviorOptions = new Options[LabProtocol.ErrorBehavior] {
      addOption(I18N.gui(LabProtocol.IgnoreErrors.key), LabProtocol.IgnoreErrors)
      addOption(I18N.gui(LabProtocol.AbortRun.key), LabProtocol.AbortRun)
      addOption(I18N.gui(LabProtocol.AbortExperiment.key), LabProtocol.AbortExperiment)

      selectValue(protocol.errorBehavior)
    }

    def spreadsheet: String = protocol.spreadsheet
    def setSpreadsheet(s: String): Unit = {
      protocol.spreadsheet = s
    }

    def table: String = protocol.table
    def setTable(s: String): Unit = {
      protocol.table = s
    }

    def stats: String = protocol.stats
    def setStats(s: String): Unit = {
      protocol.stats = s
    }

    def lists: String = protocol.lists
    def setLists(s: String): Unit = {
      protocol.lists = s
    }

    def updateView: Boolean = protocol.updateView
    def setUpdateView(b: Boolean): Unit = {
      protocol.updateView = b
    }

    def updatePlotsAndMonitors: Boolean = protocol.updatePlotsAndMonitors
    def setUpdatePlotsAndMonitors(b: Boolean): Unit = {
      protocol.updatePlotsAndMonitors = b
    }

    def threadCount: Int = protocol.threadCount
    def setThreadCount(i: Int): Unit = {
      protocol.threadCount = i
    }

    def memoryLimit: Int = protocol.memoryLimit
    def setMemoryLimit(i: Int): Unit = {
      protocol.memoryLimit = i
    }

    def mirrorHeadlessOutput: Boolean = protocol.mirrorHeadlessOutput
    def setMirrorHeadlessOutput(b: Boolean): Unit = {
      protocol.mirrorHeadlessOutput = b
    }

    def errorBehavior: Options[LabProtocol.ErrorBehavior] = errorBehaviorOptions
    def setErrorBehavior(options: Options[LabProtocol.ErrorBehavior]): Unit = {
      errorBehaviorOptions = options
      protocol.errorBehavior = options.chosenValue
    }

    override def editPanel: EditPanel = new RunOptionsEditPanel(this, currentDirectory, spreadsheetFile, tableFile,
                                                                statsFile, listsFile, defaultProcessors.toString,
                                                                totalProcessors.toString)

    // boilerplate for Editable
    def helpLink = Some(("behaviorspace", "running-an-experiment"))
    val sourceOffset = 0
    def editFinished() = true
  }
}
