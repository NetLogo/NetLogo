package org.nlogo.awt;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.Component;
import java.awt.Point;

public strictfp class Mouse {

  // this class is not instantiable
  private Mouse() { throw new IllegalStateException(); }

  public static MouseEvent translateMouseEvent(MouseEvent e,
                                               Component target,
                                               Point offsets) {
    return new MouseEvent(target, e.getID(), e.getWhen(), e.getModifiers(),
        e.getX() + offsets.x, e.getY() + offsets.y,
        e.getClickCount(), e.isPopupTrigger());
  }

  public static boolean button1Mask(MouseEvent e) {
    return (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0;
  }

}
