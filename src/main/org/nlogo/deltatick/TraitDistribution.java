package org.nlogo.deltatick;

import org.nlogo.swingx.MultiSplitLayout;
import org.nlogo.swingx.MultiSplitPane;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/18/13
 * Time: 9:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitDistribution
        extends MultiSplitPane {

    final int traitDistributionWidth = 350;
    MultiSplitLayout.Node modelRoot;
    String breed;
    String trait;
    ArrayList<String> selectedVariations = new ArrayList<String>();
    HashMap<String, String> selectedVariationsPercent = new HashMap<String, String>();
    MultiSplitLayout.Divider dDiv;

    public TraitDistribution() {
        this.breed = "filler";
        this.trait = "filler_trait";
        this.selectedVariations.toString();
        //this.selectedVariations.add("All " + breed);
        //this.selectedVariations.add("No variations selected");

        //initComponents(breed, trait, selectedVariations);
        this.setPreferredSize(new Dimension(350, 30));
        this.validate();

    }

    public TraitDistribution(String breed, String trait, ArrayList<String> selectedVariations) {
        this.breed = breed;
        this.trait = trait;
        this.selectedVariations = selectedVariations;
        initComponents(breed, trait, selectedVariations);
    }

    public TraitDistribution(String breed, String trait, ArrayList<String> selectedVariations, HashMap<String, String>selectedVariationsPercent) {
        this.breed = breed;
        this.trait = trait;
        this.selectedVariations = selectedVariations;
        this.selectedVariationsPercent.putAll(selectedVariationsPercent);
        initComponents(breed, trait, selectedVariations);
    }


    public void setBreed(String breed) {
        this.breed = breed;
    }

    public void setTrait(String trait) {
        this.trait = trait;
    }

    public void setVariations (ArrayList<String> selectedVariations) {
        this.selectedVariations = selectedVariations;
    }

    public ArrayList<String> getVariations() {
        return selectedVariations;
    }

    public void initComponents(String breed, String trait, ArrayList<String> selectedVariations) {
        boolean addDummy = (selectedVariations.size() == 1);
        this.setMinimumSize(new Dimension(traitDistributionWidth, 30));    //to fix size of trait distribution
        this.setMaximumSize(new Dimension(traitDistributionWidth, 30));

        this.breed = breed;
        this.trait = trait;
        this.selectedVariations = selectedVariations;

        String s = new String();
        String layout = new String();
        layout = "(ROW ";
        //double weights = (double ) 1.0 / selectedVariations.size();
        double weights = 1.0 / selectedVariations.size();
        double totalWeight = 0.0;

        String totalWeightStr = "";
        String weightsStr = "";

        for (String variation : selectedVariations) {
            s = s.concat(variation + " ");

            BigDecimal bd = new BigDecimal(totalWeight);
            BigDecimal rd = bd.setScale(3, BigDecimal.ROUND_HALF_EVEN);
            totalWeight = rd.doubleValue();
            //totalWeightStr = Float.toString(totalWeight);
            //weightsStr = Float.toString(weights);

            if (selectedVariationsPercent.size() == selectedVariations.size()) {
                weights = Double.parseDouble(selectedVariationsPercent.get(variation)) / 100.0;
            }


            if (totalWeight + weights > 1.0) {
                weights = 1.0 - totalWeight;
                System.out.println("error");
            }
            layout = layout + "(LEAF name="+variation+ " weight="+weights+ ") ";
            totalWeight = totalWeight + weights;
        } // for

        if (addDummy) {
            //layout = "(ROW weight = 1.0 " + s + " dummy)";
            layout = "(ROW (LEAF name="+s+" weight=1.0) (LEAF name=dummy weight=0.0))";
        }
        else {
            layout = layout + ")";
        }

        if (selectedVariations.size() > 0) {
            modelRoot = MultiSplitLayout.parseModel(layout);
            this.getMultiSplitLayout().setModel(modelRoot);

            for (String variation : selectedVariations) {
                if (addDummy) {
                    JLabel leaf = new JLabel("all " + breed + " have " + variation + " " + trait);//JButton("all " + breed + " have " + variation + " " + trait);
                    leaf.setHorizontalAlignment(SwingConstants.CENTER);
                    //leaf.setEnabled(false);
                    //Slider leaf = new Slider("all " + breed + " have " + variation + " " + trait);
                    this.add(leaf, variation);
                    //JButton dummy = new JButton("dummy");
                    JLabel dummy = new JLabel("dummy");
                    //leaf.setPreferredSize(this.getMaximumSize());
                    dummy.setPreferredSize(new Dimension(0, 0));

                    ////dDiv = new MultiSplitLayout.Divider();
                    ////List children = Arrays.asList(leaf, dDiv, dummy);

                    ///List children = Arrays.asList(leaf, new MultiSplitLayout.Divider(), dummy);

                    // For dummy, make divider zero-size
                    this.getMultiSplitLayout().setDividerSize(0);

                }
                else {
                    //JButton leaf = new JButton(variation + " " + breed);
                    JLabel leaf = new JLabel(variation + " " + breed);
                    leaf.setHorizontalAlignment(SwingConstants.CENTER);
                    leaf.setPreferredSize(new Dimension(5,5));
                    //leaf.setMargin(new Insets(0,0,0,0));
                    leaf.setBounds(0, 0, 0, 0);
                    this.add(leaf, variation);

                    // More than one variation
                    ////this.getMultiSplitLayout().setDividerSize(2);
                }
            }




            MultiSplitLayout.Split split = (MultiSplitLayout.Split) this.getMultiSplitLayout().getModel().getParent().getChildren().get(0);
            for (MultiSplitLayout.Node node : split.getChildren()){
                if (node instanceof MultiSplitLayout.Leaf) {
                    Rectangle rect = node.getBounds();
                }
                else if ((addDummy == false) &&
                         (node instanceof MultiSplitLayout.Divider)) {
                    this.getDividerPainter().paint(this.getGraphics(), (MultiSplitLayout.Divider) node);


                    //System.out.println("TD 167: Setting Divider Paint");
                }
            }
            //calculatePercentage();
            this.setVisible(true);
            this.revalidate();


        }
        else {
            this.setVisible(false);
        }
    }



//    //not used anymore
//    public void calculatePercentage(Rectangle rect, List<MultiSplitLayout.Node> nodeList) {
//        //MultiSplitLayout.Split split = (MultiSplitLayout.Split) this.getMultiSplitLayout().getModel().getParent().getChildren().get(0);
//        for (MultiSplitLayout.Node node : nodeList){
//            if (node instanceof MultiSplitLayout.Leaf) {
//                //Rectangle rect = node.getBounds();
//                int width = rect.width;
//                int totalDivider = 1;
//                if (selectedVariations.size() == 1) {
//                    totalDivider = 2;
//                    int percentage = (width/ (traitDistributionWidth - totalDivider)) * 100;
//                }
//                else {
//                    totalDivider = (selectedVariations.size() - 1) * 2;
//                    int percentage = (width/ (traitDistributionWidth - totalDivider));
//                }
//            }
//        }
//    }

    // This function is called when the divider is clicked-and-dragged to change widths
    // See TraitPreview. MouseMotionListener on traitDistribution
    public void updatePercentages() {
        this.revalidate();
        if (this.getMultiSplitLayout().getModel().getParent() != null) {
            MultiSplitLayout.Split split = (MultiSplitLayout.Split) this.getMultiSplitLayout().getModel().getParent().getChildren().get(0);

            for (MultiSplitLayout.Node node : split.getChildren()) {
                if (node instanceof MultiSplitLayout.Leaf) {
                    if (((MultiSplitLayout.Leaf) node).getName() != "dummy") {  //why is it entering dummy?
                        float totalDivider;
                        if (this.getVariations().size() > 1) {
                            totalDivider = (this.getVariations().size() - 1);
                        }
                        else {
                            totalDivider = 0;
                        }
                        Rectangle rect = node.getBounds();
                        float width = rect.width;
                        double percentage = ((double) width / (double) (traitDistributionWidth - (totalDivider * this.getMultiSplitLayout().getDividerSize()))) * 100.0;
                        BigDecimal per = new BigDecimal(percentage);
                        BigDecimal p = per.setScale(3, BigDecimal.ROUND_HALF_EVEN);
                        String perc = p.toString();

                        this.savePercentages(((MultiSplitLayout.Leaf) node).getName(), perc);
                        //System.out.println("ln 178 " + p + " " + percentage + " ");
                    }
                }
            }
        }

    } // updatePercentages

    public void savePercentages(String variation, String percentage) {
        selectedVariationsPercent.put(variation, percentage);
    }

    public HashMap<String, String> getSelectedVariationsPercent() {
        return selectedVariationsPercent;
    }

    class Slider extends JButton {
        public Slider(String text) {

        }

            public void paintComponent (Graphics g) {
                    Rectangle r = getBounds();
                    int x = r.x + 20;
                    int y = r.y + 20;
                    int width = r.width - 40;
                    int height = r.height- 40;
                    g.setColor(Color.BLACK);
                    g.fillOval(x, y, width, height);
                    x += 2;
                    y += 2;
                    width -= 4;
                    height -= 4;
                    g.setColor(getBackground());
                    g.fillOval(x, y, width, height);
                    g.setColor(getForeground());
                    y += (height / 2) - 10;
                    g.drawString(getText(), x, y);
                }


        }
    }

