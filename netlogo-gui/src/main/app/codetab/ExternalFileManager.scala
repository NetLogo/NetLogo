// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import org.nlogo.util.PathUtils
import org.nlogo.window.{ ExternalFileManager => WindowExternalFileManager }

class ExternalFileManager extends WindowExternalFileManager {
  private var externalTabs = Map.empty[String, TemporaryCodeTab]

  def add(codeTab: TemporaryCodeTab): Unit = {
    codeTab.filename.fold(
      name => externalTabs += (name -> codeTab),
      name => externalTabs += (name -> codeTab))
  }

  def remove(codeTab: TemporaryCodeTab): Unit = {
    codeTab.filename.fold(
      externalTabs -= _,
      externalTabs -= _)
  }

  def nameChanged(oldName: String, newName: String): Unit = {
    val tab = externalTabs.get(oldName)
    externalTabs = (externalTabs - oldName) ++
      (tab.map(t => Map[String, TemporaryCodeTab](newName -> t)) getOrElse
        Map.empty[String, TemporaryCodeTab])
  }

  def getSource(filename: String): Option[String] =
    externalTabs.get(PathUtils.standardize(filename)).map(_.innerSource)
}
