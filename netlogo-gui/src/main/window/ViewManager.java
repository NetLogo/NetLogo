// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nlogo.nvm.DisplayStatus;
import org.nlogo.api.ViewInterface;

public strictfp class ViewManager implements ChangeListener, PropertyChangeListener {
  private final java.util.LinkedList<ViewInterface> views =
    new java.util.LinkedList<ViewInterface>();
  private ArrayList<DisplaySwitch> displaySwitches = new ArrayList<DisplaySwitch>();
  private boolean displaySwitchedOn = true;
  private boolean ignoreChangeEvents = false;
  private AtomicReference<DisplayStatus> displayStatus;

  public ViewManager(AtomicReference<DisplayStatus> displayStatus) {
    this.displayStatus = displayStatus;
  }

  // note that primary views *must* be local views aka not hubnet
  public LocalViewInterface getPrimary() {
    return (LocalViewInterface) views.get(0);
  }

  public void setPrimary(LocalViewInterface view) {
    views.remove(view);
    views.addFirst(view);
  }

  public void setSecondary(ViewInterface view) {
    views.remove(view);
    views.add(1, view);
  }

  public void add(ViewInterface v) {
    views.addLast(v);
  }

  public void remove(ViewInterface v) {
    views.remove(v);
  }

  void paintImmediately(boolean force) {
    for (ViewInterface view : views) {
      view.paintImmediately(force);
    }
  }

  void framesSkipped() {
    for (ViewInterface v : views) {
      v.framesSkipped();
    }
  }

  void incrementalUpdateFromEventThread() {
    for (ViewInterface v : views) {
      if (!v.isDead() && v.viewIsVisible()) {
        v.incrementalUpdateFromEventThread();
      }
    }
  }

  private final Runnable updateRunnable =
      new Runnable() {
        public void run() {
          incrementalUpdateFromEventThread();
        }
      };

  void incrementalUpdateFromJobThread() {
    try {
      org.nlogo.awt.EventQueue.invokeAndWait(updateRunnable);
    } catch (InterruptedException ex) {
      getPrimary().repaint();
    }
  }

  void applyNewFontSize(int newFontSize) {
    for (ViewInterface v : views) {
      v.applyNewFontSize(newFontSize, 0);
    }
  }

  public void shapeChanged(org.nlogo.core.Shape shape) {
    for (ViewInterface view : views) {
      view.shapeChanged(shape);
    }
  }

  public boolean mouseDown() {
    for (ViewInterface view : views) {
      if (view.mouseDown()) {
        return true;
      }
    }
    return false;
  }

  public boolean mouseInside() {
    for (ViewInterface view : views) {
      if (view.mouseInside()) {
        return true;
      }
    }
    return false;
  }

  public double mouseXCor() {
    for (ViewInterface view : views) {
      if (view.mouseInside()) {
        return view.mouseXCor();
      }
    }
    return getPrimary().mouseXCor();
  }

  public double mouseYCor() {
    for (ViewInterface view : views) {
      if (view.mouseInside()) {
        return view.mouseYCor();
      }
    }
    return getPrimary().mouseYCor();
  }

  public void resetMouseCors() {
    for (ViewInterface view : views) {
      view.resetMouseCors();
    }
  }

  public boolean displaySwitchStatus() {
    return displaySwitchedOn;
  }

  public void setDisplaySwitchStatus(boolean on) {
    displaySwitchedOn = on;
    if (displaySwitches.size() > 1) {
      updateAllSwitches(on);
    }
    displayStatus.updateAndGet(s -> s.switchSet(on));
  }

  public void reloadSwitchStatus(boolean status) {
    displaySwitchedOn = status;
    updateAllSwitches(displaySwitchedOn);
  }

  public void registerDisplaySwitch(DisplaySwitch displaySwitch) {
    displaySwitch.setOn(displaySwitchedOn);
    displaySwitch.addChangeListener(this);
    displaySwitches.add(displaySwitch);
  }

  private void updateAllSwitches(boolean on) {
    for (DisplaySwitch displaySwitch : displaySwitches) {
      displaySwitch.removeChangeListener(this);
      displaySwitch.setOn(on);
      displaySwitch.addChangeListener(this);
    }
  }

  public void stateChanged(ChangeEvent e) {
    if (e.getSource() instanceof DisplaySwitch && ! ignoreChangeEvents) {
      boolean on = ((DisplaySwitch) e.getSource()).isSelected();
      displaySwitchedOn = on;
      if (on) {
        views.get(0).thaw();
        incrementalUpdateFromEventThread();
      } else {
        views.get(0).freeze();
      }
      updateAllSwitches(displaySwitchedOn);
    }
  }

  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(WorldViewSettings.ViewFontSizeProperty()) && (evt.getNewValue() instanceof java.lang.Integer)) {
      applyNewFontSize(((java.lang.Integer) evt.getNewValue()).intValue());
    }
  }
}
