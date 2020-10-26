// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ BorderLayout, Dimension, Frame }
import javax.swing.{ JDialog, JTabbedPane, WindowConstants }
import org.nlogo.window.Event.LinkChild

// This is the separate code tab window.
// It contains the CodeTabsPanel which owns and manages the CodeTabs.
// It is created and destroyed 'on demand' as needed. AAB 10/2020
class CodeTabContainer(owner:          Frame,
                       codeTabbedPane: JTabbedPane) extends JDialog(owner) with LinkChild {

  this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
  this.add(codeTabbedPane, BorderLayout.CENTER)
  this.setSize(new Dimension(600, 400))
  this.setLocationRelativeTo(null)
  this.setVisible(true)
  // This is needed for proper Event handling AAB 10/2020
  def getLinkParent: Frame = { owner }
}
