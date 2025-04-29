// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

/** note that multiple instances of this class may exist as there are now multiple frames that each
 have their own menu bar and menus ev 8/25/05 */

import javax.swing.Action

import org.nlogo.api.Refreshable
import org.nlogo.core.I18N
import org.nlogo.swing.{ Menu, UserAction },
  UserAction.{ EditClipboardGroup, EditFindGroup, EditFormatGroup, EditSelectionGroup, EditUndoGroup }

object EditMenu {
  def sortOrder = Seq(EditUndoGroup, EditClipboardGroup, EditSelectionGroup, EditFindGroup, EditFormatGroup)
}

class EditMenu extends Menu(I18N.gui.get("menu.edit"), Menu.model(EditMenu.sortOrder)) {

  implicit val i18nName: org.nlogo.core.I18N.Prefix = I18N.Prefix("menu.edit")

  private var refreshables = Set.empty[Refreshable]

  setMnemonic('E')

  addMenuListener(new javax.swing.event.MenuListener() {
    override def menuSelected(e: javax.swing.event.MenuEvent): Unit = {
      refreshables.foreach(_.refresh())
    }

    override def menuDeselected(e: javax.swing.event.MenuEvent): Unit = { }

    override def menuCanceled(e: javax.swing.event.MenuEvent): Unit = { }
  })

  override def offerAction(action: Action): Unit = {
    action match {
      case refreshable: Refreshable => refreshables = refreshables + refreshable
      case _ =>
    }
    super.offerAction(action)
  }
}
