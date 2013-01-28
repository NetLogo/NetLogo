package org.nlogo.deltatick;

import org.nlogo.deltatick.buttons.EditButton;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
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
            row[2] = new String(var.color);
            row[3] = new EditButton(traitInfoTable, traitTableModel.editAction, 3);
            tempTableData.add(row);
        } // for map


        traitTableModel.setTableData(tempTableData);
        traitInfoTable.setModel(traitTableModel);
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
            return flagData.get(rowIndex)[columnIndex];              //locating cell, and getting flag data -(Jan 26, 2013)
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
