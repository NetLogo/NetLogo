// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.Editable;
import org.nlogo.api.I18N;
import org.nlogo.api.Property;

import java.util.List;

public strictfp class DummyViewWidget
    extends SingleErrorWidget
    implements Editable {
  private final org.nlogo.agent.World world;

  @Override
  public String classDisplayName() {
    return I18N.guiJ().get("tabs.run.widgets.view");
  }

  public DummyViewWidget(org.nlogo.agent.World world) {
    this.world = world;
    setBackground(java.awt.Color.black);
    setBorder
        (javax.swing.BorderFactory.createCompoundBorder
            (widgetBorder(),
                javax.swing.BorderFactory.createMatteBorder
                    (1, 3, 4, 2,
                        java.awt.Color.black)));
    newWidth = (int) StrictMath.round(world.worldWidth() * world.patchSize());
    newHeight = (int) StrictMath.round(world.worldHeight() * world.patchSize());
    setSize(newWidth, newHeight);
  }

  private int newWidth;

  public int width() {
    return newWidth;
  }

  public void width(int width) {
    newWidth = width;
  }

  private int newHeight;

  public int height() {
    return newHeight;
  }

  public void height(int height) {
    newHeight = height;
  }

  public List<Property> propertySet() {
    return Properties.dummyView();
  }

  @Override
  public boolean editFinished() {
    if (newWidth != getWidth() || newHeight != getHeight()) {
      setSize(new java.awt.Dimension(newWidth, newHeight));
      resetSizeInfo();
    }

    return true;
  }

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension(world.worldWidth(), world.worldHeight());
  }

  @Override
  public boolean needsPreferredWidthFudgeFactor() {
    return false;
  }

  @Override
  public java.awt.Rectangle constrainDrag(java.awt.Rectangle newBounds,
                                          java.awt.Rectangle originalBounds,
                                          MouseMode mouseMode) {
    newWidth = newBounds.width;
    newHeight = newBounds.height;
    return newBounds;
  }

  @Override
  public boolean hasContextMenu() {
    return false;
  }

  /// load & save

  @Override
  public String save() {
    return
        "VIEW\n" +
            getBoundsString() +
            "0\n0\n" + // screen-edge-x/y
            "0\n" + //7
            "1\n1\n" + // 8 9
            // old exactDraw settings, no longer used - ST 8/13/03
            "1\n1\n1\n" +  // 10 11 12
            // hooray for hex! - ST 6/16/04
            "0\n" + // 13
            "1\n1\n" + // 14 15
            "1\n" + // thin turtle pens are always on 16
            world.minPxcor() + "\n" + // 17
            world.maxPxcor() + "\n" + // 18
            world.minPycor() + "\n" + // 19
            world.maxPycor() + "\n";// 20
  }

  @Override
  public Object load(String[] strings, Widget.LoadHelper helper) {
    int x1 = Integer.parseInt(strings[1]);
    int y1 = Integer.parseInt(strings[2]);
    int x2 = Integer.parseInt(strings[3]);
    int y2 = Integer.parseInt(strings[4]);

    double patchSize = Double.parseDouble(strings[7]);
    // in older models don't trust the saved width and height because
    // some are wrong but the patchSize should always be correct.
    // I don't know what the problem was or how long it was a problem
    // but it's irrelevant now that client views can have any dimensions ev 6/20/08
    if (patchSize > 0) {
      setBounds(x1, y1, (int) (patchSize * world.worldWidth()),
          (int) (patchSize * world.worldHeight()));
    } else {
      setBounds(x1, y1, x2 - x1, y2 - y1);
    }

    newWidth = getWidth();
    newHeight = getHeight();

    return this;
  }
}
