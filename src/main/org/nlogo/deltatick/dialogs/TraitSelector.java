package org.nlogo.deltatick.dialogs;

import org.nlogo.deltatick.TraitBlock;
//import org.nlogo.deltatick.TraitDisplayPanel;
import org.nlogo.deltatick.TraitDistribution;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 1/24/13
 * Time: 10:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class TraitSelector extends JDialog {
        //Buttons & text
        private javax.swing.JButton cancel;
        private javax.swing.JButton add;
        private JLabel traitText;
        private JLabel variationText;
        private JLabel variationValueText;
        boolean isTraitSelected;

        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JList myTraitsList;
        private String selectedTrait;
        private JList myVariationsList;
        HashMap<String, TraitBlock> breedTraitHashMap = new HashMap<String, TraitBlock>();
        //to store breed and corresponding trait
        ListSelectionModel listSelectionModel;
        JTable traitInfoTable;
        TraitDistribution traitDistribution;

        ArrayList<Trait> traitsList = new ArrayList<Trait>();
        public static final int NUMBER_COLUMNS = 4;

        private javax.swing.JDialog thisDialog = this;

        public TraitSelector() {
            initComponents();
            add.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent evt) {//
                                isTraitSelected = true;
                                thisDialog.setVisible(false);
                            }
                        });
                        cancel.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                isTraitSelected = false;
                                thisDialog.setVisible(false);
                            }
                        });

            this.setPreferredSize(new Dimension(600, 250));
            this.setVisible(false);
        }

        public void setTraits(ArrayList<Trait> list) {
            this.traitsList = list;
        }

         private String [] getTraitTypes() {
           String[] traitTypes = new String[traitsList.size()];
            int i = 0;
            for (Trait trait : traitsList) {
            traitTypes[i] = trait.getNameTrait();
            i++;
            }
             return traitTypes;
         }

        public boolean getIsTraitSelected() {
            return isTraitSelected;
        }

        public void showMe() {
            final String[] traitStrings = getTraitTypes();
                    //breedBlock.getTraitTypes();
            myTraitsList = new JList(traitStrings);

            myTraitsList.setModel(new javax.swing.AbstractListModel() {
                public int getSize() {
                    return traitStrings.length;
                }
                public Object getElementAt(int i) {
                    return traitStrings[i];
                }
            });
            jScrollPane1.setViewportView(myTraitsList);

            listSelectionModel = myTraitsList.getSelectionModel();
            listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listSelectionModel.addListSelectionListener(
                    new TraitListSelectionHandler());
            this.setVisible(true);
        }


        class TraitListSelectionHandler implements ListSelectionListener {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                myVariationsList = new JList();
                if (lsm.isSelectionEmpty()) {
                    System.out.println("No trait selected");
                }
                else {
                    // gen data[][] based on selected trait
                    ArrayList<Object[]> tempTableData = new ArrayList<Object[]>();

                    for (Trait trait : traitsList) {
                        if (trait.getNameTrait().equalsIgnoreCase(getSelectedTraitName())) {
                            for (Map.Entry<String, Variation> entry : trait.getVariationHashMap().entrySet()) {
                                String key = entry.getKey();
                                Variation var = entry.getValue();
                                Object[] row = new Object[NUMBER_COLUMNS];

                                row[0] = new String(key);
                                row[1] = new String(var.value);
                                row[2] = new Boolean(false);
                                //row[2] = new JRadioButton("add");
                                //row[2] = new String(var.color);
                                String s = String.valueOf(var.percent);
                                row[3] = new String(s + "%");
                                tempTableData.add(row);

                            } // for map
                        } // trait match
                    } // for trait


                    // make table model
                    // send data to tablemodel
                    TraitTableModel traitTableModel = new TraitTableModel();
                    traitTableModel.setTraitData(tempTableData);
                    traitInfoTable.setModel(traitTableModel);

                    traitTableModel.addTableModelListener(new TableModelListener() {
                        @Override
                        public void tableChanged(TableModelEvent e) {
                            //int row = e.getFirstRow();
                            int column = e.getColumn();
                            TableModel model = (TableModel)e.getSource();
                            String columnName = model.getColumnName(column);
                            //Object data = model.getValueAt(row, column);

                            ArrayList<String> selectedVariations = new ArrayList<String>();
                            for (int row = 0; row < model.getRowCount(); row++) {
                              if ((Boolean) model.getValueAt(row, 2) == true) {
                                selectedVariations.add((String) model.getValueAt(row, 0));
                              }
                            } // for
                            //TraitDistribution traitDistribution = new TraitDistribution("breed", selectedTrait, selectedVariations);
                            //traitDistribution.initComponents("breed", selectedTrait, selectedVariations);
                        }
                    }); // Listener

                    final String[] variationStrings = getVariationTypes(getSelectedTraitName()) ;
                        myVariationsList.setModel(new javax.swing.AbstractListModel() {
                        public int getSize() {
                            return variationStrings.length;
                        }
                        public Object getElementAt(int i) {
                            return variationStrings[i];
                        }
                    });
                }
            }
        }


        public String[] getVariationTypes(String traitName) {
            String [] variations = null;
            for (Trait trait : traitsList) {
                if (trait.getNameTrait().equalsIgnoreCase(traitName)) {
                    variations = new String[trait.getVariationsList().size()];
                    trait.getVariationsList().toArray(variations);
                }
            }
            return variations;
        }

        public void activateButtons() {
//            add.addActionListener(new java.awt.event.ActionListener() {
//                public void actionPerformed(java.awt.event.ActionEvent evt) {//
//                    thisDialog.setVisible(false);
//                }
//            });
//            cancel.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    thisDialog.setVisible(false);
//                }
//            });
        }

        public String getSelectedTraitName() {
            selectedTrait = myTraitsList.getSelectedValue().toString();
            return selectedTrait;
        }

        public Trait getSelectedTrait() {
            Trait temp = new Trait();
            for (Trait trait : traitsList) {
                if (trait.getNameTrait().equalsIgnoreCase(getSelectedTraitName())) {
                    temp = trait;
                }
            }
            return temp;
        }

        public void initComponents() {
            traitText = new JLabel("Pick a trait");
            traitText.setPreferredSize(new Dimension(20, 100));
            add = new JButton("Add");

            jScrollPane1 = new JScrollPane();
            jScrollPane1.setPreferredSize(new Dimension(150, 75));

            cancel = new JButton("Cancel");

            traitInfoTable = new JTable(new TraitTableModel());

            traitDistribution = new TraitDistribution();

            //traitDistribution.initComponents();

            traitInfoTable.setPreferredScrollableViewportSize(new Dimension(350, 250));
            traitInfoTable.setPreferredSize(new Dimension(350, 250));
            JTableHeader header = traitInfoTable.getTableHeader();
            initColumnSizes(traitInfoTable);
            activateButtons();

            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

            org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);

            layout.setHorizontalGroup(
                    layout.createParallelGroup()
                            .add(layout.createSequentialGroup()
                                    .add(layout.createParallelGroup()
                                            .add(traitText)
                                            .add(jScrollPane1)

                                    )
                                    .add(layout.createParallelGroup()
                                            .add(header)
                                            .add(traitInfoTable)
                                    ))
                                    .add(traitDistribution)
                                    .add(layout.createSequentialGroup()
                                            .add(add)
                                            .add(cancel)
                                    )
            );

            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .add(layout.createParallelGroup()
                                    .add(traitText)
                                    .add(header)
                            )
                            .add(layout.createParallelGroup()
                                    .add(jScrollPane1)
                                    .add(traitInfoTable)
                            )
                            .add(traitDistribution)
                            .add(layout.createParallelGroup()
                                    .add(cancel)
                                    .add(add)
                            )
            );
            pack();
        }

    private void initColumnSizes(JTable table) {
        TraitTableModel model = (TraitTableModel)table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        TableCellRenderer headerRenderer =
            table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < NUMBER_COLUMNS; i++) {
            column = table.getColumnModel().getColumn(i);
            headerWidth = 100;
            cellWidth = 100;
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }


    class TraitTableModel extends AbstractTableModel {
        private String[] columnNames = {"Variation name", "Value", "Add variation?", "Edit"};

        private ArrayList<Object[]> tableData = new ArrayList<Object[]>();




        public void setTraitData(ArrayList<Object[]> source) {
            // Clear previous data
            tableData.clear();

            for (int i = 0; i < source.size(); i++) { // Clear the row (must start with empty row)
                Object[] row = new Object[NUMBER_COLUMNS];         // Generate the row
                for (int j = 0; j < NUMBER_COLUMNS; j++) {  // for j, Add the row to tableData
                    row[j] = source.get(i)[j];
                }
                tableData.add(row);
            } // for i
        }

        public boolean isCellEditable(int rowIndex, int columnIndex){
            if (columnIndex == 2) {
                return true;
            }
            else {
            return false;
            }
        }

        public void setValueAt(Object value, int row, int col) {
            tableData.get(row)[col] = value;
            fireTableCellUpdated(row, col);
        }
        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
       public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
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



        public void displayColorButton(int row, int col) {
            if (getValueAt(row, 2).equals(false)) {
                setValueAt("color", row, 3);
            }
        }


    }
    }





