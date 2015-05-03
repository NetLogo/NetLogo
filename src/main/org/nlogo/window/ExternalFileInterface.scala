// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ AgentKind, SourceOwner }

class ExternalFileInterface(fileName: String) extends SourceOwner {
  def getFileName: String = fileName
  def classDisplayName: String = "ExternalFileInterface"
  def kind: AgentKind = AgentKind.Observer
  def headerSource: String = ""
  def innerSource: String = ""
  def innerSource_=(s: String): Unit = {}
  def source: String = headerSource + innerSource
}
