// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.contrib.DiamondFigure;
import org.jhotdraw.framework.FigureAttributeConstant;
import org.jhotdraw.framework.HandleEnumeration;
import org.jhotdraw.standard.HandleEnumerator;
import org.jhotdraw.standard.NullHandle;
import org.jhotdraw.standard.RelativeLocator;
import org.nlogo.api.Property;

import java.util.ArrayList;
import java.util.List;

public strictfp class ConverterFigure extends DiamondFigure
    implements
    ModelElementFigure,
    org.jhotdraw.util.Storable,
    org.nlogo.api.Editable {
  private org.nlogo.sdm.Converter converter;

  public ConverterFigure() {
    setAttribute
        (FigureAttributeConstant.FILL_COLOR,
            org.nlogo.window.InterfaceColors.SLIDER_BACKGROUND);
    converter = new org.nlogo.sdm.Converter();
  }

  public org.nlogo.sdm.ModelElement getModelElement() {
    return converter;
  }

  public boolean anyErrors() {
    return false;
  }

  public void error(Object o, Exception e) {
  }

  public Exception error(Object o) {
    return null;
  }

  public int sourceOffset() {
    return 0;
  }

  @Override
  public void draw(java.awt.Graphics g) {
    ((java.awt.Graphics2D) g).setRenderingHint
        (java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
    super.draw(g);
    if (converter != null) {
      java.awt.Color oldColor = g.getColor();
      if (!converter.isComplete()) {
        g.setColor(java.awt.Color.RED);
      }
      String name = converter.getName();
      if (name.length() == 0) {
        name = "?";
      }
      Utils.drawStringInBox
          (g,
              name,
              displayBox().x + 14,
              displayBox().y + 1 + displayBox().height / 2);
      g.setColor(oldColor);
    }
  }

  @Override
  protected java.awt.Rectangle invalidateRectangle(java.awt.Rectangle r) {
    java.awt.Rectangle box = super.invalidateRectangle(r);
    box.grow
        (converter.getName().length() * 10,
            converter.getName().length() * 10);
    return box;
  }

  // Return no resize handles
  @Override
  public HandleEnumeration handles() {
    List<NullHandle> handles = new ArrayList<NullHandle>();
    handles.add(new NullHandle(this, RelativeLocator.southEast()));
    handles.add(new NullHandle(this, RelativeLocator.southWest()));
    handles.add(new NullHandle(this, RelativeLocator.northEast()));
    handles.add(new NullHandle(this, RelativeLocator.northWest()));
    return new HandleEnumerator(handles);
  }

  @Override
  public void write(org.jhotdraw.util.StorableOutput dw) {
    super.write(dw);
    dw.writeStorable(Wrapper.wrap(converter));
  }

  @Override
  public void read(org.jhotdraw.util.StorableInput dr)
      throws java.io.IOException {
    super.read(dr);
    converter = ((WrappedConverter) dr.readStorable()).converter;
  }

  /// For org.nlogo.window.Editable
  public scala.Option<String> helpLink() {
    return scala.Option.apply(null);
  }

  public List<Property> propertySet() {
    return Properties.converter();
  }

  public String classDisplayName() {
    return "Variable";
  }

  public boolean editFinished() {
    return true;
  }

  private boolean dirty = false;

  public boolean dirty() {
    return dirty;
  }

  public void nameWrapper(String name) {
    dirty = dirty || !converter.getName().equals(name);
    converter.setName(name);
  }

  public String nameWrapper() {
    return converter.getName();
  }

  public void expressionWrapper(String expression) {
    dirty = dirty || !converter.getExpression().equals(expression);
    converter.setExpression(expression);
  }

  public String expressionWrapper() {
    return converter.getExpression();
  }

}
