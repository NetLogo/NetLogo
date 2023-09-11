// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object LabPostProcessorInputFormat {
  trait Format
  case class Spreadsheet(fileName: String) extends Format
  case class Table(fileName: String) extends Format
}
