// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

// if the size of the border surrounding the View changes, be sure
// to change the associated constants in ModelLoader

import org.nlogo.api.Dump;
import org.nlogo.api.I18N;

public strictfp class ViewWidget
    extends Widget
    implements
    ViewWidgetInterface,
    org.nlogo.window.Events.PeriodicUpdateEvent.Handler,
    org.nlogo.window.Events.LoadBeginEvent.Handler,
    org.nlogo.window.Events.LoadEndEvent.Handler {

  @Override
  public String classDisplayName() {
    return "World & View";
  }

  private final GUIWorkspace workspace;
  public final View view;
  public final ViewControlStrip controlStrip;
  public final javax.swing.JLabel tickCounter = new TickCounterLabel();
  public final DisplaySwitch displaySwitch;

  private static final int INSIDE_BORDER_HEIGHT = 1;

  public final int getExtraHeight() {
    return getInsets().top + getInsets().bottom + INSIDE_BORDER_HEIGHT;
  }

  public int getAdditionalHeight() {
    return (getExtraHeight() + controlStrip.getHeight());
  }

  ViewWidget(GUIWorkspace workspace) {
    this.workspace = workspace;
    displaySwitch = new DisplaySwitch(workspace);
    org.nlogo.awt.Fonts.adjustDefaultFont(tickCounter);
    view = new View(workspace);

    controlStrip = new ViewControlStrip(workspace, this);
    setBackground(InterfaceColors.GRAPHICS_BACKGROUND);
    setBorder
        (javax.swing.BorderFactory.createCompoundBorder
            (widgetBorder(),
                javax.swing.BorderFactory.createMatteBorder
                    (1, 3, 4, 2,
                        InterfaceColors.GRAPHICS_BACKGROUND)));
    setLayout(null);
    add(view);
    add(controlStrip);
    if (org.nlogo.api.Version.is3D()) {
      settings = new WorldViewSettings3D(workspace, this);
    } else {
      settings = new WorldViewSettings2D(workspace, this);
    }
  }

  private final WorldViewSettings settings;

  public WorldViewSettings settings() {
    return settings;
  }

  @Override
  public void doLayout() {
    int availableWidth = getWidth() - getInsets().left - getInsets().right;
    double patchSize = computePatchSize(availableWidth, workspace.world.worldWidth());
    int graphicsHeight =
        (int) StrictMath.round(patchSize * workspace.world.worldHeight());
    int stripHeight = getHeight() - graphicsHeight - getInsets().top - getInsets().bottom;
    // Note that we set the patch size first and then set the bounds of the view.
    // view.setBounds will force the Renderer to a particular size, overriding the
    // calculation the Render makes internally if need be -- CLB
    view.visualPatchSize(patchSize);
    view.setBounds
        (getInsets().left, getInsets().top + INSIDE_BORDER_HEIGHT + stripHeight,
            availableWidth, graphicsHeight);
    controlStrip.setBounds
        (getInsets().left, getInsets().top,
            availableWidth, stripHeight);
  }

  @Override
  public Object getEditable() {
    return settings;
  }

  public double computePatchSize(int width, int numPatches) {
    // This is sneaky.  We'd rather not have numbers with a zillion decimal places
    // show up in "Patch Size" when you edit the graphics window.
    // So instead of setting the patch to the exact quotient of
    // the size in pixels divided by the number of patches, we set
    // it to the number with the least junk after the decimal
    // point that still rounds to the correct # of pixels - ST 4/6/03
    double exactPatchSize = (double) width / (double) numPatches;
    for (int precision = 0; precision < 15; precision++) {
      double roundedPatchSize = org.nlogo.api.Approximate.approximate(exactPatchSize, precision);
      if ((int) (numPatches * roundedPatchSize) == width) {
        return roundedPatchSize;
      }
    }
    return exactPatchSize;
  }

  /// sizing

  @Override
  public java.awt.Dimension getPreferredSize(java.awt.Font font) {
    // just returning zeros prevents the "smart" preferred-size
    // code in EditView from getting confused - ST 6/6/02
    return new java.awt.Dimension(0, 0);
  }

  @Override
  public boolean needsPreferredWidthFudgeFactor() {
    return false;
  }

  @Override
  public java.awt.Dimension getMinimumSize() {
    java.awt.Dimension gSize = view.getMinimumSize();
    java.awt.Dimension stripSize = controlStrip.getMinimumSize();
    if (gSize.width > stripSize.width) {
      return new java.awt.Dimension
          (gSize.width + getInsets().left + getInsets().right,
              gSize.height + getExtraHeight() + stripSize.height);
    } else {
      // this gets tricky because if it's the control strip that's
      // determining the minimum width, then we need to calculate
      // what the graphics window's height will be at that width
      int ssx = workspace.world.worldWidth();
      int ssy = workspace.world.worldHeight();
      double minPatchSize = computePatchSize(stripSize.width, ssx);
      return new java.awt.Dimension
          (stripSize.width + getInsets().left + getInsets().right,
              stripSize.height + getExtraHeight() + (int) (minPatchSize * ssy));
    }
  }

  public int insetWidth() {
    return getInsets().left + getInsets().right;
  }

  public int calculateWidth(int worldWidth, double patchSize) {
    return (int) (worldWidth * patchSize) + getInsets().right + getInsets().left;
  }

  public int calculateHeight(int worldHeight, double patchSize) {
    java.awt.Dimension stripSize = controlStrip.getMinimumSize();
    return stripSize.height + getExtraHeight() + (int) (patchSize * worldHeight);
  }

  public int getMinimumWidth() {
    return controlStrip.getMinimumSize().width + insetWidth();
  }

  void resetSize() {
    view.setSize(workspace.world.worldWidth(), workspace.world.worldHeight(),
        workspace.world.patchSize());

    java.awt.Dimension dim = view.getPreferredSize();

    setSize(dim.width + getInsets().left + getInsets().right,
        dim.height + getExtraHeight() + controlStrip.getPreferredSize().height);
    doLayout();
    resetZoomInfo();
  }

  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height);
    new org.nlogo.window.Events.ResizeViewEvent
        (workspace.world.worldWidth(), workspace.world.worldHeight())
        .raise(this);
  }

  @Override
  public void setBounds(int x, int y, int width, int height) {
    java.awt.Rectangle bounds = getBounds();
    // only set the bounds if they've changed
    if (width != bounds.width || height != bounds.height || x != bounds.x || y != bounds.y) {
      super.setBounds(x, y, width, height);
      resetSizeInfo();
    }
  }

  @Override
  public void setBounds(java.awt.Rectangle bounds) {
    setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
  }

  @Override
  public String getBoundsString() {
    // Oh man, this is hairy.  The purpose of this method is to
    // determine what bounds are written out when the model is
    // saved.  In the case of ViewWidget, the position information
    // is used at load time, but the sizing information is
    // ignored; instead, we let the size be determined by the
    // patch size.  However, there is one place where it matters
    // what size we write, and that's for the applets on the
    // NetLogo website.  We have Perl code on the site that looks
    // at all of the widgets in a model and computes their overall
    // bounding rectangle, so it knows what overall size to make
    // the applet for that model.  That sizing code runs into
    // trouble in the case of Algae, or any model where the world
    // is very tall and skinny, because in the applet,
    // ViewWidget's minimum width is larger than in the app,
    // because in the app the view control strip doesn't contain
    // the speed slider, but in the applet, it does.  So we need
    // to override here in order to write out a size that's right
    // for the applet.  The overriding is slightly tricky because
    // we need to make sure that we're working with an unzoomed size,
    // not a zoomed size. - ST 1/20/11
    java.awt.Rectangle r =
        findWidgetContainer() == null
            ? getBounds()
            : findWidgetContainer().getUnzoomedBounds(this);
    // The 245 here was determined empirically by measuring the width
    // on Mac OS X and then adding some slop.  Yes, this an incredible
    // kludge, but I figure it's not worth it to try to do it the right
    // way, since all of this view widget sizing code is targeted to be
    // thrown out and redone anyway - ST 1/20/11
    int width = StrictMath.max(245, r.width);
    // and, ugh, this is copy-and-pasted from Widget.java. more
    // kludginess - ST 1/20/11
    StringBuilder buf = new StringBuilder();
    buf.append(r.x + "\n");
    buf.append(r.y + "\n");
    buf.append((r.x + width) + "\n");
    buf.append((r.y + r.height) + "\n");
    return buf.toString();
  }

  @Override
  public java.awt.Rectangle constrainDrag(java.awt.Rectangle newBounds,
                                          java.awt.Rectangle originalBounds,
                                          MouseMode mouseMode) {
    int stripHeight = controlStrip.getMinimumSize().height;
    double patchSizeBasedOnNewWidth =
        computePatchSize(newBounds.width - getInsets().left + getInsets().right,
            workspace.world.worldWidth());
    double patchSizeBasedOnNewHeight =
        computePatchSize(newBounds.height - stripHeight - getExtraHeight(),
            workspace.world.worldHeight());
    double newPatchSize;
    // case 1: only width changed; adjust height to match
    if (newBounds.height == originalBounds.height) {
      newPatchSize = patchSizeBasedOnNewWidth;
    }
    // case 2: only height changed; adjust width to match
    else if (newBounds.width == originalBounds.width) {
      newPatchSize = patchSizeBasedOnNewHeight;
    }
    // case 3: they both changed, use whichever results in the larger patch length
    else {
      newPatchSize = StrictMath.max(patchSizeBasedOnNewWidth, patchSizeBasedOnNewHeight);
    }

    // since the new patch size is based on the new width make sure
    // to take into account the change in width due to zooming
    // newPatchSize -= view.renderer.zoom() ;
    workspace.world.patchSize(newPatchSize);
    view.setSize(workspace.world.worldWidth(), workspace.world.worldHeight(),
        newPatchSize);

    view.renderer.trailDrawer().rescaleDrawing();

    int newWidth = (int)
        (newPatchSize * workspace.world.worldWidth()) +
        getInsets().left + getInsets().right;
    int newHeight = (int)
        (newPatchSize * workspace.world.worldHeight()) +
        getExtraHeight() + stripHeight;
    int widthAdjust = newBounds.width - newWidth;
    int heightAdjust = newBounds.height - newHeight;
    int newX = newBounds.x;
    int newY = newBounds.y;
    switch (mouseMode) {
      case NE:
        newY += heightAdjust;
        break;
      case NW:
        newX += widthAdjust;
        newY += heightAdjust;
        break;
      case SE:
        break;
      case SW:
        newX += widthAdjust;
        break;
      case S:
        break;
      case W:
        newX += widthAdjust;
        break;
      case E:
        break;
      case N:
        newY += heightAdjust;
        break;
      default:
        throw new IllegalStateException();
    }
    switch (mouseMode) {
      case N:
      case S: {
        int midpointX = originalBounds.x + originalBounds.width / 2;
        newX = midpointX - newWidth / 2;
        break;
      }
      case E:
      case W: {
        int midpointY = originalBounds.y + originalBounds.height / 2;
        newY = midpointY - newHeight / 2;
        break;
      }
      default:
        // do nothing
        break;
    }
    return new java.awt.Rectangle(newX, newY, newWidth, newHeight);
  }

  /// font handling for turtle and patch labels

  void applyNewFontSize(int newFontSize) {
    java.awt.Font font = view.getFont();
    int zoomDiff = font.getSize() - view.fontSize();
    view.applyNewFontSize(newFontSize, zoomDiff);
  }

  /// tell the zooming code it's OK to grab our subcomponents and zoom them too

  @Override
  public boolean zoomSubcomponents() {
    return true;
  }

  /// ViewWidgetInterface

  public Widget asWidget() {
    return this;
  }

  /// events

  private String tickCounterLabel;

  public void handle(org.nlogo.window.Events.LoadBeginEvent e) {
    tickCounter.setText("");
    tickCounterLabel = "ticks";
    tickCounter.setVisible(true);
  }

  public void handle(org.nlogo.window.Events.LoadEndEvent e) {
    controlStrip.reset();
  }

  public void handle(org.nlogo.window.Events.PeriodicUpdateEvent e) {
    double ticks = workspace.world.tickCounter.ticks();
    String tickText =
        ticks == -1
            ? ""
            : Dump.number(StrictMath.floor(ticks));
    tickCounter.setText("     " + tickCounterLabel + ": " + tickText);
  }

  /// tick counter

  public void showTickCounter(boolean visible) {
    tickCounter.setVisible(visible);
  }

  public boolean showTickCounter() {
    return tickCounter.isVisible();
  }

  public void tickCounterLabel(String label) {
    tickCounterLabel = label;
    handle((org.nlogo.window.Events.PeriodicUpdateEvent) null);
  }

  public String tickCounterLabel() {
    return tickCounterLabel;
  }

  private class TickCounterLabel
      extends javax.swing.JLabel {
    @Override
    public java.awt.Dimension getPreferredSize() {
      return getMinimumSize();
    }

    @Override
    public java.awt.Dimension getMinimumSize() {
      java.awt.Dimension d = super.getMinimumSize();
      java.awt.FontMetrics fontMetrics = getFontMetrics(getFont());
      d.width = StrictMath.max(d.width,
          fontMetrics.stringWidth(tickCounterLabel + ": 00000000"));
      return d;
    }
  }

  @Override
  public boolean hasContextMenu() {
    return true;
  }

  @Override
  public java.awt.Point populateContextMenu(javax.swing.JPopupMenu menu, java.awt.Point p,
                                            java.awt.Component source) {
    return view.populateContextMenu(menu, p, source);
  }

  /// display switch

  void displaySwitchOn(boolean on) {
    displaySwitch.actionPerformed(null);
  }


  /// load & save

  @Override
  public String save() {
    return settings.save();
  }

  @Override
  public Object load(String[] strings, Widget.LoadHelper helper) {
    return settings.load(strings, helper.version());
  }

  @Override
  public boolean copyable() {
    return false;
  }
}
