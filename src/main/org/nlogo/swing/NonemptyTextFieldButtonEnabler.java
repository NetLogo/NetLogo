// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

import javax.swing.JButton;
import javax.swing.JTextField;
import java.util.HashSet;
import java.util.Set;

/**
 * Makes a JButton enabled only if a set of TextFields are not empty.
 */
public strictfp class NonemptyTextFieldButtonEnabler
    implements javax.swing.event.DocumentListener {

  private final JButton target;
  private final Set<JTextField> fields;
  private boolean enabled;

  /**
   * Creates a new button enabler.
   */
  public NonemptyTextFieldButtonEnabler(JButton target) {
    this.target = target;
    fields = new HashSet<JTextField>();
    enabled = true;
  }

  /**
   * Adds a field to the set of fields that must be
   * not empty for the target button to be enabled.
   */
  public void addRequiredField(JTextField field) {
    field.getDocument().addDocumentListener(this);
    fields.add(field);
    update();
  }

  /**
   * Disables or Enables the Enabler (and button).
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    update();
  }

  /**
   * Updates the enabled state of the button.
   */
  private void update() {
    target.setEnabled(areFieldsNotEmpty());
  }

  /**
   * Returns true if all the fields are not empty.
   */
  private boolean areFieldsNotEmpty() {
    if (!enabled) {
      return false;
    }
    for (JTextField field : fields) {
      if (field.getDocument().getLength() == 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * From interface DocumentListener
   */
  public void changedUpdate(javax.swing.event.DocumentEvent e) {
    update();
  }

  /**
   * From interface DocumentListener
   */
  public void insertUpdate(javax.swing.event.DocumentEvent e) {
    update();
  }

  /**
   * From interface DocumentListener
   */
  public void removeUpdate(javax.swing.event.DocumentEvent e) {
    update();
  }

}
