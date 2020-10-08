// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ BorderLayout, Dimension, Frame }
// import javax.swing.{ JDialog, JPanel, JTabbedPane, WindowConstants }
import javax.swing.{ JDialog, JTabbedPane, WindowConstants }
import org.nlogo.window.Event.LinkChild

class CodeTabContainer(owner:          Frame,
                       codeTabbedPane: JTabbedPane) extends JDialog(owner) with LinkChild    {

  // val reattachPopOut = new javax.swing.JButton("Reattach PopOut")
  // val northPanel = new JPanel
  // def getNorthPanel = northPanel
  // def getReattachPopOut = reattachPopOut
  // northPanel.add(reattachPopOut)
  this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
  // this.add(northPanel, BorderLayout.NORTH)
  this.add(codeTabbedPane, BorderLayout.CENTER)
  this.setSize(new Dimension(600, 400))
  this.setLocationRelativeTo(null)
  this.setVisible(true)
  def getLinkParent: Frame = owner // for Event.LinkChild
}
