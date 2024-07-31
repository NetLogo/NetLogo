// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.I18N;
import org.nlogo.swing.DialogForegrounder$;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ColorDialog extends JDialog implements ActionListener,
    ChangeListener,
    ClipboardOwner,
    WindowListener {

  private int okCancelFlag = 0;
  private boolean plotPenMode = false;

  private enum ColorMode {
    RGB, HSB
  }

  private ColorMode mode = ColorMode.RGB;

  private class ModeSelect extends JPanel {
    public ModeSelect() {
      super();

      add(new JLabel("Mode:"));

      JComboBox<ColorMode> combo = new JComboBox<ColorMode>(new ColorMode[] { ColorMode.RGB, ColorMode.HSB });

      add(combo);

      combo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          switchMode((ColorMode)combo.getSelectedItem());
          updateColors();
        }
      });
    }
  }

  private ModeSelect modeSelect = new ModeSelect();

  private JButton copyButton = new JButton(I18N.guiJ().get("tools.colorswatch.copy"));
  private JButton okButton = new JButton(I18N.guiJ().get("common.buttons.ok"));
  private JButton cancelButton = new JButton(I18N.guiJ().get("common.buttons.cancel"));

  private Color selectedColor;

  private class ColorSliderUI extends BasicSliderUI {
    private Color[] colorBounds = new Color[] { Color.black, Color.white };
    private float[] fracs = new float[] { 0, 1 };

    public ColorSliderUI(JSlider slider) {
      super(slider);
    }

    public void paintThumb(Graphics g) {
      g.setColor(Color.white);
      g.fillRect(thumbRect.x + 1, thumbRect.y + 1, thumbRect.width - 2, thumbRect.height - 2);
    }

    public void paintTrack(Graphics g) {
      Graphics2D g2d = (Graphics2D)g;

      g2d.setPaint(new LinearGradientPaint(trackRect.x, trackRect.y, trackRect.width, trackRect.height, fracs,
                                           colorBounds));
      g2d.fillRect(trackRect.x - thumbRect.width / 2, trackRect.y, trackRect.width + thumbRect.width,
                   trackRect.height);
    }

    public void setColorBounds(Color start, Color end) {
      colorBounds = new Color[] { start, end };
      fracs = new float[] { 0, 1 };
    }

    public void setColorBoundsHue(float s, float b) {
      colorBounds = new Color[] {
        Color.getHSBColor(0, s, b),
        Color.getHSBColor(0.2f, s, b),
        Color.getHSBColor(0.4f, s, b),
        Color.getHSBColor(0.6f, s, b),
        Color.getHSBColor(0.8f, s, b),
        Color.getHSBColor(1, s, b)
      };

      fracs = new float[] { 0, 0.2f, 0.4f, 0.6f, 0.8f, 1 };
    }
  }

  private class ColorSlider extends JSlider {
    private ColorSliderUI sliderUI;

    public ColorSlider() {
      super(0, 255, 0);

      sliderUI = new ColorSliderUI(this);

      setUI(sliderUI);
    }

    public void setColorBounds(Color start, Color end) {
      sliderUI.setColorBounds(start, end);

      repaint();
    }

    public void setColorBoundsHue(float s, float b) {
      sliderUI.setColorBoundsHue(s, b);

      repaint();
    }
  }

  private class ColorPreview extends JPanel {
    public Dimension getPreferredSize() {
      return new Dimension(50, getHeight());
    }
  }

  private ColorSlider slider1 = new ColorSlider();
  private ColorSlider slider2 = new ColorSlider();
  private ColorSlider slider3 = new ColorSlider();

  private ColorPreview colorPreview = new ColorPreview();

  public ColorDialog(Frame frame, boolean modalFlag) {
    super(frame, I18N.guiJ().get("tools.colorswatch"), modalFlag);

    setVisible(false);
    setResizable(false);

    final Container pane = getContentPane();

    pane.setLayout(new GridBagLayout());
    setDefaultCloseOperation(HIDE_ON_CLOSE);

    addWindowListener(this);

    final int margin = 10;

    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(margin, margin, 0, 0);

    pane.add(modeSelect, c);

    c.gridx = 0;
    c.gridy = 1;
    c.anchor = GridBagConstraints.CENTER;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(margin, margin, 0, 0);

    pane.add(slider1, c);

    c.gridy = 2;
    c.insets = new Insets(margin, margin, margin, 0);

    pane.add(slider2, c);

    c.gridy = 3;
    c.insets = new Insets(0, margin, margin, 0);

    pane.add(slider3, c);

    slider1.addChangeListener(this);
    slider2.addChangeListener(this);
    slider3.addChangeListener(this);

    c.gridx = 2;
    c.gridy = 1;
    c.gridheight = 3;
    c.fill = GridBagConstraints.VERTICAL;
    c.insets = new Insets(margin, margin, margin, margin);

    pane.add(colorPreview, c);

    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, margin, margin, 0);

    pane.add(copyButton, c);

    c.gridx = 1;
    c.insets = new Insets(0, 0, margin, margin);

    pane.add(okButton, c);

    c.gridx = 2;

    pane.add(cancelButton, c);

    copyButton.setActionCommand("Copy");

    copyButton.addActionListener(this);
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);

    org.nlogo.swing.Utils.addEscKeyAction
        (this,
            new javax.swing.AbstractAction() {
              public void actionPerformed(java.awt.event.ActionEvent e) {
                setVisible(false);
                dispose();
              }
            });
    DialogForegrounder$.MODULE$.apply(this);

    pack();
  }

  public void stateChanged(ChangeEvent e) {
    updateColors();
  }

  private void switchMode(ColorMode newMode) {
    Color color;

    if (mode == ColorMode.RGB) {
      color = new Color(slider1.getValue(), slider2.getValue(), slider3.getValue());
    }

    else {
      color = Color.getHSBColor(slider1.getValue() / 255f, slider2.getValue() / 255f, slider3.getValue() / 255f);
    }

    if (newMode == ColorMode.RGB) {
      slider1.setValue(color.getRed());
      slider2.setValue(color.getGreen());
      slider3.setValue(color.getBlue());
    }

    else {
      float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

      slider1.setValue((int)(hsb[0] * 255));
      slider2.setValue((int)(hsb[1] * 255));
      slider3.setValue((int)(hsb[2] * 255));
    }

    mode = newMode;
  }

  private void updateColors() {
    int one = slider1.getValue();
    int two = slider2.getValue();
    int three = slider3.getValue();

    if (mode == ColorMode.RGB) {
      selectedColor = new Color(one, two, three);

      slider1.setColorBounds(new Color(0, two, three), new Color(255, two, three));
      slider2.setColorBounds(new Color(one, 0, three), new Color(one, 255, three));
      slider3.setColorBounds(new Color(one, two, 0), new Color(one, two, 255));
    }
    
    else {
      float onef = (float)one / 255;
      float twof = (float)two / 255;
      float threef = (float)three / 255;

      selectedColor = Color.getHSBColor(onef, twof, threef);

      slider1.setColorBoundsHue(twof, threef);
      slider2.setColorBounds(Color.getHSBColor(onef, 0, threef), Color.getHSBColor(onef, 1, threef));
      slider3.setColorBounds(Color.getHSBColor(onef, twof, 0), Color.getHSBColor(onef, twof, 1));
    }

    colorPreview.setBackground(selectedColor);
  }

  public void showDialog() {
    plotPenMode = false;

    updateColors();
    setVisible(true);
  }

  public Color showPlotPenDialog(Color initialColor) {
    plotPenMode = true;

    slider1.setValue(initialColor.getRed());
    slider2.setValue(initialColor.getGreen());
    slider3.setValue(initialColor.getBlue());

    updateColors();
    setVisible(true);

    if (okCancelFlag == 1) {
      return selectedColor;
    } else {
      return null;
    }
  }

  public Color showInputBoxDialog(Color initialColor) {
    plotPenMode = true;

    slider1.setValue(initialColor.getRed());
    slider2.setValue(initialColor.getGreen());
    slider3.setValue(initialColor.getBlue());

    updateColors();
    setVisible(true);

    if (okCancelFlag == 1) {
      return selectedColor;
    } else {
      return initialColor;
    }
  }

  /// Event Handling

  public void actionPerformed(ActionEvent e) {
    String actionCommand = e.getActionCommand();
    if (actionCommand.equals("Copy")) {
      Toolkit tk = Toolkit.getDefaultToolkit();
      StringSelection st = new StringSelection(org.nlogo.api.Color.getClosestColorNameByARGB(selectedColor.getRGB()));
      Clipboard cp = tk.getSystemClipboard();
      cp.setContents(st, this);
    } else if (actionCommand.equals(I18N.guiJ().get("common.buttons.ok"))) {
      okCancelFlag = 1;
      dispose();
    } else if (actionCommand.equals(I18N.guiJ().get("common.buttons.cancel"))) {
      okCancelFlag = -1;
      dispose();
    }
  }

  public void windowClosing(WindowEvent e) {
    // I check the flag below to see if we are in PlotPenMode
    // In that case closing the window is the same as pressing OK
    // This is a different behavior than in the "MenuToolsMode"
    if (plotPenMode) {
      okCancelFlag = 1;
      okButton.doClick();
    }
  }

  // The following callback are not used for anything
  // but the Interfaces demands them

  public void lostOwnership(Clipboard clip, Transferable tr) {
  }

  public void windowOpened(WindowEvent arg0) {
  }

  public void windowClosed(WindowEvent arg0) {
  }

  public void windowIconified(WindowEvent arg0) {
  }

  public void windowDeiconified(WindowEvent arg0) {
  }

  public void windowActivated(WindowEvent arg0) {
  }

  public void windowDeactivated(WindowEvent arg0) {
  }
}
