package org.nlogo.widget;

import java.util.ArrayList;
import java.util.List;

import org.nlogo.api.Editable;
import org.nlogo.api.I18N;
import org.nlogo.api.Property;
import org.nlogo.window.Widget;

public strictfp class DummySwitchWidget
    extends Switch
    implements Editable {

  @Override
  public String classDisplayName() {
    return I18N.gui().get("tabs.run.widgets.switch");
  }

  public List<Property> propertySet() {
    return Properties.dummySwitch();
  }

  @Override
  public void updateConstraints() {
    // we never update constraints in a dummy widget -- CLB
  }

  /// load and save
  @Override
  public Object load(String[] strings, Widget.LoadHelper helper) {
    super.name(org.nlogo.api.File.restoreLines(strings[6]));
    isOn(Double.valueOf(strings[7]).doubleValue() == 0);
    int x1 = Integer.parseInt(strings[1]);
    int y1 = Integer.parseInt(strings[2]);
    int x2 = Integer.parseInt(strings[3]);
    int y2 = Integer.parseInt(strings[4]);
    setSize(x2 - x1, y2 - y1);
    return this;
  }

  @Override
  public String save() {
    StringBuilder s = new StringBuilder();
    s.append("SWITCH\n");
    s.append(getBoundsString());
    if ((null != displayName()) && (!displayName().trim().equals(""))) {
      s.append(displayName() + "\n");
    } else {
      s.append("NIL\n");
    }
    if ((null != name()) && (!name().trim().equals(""))) {
      s.append(name() + "\n");
    } else {
      s.append("NIL\n");
    }

    if (isOn()) {
      s.append(0 + "\n");
    } else {
      s.append(1 + "\n");
    }

    s.append(1 + "\n");  // for compatibility
    s.append(-1000 + "\n"); // for compatibility

    return s.toString();
  }

  public void handle(org.nlogo.window.Events.AfterLoadEvent e) {
    // do nothing
  }


}
