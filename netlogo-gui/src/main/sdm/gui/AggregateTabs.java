// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.nlogo.core.CompilerException;
import org.nlogo.core.I18N;
import org.nlogo.core.TokenType;

strictfp class AggregateTabs
    extends javax.swing.JTabbedPane
    implements javax.swing.event.ChangeListener {
  private final AggregateProceduresTab proceduresTab;
  private final AggregateModelEditor editor;

  AggregateTabs(
      javax.swing.JPanel activePanel,
      AggregateModelEditor editor,
      org.nlogo.editor.Colorizer<TokenType> colorizer) {
    addChangeListener(this);
    this.editor = editor;

    proceduresTab = new AggregateProceduresTab(colorizer);
    add(I18N.guiJ().get("tools.sdm.diagram"), activePanel);
    add(I18N.guiJ().get("tabs.code"), proceduresTab);
    setSelectedComponent(activePanel);
  }

  public void stateChanged(javax.swing.event.ChangeEvent e) {
    proceduresTab.setText(editor.toNetLogoCode());
  }

  private static final java.awt.Color ERROR_COLOR = java.awt.Color.RED;

  void recolorTab(java.awt.Component component, boolean hasError) {
    int index = indexOfComponent(component);
    setForegroundAt(index, hasError ? ERROR_COLOR : null);
  }

  void setError(CompilerException e) {
    if (e != null) {
      setSelectedComponent(proceduresTab);
    }
    proceduresTab.setError(e);
    setForegroundAt(indexOfComponent(proceduresTab), e != null ? ERROR_COLOR : null);
  }
}
