// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Dimension, Font }
import javax.swing.{ Box, JComponent, JDialog, JLabel, SwingConstants }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

object TextFieldBox {
  /**
   * For testing. *
   */
  def main(argv: List[String]): Unit = {
    val fb = new TextFieldBox(SwingConstants.LEFT)

    fb.addField("Name:", new TextField(20))
    fb.addField("Server location or IP address:", new TextField(8))
    fb.addField("Port:", new TextField(20))

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
 * nicely aligned.
 */
class TextFieldBox(labelAlignment: Int = SwingConstants.LEFT, labelFont: Option[Font] = None,
                   fieldFont: Option[Font] = None) extends BoxColumn(4) with ThemeSync {

  private var maxLabelWidth = 0
  private var labels = Seq[JLabel]()

  /**
   * Adds a field.
   *
   * @param prompt    the text of the label
   * @param textField the field
   */
  def addField(prompt: String, textField: JComponent & Zoomable): Unit = {
    addField(new JLabel(prompt, labelAlignment) with Zoomable, textField)
  }

  /**
   * Adds a field.
   *
   * @param label     the text of the label
   * @param textField the field
   */
  def addField(label: JLabel & Zoomable, textField: JComponent & Zoomable): Unit = {
    label.setLabelFor(textField)

    labelFont.foreach(label.setBaseFont)
    fieldFont.foreach(textField.setBaseFont)

    textField.setMaximumSize(textField.getPreferredSize)

    add(new BoxRow(Seq(label, textField), 8, BoxAlign.Start) with MaximumHeight)

    labels = labels :+ label

    // Make sure all the labels have the same preferred width
    val w = label.getPreferredSize.width

    if (w > maxLabelWidth)
      maxLabelWidth = w

    for (label <- labels)
      label.setPreferredSize(new Dimension(maxLabelWidth, label.getPreferredSize.height))
  }

  override def syncTheme(): Unit = {
    labels.foreach(_.setForeground(InterfaceColors.dialogText()))
  }
}
