// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import javax.swing.Action

import org.nlogo.core.I18N
import org.nlogo.swing.{ Menu => SwingMenu, UserAction }

object FileMenu {
  val ExportImportGroup = "ExportImportGroup"

  def sortOrder = Seq(UserAction.FileOpenGroup, UserAction.FileSaveGroup, UserAction.FileShareGroup, ExportImportGroup)
}

import FileMenu._

class FileMenu extends SwingMenu(I18N.gui.get("menu.file"), SwingMenu.model(sortOrder)) {

  implicit val i18nPrefix = I18N.Prefix("menu.file")

  setMnemonic('F')

  val subcategoryNamesAndGroups = Map(
    UserAction.FileExportSubcategory -> (I18N.gui("export") -> ExportImportGroup),
    UserAction.FileImportSubcategory -> (I18N.gui("import") -> ExportImportGroup),
    UserAction.FileRecentSubcategory -> (I18N.gui("recent") -> UserAction.FileOpenGroup))

  override def subcategoryNameAndGroup(key: String): (String, String) = {
    subcategoryNamesAndGroups.get(key).getOrElse(super.subcategoryNameAndGroup(key))
  }

  override def offerAction(action: Action): Unit = {
    val isMac = System.getProperty("os.name").startsWith("Mac")
    if (! (isMac && action.getValue(UserAction.ActionGroupKey) == "Quit"))
      super.offerAction(action)
  }
}
