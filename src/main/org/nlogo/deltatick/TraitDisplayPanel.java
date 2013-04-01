package org.nlogo.deltatick;

import org.nlogo.deltatick.buttons.EditButton;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 1/25/13
 * Time: 9:08 PM
 * To change this template use File | Settings | File Templates.
 */

// Not in use any more

public class TraitDisplayPanel extends JPanel {
    String traitName; //name of tab
    public static final int NUMBER_COLUMNS = 4;
    JTable traitInfoTable = new JTable(new TraitTableModel());

    public TraitDisplayPanel (Trait trait) {
        ArrayList<Object[]> tempTableData = new ArrayList<Object[]>();
        TraitTableModel traitTableModel = new TraitTableModel();

        for (Map.Entry<String, Variation> entry : trait.getVariationHashMap().entrySet()) {
            String key = entry.getKey();
            Variation var = entry.getValue();
            Object[] row = new Object[NUMBER_COLUMNS];
            row[0] = new String(key);
            row[1] = new String(var.value);
            row[2] = new String(var.value);
            row[3] = new String("Edit");
            //row[3] = new EditButton(traitInfoTable, traitTableModel.editAction, 3);
            tempTableData.add(row);
        } // for map

        traitTableModel.setTableData(tempTableData);
        traitInfoTable.setModel(traitTableModel);
        traitInfoTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        traitInfoTable.getColumn("Edit").setCellEditor(
        new ButtonEditor(new JCheckBox()));
        this.add(traitInfoTable);
    }

    class TraitTableModel extends AbstractTableModel {
        private String[] columnNames = {"Variation name", "Value", "Color", "Edit"};

        private ArrayList<Object[]> tableData = new ArrayList<Object[]>();
        private ArrayList<Boolean[]> flagData = new ArrayList<Boolean[]>();

        public void setTableData(ArrayList<Object[]> source) {
            // Clear previous data
            tableData.clear();
            Boolean[] flag = new Boolean[NUMBER_COLUMNS];

            for (int i = 0; i < source.size(); i++) {
                Object[] row = new Object[NUMBER_COLUMNS]; // Clear the row (must start with empty row)
                for (int j = 0; j < NUMBER_COLUMNS; j++) { // Generate the row
                    row[j] = source.get(i)[j];
                    flag[j] = false;
                } // for j
                // Add the row to tableData
                tableData.add(row);
                flagData.add(flag);
            } // for i
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            //return flagData.get(rowIndex)[columnIndex];              //locating cell, and getting flag data -(Jan 26, 2013)
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            tableData.get(row)[col] = value;
            fireTableCellUpdated(row, col);
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return tableData.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return tableData.get(row)[col];
        }

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable)e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand());
                for (int i = 0; i < NUMBER_COLUMNS - 1; i++) {
                    flagData.get(modelRow)[i] = false;
                }
        }};
    }
}

class ButtonRenderer extends JButton implements TableCellRenderer {

  public ButtonRenderer() {
    setOpaque(true);
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      setForeground(table.getSelectionForeground());
      setBackground(table.getSelectionBackground());
    } else {
      setForeground(table.getForeground());
      setBackground(UIManager.getColor("Button.background"));
    }
    setText((value == null) ? "" : value.toString());
    return this;
  }
}

class ButtonEditor extends DefaultCellEditor {
  protected JButton button;

  private String label;

  private boolean isPushed;

  public ButtonEditor(JCheckBox checkBox) {
    super(checkBox);
    button = new JButton();
    button.setOpaque(true);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fireEditingStopped();
      }
    });
  }
    public Component getTableCellEditorComponent(JTable table, Object value,
          boolean isSelected, int row, int column) {
        if (isSelected) {
          button.setForeground(table.getSelectionForeground());
          button.setBackground(table.getSelectionBackground());
        } else {
          button.setForeground(table.getForeground());
          button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
      }

      public Object getCellEditorValue() {
        if (isPushed) {
          // add Action here
        }
        isPushed = false;
        return new String(label);
      }

      public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
      }

      protected void fireEditingStopped() {
        super.fireEditingStopped();
      }
    }





