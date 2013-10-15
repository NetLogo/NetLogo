// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.nlogo.awt.Hierarchy;
import org.nlogo.awt.UserCancelException;
import org.nlogo.swing.FileDialog;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public strictfp class FileSelector extends JPanel {

  private static final String NO_FILE_SELECTED = "No file selected";

  private String filePath;
  private JButton selectFileButton;
  private JLabel filePathLabel;
  private JPanel filePathLabelPanel;
  private JComponent parent;

  public FileSelector(JComponent parent){
    super();
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setForeground(Color.BLUE);
    this.parent = parent;
    JPanel buttonPanel = new JPanel();
    selectFileButton = new JButton("Select File");
    //buttonPanel.add(selectFileButton);
    add(Box.createRigidArea(new Dimension(10, 0)));
    add(selectFileButton);
    add(Box.createRigidArea(new Dimension(10, 0)));

    filePathLabelPanel = new JPanel(new BorderLayout());
    filePathLabel = new JLabel(NO_FILE_SELECTED);

    filePathLabelPanel.add(filePathLabel, BorderLayout.CENTER);
    //add(buttonPanel, BorderLayout.LINE_START);


    add(filePathLabelPanel);

    selectFileButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        try {

          filePath = FileDialog.show(Hierarchy.getFrame(FileSelector.this), "Select image to use as preview image", java.awt.FileDialog.LOAD);
          resizeSelectedFileLabel();
        } catch(UserCancelException e) {
          //We don't care if the user cancels (filePath just stays null), so we can ignore this exception
        }
      }

    });
    parent.addComponentListener(new ComponentListener() {

      @Override
      public void componentResized(ComponentEvent componentEvent) {
        resizeSelectedFileLabel();
      }

      @Override
      public void componentMoved(ComponentEvent componentEvent) {}

      @Override
      public void componentShown(ComponentEvent componentEvent) {}

      @Override
      public void componentHidden(ComponentEvent componentEvent) {}

    });
  }

  @Override
  public void setEnabled(boolean enable) {
    filePathLabel.setEnabled(enable);
    selectFileButton.setEnabled(enable);
  }

  private void resizeSelectedFileLabel() {
    if(filePath != null) {
      String toSet = filePath;
      FontMetrics metrics = filePathLabel.getFontMetrics(filePathLabel.getFont());
      double availableWidth = filePathLabelPanel.getVisibleRect().getWidth();
      System.out.println(availableWidth);
      while(toSet.length() > 2 && metrics.stringWidth(toSet) > availableWidth) {
        toSet = "\u2026" + toSet.substring(2);
      }
      filePathLabel.setText(toSet);
    }

  }

  public String getFilePath() {
    return filePath;
  }
}

