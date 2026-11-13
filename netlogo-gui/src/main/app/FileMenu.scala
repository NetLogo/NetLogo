// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ Menu, UserAction },
  UserAction.{ FileExportSubcategory, FileImportSubcategory, FileOpenGroup, FilePrintGroup, FileQuitGroup,
               FileSaveGroup, FileShareGroup, FileRecentSubcategory, FileResourcesGroup, MenuAction }

object FileMenu {
  val ExportImportGroup = "ExportImportGroup"

  def sortOrder = Seq(FileOpenGroup, FileSaveGroup, FileShareGroup, FileResourcesGroup, ExportImportGroup,
                      FilePrintGroup, FileQuitGroup)
}

import FileMenu._

class FileMenu extends Menu(loc"File", Menu.model(sortOrder)) {

  setMnemonic('F')

  val subcategoryNamesAndGroups = Map(
    FileExportSubcategory -> (loc"Export" -> ExportImportGroup),
    FileImportSubcategory -> (loc"Import" -> ExportImportGroup),
    FileRecentSubcategory -> (loc"Recent Files" -> FileOpenGroup))

  override def subcategoryNameAndGroup(key: String): (String, String) = {
    subcategoryNamesAndGroups.get(key).getOrElse(super.subcategoryNameAndGroup(key))
  }

  override def offerAction(action: MenuAction): Unit = {
    val isMac = System.getProperty("os.name").startsWith("Mac")
    if (! (isMac && action.group == "Quit"))
      super.offerAction(action)
  }
}
