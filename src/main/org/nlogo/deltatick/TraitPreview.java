package org.nlogo.deltatick;

import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/23/13
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitPreview extends JPanel {

    private JFrame myFrame;

    //Buttons & text
    private javax.swing.JButton cancel;
    private javax.swing.JButton add;
    private JLabel traitText;
    private JLabel variationText;
    private JLabel variationValueText;
    JPanel traitDistriPanel;
    TraitDisplay traitDisplay;
    LabelPanel labelPanel;
    boolean isTraitSelected;
    String breed;

    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList myTraitsList;
    private String selectedTraitName;
    private Trait selectedTrait;
    private JList myVariationsList;
    HashMap<String, TraitBlock> breedTraitHashMap = new HashMap<String, TraitBlock>();
    //to store breed and corresponding trait
    ListSelectionModel listSelectionModel;
    JTable traitInfoTable;
    TraitDistribution traitDistribution;


    ArrayList<Trait> traitsList = new ArrayList<Trait>();
    ArrayList<String> selectedVariations;
    //HashMap<Trait, Variation> selectedTraitVariations = new HashMap<Trait, Variation>(); //??
    HashMap<String, String> selectedTraitVariations = new HashMap<String, String>();
    HashMap<String, String> selectedTraitValues = new HashMap<String, String>();
    public static final int NUMBER_COLUMNS = 3;

    // Holds final selected traits (and variations) as selected by the user
    // This should be used to instantiate the trait block
    HashMap<String, TraitState> selectedTraitsMap = new HashMap<String, TraitState>();
    //ArrayList<Trait> selectedTraitsList = new ArrayList<Trait>();


    public TraitPreview(String breed, TraitDisplay traitDisplay, LabelPanel labelPanel, JFrame myFrame) {
        this.myFrame = myFrame;
        this.breed = breed;
        this.traitDisplay = traitDisplay;
        this.labelPanel = labelPanel;
        traitDisplay.setBackground(Color.BLACK);
        traitDisplay.revalidate();
        initComponents();
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
        //myTraitsList.setSelectedIndex(0);
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(new TraitListSelectionHandler());
        this.setVisible(true);
    }


    class TraitListSelectionHandler implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            myVariationsList = new JList();
            if (lsm.isSelectionEmpty()) {
                System.out.println("No trait selected");
            }
            else {  // gen data[][] based on selected trait
                ArrayList<Object[]> tempTableData = new ArrayList<Object[]>();
                for (Trait trait : traitsList) {
                    if (trait.getNameTrait().equalsIgnoreCase(getSelectedTraitName())) {
                        selectedTrait = trait;
                        for (Map.Entry<String, Variation> entry : trait.getVariationHashMap().entrySet()) {
                            String key = entry.getKey();
                            Variation var = entry.getValue();
                            Object[] row = new Object[NUMBER_COLUMNS];

                            row[0] = new String(key);
                            row[1] = new String(var.value);

                            boolean varSelected = false;
                            if (selectedTraitsMap.containsKey(getSelectedTraitName())) {
                                //check if the variationhashmap in trait state has the variation selected
                                varSelected = selectedTraitsMap.get(getSelectedTraitName()).getVariationHashMap().containsKey(key);
                            }
                            row[2] = new Boolean(varSelected);
                            tempTableData.add(row);
                        } // for map
                    } // trait match
                } // for trait

                    // make table model & send data to tablemodel
                TraitTableModel traitTableModel = new TraitTableModel();
                traitTableModel.setTraitData(tempTableData);
                traitTableModel.addTableModelListener(new traitTableModelListener());
                traitInfoTable.setModel(traitTableModel);
                traitInfoTable.validate();

                // Can/Must read percentages from selectedTraitsMaps or from memory based on what was previously done
                updateTraitDistriPanel(traitInfoTable.getModel(), true);

                updatePieChart();


                final String[] variationStrings = getVariationTypes(getSelectedTraitName()) ;
                myVariationsList.setModel(new javax.swing.AbstractListModel() {
                    public int getSize() {
                        return variationStrings.length;
                    }
                    public Object getElementAt(int i) {
                        return variationStrings[i];
                    }
                });
            } // else


        } // valueChanged
    } // TraitListSelectionHandler

    class traitTableModelListener implements TableModelListener {

        public void tableChanged(TableModelEvent e) {

            TableModel model = (TableModel)e.getSource();

            // A new variation has been added/removed. Previous percentages are invalid
            // Hence no need to read percentages from selectedTraitsMap. Set 2nd parameter to false
            updateTraitDistriPanel(model, false);

            // Update chart to reflect percentages
            updatePieChart();


            // Some variation selected/unselected
            // Update the hash map
            // In TableModelListener, map can ONLY be updated after updateTraitDistriPanel()
            updateSelectedTraitsMap(model);

            updateCheckBoxes(selectedTraitsMap);


        }
    }

    // MOUSE LISTENER TO DETECT CHANGES TO traitDistribution
    class traitDistriMouseMotionListener implements MouseMotionListener {

        // Implement MouseMotionListener interfaces
        // mouseDragged: Triggered when mouse is clicked-and-dragged on the object
        public void mouseDragged(MouseEvent e) {

            // Calculate and update percentages
            traitDistribution.updatePercentages();

            updatePieChart();

            // Percentages may have changed, update selectedTraitsMap
            updateSelectedTraitsMap(traitInfoTable.getModel());

        } // mouseDragged

        // Other interfaces that must be implemented. Empty implementation is okay.
        public void mouseMoved(MouseEvent e){}

    } // traitDistriMouseMotionListener

    private void updateSelectedTraitsMap(TableModel model) {

        // If any variations are selected, add that trait+variations to selectedTraitList
        // Else (no variations selected), remove that trait if present in selectedTraitList
        boolean someVariationSelected = false;
        //HashMap<String, Variation> tmpVarHashMap = new HashMap<String, Variation>(selectedTrait.getVariationHashMap());
        HashMap<String, Variation> tmpVarHashMap = new HashMap<String, Variation>();
        for (int row = 0; row < model.getRowCount(); row++) {
            someVariationSelected = someVariationSelected || ((Boolean) model.getValueAt(row, 2));

            if ((Boolean) model.getValueAt(row, 2) == true) {
                String variationName = (String) model.getValueAt(row, 0);
                tmpVarHashMap.put(variationName, selectedTrait.getVariationHashMap().get(variationName));
            }
        } // for

        /// Update state of selectedTraitsMap
        // Remove first
        selectedTraitsMap.remove(selectedTraitName);

        if (someVariationSelected) {
            TraitState tmpTraitState = new TraitState(selectedTrait, traitDistribution.getSelectedVariationsPercent());
            tmpTraitState.getVariationHashMap().clear();
            tmpTraitState.getVariationHashMap().putAll(tmpVarHashMap);
            selectedTraitsMap.put(selectedTraitName, tmpTraitState);
        }
        /// Update complete

    } // updateSelectedTraitsMap

    // From the given table model (with the table data), this function updates the traitDistriPanel
    // Reads which variations are selected and generates a traitDistribution accordingly
    // Also updates/initialized percentages for piechart
    //readPercent: if going to a different trait, needs to read percentage from memory. if adding variation to already selectd
    // trait, then only needs to automatically set percentages, then readpercent = false

    private void updateTraitDistriPanel(TableModel model, boolean readPercent) {
        selectedVariations = new ArrayList<String>();
        for (int row = 0; row < model.getRowCount(); row++) {
            if ((Boolean) model.getValueAt(row, 2) == true) {
                selectedVariations.add((String) model.getValueAt(row, 0));
                selectedTraitVariations.put(selectedTraitName, model.getValueAt(row, 0).toString());
                selectedTraitValues.put(selectedTraitName, model.getValueAt(row, 1).toString());
            }
        }
        traitDistriPanel.remove(traitDistribution);
        validate();
        if (readPercent &&
            selectedTraitsMap.containsKey(selectedTraitName)) {
            //traitDistribution = new TraitDistribution(breed, selectedTraitName, selectedVariations);
            traitDistribution = new TraitDistribution(breed, selectedTraitName, selectedVariations, selectedTraitsMap.get(selectedTraitName).selectedVariationsPercent);
        }
        else {
            traitDistribution = new TraitDistribution(breed, selectedTraitName, selectedVariations);
        }
        traitDistribution.addMouseMotionListener(new traitDistriMouseMotionListener());
        traitDistriPanel.add(traitDistribution);
        validate(); // this is important because it updates the jpanel -Aditi (feb 23, 2013)

        traitDistribution.updatePercentages();

    } // updateTraitDistrPanel

    // Updates the pie chart
    private void updatePieChart() {
        traitDisplay.updateChart(selectedTraitName, traitDistribution.getSelectedVariationsPercent());
        traitDisplay.revalidate();
    }

    //Create and update Checkboxes for LabelPanel
