package org.nlogo.modelingcommons;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.Color;
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
public class DisableableComboBox extends JComboBox {
  private class Item {
    Object obj;
    boolean enabled;
    public Item(Object obj, boolean enabled) {
      this.obj = obj;
      this.enabled = enabled;
    }

    public Object getObj() {
      return obj;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String toString() {
      return obj.toString();
    }
  }
  //private List<Boolean> isEnabled = new ArrayList<Boolean>();
  public DisableableComboBox() {
    super();
    setRenderer(new BasicComboBoxRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if( value != null && !( ((Item)(value)).isEnabled() ) ) {
          component.setEnabled(false);
          component.setForeground(UIManager.getColor("ComboBox.disabledForeground"));
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
    Item item = new Item(anObject, isObjectEnabled);
    super.addItem(item);
    return getItemCount() - 1;
  }

  public void setIndexEnabled(int index, boolean isObjectEnabled) {
    if(index >= 0 && index < getItemCount()) {
      Item item = (Item)(getItemAt(index));
      item.setEnabled(isObjectEnabled);

    }
  }

  @Override
  public void setSelectedIndex(int i) {
    if(i >= 0 && i < getItemCount() && !( ((Item)(getItemAt(i))).isEnabled())  ) {
      return;
    }
    super.setSelectedIndex(i);
  }

  //Use instead of getSelectedItem
  public Object getSelectedObject() {
    Object selectedItem = super.getSelectedItem();
    if(selectedItem == null) {
      return null;
    } else {
      return ((Item)(selectedItem)).getObj();
    }
  }
}
