// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.CompilerException;
import org.nlogo.api.Editable;
import org.nlogo.api.I18N;
import org.nlogo.api.Property;
import org.nlogo.nvm.Workspace;

import java.util.ArrayList;
import java.util.List;

public abstract strictfp class WorldViewSettings
    implements Editable,
    org.nlogo.workspace.WorldLoaderInterface,
    org.nlogo.api.WorldPropertiesInterface {
  protected final GUIWorkspace workspace;
  protected final ViewWidget gWidget;

  public String classDisplayName() {
    return "Model Settings";
  }

  WorldViewSettings(GUIWorkspace workspace, ViewWidget gw) {
    this.workspace = workspace;
    gWidget = gw;
    addProperties();
  }

  public abstract void resizeWithProgress(boolean showProgress);

  public abstract String save();

  public abstract void addWrappingProperties();

  public abstract void addDimensionProperties();

  protected List<Property> dimensionProperties;
  protected List<Property> wrappingProperties;
  protected List<Property> viewProperties;
  protected List<Property> modelProperties;
  protected List<OriginConfiguration> cornerChoices;
  protected List<OriginConfiguration> edgeChoices;
  protected List<OriginConfiguration> originConfigurations;

  protected void addProperties() {
    propertySet(new ArrayList<Property>());
    dimensionProperties = new ArrayList<Property>();
    addDimensionProperties();
    wrappingProperties = new ArrayList<Property>();
    addWrappingProperties();
    viewProperties = new ArrayList<Property>();
    addViewProperties();
    modelProperties = new ArrayList<Property>();
    addModelProperties();
    cornerChoices = new ArrayList<OriginConfiguration>();
    addCornerChoices();
    edgeChoices = new ArrayList<OriginConfiguration>();
    addEdgeChoices();
    originConfigurations = new ArrayList<OriginConfiguration>();
    addOriginConfigurations();
  }

  public List<Property> getDimensionProperties() {
    return dimensionProperties;
  }

  public List<Property> getWrappingProperties() {
    return wrappingProperties;
  }

  public List<Property> getViewProperties() {
    return viewProperties;
  }

  public List<Property> getModelProperties() {
    return modelProperties;
  }

  public List<OriginConfiguration> getCornerChoices() {
    return cornerChoices;
  }

  public List<OriginConfiguration> getEdgeChoices() {
    return edgeChoices;
  }

  public List<OriginConfiguration> getOriginConfigurations() {
    return originConfigurations;
  }

  public void addViewProperties() {
    viewProperties.addAll(Properties.view2D());
  }

  public void addModelProperties() {
    modelProperties.addAll(Properties.model());
  }

  public void refreshViewProperties(boolean threedView) {
    viewProperties.clear();
    addViewProperties();
    if (threedView) {
      viewProperties.addAll(Properties.view3D());
    }
  }

  public void addCornerChoices() {
    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.corner.bottomLeft"),
        new boolean[]{false, true, false, true},
        new boolean[]{true, false, true, false}));
    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.corner.topLeft"),
        new boolean[]{false, true, true, false},
        new boolean[]{true, false, false, true}));
    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.corner.topRight"),
        new boolean[]{true, false, true, false},
        new boolean[]{false, true, false, true}));
    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.corner.bottomRight"),
        new boolean[]{true, false, false, true},
        new boolean[]{false, true, true, false}));
  }

  public void addEdgeChoices() {
    edgeChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.edge.bottom"),
        new boolean[]{true, true, false, true},
        new boolean[]{false, false, true, false}));
    edgeChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.edge.top"),
        new boolean[]{true, true, true, false},
        new boolean[]{false, false, false, true}));
    edgeChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.edge.right"),
        new boolean[]{true, false, true, true},
        new boolean[]{false, true, false, false}));
    edgeChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.edge.left"),
        new boolean[]{false, true, true, true},
        new boolean[]{true, false, false, false}));
  }

  public void addOriginConfigurations() {
    originConfigurations.add(new OriginConfiguration
        (I18N.guiJ().get("edit.viewSettings.origin.location.center"),
            new boolean[]{false, true, false, true},
            new boolean[]{false, false, false, false}));
    originConfigurations.add(new OriginConfiguration
        (I18N.guiJ().get("edit.viewSettings.origin.location.corner"),
            new boolean[]{true, true, true, true},
            new boolean[]{false, false, false, false}));
    originConfigurations.add(new OriginConfiguration
        (I18N.guiJ().get("edit.viewSettings.origin.location.edge"),
            new boolean[]{true, true, true, true},
            new boolean[]{false, false, false, false}));
    originConfigurations.add(new OriginConfiguration
        (I18N.guiJ().get("edit.viewSettings.origin.location.custom"),
            new boolean[]{true, true, true, true},
            new boolean[]{false, false, false, false}));
  }

  public int firstEditor() {
    return 0;
  }

  public int lastEditor() {
    return 3;
  }

  public int getSelectedLocation() {
    int minx = minPxcor();
    int maxx = maxPxcor();
    int miny = minPycor();
    int maxy = maxPycor();

    if (minx == (-maxx) && miny == (-maxy)) {
      return 0;
    } else if ((minx == 0 || maxx == 0) && (miny == 0 || maxy == 0)) {
      return 1;
    } else if (minx == 0 || maxx == 0 || miny == 0 || maxy == 0) {
      return 2;
    }

    return 3;
  }

  public int getSelectedConfiguration() {
    int minx = minPxcor();
    int maxx = maxPxcor();
    int miny = minPycor();
    int maxy = maxPycor();

    if (minx == 0 && miny == 0) {
      return 0;
    } else if (minx == 0 && maxy == 0) {
      return 1;
    } else if (maxx == 0 && maxy == 0) {
      return 2;
    } else if (maxx == 0 && miny == 0) {
      return 3;
    } else if (minx == 0) {
      return 3;
    } else if (maxx == 0) {
      return 2;
    } else if (miny == 0) {
      return 0;
    } else if (maxy == 0) {
      return 1;
    } else {
      return 0;
    }
  }

  public Object load(String[] strings, String version) {
    workspace.loadWorld(strings, version, this);
    // we can't clearAll here because the globals may not
    // be allocated yet ev 7/12/06
    // note that we clear turtles inside the load method so
    // it can happen before we set the topology ev 7/19/06
    workspace.world.tickCounter.clear();
    workspace.world.clearPatches();
    workspace.world.displayOn(true);
    return this;
  }

  public boolean smooth() {
    return workspace.glView.antiAliasingOn();
  }

  public void smooth(boolean smooth) {
    if (workspace.glView.antiAliasingOn() != smooth) {
      workspace.glView.antiAliasingOn(smooth);
    }
  }

  public boolean wireframe() {
    return workspace.glView.wireframeOn();
  }

  public void wireframe(boolean on) {
    if (on != wireframe()) {
      workspace.glView.wireframeOn_$eq(on);
      workspace.glView.repaint();
    }
  }

  public boolean dualView() {
    return workspace.dualView();
  }

  public void dualView(boolean on) {
    workspace.dualView(on);
  }

  public scala.Option<String> helpLink() {
    return scala.Option.apply(null);
  }

  protected List<Property> propertySet = null;

  public List<Property> propertySet() {
    return propertySet;
  }

  public void propertySet(List<Property> propertySet) {
    this.propertySet = propertySet;
  }

  protected boolean wrappingChanged = false;
  protected boolean edgesChanged = false;
  protected boolean patchSizeChanged = false;
  protected boolean fontSizeChanged = false;
  protected double newPatchSize;

  protected int newMinX;
  protected int newMaxX;
  protected int newMinY;
  protected int newMaxY;

  protected boolean newWrapX;
  protected boolean newWrapY;

  public void minPxcor(int minPxcor) {
    if (minPxcor <= 0) {
      newMinX = minPxcor;
      edgesChanged = edgesChanged || (newMinX != workspace.world.minPxcor());
    }
  }

  public int minPxcor() {
    return newMinX;
  }

  public void maxPxcor(int maxPxcor) {
    if (maxPxcor >= 0) {
      newMaxX = maxPxcor;
      edgesChanged = edgesChanged || (newMaxX != workspace.world.maxPxcor());
    }
  }

  public int maxPxcor() {
    return newMaxX;
  }

  public void minPycor(int minPycor) {
    if (minPycor <= 0) {
      newMinY = minPycor;
      edgesChanged = edgesChanged || (newMinY != workspace.world.minPycor());
    }
  }

  public int minPycor() {
    return newMinY;
  }

  public void maxPycor(int maxPycor) {
    if (maxPycor >= 0) {
      newMaxY = maxPycor;
      edgesChanged = edgesChanged || (newMaxY != workspace.world.maxPycor());
    }
  }

  public int maxPycor() {
    return newMaxY;
  }

  public void patchSize(double size) {
    newPatchSize = size;
    patchSizeChanged = patchSizeChanged || (size != patchSize());
  }

  public double patchSize() {
    return workspace.world.patchSize();
  }

  public Workspace.UpdateMode updateMode() {
    return workspace.updateMode();
  }

  public void updateMode(Workspace.UpdateMode updateMode) {
    workspace.updateMode(updateMode);
  }

  public boolean wrappingX() {
    if (!wrappingChanged) {
      newWrapX = workspace.world.wrappingAllowedInX();
    }

    return newWrapX;
  }

  public void wrappingX(boolean value) {
    newWrapX = value;
    wrappingChanged = wrappingChanged || (newWrapX != workspace.world.wrappingAllowedInX());
  }

  public boolean wrappingY() {
    if (!wrappingChanged) {
      newWrapY = workspace.world.wrappingAllowedInY();
    }

    return newWrapY;
  }

  public void wrappingY(boolean value) {
    newWrapY = value;
    wrappingChanged = wrappingChanged || (newWrapY != workspace.world.wrappingAllowedInY());
  }

  protected int newFontSize;

  public int fontSize() {
    return gWidget.view.fontSize();
  }

  // this must be public because it's listed in our property set - ST 1/20/04
  public void fontSize(int newFontSize) {
    this.newFontSize = newFontSize;
    if (newFontSize != fontSize()) {
      fontSizeChanged = true;
    }
    workspace.viewManager.applyNewFontSize(newFontSize);
  }

  public double frameRate() {
    return workspace.frameRate();
  }

  public void frameRate(double frameRate) {
    workspace.frameRate(frameRate);
  }

  public void showTickCounter(boolean visible) {
    workspace.viewWidget.showTickCounter(visible);
  }

  public boolean showTickCounter() {
    return workspace.viewWidget.showTickCounter();
  }

  public String tickCounterLabel() {
    return workspace.viewWidget.tickCounterLabel();
  }

  public void tickCounterLabel(String label) {
    workspace.viewWidget.tickCounterLabel(label);
  }

  public void changeTopology(boolean wrapX, boolean wrapY) {
    workspace.changeTopology(wrapX, wrapY);
  }

  public void clearTurtles() {
    workspace.world.clearTurtles();
  }

  protected CompilerException error = null;

  public boolean anyErrors() {
    return error != null;
  }

  public void error(Exception e) {
    if (e instanceof CompilerException) {
      error = (CompilerException) e;
    } else {
      throw new IllegalStateException(e);
    }
  }

  public void error(Object o, Exception e) {
    error(e);
  }

  public Exception error() {
    return error;
  }

  public Exception error(Object o) {
    return error;
  }

  public void setSize(int x, int y) {
    gWidget.setSize(x, y);
  }

  public int getMinimumWidth() {
    return gWidget.getMinimumWidth();
  }

  public int insetWidth() {
    return gWidget.insetWidth();
  }

  public double computePatchSize(int width, int numPatches) {
    return gWidget.computePatchSize(width, numPatches);
  }

  public int calculateHeight(int worldHeight, double patchSize) {
    return gWidget.calculateHeight(worldHeight, patchSize);
  }

  public int calculateWidth(int worldWidth, double patchSize) {
    return gWidget.calculateWidth(worldWidth, patchSize);
  }

  public void setDimensions(org.nlogo.api.WorldDimensions d, double patchSize) {
    workspace.world.patchSize(patchSize);
    setDimensions(d);
    patchSize(patchSize);
    gWidget.resetSize();
  }

  public void setDimensions(org.nlogo.api.WorldDimensions d) {
    setDimensions(d.minPxcor(), d.maxPxcor(), d.minPycor(), d.maxPycor());
  }

  public void setDimensions(int minPxcor, int maxPxcor,
                            int minPycor, int maxPycor) {
    newMinX = minPxcor;
    newMaxX = maxPxcor;
    newMinY = minPycor;
    newMaxY = maxPycor;
    if (minPxcor != workspace.world.minPxcor() ||
        maxPxcor != workspace.world.maxPxcor() ||
        minPycor != workspace.world.minPycor() ||
        maxPycor != workspace.world.maxPycor()) {
      prepareForWorldResize();
      workspace.world.createPatches(minPxcor, maxPxcor,
          minPycor, maxPycor);
      finishWorldResize();
    }
  }

  void prepareForWorldResize() {
    workspace.jobManager.haltNonObserverJobs();
    workspace.world.clearTurtles();
    workspace.world.clearLinks();
  }

  void finishWorldResize() {
    workspace.patchesCreatedNotify();
    gWidget.resetSize();
    workspace.clearDrawing();
  }

  public int sourceOffset() {
    // we should never be dealing with errors
    // related to the view
    throw new IllegalStateException();
  }

}
