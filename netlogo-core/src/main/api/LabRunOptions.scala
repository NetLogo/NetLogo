// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// moved out of netlogo-gui into netlogo-core to fix headless compiler error - IOB 8/16/23
case class LabRunOptions(threadCount: Int = LabDefaultValues.getDefaultThreads,
                         table: String = LabDefaultValues.getDefaultTable,
                         spreadsheet: String = LabDefaultValues.getDefaultSpreadsheet,
                         stats: String = LabDefaultValues.getDefaultStats,
                         lists: String = LabDefaultValues.getDefaultLists,
                         updateView: Boolean = LabDefaultValues.getDefaultUpdateView,
                         updatePlotsAndMonitors: Boolean = LabDefaultValues.getDefaultUpdatePlotsAndMonitors,
                         mirrorHeadlessOutput: Boolean = LabDefaultValues.getDefaultMirrorHeadlessOutput,
                         firstRun: Boolean = true)
