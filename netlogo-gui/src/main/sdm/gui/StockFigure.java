// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.figures.RectangleFigure;
import org.jhotdraw.framework.FigureAttributeConstant;
import org.jhotdraw.framework.HandleEnumeration;
import org.jhotdraw.standard.HandleEnumerator;
import org.jhotdraw.standard.NullHandle;
import org.jhotdraw.standard.RelativeLocator;
import org.nlogo.api.Property;

import java.util.ArrayList;
import java.util.List;

public strictfp class StockFigure
    extends RectangleFigure
    implements
    ModelElementFigure,
    org.nlogo.api.Editable {
  private org.nlogo.sdm.Stock stock;

  public StockFigure() {
    super();

    setAttribute(FigureAttributeConstant.FILL_COLOR,
        org.nlogo.window.InterfaceColors.MONITOR_BACKGROUND);
    stock = new org.nlogo.sdm.Stock();
  }

  public org.nlogo.sdm.ModelElement getModelElement() {
    return stock;
  }

  public boolean anyErrors() {
    return false;
  }

  public void error(Object o, Exception e) {
  }

  public Exception error(Object key) {
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
    if (stock != null) {
      java.awt.Color oldColor = g.getColor();
      if (!stock.isComplete()) {
        g.setColor(java.awt.Color.RED);
      }

      String name = stock.getName();
      if (name.length() == 0) {
        name = "?";
      }

      java.awt.Font oldFont = g.getFont();
      g.setFont(oldFont.deriveFont(java.awt.Font.BOLD));
      int height =
          g.getFontMetrics().getMaxAscent() +
              g.getFontMetrics().getMaxDescent();
      int width = g.getFontMetrics().stringWidth(stock.getName());
      g.drawString
          (name,
              displayBox().x + (displayBox().width - width) / 2,
              displayBox().y + (displayBox().height - height) / 2
                  + g.getFontMetrics().getMaxAscent());
      g.setFont(oldFont);
      g.setColor(oldColor);
    }

  }

  @Override
  protected java.awt.Rectangle invalidateRectangle(java.awt.Rectangle r) {
    java.awt.Rectangle box = super.invalidateRectangle(r);
    box.grow(50, 50);
    return box;
  }

  @Override
  public java.awt.Rectangle displayBox() {
    java.awt.Rectangle box = super.displayBox();
    box.grow(12, 12);
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
    dw.writeStorable(Wrapper.wrap(stock));
  }

  @Override
  public void read(org.jhotdraw.util.StorableInput dr)
      throws java.io.IOException {
    super.read(dr);
    stock = ((WrappedStock) dr.readStorable()).stock;
  }


  /// For org.nlogo.window.Editable

  public scala.Option<String> helpLink() {
    return scala.Option.apply(null);
  }

  public List<Property> propertySet() {
    return Properties.stock();
  }

  public String classDisplayName() {
    return "Stock";
  }

  public boolean editFinished() {
    return true;
  }

  private boolean dirty = false;

  public boolean dirty() {
    return dirty;
  }

  public void nameWrapper(String name) {
    dirty = dirty || !stock.getName().equals(name);
    stock.setName(name);
  }

  public String nameWrapper() {
    return stock.getName();
  }

  public void initialValueExpressionWrapper(String expression) {
    dirty = dirty || !stock.getInitialValueExpression().equals(expression);
    stock.setInitialValueExpression(expression);
  }

  public String initialValueExpressionWrapper() {
    return stock.getInitialValueExpression();
  }

  public boolean allowNegative() {
    return !stock.isNonNegative();
  }

  public void allowNegative(boolean b) {
    dirty = dirty || stock.isNonNegative() == b;
    stock.setNonNegative(!b);
  }

}
