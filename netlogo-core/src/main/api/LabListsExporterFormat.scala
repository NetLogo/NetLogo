// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// moved out of netlogo-gui into netlogo-core to fix headless compiler error - IOB 8/29/23
object LabListsExporterFormat {
  trait Format
  case class SpreadsheetFormat(fileName: String) extends Format
  case class TableFormat(fileName: String) extends Format
}
