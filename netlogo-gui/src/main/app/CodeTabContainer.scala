// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ BorderLayout, Dimension, Frame }
import javax.swing.{ JDialog, JPanel, JTabbedPane, WindowConstants }
import org.nlogo.window.Event.LinkChild

class CodeTabContainer(owner:          Frame,
                       codeTabbedPane: JTabbedPane) extends JDialog(owner)
    with LinkChild    {
    val killPopOut = new javax.swing.JButton("Kill PopOut")
    val northPanel = new JPanel
    def getNorthPanel = northPanel
    def getKillPopOut =killPopOut
    northPanel.add(killPopOut)
    this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
    this.add(northPanel, BorderLayout.NORTH)
    this.add(codeTabbedPane, BorderLayout.CENTER)
    this.setSize(new Dimension(600, 400))
    this.setLocationRelativeTo(null)
    this.setVisible(true)
    def getLinkParent: Frame = owner // for Event.LinkChild

    // val contentPane = this.getContentPane.asInstanceOf[JPanel]
    //
    // //val inputMap: InputMap = contentPane.getInputMap(JComponent.WHEN_FOCUSED)
    // val inputMap: InputMap = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    // //val inputMap: InputMap = contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
    //
    // val actionMap: ActionMap = contentPane.getActionMap();
    // val SWITCH_FOCUS1 = "switchFocus1"
    //
    // val key = KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
    // inputMap.put(key, SWITCH_FOCUS1)
    // actionMap.put(SWITCH_FOCUS1, SwitchFocusAction)
  }
