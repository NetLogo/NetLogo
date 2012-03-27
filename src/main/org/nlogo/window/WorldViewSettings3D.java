// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.agent.World3D;
import org.nlogo.api.I18N;

public strictfp class WorldViewSettings3D
    extends WorldViewSettings {
  protected final World3D world;

  WorldViewSettings3D(GUIWorkspace workspace, ViewWidget gw) {
    super(workspace, gw);
    world = (World3D) workspace.world;
  }

  protected int newMinZ;
  protected int newMaxZ;

  protected boolean newWrapZ;

  public void minPzcor(int minPzcor) {
    if (minPzcor <= 0) {
      newMinZ = minPzcor;
      edgesChanged = edgesChanged || (newMinZ != world.minPzcor());
    }
  }

  public int minPzcor() {
    return newMinZ;
  }

  public void maxPzcor(int maxPzcor) {
    if (maxPzcor >= 0) {
      newMaxZ = maxPzcor;
      edgesChanged = edgesChanged || (newMaxZ != world.maxPzcor());
    }
  }

  public int maxPzcor() {
    return newMaxZ;
  }


  public boolean wrappingZ() {
    if (!wrappingChanged) {
      newWrapZ = world.wrappingAllowedInZ();
    }

    return newWrapZ;
  }

  public void wrappingZ(boolean value) {
    newWrapZ = value;
    wrappingChanged = wrappingChanged || (newWrapZ != world.wrappingAllowedInZ());
  }

  @Override
  public void addDimensionProperties() {
    dimensionProperties.addAll(Properties.dims3D());
  }

  @Override
  public void addWrappingProperties() {
    wrappingProperties.addAll(Properties.wrap3D());
  }

  @Override
  public void addCornerChoices() {
    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.corner.bottomSouthwest"),
        new boolean[]{false, true, false,
            true, false, true},
        new boolean[]{true, false, true,
            false, true, false}));
    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.corner.bottomNorthwest"),
        new boolean[]{false, true, true,
            false, false, true},
        new boolean[]{true, false, false,
            true, true, false}));
    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.corner.bottomNortheast"),
        new boolean[]{true, false, true,
            false, false, true},
        new boolean[]{false, true, false,
            true, true, false}));
    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.corner.bottomSoutheast"),
        new boolean[]{true, false, false,
            true, false, true},
        new boolean[]{false, true, true,
            false, true, false}));

    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.corner.topSouthwest"),
        new boolean[]{false, true, false,
            true, true, false},
        new boolean[]{true, false, true,
            false, false, true}));
    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.corner.topNorthwest"),
        new boolean[]{false, true, true,
            false, true, false},
        new boolean[]{true, false, false,
            true, false, true}));
    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.corner.topNortheast"),
        new boolean[]{true, false, true,
            false, true, false},
        new boolean[]{false, true, false,
            true, false, true}));
    cornerChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.corner.topSoutheast"),
        new boolean[]{true, false, false,
            true, true, false},
        new boolean[]{false, true, true,
            false, false, true}));
  }

  @Override
  public void addEdgeChoices() {
    edgeChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.edge.south"),
        new boolean[]{true, true, false,
            true, true, true},
        new boolean[]{false, false, true,
            false, false, false}));
    edgeChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.edge.north"),
        new boolean[]{true, true, true,
            false, true, true},
        new boolean[]{false, false, false,
            true, false, false}));
    edgeChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.edge.east"),
        new boolean[]{true, false, true,
            true, true, true},
        new boolean[]{false, true, false,
            false, false, false}));
    edgeChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.edge.west"),
        new boolean[]{false, true, true,
            true, true, true},
        new boolean[]{true, false, false,
            false, false, false}));
    edgeChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.edge.bottom"),
        new boolean[]{true, true, true,
            true, false, true},
        new boolean[]{false, false, false,
            false, true, false}));
    edgeChoices.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.3D.origin.location.edge.top"),
        new boolean[]{true, true, true,
            true, true, false},
        new boolean[]{false, false, false,
            false, false, true}));
  }

  @Override
  public void addOriginConfigurations() {
    originConfigurations.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.center"),
        new boolean[]{false, true, false,
            true, false, true},
        new boolean[]{false, false, false,
            false, false, false}));
    originConfigurations.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.corner"),
        new boolean[]{true, true, true,
            true, true, true},
        new boolean[]{false, false, false,
            false, false, false}));
    originConfigurations.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.edge"),
        new boolean[]{true, true, true,
            true, true, true},
        new boolean[]{false, false, false,
            false, false, false}));
    originConfigurations.add(new OriginConfiguration(I18N.guiJ().get("edit.viewSettings.origin.location.custom"),
        new boolean[]{true, true, true,
            true, true, true},
        new boolean[]{false, false, false,
            false, false, false}));
  }

  @Override
  public int firstEditor() {
    return 0;
  }

  @Override
  public int lastEditor() {
    return 5;
  }

  @Override
  public int getSelectedLocation() {
    int minx = minPxcor();
    int maxx = maxPxcor();
    int miny = minPycor();
    int maxy = maxPycor();
    int maxz = maxPzcor();
    int minz = minPzcor();

    if (minx == (-maxx) && miny == (-maxy) && minz == (-maxz)) {
      return 0;
    } else if ((minx == 0 || maxx == 0)
        && (miny == 0 || maxy == 0)
        && (minz == 0 || maxz == 0)) {
      return 1;
    } else if (minx == 0 || maxx == 0 ||
        miny == 0 || maxy == 0 ||
        minz == 0 || maxz == 0) {
      return 2;
    }

    return 3;
  }

  @Override
  public int getSelectedConfiguration() {
    int minx = minPxcor();
    int maxx = maxPxcor();
    int miny = minPycor();
    int maxy = maxPycor();
    int minz = minPzcor();
    int maxz = maxPzcor();

    if (minx == 0 && miny == 0 && minz == 0) {
      return 0;
    } else if (minx == 0 && maxy == 0 && minz == 0) {
      return 1;
    } else if (maxx == 0 && maxy == 0 && minz == 0) {
      return 2;
    } else if (maxx == 0 && miny == 0 && minz == 0) {
      return 3;
    } else if (minx == 0 && miny == 0 && maxz == 0) {
      return 4;
    } else if (minx == 0 && maxy == 0 && maxz == 0) {
      return 5;
    } else if (maxx == 0 && maxy == 0 && maxz == 0) {
      return 6;
    } else if (maxx == 0 && miny == 0 && maxz == 0) {
      return 7;
    } else if (minx == 0) {
      return 3;
    } else if (maxx == 0) {
      return 2;
    } else if (miny == 0) {
      return 0;
    } else if (maxy == 0) {
      return 1;
    } else if (minz == 0) {
      return 4;
    } else if (maxz == 0) {
      return 5;
    } else {
      return 0;
    }
  }

  public boolean editFinished() {
    gWidget.editFinished();

    if (wrappingChanged) {
      workspace.changeTopology(newWrapX, newWrapY);
      wrappingChanged = false;
    }
    if (edgesChanged || patchSizeChanged) {
      resizeWithProgress(true);
      edgesChanged = false;
      patchSizeChanged = false;
    }
    if (fontSizeChanged) {
      gWidget.applyNewFontSize(newFontSize);
      fontSizeChanged = false;
    }
    gWidget.view.dirty();
    gWidget.view.repaint();
    workspace.glView.editFinished();
    return true;
  }

  @Override
  public void resizeWithProgress(boolean showProgress) {
    boolean oldGraphicsOn = world.displayOn();
    if (oldGraphicsOn) {
      world.displayOn(false);
    }

    Runnable runnable =
        new Runnable() {
          public void run() {
            if (edgesChanged) {
              new org.nlogo.window.Events.RemoveAllJobsEvent().raise(gWidget);
              world.clearTurtles();
              world.clearLinks();
              world.createPatches(newMinX, newMaxX,
                  newMinY, newMaxY,
                  newMinZ, newMaxZ);
              workspace.patchesCreatedNotify();
              gWidget.resetSize();
            }
            if (patchSizeChanged) {
              world.patchSize(newPatchSize);
              gWidget.resetSize();
            }

            if (edgesChanged) {
              workspace.clearDrawing();
            } else {
              gWidget.view.renderer.trailDrawer().rescaleDrawing();
            }
          }
        };
    if (showProgress) {
      org.nlogo.swing.ModalProgressTask.apply(
        org.nlogo.awt.Hierarchy.getFrame(gWidget), "Resizing...", runnable);
    } else {
      runnable.run();
    }
    gWidget.displaySwitchOn(true);
    if (oldGraphicsOn) {
      world.displayOn(true);
      gWidget.view.dirty();
      gWidget.view.repaint();
    }
  }

  @Override
  public void setDimensions(org.nlogo.api.WorldDimensions d) {
    if (d instanceof org.nlogo.api.WorldDimensions3D) {
      org.nlogo.api.WorldDimensions3D dd = (org.nlogo.api.WorldDimensions3D) d;
      setDimensions
        (dd.minPxcor(), dd.maxPxcor(), dd.minPycor(), dd.maxPycor(), dd.minPzcor(), dd.maxPzcor());
    } else {
      setDimensions
        (d.minPxcor(), d.maxPxcor(), d.minPycor(), d.maxPycor(), 0, 0);
    }
  }

  public void setDimensions(int minPxcor, int maxPxcor,
                            int minPycor, int maxPycor,
                            int minPzcor, int maxPzcor) {
    newMinX = minPxcor;
    newMaxX = maxPxcor;
    newMinY = minPycor;
    newMaxY = maxPycor;
    newMinZ = minPzcor;
    newMaxZ = maxPzcor;

    if (minPxcor != world.minPxcor() ||
        maxPxcor != world.maxPxcor() ||
        minPycor != world.minPycor() ||
        maxPycor != world.maxPycor() ||
        minPzcor != world.minPzcor() ||
        maxPzcor != world.maxPzcor()) {
      prepareForWorldResize();
      world.createPatches(minPxcor, maxPxcor,
          minPycor, maxPycor,
          minPzcor, maxPzcor);
      finishWorldResize();
    }
  }

  @Override
  public String save() {
    return
        "GRAPHICS-WINDOW\n" +
            gWidget.getBoundsString() +
            ((-world.minPxcor()
                == world.maxPxcor()) ? world.maxPxcor() : -1) + "\n" +
            ((-world.minPycor()
                == world.maxPycor()) ? world.maxPycor() : -1) + "\n" +
            world.patchSize() + "\n" + //7
            "1\n" + //8 shapesOn
            gWidget.view.fontSize() + "\n" + //9
            // old exactDraw & hex settings, no longer used - ST 8/13/03, 1/4/07
            "1\n1\n1\n0\n" +  // 10 11 12 13
            (world.wrappingAllowedInX() ? "1" : "0") + "\n" + // 14
            (world.wrappingAllowedInY() ? "1" : "0") + "\n" + //15
            "1\n" + // thin turtle pens are always on 16
            world.minPxcor() + "\n" + // 17
            world.maxPxcor() + "\n" + // 18
            world.minPycor() + "\n" + // 19
            world.maxPycor() + "\n" + // 20
            world.minPzcor() + "\n" + // 21
            world.maxPzcor() + "\n" + // 22
            (world.wrappingAllowedInZ() ? "1" : "0") + "\n" + // 23
            workspace.updateMode().save() + "\n" + // 24
            (showTickCounter() ? "1" : "0") + "\n" + // 25
            (tickCounterLabel().trim().equals("") ? "NIL" : tickCounterLabel()) + "\n" + // 26
            frameRate() + "\n"; // 27
  }
}
