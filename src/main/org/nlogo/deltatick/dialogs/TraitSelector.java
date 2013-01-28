package org.nlogo.deltatick.dialogs;

import com.ibm.media.bean.multiplayer.RelatedLink;
import org.jdesktop.layout.*;
import org.jdesktop.layout.LayoutStyle;
import org.nlogo.deltatick.TraitBlock;
import org.nlogo.deltatick.TraitDisplayPanel;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        //private javax.swing.JLabel breedText;
        private JLabel traitText;
        private JLabel variationText;


        private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList myTraitsList;
        private String selectedTrait;
        private JList myVariationsList;
        HashMap<String, TraitBlock> breedTraitHashMap = new HashMap<String, TraitBlock>();
        //to store breed and corresponding trait

        ListSelectionModel listSelectionModel;
        JTable traitInfoTable;

        ArrayList<Trait> traitsList = new ArrayList<Trait>();
        public static final int NUMBER_COLUMNS = 4;

        private javax.swing.JDialog thisDialog = this;


        public TraitSelector() {

            initComponents();
            this.setPreferredSize(new Dimension(600, 250));
            this.setVisible(false);
        }

        public void setTraits(ArrayList<Trait> list) {
            this.traitsList = list;
            //((TraitTableModel)traitInfoTable).setTableData();
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
                                row[2] = new String(var.color);
                                row[3] = new String("EDIT");

                                tempTableData.add(row);

                            } // for map
                        } // trait match
                    } // for trait


                    // make table model
                    // send data to tablemodel
                    TraitTableModel traitTableModel = new TraitTableModel();
                    traitTableModel.setTraitData(tempTableData);
                    traitInfoTable.setModel(traitTableModel);

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
            add.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
//                    TraitDisplayPanel traitDisplayPanel = new TraitDisplayPanel(getSelectedTrait());
                    thisDialog.setVisible(false);
                }
            });
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
            variationText = new JLabel("Available variations");
            add = new JButton("Add");

            jScrollPane1 = new JScrollPane();
            jScrollPane1.setPreferredSize(new Dimension(150, 75));

            cancel = new JButton();
            traitInfoTable = new JTable(new TraitTableModel());
            traitInfoTable.setPreferredScrollableViewportSize(new Dimension(350, 150));
            traitInfoTable.setPreferredSize(new Dimension(350, 150));
            initColumnSizes(traitInfoTable);

            activateButtons();

            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

            org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);

//            layout.setHorizontalGroup(
//                    layout.createSequentialGroup()
//                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)// org.jdesktop.layout.GroupLayout.LEADING)
//                                    .add(traitText)
//                                            //.add(jScrollPane1)
//                                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 199, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                            )
//                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//                                    .add(variationText)
//                                    .add(layout.createSequentialGroup()
//                                            .add(add)
//                                    ))
//                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.HORIZONTAL)
//                                    .add(variationText)
//                                    .add(traitInfoTable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 199, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
//                    //.add(traitInfoTable)
//            );

            layout.setHorizontalGroup(
                    layout.createParallelGroup()
                        .add(layout.createSequentialGroup()
                            .add(layout.createParallelGroup()
                                 .add(traitText)
                                 .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createParallelGroup()
                                 .add(variationText)
                                 .add(traitInfoTable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(add))
            );


//            layout.setVerticalGroup(
//                    layout.createSequentialGroup()
//                            .add(layout.createParallelGroup()
//                                    .add(traitText)
//                                    //.add(variationText)
//                                    .add(variationText))
//                            .add(layout.createParallelGroup()
//                                    .add(jScrollPane1)
//                                    .add(traitInfoTable))
//                            .add(layout.createParallelGroup()
//                                    .add(add)
//                            )
//            );

            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .add(layout.createParallelGroup()
                                    .add(layout.createSequentialGroup()
                                            .add(traitText)
                                            .add(jScrollPane1))
                                    .add(layout.createSequentialGroup()
                                            .add(variationText)
                                            .add(traitInfoTable)))
                            .add(add)
            );

            pack();
        }

    private void initColumnSizes(JTable table) {
        TraitTableModel model = (TraitTableModel)table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        //Object[] longValues = model.longValues;
        TableCellRenderer headerRenderer =
            table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < NUMBER_COLUMNS; i++) {
            column = table.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                                 null, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;
            headerWidth = 100;

            comp = table.getDefaultRenderer(model.getColumnClass(i)).
                             getTableCellRendererComponent(
                                 table, "               ",
                                 false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;
            cellWidth = 100;
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    class TraitTableModel extends AbstractTableModel {
        private String[] columnNames = {"Variation name", "Value", "Color", "Edit"};

        private ArrayList<Object[]> tableData = new ArrayList<Object[]>();



        public void setTraitData(ArrayList<Object[]> source) {
            // Clear previous data
            tableData.clear();

            for (int i = 0; i < source.size(); i++) {
                // Clear the row (must start with empty row)
                Object[] row = new Object[NUMBER_COLUMNS];
                // Generate the row
                for (int j = 0; j < NUMBER_COLUMNS; j++) {
                    row[j] = source.get(i)[j];
                } // for j
                // Add the row to tableData
                tableData.add(row);
            } // for i
        }

        public boolean isCellEditable(int rowIndex, int columnIndex){
            //return columnIndex == 0; //Or whatever column index you want to be editable
            return false;
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
    }
    }