//    public void makeCheckBoxes() {
//        labelPanel = new LabelPanel();
//    }

    private void updateCheckBoxes(HashMap<String, TraitState> temp) {
            labelPanel.updateData(temp);
            //labelPanel.addTraitCheckBox(string);
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


    public String getSelectedTraitName() {
        selectedTraitName = myTraitsList.getSelectedValue().toString();
        return selectedTraitName;
    }

    public Trait getSelectedTrait() {
        return selectedTrait;
    }

    public ArrayList<String> getSelectedVariations() {
        return selectedVariations;
    }

    public LabelPanel getLabelPanel() {
        return labelPanel;
    }


    public void initComponents() {
        traitText = new JLabel("Pick a trait");
        traitText.setPreferredSize(new Dimension(20, 100));
        //add = new JButton("Add");

        jScrollPane1 = new JScrollPane();
        jScrollPane1.setPreferredSize(new Dimension(150, 100));
        jScrollPane1.setMaximumSize(new Dimension(150, 100));
        //cancel = new JButton("Cancel");

        traitInfoTable = new JTable(new TraitTableModel());
        traitDistriPanel = new JPanel();
        traitDistriPanel.setLayout(new BoxLayout(traitDistriPanel, BoxLayout.Y_AXIS));
        traitDistriPanel.setPreferredSize(new Dimension(350, 30));
        traitDistriPanel.setMinimumSize(new Dimension(350,30));
        TitledBorder titleMidPanel;
        titleMidPanel = BorderFactory.createTitledBorder("Variations in " + breed);
        traitDistriPanel.setBorder(titleMidPanel);

        traitDistribution = new TraitDistribution();
        traitDistribution.addMouseMotionListener(new traitDistriMouseMotionListener());
        traitDistriPanel.add(traitDistribution);

        //labelPanel = new LabelPanel();
        //traitDistriPanel.add(labelPanel);

        traitDistriPanel.validate();


//        traitDisplayPanel = new JPanel();
        //traitDisplay.setLayout(new BoxLayout(traitDisplay, BoxLayout.X_AXIS));
        //traitDisplay.setSize(new Dimension(100, 100));
        //TitledBorder displayBorder = BorderFactory.createTitledBorder("Display traits ");


        traitInfoTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
        traitInfoTable.setPreferredSize(new Dimension(225, 100));
        traitInfoTable.validate();
        JTableHeader header = traitInfoTable.getTableHeader();
        initColumnSizes(traitInfoTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);

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
                                //.add(traitDistribution)
                        .add(traitDistriPanel)
                        //.add(labelPanel)
//                        .add(layout.createSequentialGroup()
//                                .add(add)
//                                .add(cancel)
                        //)
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
                                //.add(traitDistribution)
                        .add(traitDistriPanel)
                //.add(labelPanel)
//                        .add(layout.createParallelGroup()
//                                .add(cancel)
//                                .add(add)
//                        )

        );
        validate();
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
            headerWidth = 75;
            cellWidth = 75;
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }


    class TraitTableModel extends AbstractTableModel {
        private String[] columnNames = {"Variation name", "Value", "Add variation?"};
        //, "Edit"};

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

    public TraitDistribution getTraitDistribution() {
        return traitDistribution;
    }

    public HashMap<String, String> getSelectedTraitValues() {
        return selectedTraitValues;
    }

    public HashMap<String, String> getSelectedTraitVariations() {
        return selectedTraitVariations;
    }

    public HashMap<String, TraitState> getTraitStateMap() {
        return selectedTraitsMap;
    }

//    public void sendTraitBlockData() {
//        for (Trait trait : traitsList) {
//            if (trait.equals(selectedTrait)) {
//                for (String t : trait.getVariationHashMap().keySet()) {
//
//
//                }
//            }
//        }
//    }
}







