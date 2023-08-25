// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// moved out of netlogo-gui into netlogo-core to fix headless compiler error - IOB 8/16/23
case class LabRunOptions(threadCount: Int, table: String, spreadsheet: String, updateView: Boolean, updatePlotsAndMonitors: Boolean)
