// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.CompilerException;
import org.nlogo.api.CompilerServices;
import org.nlogo.api.Dump;
import org.nlogo.api.Editable;
import org.nlogo.api.I18N;
import org.nlogo.core.LogoList;
import org.nlogo.api.Property;

import java.util.Iterator;
import java.util.List;

public strictfp class DummyChooserWidget
    extends Chooser
    implements Editable {

  public DummyChooserWidget(CompilerServices compiler) {
    super(compiler);
    setBorder(widgetBorder());
  }

  @Override
  public void updateConstraints() {
    // we never update constraints in a dummy widget -- CLB
  }

  @Override
  public String classDisplayName() {
    return I18N.guiJ().get("tabs.run.widgets.chooser");
  }

  public List<Property> propertySet() {
    return Properties.dummyChooser();
  }

  @Override
  public boolean editFinished() {
    super.editFinished();
    name(name());
    return true;
  }

  public String choicesWrapper() {
    StringBuilder buf = new StringBuilder();
    for (Iterator<Object> it = constraint.acceptedValues().javaIterator(); it.hasNext();) {
      buf.append
          (Dump.logoObject(it.next(), true, false));
      buf.append('\n');
    }
    return buf.toString();
  }

  public void choicesWrapper(String choicesString) {
    try {
      Object oldValue = value();
      LogoList newChoices = (LogoList) compiler.readFromString
          ("[ " + choicesString + " ]");

      constraint.acceptedValues(newChoices);

      int newIndex = constraint.indexForValue(oldValue);
      if (newIndex == -1) {
        index(0);
      } else {
        index(newIndex);
      }


    } catch (CompilerException e) {
      // this should never happen because LogoListStringEditor
      // checks it for us first when the user types it in
      throw new IllegalStateException(e);
    }
  }


  /// load and save

  @Override
  public Object load(String[] strings, Widget.LoadHelper helper) {
    int x1 = Integer.parseInt(strings[1]);
    int y1 = Integer.parseInt(strings[2]);
    int x2 = Integer.parseInt(strings[3]);
    int y2 = Integer.parseInt(strings[4]);
    setSize(x2 - x1, y2 - y1);
    name(org.nlogo.api.ModelReader.restoreLines(strings[5]));
    choicesWrapper(strings[7]);
    index(Integer.parseInt(strings[8]));
    return this;
  }

  @Override
  public String save() {

    StringBuilder s = new StringBuilder();

    s.append("CHOOSER\n");
    s.append(getBoundsString());
    // the file format has separate entries for name and display name,
    // but at least at present, they are always equal, so we just
    // write out the name twice - ST 6/3/02
    if ((null != name()) && (!name().trim().equals(""))) {
      s.append(name() + "\n");
      s.append(name() + "\n");
    } else {
      s.append("NIL\n");
      s.append("NIL\n");
    }
    s.append(choicesWrapper().trim().replaceAll("\n", " ") + "\n");
    s.append(index() + "\n");
    return s.toString();
  }
}
