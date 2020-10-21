// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ BorderLayout, Dimension, Frame }
import javax.swing.{ JDialog, JTabbedPane, WindowConstants }
import org.nlogo.window.Event.LinkChild

// This is the separate code tab window.
// It contains the CodeTabsPanel which owns and manages the CodeTabs.
// It is created and destroyed 'on demand' as needed.
class CodeTabContainer(owner:          Frame,
                       codeTabbedPane: JTabbedPane) extends JDialog(owner) with LinkChild    {

  this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
  this.add(codeTabbedPane, BorderLayout.CENTER)
  this.setSize(new Dimension(600, 400))
  this.setLocationRelativeTo(null)
  this.setVisible(true)
  // This is needed for proper Event handling
  def getLinkParent: Frame = { owner } // for Event.LinkChild
}
