// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * A box for TextFields and their labels that keeps the fields and labels
 * nicely alligned.
 */
public strictfp class TextFieldBox extends JPanel {

  private int labelAlignment;
  private Font labelFont, fieldFont;
  private int maxLabelWidth = 0;
  private final List<JLabel> labels = new ArrayList<JLabel>();

  /**
   * Creates a new TextFieldBox with labels aligned on the left.
   */
  public TextFieldBox() {
    this(SwingConstants.LEFT, null, null);
  }

  /**
   * Creates a new TextFieldBox.
   *
   * @param labelAlignment how the labels are aligned.
   *                       (<code>SwingConstants.RIGHT</code>, <code>SwingConstants.LEFT</code>, etc.)
   */
  public TextFieldBox(int labelAlignment) {
    this(labelAlignment, null, null);
  }

  /**
   * Creates a new TextFieldBox.
   *
   * @param labelAlignment how the labels are aligned.
   *                       (<code>SwingConstants.RIGHT</code>, <code>SwingConstants.LEFT</code>, etc.)
   */
  public TextFieldBox(int labelAlignment, Font labelFont, Font fieldFont) {
    super();
    this.labelAlignment = labelAlignment;
    this.labelFont = labelFont;
    this.fieldFont = fieldFont;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  }

  /**
   * Adds a field.
   *
   * @param prompt    the text of the label
   * @param textField the field
   */
  public void addField(String prompt, JComponent textField) {
    JLabel label = new JLabel(prompt, labelAlignment);
    addField(label, textField);
  }

  /**
   * Adds a field.
   *
   * @param label     the text of the label
   * @param textField the field
   */
  public void addField(JLabel label, JComponent textField) {
    label.setLabelFor(textField);

    if (labelFont != null) {
      label.setFont(labelFont);
    }
    if (fieldFont != null) {
      textField.setFont(fieldFont);
    }

    textField.setMaximumSize(textField.getPreferredSize());

    Box holder = new Box(BoxLayout.X_AXIS);
    labels.add(label);

    // Add the label and the textfield in a holder
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    textField.setAlignmentX(Component.LEFT_ALIGNMENT);
    holder.add(label);
    holder.add(Box.createHorizontalStrut(8));
    holder.add((textField));
    holder.setAlignmentX(Component.LEFT_ALIGNMENT);
    holder.add(Box.createHorizontalGlue());
    holder.setMaximumSize(
        new Dimension(holder.getMaximumSize().width, holder.getPreferredSize().height)
    );
    add(holder);

    add(Box.createVerticalStrut(4));

    // Make sure all the labels have the same preferred width
    int w = label.getPreferredSize().width;

    if (w > maxLabelWidth) {
      maxLabelWidth = w;
    }
    for (int j = 0; j < labels.size(); j++) {
      JLabel l = labels.get(j);
      l.setPreferredSize(new java.awt.Dimension(maxLabelWidth, l.getPreferredSize().height));
    }

  }

  /**
   * For testing. *
   */
  public static void main(String[] argv) {
    TextFieldBox fb = new TextFieldBox(SwingConstants.LEFT);

    fb.addField("Name:", new JTextField(20));
    fb.addField("Server location or IP address:", new JTextField(8));
    fb.addField("Port:", new JTextField(20));

    fb.add(Box.createGlue());

    javax.swing.JDialog d = new javax.swing.JDialog();
    d.setTitle("TextFieldBox");
    d.setContentPane(fb);
    d.setVisible(true);

    d.setSize(new Dimension(fb.getPreferredSize().width + 20, fb.getPreferredSize().height + 40));
  }

}
