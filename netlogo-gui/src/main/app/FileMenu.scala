// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.core.I18N
import org.nlogo.swing.{ Menu, UserAction },
  UserAction.{ FileExportSubcategory, FileImportSubcategory, FileOpenGroup, FileSaveGroup, FileShareGroup,
               FileRecentSubcategory, FileResourcesGroup, MenuAction }

object FileMenu {
  val ExportImportGroup = "ExportImportGroup"

  def sortOrder = Seq(FileOpenGroup, FileSaveGroup, FileShareGroup, FileResourcesGroup, ExportImportGroup)
}

import FileMenu._

class FileMenu extends Menu(I18N.gui.get("menu.file"), Menu.model(sortOrder)) {

  implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("menu.file")

  setMnemonic('F')

  val subcategoryNamesAndGroups = Map(
    FileExportSubcategory -> (I18N.gui("export") -> ExportImportGroup),
    FileImportSubcategory -> (I18N.gui("import") -> ExportImportGroup),
    FileRecentSubcategory -> (I18N.gui("recent") -> FileOpenGroup))

  override def subcategoryNameAndGroup(key: String): (String, String) = {
    subcategoryNamesAndGroups.get(key).getOrElse(super.subcategoryNameAndGroup(key))
  }

  override def offerAction(action: MenuAction): Unit = {
    val isMac = System.getProperty("os.name").startsWith("Mac")
    if (! (isMac && action.group == "Quit"))
      super.offerAction(action)
  }
}
