// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Toolkit }
import java.awt.event.InputEvent
import javax.swing.{ Action, KeyStroke }

object UserAction {
  /* Key denoting in which menu an action ought to be included */
  val ActionCategoryKey    = "org.nlogo.swing.ActionCategoryKey"
  /* Key for an action to denote what actions it should be grouped with. Allows like actions to be grouped together. */
  val ActionGroupKey       = "org.nlogo.swing.ActionGroupKey"
  /* Key for an action to share a submenu with */
  val ActionSubcategoryKey = "org.nlogo.swing.ActionSubcategoryKey"
  /* Key for an action to indicate it's rank within the group, expressed as a java.lang.Double.
   * Lower ranks are listed before higher ranks. Actions missing this key are assumed to have
   * the highest rank. */
  val ActionRankKey        = "org.nlogo.swing.ActionRankKey"
  /* Key for an action to have a registered component from which to raise events */
  val ActionComponentKey   = "org.nlogo.swing.ActionComponentKey"

  val FileCategory  = "org.nlogo.swing.FileCategory"
  val EditCategory  = "org.nlogo.swing.EditCategory"
  val ToolsCategory = "org.nlogo.swing.ToolsCategory"
  val TabsCategory  = "org.nlogo.swing.TabsCategory"
  val HelpCategory  = "org.nlogo.swing.HelpCategory"

  val EditClipboardGroup = "org.nlogo.swing.EditClipboardGroup"
  val EditSelectionGroup = "org.nlogo.swing.EditSelectionGroup"
  val EditFormatGroup    = "org.nlogo.swing.EditFormatGroup"
  val EditFindGroup      = "org.nlogo.swing.EditFindGroup"
  val EditUndoGroup      = "org.nlogo.swing.EditUndoGroup"

  val FileExportSubcategory = "org.nlogo.swing.FileExportSubcategory"
  val FileImportSubcategory = "org.nlogo.swing.FileImportSubcategory"
  val FileRecentSubcategory = "org.nlogo.swing.FileRecentSubcategory"
  val FileOpenGroup         = "org.nlogo.swing.FileOpenGroup"
  val FileSaveGroup         = "org.nlogo.swing.FileSaveGroup"
  val FileShareGroup        = "org.nlogo.swing.FileShareGroup"
  val FileSwitchArityGroup  = "org.nlogo.swing.FileSwitchArityGroup"

  val HelpWebGroup     = "org.nlogo.swing.HelpWebGroup"
  val HelpContextGroup = "org.nlogo.swing.HelpContextGroup"
  val HelpAboutGroup   = "org.nlogo.swing.HelpAboutGroup"

  val ToolsMonitorGroup = "org.nlogo.swing.ToolsMonitorGroup"
  val ToolsDialogsGroup = "org.nlogo.swing.ToolsDialogsGroup"
  val ToolsHubNetGroup  = "org.nlogo.swing.ToolsHubNetGroup"

  val DefaultGroup = "UndefinedGroup"
  val DefaultRank  = Double.MaxValue

  trait Menu {
    def offerAction(action: javax.swing.Action): Unit
    def revokeAction(action: javax.swing.Action): Unit
  }

  trait CheckBoxAction {
    def checkedState: Boolean
  }

  // convenience methods
  object KeyBindings {

    def keystrokeChar(key: Char, withMenu: Boolean = false, withShift: Boolean = false): KeyStroke = {
      val mask: Int =
        (if (withMenu) Toolkit.getDefaultToolkit.getMenuShortcutKeyMask else 0) | (if (withShift) InputEvent.SHIFT_MASK else 0)
      KeyStroke.getKeyStroke(Character.toUpperCase(key), mask)
    }

    def keystroke(key: Int, withMenu: Boolean = false, withShift: Boolean = false): KeyStroke = {
      val mask: Int =
        (if (withMenu) Toolkit.getDefaultToolkit.getMenuShortcutKeyMask else 0) | (if (withShift) InputEvent.SHIFT_MASK else 0)
      KeyStroke.getKeyStroke(key, mask)
    }
  }

  trait MenuAction { this: Action =>
    def accelerator: Option[KeyStroke] =
      getValue(Action.ACCELERATOR_KEY) match {
        case k: KeyStroke => Some(k)
        case _            => None
      }

    def accelerator_=(k: KeyStroke): Unit = {
      putValue(Action.ACCELERATOR_KEY, k)
    }

    def component: Option[Component] = {
      getValue(ActionComponentKey) match {
        case c: Component => Some(c)
        case _ => None
      }
    }

    def category: Option[String] =
      getValue(ActionCategoryKey) match {
        case s: String => Some(s)
        case _         => None
      }

    def category_=(s: String): Unit = {
      putValue(ActionCategoryKey, s)
    }

    def group: String =
      getValue(ActionGroupKey) match {
        case s: String => s
        case _         => DefaultGroup
      }

    def group_=(s: String): Unit = {
      putValue(ActionGroupKey, s)
    }

    def rank: Double =
      getValue(ActionRankKey) match {
        case d: java.lang.Double => d.doubleValue
        case _                   => Double.MaxValue
      }

    def rank_=(d: Double): Unit = {
      putValue(ActionRankKey, Double.box(d))
    }

    def subcategory: Option[String] =
      getValue(ActionSubcategoryKey) match {
        case s: String => Some(s)
        case _         => None
      }

    def subcategory_=(s: String): Unit = {
      putValue(ActionSubcategoryKey, s)
    }
  }

  implicit class RichUserAction(action: Action) {
    def group: String =
      action.getValue(ActionGroupKey) match {
        case s: String => s
        case _         => DefaultGroup
      }

    def rank: Double =
      action.getValue(ActionRankKey) match {
        case d: java.lang.Double => d.doubleValue
        case _                   => Double.MaxValue
      }

    def subcategory: Option[String] =
      action.getValue(ActionSubcategoryKey) match {
        case s: String => Some(s)
        case _         => None
      }
  }
}

