// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// helper for holding data extracted from a paused BehaviorSpace spreadsheet output file (Isaac B 10/3/25)
class PartialData {
  var runNumbers: String = ""
  var variables: Seq[String] = Seq[String]()
  var reporters: String = ""
  var finals: String = ""
  var mins: String = ""
  var maxes: String = ""
  var means: String = ""
  var steps: String = ""
  var dataHeaders: String = ""
  var data: Seq[String] = Seq[String]()
}
