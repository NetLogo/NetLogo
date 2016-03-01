// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.nlogo.window.ErrorLabel;

import java.awt.BorderLayout;

strictfp class AggregateEditorTab
    extends javax.swing.JPanel {

  AggregateEditorTab(AggregateModelEditor editor, java.awt.Component contents) {
    setAlignmentX(LEFT_ALIGNMENT);
    setAlignmentY(TOP_ALIGNMENT);
    setLayout(new BorderLayout());

    AggregateModelEditorToolBar toolbar =
        new AggregateModelEditorToolBar(editor);
    editor.setToolbar(toolbar);
    add(toolbar, BorderLayout.NORTH);

    javax.swing.JPanel codePanel = new javax.swing.JPanel(new java.awt.BorderLayout());
    ErrorLabel errorLabel = new ErrorLabel();
    codePanel.add(errorLabel, BorderLayout.NORTH);
    codePanel.add(new javax.swing.JScrollPane(contents), BorderLayout.CENTER);

    add(codePanel, java.awt.BorderLayout.CENTER);
  }
}
