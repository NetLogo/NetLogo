// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.contrib.DiamondFigure;
import org.jhotdraw.framework.FigureAttributeConstant;
import org.jhotdraw.framework.HandleEnumeration;
import org.jhotdraw.standard.HandleEnumerator;
import org.jhotdraw.standard.NullHandle;
import org.jhotdraw.standard.RelativeLocator;
import org.nlogo.api.CompilerServices;
import org.nlogo.api.ExtensionManager;
import org.nlogo.editor.Colorizer;
import org.nlogo.window.Editable;
import org.nlogo.window.EditPanel;

import java.util.List;
import java.util.ArrayList;

import org.nlogo.api.Options;
import org.nlogo.sdm.Converter;
import org.nlogo.theme.InterfaceColors;

public class ConverterFigure extends DiamondFigure
    implements
    ModelElementFigure,
    org.jhotdraw.util.Storable,
    Editable {
  private org.nlogo.sdm.Converter converter;

  private transient CompilerServices compiler;
  private transient Colorizer colorizer;
  private transient ExtensionManager extensionManager;

  public static ConverterFigure create() {
    ConverterFigure figure = new ConverterFigure();

    figure.setAttribute(FigureAttributeConstant.FILL_COLOR, InterfaceColors.converterBackground());

    return figure;
  }

  private ConverterFigure() {
    converter = new Converter();

    converter.setSelected("Select");
  }

  // if these go in the constructor it messes up the old deserialization code (Isaac B 3/31/25)
  public void setConstructorDeps(CompilerServices compiler, Colorizer colorizer, ExtensionManager extensionManager) {
    this.compiler = compiler;
    this.colorizer = colorizer;
    this.extensionManager = extensionManager;
  }

  public org.nlogo.sdm.ModelElement getModelElement() {
    return converter;
  }

  public boolean anyErrors() {
    return false;
  }

  public void error(Object o, Exception e) {
  }

  public scala.Option<Exception> error(Object o) {
    return scala.Option.apply(null);
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

  public EditPanel editPanel() {
    return new ConverterEditPanel(this, compiler, colorizer, extensionManager);
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

  public void inputs(scala.collection.immutable.List<String> ls) {
    converter.setInputs(ls);
  }

  public void inputs(Options<String> newInputs) {
    String oldChoice = newInputs.chosenName();
    String newChoice = converter.getSelected();
    converter.setSelected(oldChoice);
    if (!"Select".equals(oldChoice) && !oldChoice.equals(newChoice)) {
      String oldExpression = converter.getExpression();
      String newExpression = oldExpression.trim() + " " + oldChoice + " ";
      converter.setExpression(newExpression);
    }
  }

  public Options<String> inputs() {
    Options<String> inputs = new Options<String>();
    scala.collection.Iterator<String> ins = converter.getInputs().iterator();
    inputs.addOption("Select", "null");

    while (ins.hasNext()) {
      inputs.addOption(ins.next(), "unused");
    }

    String name = converter.getSelected();
    if (name == "") {
      name = inputs.names().head();
      inputs.selectByName(name);
      converter.setSelected(name);
    }
    else {
      inputs.selectByName(name);
    }
    return inputs;
  }

}
