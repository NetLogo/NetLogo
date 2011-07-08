package org.nlogo.sdm.gui;

import org.nlogo.api.CompilerServices;
import org.nlogo.api.TokenType;
import org.nlogo.api.ModelSectionJ;
import org.nlogo.window.EditDialogFactoryInterface;

public strictfp class GUIAggregateManager
    implements
    org.nlogo.api.AggregateManagerInterface,
    org.nlogo.window.Event.LinkChild,
    org.nlogo.window.Events.CompiledEvent.Handler,
    org.nlogo.window.Events.BeforeLoadEvent.Handler,
    org.nlogo.window.Events.LoadSectionEvent.Handler {
  AggregateModelEditor editor = null;
  private final CompilerServices compiler;
  private final java.awt.Component linkParent;
  private final org.nlogo.window.MenuBarFactory menuBarFactory;
  private final org.nlogo.editor.Colorizer<TokenType> colorizer;
  private final EditDialogFactoryInterface dialogFactory;

  public GUIAggregateManager(
      java.awt.Component linkParent,
      org.nlogo.window.MenuBarFactory menuBarFactory,
      CompilerServices compiler,
      org.nlogo.editor.Colorizer<TokenType> colorizer,
      EditDialogFactoryInterface dialogFactory) {
    this.linkParent = linkParent;
    this.menuBarFactory = menuBarFactory;
    this.compiler = compiler;
    this.colorizer = colorizer;
    this.dialogFactory = dialogFactory;
  }

  public void showEditor() {
    // if it's the first time, make a new aggregate model editor
    if (editor == null) {
      editor = new AggregateModelEditor
          (linkParent, colorizer, menuBarFactory, compiler, dialogFactory);
    }
    editor.setVisible(true);
    editor.toFront();
  }

  public Object getLinkParent() {
    return linkParent;
  }

  public String save() {
    if (editor != null &&
        editor.view().drawing().figures().hasNextFigure()) {
      java.io.ByteArrayOutputStream s =
          new java.io.ByteArrayOutputStream();
      org.jhotdraw.util.StorableOutput output =
          new org.jhotdraw.util.StorableOutput(s);
      output.writeDouble(editor.getModel().getDt());
      output.writeStorable(editor.view().drawing());
      output.close();
      // JHotDraw has an annoying habit of including spaces at the end of lines.
      // we have stripped those out of the models in version control, so to prevent
      // spurious diffs, we need to keep them from coming back - ST 3/10/09
      return s.toString().replaceAll(" *\n", "\n").trim();
    }
    return null;
  }

  public void handle(org.nlogo.window.Events.BeforeLoadEvent e) {
    if (editor != null) {
      editor.dispose();
      editor = null;
    }
  }

  public void handle(org.nlogo.window.Events.LoadSectionEvent e) {
    if (e.section == ModelSectionJ.AGGREGATE()) {
      load(e.text, compiler);
    }
  }

  public void load(String text, CompilerServices compiler) {
    if (!text.trim().equals("")) {
      try {
        text = org.nlogo.sdm.Model.mungeClassNames(text);

        // first parse out dt on our own as jhotdraw does not deal with scientific notation
        // properly. ev 10/11/05

        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.StringReader(text));

        double dt = Double.parseDouble(br.readLine());
        String str = br.readLine();

        text = text.substring(text.indexOf(str));
        java.io.ByteArrayInputStream s = new java.io.ByteArrayInputStream(text.getBytes());

        org.jhotdraw.util.StorableInput input =
            new org.jhotdraw.util.StorableInput(s);

        AggregateDrawing drawing =
            (AggregateDrawing) input.readStorable();
        drawing.getModel().setDt(Double.valueOf(dt));
        editor = new AggregateModelEditor
            (linkParent, colorizer, menuBarFactory, drawing, compiler, dialogFactory);
        if (drawing.getModel().elements().size() == 0) {
          editor.setVisible(false);
        }
      } catch (java.io.IOException ie) {
        throw new IllegalStateException(ie);
      } catch (org.nlogo.sdm.Model.ModelException ame) {
        throw new IllegalStateException(ame);
      }
    }
  }


  public void handle(org.nlogo.window.Events.CompiledEvent e) {
    if (editor != null) {
      if (e.error != null && e.sourceOwner == this) {
        editor.setError(this, e.error);
      } else {
        editor.setError(this, null);
      }
    }
  }

  /// from org.nlogo.nvm.SourceOwner

  public String innerSource() {
    return editor == null
        ? ""
        : editor.toNetLogoCode();
  }

  public String source() {
    return headerSource() + innerSource();
  }

  public void innerSource(String s) {
  }

  public String classDisplayName() {
    return "Aggregate";
  }

  public Class<?> agentClass() {
    return org.nlogo.agent.Observer.class;
  }

  public String headerSource() {
    return "";
  }

}
