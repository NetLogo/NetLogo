/**
 * The ColorSchemeDialog is a simply widget that contains a color SchemesPanel.
 * The values of the selected color scheme are accessed throught the contained
 * widget:
 * colorSchemesDialog.colorSchemesWidget.getcolorSchemeRGBArray();
 * or
 * colorSchemesDialog.colorSchemesWidget.getcolorSchemeColorArray();
 *
 * The Ok Button leaves the selected scheme while the Cancel Button restores
 * the initial values of the widget
 */

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.AWTPermission;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;


import javax.swing.border.EtchedBorder;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.nlogo.api.ExtensionException;


//TODO: Handle better the InitialcolorLegendName, InitialcolorLegendName, InitialcolorSchemeSize

public class ColorSchemesDialog extends JDialog implements ActionListener,
ClipboardOwner {

  private static final long serialVersionUID = 1L;
  ColorSchemesPanel colorSchemesPanel;
  JButton copyButton;
  JButton closeButton;
  JLabel statusLabel;

  String InitialcolorLegendName;
  String InitialcolorSchemeType;
  int InitialcolorSchemeSize;

  public ColorSchemesDialog (Frame frame) {
    super (frame, "Color Scheme Swatches");
    setAlwaysOnTop(true);
    statusLabel = new JLabel("TEST                      ");

    try {
      colorSchemesPanel= new ColorSchemesPanel(statusLabel);
    }
    catch(ExtensionException ex) { }

    InitialcolorLegendName = colorSchemesPanel.colorLegendName;
    InitialcolorSchemeType = colorSchemesPanel.colorSchemeType;
    InitialcolorSchemeSize = colorSchemesPanel.colorSchemeSize;

    colorSchemesPanel.setVisible(true);

    copyButton = new JButton("Copy");
    copyButton.addActionListener(this);

    closeButton = new JButton("Close");
    closeButton.addActionListener(this);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel,
                                        BoxLayout.LINE_AXIS));

    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(copyButton);

    //Add the Status Label
    statusLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    statusLabel.setHorizontalAlignment(JLabel.RIGHT);
    buttonPanel.add(statusLabel, BorderLayout.CENTER);

    buttonPanel.add(Box.createRigidArea(new Dimension(50,5)));
    buttonPanel.add(closeButton);
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,5));


    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.add(buttonPanel, BorderLayout.PAGE_END);
    contentPane.add(colorSchemesPanel, BorderLayout.CENTER);
    contentPane.setOpaque(true);
    setContentPane(contentPane);

    //Pack it.
    pack();
  }

  public void showDialog() {
    setVisible(true);
  }

    /** This method handles events for the text field. */
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == copyButton) {
      // The java.lang.SecurityManager API has been terminally deprecated.
      // I removed its use here. See https://openjdk.java.net/jeps/411 for a
      // discussion of why it is being removed. AAB May 2022
      Toolkit tk = Toolkit.getDefaultToolkit();
      StringSelection st = new StringSelection(statusLabel.getText());
      Clipboard cp = tk.getSystemClipboard();
      cp.setContents(st, this);
    }
    else if (e.getSource() == closeButton) {
        // restore the selected values to the initial values
      try {
            colorSchemesPanel.setLegend(InitialcolorSchemeType,
                       InitialcolorLegendName,
                       InitialcolorSchemeSize);
      }
      catch(ExtensionException ex) { }
      setVisible(false);
      dispose();
    }
  }

  // The following callback are not used for anything
  // but the Interfaces demands them
  public void lostOwnership(Clipboard arg0,Transferable arg1) { }
}
