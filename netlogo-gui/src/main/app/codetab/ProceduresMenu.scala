// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.{ AbstractAction, JMenuItem, JPopupMenu, JTextField, MenuSelectionManager, SwingUtilities }
import java.text.Collator
import java.util.prefs.{ Preferences => JavaPreferences }

import org.nlogo.awt.EventQueue
import org.nlogo.core.I18N
import org.nlogo.swing.Implicits._
import org.nlogo.swing.{ MenuItem, RoundedBorderPanel, ToolBarMenu }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class ProceduresMenu(target: ProceduresMenuTarget)
extends ToolBarMenu(I18N.gui.get("tabs.code.procedures")) with RoundedBorderPanel with ThemeSync {

  // Locale-aware, case-insensitive ordering for optional alphabetic sorting of procedures:
  private lazy val ordering = {
    val locale = I18N.localeFromPreferences.getOrElse(I18N.gui.defaultLocale)
    Ordering.comparatorToOrdering(Collator.getInstance(locale))
  }

  setDiameter(6)
  enableHover()

  override def populate(menu: JPopupMenu) {
    menu.setBackground(InterfaceColors.MENU_BACKGROUND)

    val procsTable = {
      target.compiler.findProcedurePositions(target.getText)
    }

    val procs = {
      val prefs = JavaPreferences.userRoot.node("/org/nlogo/NetLogo")
      val AlphaSort = I18N.gui.get("tools.preferences.proceduresSortAlphabetical")
      val sort: Seq[String] => Seq[String] =
        prefs.get("proceduresMenuSortOrder", "default") match {
          // Sort procedures by alphabetical order if requested in preferences
          case AlphaSort => _.sorted(ordering)
          // Otherwise, sort procedures by order of appearance in the code tab
          case default => _.sortBy(procsTable(_).identifier.start)
        }
      sort(procsTable.keys.toSeq)
    }

    val items = procs.map { proc =>
      val namePos = procsTable(proc).identifier.start
      val end  = procsTable(proc).endKeyword.end
      new MenuItem(new AbstractAction(proc) {
        def actionPerformed(e: ActionEvent) {
          // invokeLater for the scrolling behavior we want. we scroll twice: first bring the end into
          // view, then bring the beginning into view, so then we can see both, if they fit - ST 11/4/04
          target.select(end, end)
          EventQueue.invokeLater{() =>
            target.select(namePos, namePos + proc.length)  // highlight the name
          }
        }
      })
    }

    val filterField = new JTextField {
      setBackground(InterfaceColors.MENU_BACKGROUND)
      setForeground(InterfaceColors.TOOLBAR_TEXT)
      setCaretColor(InterfaceColors.TOOLBAR_TEXT)
    }
    filterField.getDocument.addDocumentListener(() => {
      repopulate(menu, filterField, items)
      menu.getSubElements.collectFirst {
        case it: JMenuItem => MenuSelectionManager.defaultManager.setSelectedPath(Array(menu, it))
        case _ =>
      }

      // Changing the number of items changes the desired size of the menu
      // -BCH 1/29/2018
      menu.validate() // recalculate correct size
      menu.setSize(menu.getPreferredSize) // resize to that size
      // Resize the popup to match. On Mac, the popup has is in its own undecorated window, so we resize root. On
      // Windows and Linux, the root of the menu is the app itself, but the parent is the popup, so resize that.
      // -BCH 1/29/2018
      // this no longer works as commented after switching to FlatLaf, resizing root now works on all platforms.
      // (IB 11/1/24)
      val r = SwingUtilities.getRoot(menu)
      r.setSize(r.getPreferredSize)
      r.validate()
      r.repaint()
    })

    filterField.addKeyListener { e: KeyEvent =>
      // Although it seems like you should just be able to do:
      // MenuSelectionManager.defaultManager().processKeyEvent(e)
      // here and have arrow keys and enter work, this is not the case.
      // Instead, we have to pass through the keyboard events to the menu itself to make arrow keys work.
      // We have to explicitly simulate the click on enter for enter to work.
      // I decided to pass through ALL keys instead of just the keys we care about because we can't guarantee that
      // menu manipulation keys are the same on all systems (nor that I know about all of them).
      // Furthermore, there are no detrimental effects of passing on the keyboard events. -BCH 1/29/2018
      menu.dispatchEvent(e)
      if (e.getKeyCode == KeyEvent.VK_ENTER) {
        val path = MenuSelectionManager.defaultManager().getSelectedPath
        if (path.nonEmpty) path.last match {
          case it: JMenuItem if it.isArmed =>
            it.doClick()
            menu.setVisible(false)
          case _ =>
        }
      }
    }

    menu.add(filterField)
    repopulate(menu, filterField, items)

    // Windows and Linux seem to ignore requests for focus made before the popup is visible (or even right after it
    // becomes the call for visibility is made), so we delay the request until the next loop of the EDT. Note this line
    // is unnecessary for Mac, which grants focus to the field by default.
    // - BCH 1/31/2018
    SwingUtilities.invokeLater(() => filterField.requestFocusInWindow())
  }

  private def repopulate(menu: JPopupMenu, filterField: JTextField, items: Seq[JMenuItem]): Unit = {
    val query = filterField.getText

    // If the filterField is removed and re-added (as would happen if we completely cleared the menu), it loses focus on
    // every keypress in Windows and Linux. So we just remove the menu items (which makes sense anyway).
    // - BCH 1/31/2018

    // This will include a <none> item if it's there
    menu.getSubElements.collect{case (it: JMenuItem) => it}.foreach(menu.remove)
    val visibleItems =
      if (query.isEmpty) items // So they aren't sorted
      else items.zip(items.map(it => fuzzyMatch(query, it.getText, lastMatch = true))) // score
        .collect{case (it, Some(score)) => (it, score)}
        .sortBy(-_._2)
        .map(_._1)
    if (visibleItems.isEmpty)
      menu.add(new MenuItem("<"+I18N.gui.get("tabs.code.procedures.none")+">")).setEnabled(false)
    else
      visibleItems.foreach(menu.add)
  }

  /**
    * Scores how well the query matches the target. The score is the number of subsequent character matches.
    * For example, fuzzyMatch("bcdfg", "abcdefg") == Some(3) with subsequent matches being "bc", "cd", and "fg".
    * Matching first character is also counted as a subsequent match so that
    * fuzzyMatch("ab", "abc") > fuzzyMatch("ab", "cab") since people often start out typing at the beginning of words.
    *
    * Note that this could easily be optimized with memoization or dynamic programming, but I didn't think it was worth
    * it for this use case since query strings will typically be quite short. -- BCH 6/8/2018
    * @param query The characters to search for.
    * @param target The string to search in.
    * @param lastMatch Whether or not the last comparison was a match. Starts true to give a match at the first
    *                  character a bonus.
    * @return The number of subsequent matching characters or None if the query contains characters not in the target.
    */
  private def fuzzyMatch(query: String, target: String, lastMatch: Boolean = true): Option[Int] = {
    if (query.isEmpty) {
      Some(0)
    } else if (target.isEmpty) {
      None
    } else {
      val noMatchScore = fuzzyMatch(query, target.tail, lastMatch = false)
      val matchScore = if (query.head == target.head)
        fuzzyMatch(query.tail, target.tail).map(_ + (if(lastMatch) 1 else 0))
      else None
      (noMatchScore, matchScore) match {
        case (None, None) => None
        case (Some(nms), Some(ms)) => Some(nms max ms)
        case _ => noMatchScore orElse matchScore
      }
    }
  }

  def syncTheme() {
    setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
    setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)

    label.setForeground(InterfaceColors.TOOLBAR_TEXT)
  }
}
