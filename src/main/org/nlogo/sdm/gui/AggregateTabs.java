// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.nlogo.api.CompilerException;
import org.nlogo.api.I18N;
import org.nlogo.api.TokenType;

strictfp class AggregateTabs
    extends javax.swing.JTabbedPane
    implements javax.swing.event.ChangeListener {
  private final AggregateCodeTab codeTab;
  private final AggregateModelEditor editor;

  AggregateTabs(
      javax.swing.JPanel activePanel,
      AggregateModelEditor editor,
      org.nlogo.editor.Colorizer<TokenType> colorizer) {
    addChangeListener(this);
    this.editor = editor;

    codeTab = new AggregateCodeTab(colorizer);
    add(I18N.guiJ().get("tools.sdm.diagram"), activePanel);
    add(I18N.guiJ().get("tabs.code"), codeTab);
    setSelectedComponent(activePanel);
  }

  public void stateChanged(javax.swing.event.ChangeEvent e) {
    codeTab.setText(editor.toNetLogoCode());
  }

  private static final java.awt.Color ERROR_COLOR = java.awt.Color.RED;

  void recolorTab(java.awt.Component component, boolean hasError) {
    int index = indexOfComponent(component);
    setForegroundAt(index, hasError ? ERROR_COLOR : null);
  }

  void setError(CompilerException e) {
    if (e != null) {
      setSelectedComponent(codeTab);
    }
    codeTab.setError(e);
    setForegroundAt(indexOfComponent(codeTab), e != null ? ERROR_COLOR : null);
  }
}
