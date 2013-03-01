package org.nlogo.deltatick;

import org.jdesktop.swingx.MultiSplitLayout;
import org.nlogo.deltatick.reps.Piechart1;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;
import sun.plugin.dom.css.Rect;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;

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
    //Buttons & text
        private javax.swing.JButton cancel;
        private javax.swing.JButton add;
        private JLabel traitText;
        private JLabel variationText;
        private JLabel variationValueText;
        JPanel traitDistriPanel;
        TraitDisplay traitDisplay;
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
        Piechart1 piechart;

        ArrayList<Trait> traitsList = new ArrayList<Trait>();
        ArrayList<String> selectedVariations;
        //HashMap<Trait, Variation> selectedTraitVariations = new HashMap<Trait, Variation>(); //??
        HashMap<String, String> selectedTraitVariations = new HashMap<String, String>();
        HashMap<String, String> selectedTraitValues = new HashMap<String, String>();
        public static final int NUMBER_COLUMNS = 3;
        boolean showPie = false;


    public TraitPreview(String breed, TraitDisplay traitDisplay) {
        this.breed = breed;
        this.traitDisplay = traitDisplay;
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
                        for (Map.Entry<String, Variation> entry : trait.getVariationHashMap().entrySet()) {
                            String key = entry.getKey();
                            Variation var = entry.getValue();
                            Object[] row = new Object[NUMBER_COLUMNS];

                            row[0] = new String(key);
                            row[1] = new String(var.value);
                            row[2] = new Boolean(false);
                            //String s = String.valueOf(var.number);
                            //row[3] = new String(s + "%");   // % of variations
                            tempTableData.add(row);
                        } // for map
                    } // trait match
                } // for trait

                    // make table model & send data to tablemodel
                TraitTableModel traitTableModel = new TraitTableModel();
                traitTableModel.setTraitData(tempTableData);
                traitInfoTable.setModel(traitTableModel);

                traitDistriPanel.remove(traitDistribution);
                validate();
                traitDistribution = new TraitDistribution();
                /// Not sure if it is necessary to add listener here because traitDistribution is blank (and invisible) at this point
                traitDistribution.addMouseMotionListener(new traitDistriMouseMotionListener());
                traitDistriPanel.add(traitDistribution);

                // UPDATE/RESET CHART
                traitDisplay.piechart.updatePieChart(traitDistribution.getSelectedVariationsPercent(), selectedTraitName);
                //traitDisplay.add(piechart.getChartPanel());
                traitDisplay.revalidate();




                traitTableModel.addTableModelListener(new TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent e) {
                        //int row = e.getFirstRow();
                        int column = e.getColumn();
                        TableModel model = (TableModel)e.getSource();
                        String columnName = model.getColumnName(column);

                        selectedVariations = new ArrayList<String>();
                        for (int row = 0; row < model.getRowCount(); row++) {
                            if ((Boolean) model.getValueAt(row, 2) == true) {
                                selectedVariations.add((String) model.getValueAt(row, 0));
                                selectedTraitVariations.put(selectedTraitName, model.getValueAt(row, 0).toString());
                                selectedTraitValues.put(selectedTraitName, model.getValueAt(row, 1).toString());
                            }
                        } // for


                        traitDistriPanel.remove(traitDistribution);
                        validate();
                        traitDistribution = new TraitDistribution(breed, selectedTraitName, selectedVariations);
                        traitDistribution.addMouseMotionListener(new traitDistriMouseMotionListener());
                        traitDistriPanel.add(traitDistribution);
                        validate(); // this is important because it updates the jpanel -Aditi (feb 23, 2013)

                        /// READ PERCENTAGES
                        traitDistribution.revalidate();
                        if (traitDistribution.getMultiSplitLayout().getModel().getParent() != null) {
                        MultiSplitLayout.Split split = (MultiSplitLayout.Split) traitDistribution.getMultiSplitLayout().getModel().getParent().getChildren().get(0);

                        for (MultiSplitLayout.Node node : split.getChildren()) {
                            if (node instanceof MultiSplitLayout.Leaf) {
                                if (((MultiSplitLayout.Leaf) node).getName() != "dummy") {  //why is it entering dummy?
                                    float totalDivider;
                                    if (selectedVariations.size() > 1) {
                                        totalDivider = (selectedVariations.size() - 1);
                                    }
                                    else {
                                        totalDivider = 0;
                                    }
                                    Rectangle rect = node.getBounds();
                                    float width = rect.width;
                                    float percentage = (width/ (350 - totalDivider)) * 100;
                                    BigDecimal per = new BigDecimal(percentage);
                                    BigDecimal p = per.setScale(3, BigDecimal.ROUND_HALF_EVEN);
                                    String perc = p.toString();
                                    traitDistribution.savePercentages(((MultiSplitLayout.Leaf) node).getName(), perc);
                                    //System.out.println("ln 178 " + p + " " + percentage + " ");
                                }
                            }
                        }
                    }

                        //piechart = new Piechart1(traitDistribution.getSelectedVariationsPercent(), selectedTrait);
                        traitDisplay.piechart.updatePieChart(traitDistribution.getSelectedVariationsPercent(), selectedTraitName);
                        //traitDisplay.add(piechart.getChartPanel());
                        traitDisplay.revalidate();
                }
                        /// READ PERCENTAGES
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

    // MOUSE LISTENER TO DETECT CHANGES TO traitDistribution
    class traitDistriMouseMotionListener implements MouseMotionListener {

        // Implement MouseMotionListener interfaces
        // mouseDragged: Triggered when mouse is clicked-and-dragged on the object
        public void mouseDragged(MouseEvent e) {

            // Calculate percentages
            traitDistribution.revalidate();
            if (traitDistribution.getMultiSplitLayout().getModel().getParent() != null) {
                MultiSplitLayout.Split split = (MultiSplitLayout.Split) traitDistribution.getMultiSplitLayout().getModel().getParent().getChildren().get(0);

                for (MultiSplitLayout.Node node : split.getChildren()) {
                    if (node instanceof MultiSplitLayout.Leaf) {
                        if (((MultiSplitLayout.Leaf) node).getName() != "dummy") {  //why is it entering dummy?
                            float totalDivider;
                            if (traitDistribution.getVariations().size() > 1) {
                                totalDivider = (traitDistribution.getVariations().size() - 1);
                            }
                            else {
                                totalDivider = 0;
                            }
                            Rectangle rect = node.getBounds();
                            float width = rect.width;
                            float percentage = (width/ (350 - totalDivider)) * 100;
                            BigDecimal per = new BigDecimal(percentage);
                            BigDecimal p = per.setScale(3, BigDecimal.ROUND_HALF_EVEN);
                            String perc = p.toString();
                            traitDistribution.savePercentages(((MultiSplitLayout.Leaf) node).getName(), perc);
                            //System.out.println("ln 178 " + p + " " + percentage + " ");
                        }
                    }
                }
                traitDisplay.piechart.updatePieChart(traitDistribution.getSelectedVariationsPercent(), selectedTraitName);
                traitDisplay.revalidate();
            }
        } // mouseReleased

        // Other interfaces that must be implemented. Empty implementation is okay.
        public void mouseMoved(MouseEvent e){}
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
//        Trait trait = new Trait();
//        for (Trait t : traitsList) {
//            if (trait.getNameTrait().equalsIgnoreCase(getSelectedTraitName())) {
//                trait = trait;
//            }
//        }

        return selectedTrait;
    }

    public ArrayList<String> getSelectedVariations() {
        return selectedVariations;
    }


    public void initComponents() {
        traitText = new JLabel("Pick a trait");
        traitText.setPreferredSize(new Dimension(20, 100));
        add = new JButton("Add");

        jScrollPane1 = new JScrollPane();
        jScrollPane1.setPreferredSize(new Dimension(150, 75));
        cancel = new JButton("Cancel");

        traitInfoTable = new JTable(new TraitTableModel());
        traitDistriPanel = new JPanel();
        traitDistriPanel.setLayout(new BoxLayout(traitDistriPanel, BoxLayout.X_AXIS));
        traitDistriPanel.setSize(new Dimension(350, 30));
        TitledBorder titleMidPanel;
        titleMidPanel = BorderFactory.createTitledBorder("Variations in " + breed);
        traitDistriPanel.setBorder(titleMidPanel);

        traitDistribution = new TraitDistribution();
        traitDistribution.addMouseMotionListener(new traitDistriMouseMotionListener());
        traitDistriPanel.add(traitDistribution);


//        traitDisplayPanel = new JPanel();
        //traitDisplay.setLayout(new BoxLayout(traitDisplay, BoxLayout.X_AXIS));
        //traitDisplay.setSize(new Dimension(100, 100));
        //TitledBorder displayBorder = BorderFactory.createTitledBorder("Display traits ");


        traitInfoTable.setPreferredScrollableViewportSize(new Dimension(200, 100));
        traitInfoTable.setPreferredSize(new Dimension(200, 250));
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
                        .add(layout.createSequentialGroup()
                                .add(add)
                                .add(cancel)
                        )
                //.add(traitDisplay)
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
                                // .add(traitDisplay)
                        )
                                //.add(traitDistribution)
                        .add(traitDistriPanel)
                        .add(layout.createParallelGroup()
                                .add(cancel)
                                .add(add)
                        )

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
            headerWidth = 100;
            cellWidth = 100;
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

    public void sendTraitBlockData() {
        for (Trait trait : traitsList) {
            if (trait.equals(selectedTrait)) {
                for (String t : trait.getVariationHashMap().keySet()) {


                }
            }
        }
    }
}







