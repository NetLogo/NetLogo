// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

import javax.swing.JTextField;
import java.util.HashSet;
import java.util.Set;

/**
 * Makes an Action enabled only if a set of TextFields are not empty.
 */
public strictfp class NonemptyTextFieldActionEnabler
    implements javax.swing.event.DocumentListener {

  private final javax.swing.Action target;
  private final Set<JTextField> fields;

  /**
   * Creates a new button enabler.
   */
  public NonemptyTextFieldActionEnabler(javax.swing.Action target) {
    this.target = target;
    fields = new HashSet<JTextField>();
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
   * Updates the enabled state of the button.
   */
  private void update() {
    target.setEnabled(areFieldsNotEmpty());
  }

  /**
   * Returns true if all the fields are not empty.
   */
  private boolean areFieldsNotEmpty() {
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
