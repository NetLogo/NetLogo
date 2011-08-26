package org.nlogo.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;

public strictfp class Hierarchy {

  // this class is not instantiable
  private Hierarchy() { throw new IllegalStateException(); }

  /**
   * Returns the frame containing a component.
   */
  public static Frame getFrame(Component comp) {
    Component top = getTopAncestor(comp);
    if (top instanceof Frame) {
      return (Frame) top;
    } else {
      if (top instanceof Window &&
          top.getParent() != null) {
        return getFrame(top.getParent());
      }
      return null;
    }
  }

  /**
   * Returns the window containing a component.
   */
  public static Window getWindow(Component comp) {
    Component top = getTopAncestor(comp);
    if (top instanceof Window) {
      return (Window) top;
    } else {
      return null;
    }
  }

  public static Component getTopAncestor(Component comp) {
    Component top = comp;
    Container parent = top.getParent();
    while (!(top instanceof Window) && null != parent) {
      top = parent;
      parent = top.getParent();
    }
    return top;
  }

  public static boolean hasAncestorOfClass(Component component, Class<?> theClass) {
    return component != null &&
        (theClass.isInstance(component) ||
            hasAncestorOfClass(component.getParent(), theClass));
  }

  public static Container findAncestorOfClass(Component component, Class<?> theClass) {
    if (component == null) {
      return null;
    }
    if (theClass.isInstance(component)) {
      return (Container) component;
    }
    return findAncestorOfClass(component.getParent(), theClass);
  }

}
