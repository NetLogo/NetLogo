// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import javax.swing.AbstractAction
import javax.swing.ImageIcon

/**
 * Small utility class to instantiate an AbstractAction with appropriate icon.
 * Currently only used in the ReviewTab. NP 2013-03-25.
 */
class ReviewAction(name: String, icon: String, fn: () => Unit)
  extends AbstractAction(name) {
  val image = new ImageIcon(classOf[ReviewAction].getResource("/images/" + icon + ".gif"))
  putValue(javax.swing.Action.SMALL_ICON, image)
  def actionPerformed(e: java.awt.event.ActionEvent) { fn() }
}
