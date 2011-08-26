package org.nlogo.awt;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public final strictfp class Focus {

  // this class is not instantiable
  private Focus() { throw new IllegalStateException(); }

  ///

  public static void addNoisyFocusListener(final Component comp) {
    comp.addFocusListener
        (new FocusListener() {
          public void focusGained(FocusEvent fe) {
            System.out.println(comp + " gained focus at " + System.nanoTime());
            System.out.println("oppositeComponent = " + fe.getOppositeComponent());
          }

          public void focusLost(FocusEvent fe) {
            System.out.println(comp + " lost focus at " + System.nanoTime());
            System.out.println("oppositeComponent = " + fe.getOppositeComponent());
          }
        });
  }

}
