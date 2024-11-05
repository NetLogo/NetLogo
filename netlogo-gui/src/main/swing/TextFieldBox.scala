// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension, Font }
import javax.swing.{ Box, BoxLayout, JComponent, JDialog, JLabel, JPanel, JTextField, SwingConstants }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

object TextFieldBox {
  /**
   * For testing. *
   */
  def main(argv: List[String]) {
    val fb = new TextFieldBox(SwingConstants.LEFT)

    fb.addField("Name:", new JTextField(20))
    fb.addField("Server location or IP address:", new JTextField(8))
    fb.addField("Port:", new JTextField(20))

    fb.add(Box.createGlue())

    val d = new JDialog

    d.setTitle("TextFieldBox");
    d.setContentPane(fb);
    d.setVisible(true);

    d.setSize(new Dimension(fb.getPreferredSize().width + 20, fb.getPreferredSize().height + 40));
  }
}

/**
 * A box for TextFields and their labels that keeps the fields and labels
 * nicely alligned.
 */
class TextFieldBox(labelAlignment: Int = SwingConstants.LEFT, labelFont: Font = null, fieldFont: Font = null)
  extends JPanel with ThemeSync {

  private var maxLabelWidth = 0
  private var labels: List[JLabel] = Nil

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

  /**
   * Adds a field.
   *
   * @param prompt    the text of the label
   * @param textField the field
   */
  def addField(prompt: String, textField: JComponent) {
    addField(new JLabel(prompt, labelAlignment), textField)
  }

  /**
   * Adds a field.
   *
   * @param label     the text of the label
   * @param textField the field
   */
  def addField(label: JLabel, textField: JComponent) {
    label.setLabelFor(textField)

    if (labelFont != null)
      label.setFont(labelFont)
    
    if (fieldFont != null)
      textField.setFont(fieldFont)

    textField.setMaximumSize(textField.getPreferredSize)

    val holder = new Box(BoxLayout.X_AXIS)

    labels = labels :+ label

    // Add the label and the textfield in a holder
    label.setAlignmentX(Component.LEFT_ALIGNMENT)
    textField.setAlignmentX(Component.LEFT_ALIGNMENT)

    holder.add(label)
    holder.add(Box.createHorizontalStrut(8))
    holder.add((textField))
    holder.setAlignmentX(Component.LEFT_ALIGNMENT)
    holder.add(Box.createHorizontalGlue())
    holder.setMaximumSize(new Dimension(holder.getMaximumSize().width, holder.getPreferredSize().height))

    add(holder)

    add(Box.createVerticalStrut(4))

    // Make sure all the labels have the same preferred width
    val w = label.getPreferredSize.width

    if (w > maxLabelWidth)
      maxLabelWidth = w
    
    for (label <- labels)
      label.setPreferredSize(new Dimension(maxLabelWidth, label.getPreferredSize.height))
  }

  def syncTheme() {
    labels.foreach(_.setForeground(InterfaceColors.DIALOG_TEXT))
  }
}
