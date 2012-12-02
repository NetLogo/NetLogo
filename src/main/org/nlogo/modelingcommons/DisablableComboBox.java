package org.nlogo.modelingcommons;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 11/28/12
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisablableComboBox extends JComboBox {
  private List<Boolean> isEnabled = new ArrayList<Boolean>();
  public DisablableComboBox() {
    super();
    setRenderer(new BasicComboBoxRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(index >= 0 && index < isEnabled.size() && !isEnabled.get(index)) {
          component.setEnabled(false);
          component.setVisible(false);
        } else {
          component.setEnabled(true);
          component.setVisible(true);
        }
        return component;
      }

    });

  }
  public int addItem(Object anObject, boolean isObjectEnabled) {
    super.addItem(anObject);
    isEnabled.add(isObjectEnabled);
    return isEnabled.size() - 1;
  }

  public void setIndexEnabled(int index, boolean isObjectEnabled) {
    if(index >= 0 && index < isEnabled.size()) {
      isEnabled.set(index, isObjectEnabled);
    }
  }
  @Override
  public void setSelectedIndex(int i) {
    if(i >= 0 && i < isEnabled.size() && !isEnabled.get(i)) {
      return;
    }
    super.setSelectedIndex(i);
  }
}
