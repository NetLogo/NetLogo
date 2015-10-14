// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

// This is for working around the Swing bug where if you have lots and lots
// of items in your menu they don't scroll or split into multiple columns.
// Here we force splitting into multiple columns.
// This was inspired by cpol's 4/28/00 post to
// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4246124 .

public strictfp class WrappingPopupMenu
    extends javax.swing.JPopupMenu {

  @Override
  public void show(java.awt.Component invoker, int x, int y) {
    setLayout
        (new WrappingLayout
            ((int) (java.awt.Toolkit.getDefaultToolkit()
                .getScreenSize().getHeight()
                * 0.7 / getFontMetrics(getFont()).getHeight())));
    super.show(invoker, x, y);
  }

  // Mac menus are supposed to have top and bottom insets.
  // Quaqua should be taking care of this for us, but unfortunately
  // QuaquaMenuBorder is written so that only instances of JPopupMenu
  // get the right insets, not subclasses of JPopupMenu.  So we force
  // the right insets here ourselves. - ST 1/7/05
  @Override
  public java.awt.Insets getInsets() {
    if (System.getProperty("os.name").startsWith("Mac")) {
      return new java.awt.Insets(4, 0, 4, 0);
    } else {
      return super.getInsets();
    }
  }

  private class WrappingLayout
      implements java.awt.LayoutManager {
    private final int rows;

    public WrappingLayout(int rows) {
      this.rows = rows;
    }

    // not implemented
    public void addLayoutComponent(String name, java.awt.Component comp) {
    }

    // not implemented
    public void removeLayoutComponent(java.awt.Component comp) {
    }

    public void layoutContainer(java.awt.Container target) {
      java.awt.Insets insets = target.getInsets();
      int x = 0;
      int y = insets.top;
      int columnWidth = 0;
      int lastRowStart = 0;
      for (int i = 0; i < target.getComponentCount(); i++) {
        if (i % rows == 0) {
          for (int j = lastRowStart; j < i; j++) {
            target.getComponent(j).setSize
                (columnWidth,
                    target.getComponent(j).getHeight());
          }
          lastRowStart = i;
          x += columnWidth + insets.left;
          columnWidth = 0;
          y = insets.top;
        }
        java.awt.Component comp = target.getComponent(i);
        java.awt.Dimension pref = comp.getPreferredSize();
        comp.setBounds(x, y, pref.width, pref.height);
        if (pref.width > columnWidth) {
          columnWidth = pref.width;
        }
        y += pref.height;
      }
      for (int j = lastRowStart; j < target.getComponentCount(); j++) {
        target.getComponent(j).setSize
            (columnWidth,
                target.getComponent(j).getHeight());
      }
    }

    public java.awt.Dimension minimumLayoutSize(java.awt.Container target) {
      java.awt.Insets insets = target.getInsets();
      int x = 0;
      int columnWidth = 0;
      int columnHeight = 0;
      int maxColumnHeight = 0;
      for (int i = 0; i < target.getComponentCount(); i++) {
        if (i % rows == 0) {
          x += columnWidth + insets.left;
          if (columnHeight > maxColumnHeight) {
            maxColumnHeight = columnHeight;
          }
          columnWidth = 0;
          columnHeight = insets.top + insets.bottom;
        }
        java.awt.Component comp = target.getComponent(i);
        java.awt.Dimension pref = comp.getPreferredSize();
        columnHeight += pref.height;
        if (pref.width > columnWidth) {
          columnWidth = pref.width;
        }
      }
      if (columnHeight > maxColumnHeight) {
        maxColumnHeight = columnHeight;
      }
      return new java.awt.Dimension(x + columnWidth + insets.right,
          maxColumnHeight);
    }

    public java.awt.Dimension preferredLayoutSize(java.awt.Container target) {
      return minimumLayoutSize(target);
    }

  }

}
