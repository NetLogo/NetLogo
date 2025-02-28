// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Cursor, Point, Toolkit }
import java.awt.image.BufferedImage

sealed trait InterfaceMode {
  def cursor: Cursor =
    Cursor.getDefaultCursor
}

object InterfaceMode {
  case object Interact extends InterfaceMode
  case object Select extends InterfaceMode
  case object Add extends InterfaceMode {
    // the shadow widget functions as the cursor when adding a widget, so this removes the default cursor (Isaac B 2/14/25)
    override def cursor =
      Toolkit.getDefaultToolkit.createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
                                                   new Point(0, 0), null)
  }
  case object Edit extends InterfaceMode
  case object Delete extends InterfaceMode
}
