// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.agent.World;

public strictfp class WorldViewSettings2D
    extends WorldViewSettings {
  protected final World world;

  WorldViewSettings2D(GUIWorkspace workspace, ViewWidget gw) {
    super(workspace, gw);
    world = workspace.world;
  }

  @Override
  public void addDimensionProperties() {
    dimensionProperties.addAll(Properties.dims2D());
  }

  @Override
  public void addWrappingProperties() {
    wrappingProperties.addAll(Properties.wrap2D());
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
    return true;
  }

  @Override
  public void resizeWithProgress(boolean showProgress) {
    boolean oldGraphicsOn = world.displayOn();
    if (oldGraphicsOn) {
      world.displayOn(false);
    }

    Runnable runnable = new Runnable() {
      static final int KICK = 0;
      static final int IGNORE = 1;

      public void run() {
        if (edgesChanged) {
          /**
           * All turtles die when the world changes sizes.
           */
          new Events.RemoveAllJobsEvent().raise(gWidget);
          world.clearTurtles();
          world.clearLinks();
          world.createPatches(newMinX, newMaxX,
              newMinY, newMaxY);
          workspace.patchesCreatedNotify();
          gWidget.resetSize();
        }
        if (patchSizeChanged) {
          world.patchSize(newPatchSize);
          gWidget.resetSize();
        }

        if (edgesChanged) {
          gWidget.view.renderer.trailDrawer().clearDrawing();
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
  public String save() {
    return
        "GRAPHICS-WINDOW\n" +
            gWidget.getBoundsString() +
            ((-world.minPxcor()
                == world.maxPxcor()) ? world.maxPxcor() : -1) + "\n" +
            ((-world.minPycor()
                == world.maxPycor()) ? world.maxPycor() : -1) + "\n" +
            world.patchSize() + "\n" + //7
            "1\n" + //8 bye bye shapesOn
            gWidget.view.fontSize() + "\n" + //9
            // old exactDraw & hex settings, no longer used - ST 8/13/03
            "1\n1\n1\n0\n" +  // 10 11 12 13
            (world.wrappingAllowedInX() ? "1" : "0") + "\n" + // 14
            (world.wrappingAllowedInY() ? "1" : "0") + "\n" + //15
            "1\n" + // thin turtle pens are always on
            world.minPxcor() + "\n" + // 17
            world.maxPxcor() + "\n" + // 18
            world.minPycor() + "\n" + // 19
            world.maxPycor() + "\n" + // 20
            // saved twice for historical reasons
            workspace.updateMode().save() + "\n" + // 21
            workspace.updateMode().save() + "\n" + // 22
            (showTickCounter() ? "1" : "0") + "\n" + // 23
            (tickCounterLabel().trim().equals("") ? "NIL" : tickCounterLabel()) + "\n" + // 24
            frameRate() + "\n"; // 25
  }
}
