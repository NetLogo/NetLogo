package org.nlogo.deltatick;

import org.jdesktop.swingx.MultiSplitLayout;
import org.jdesktop.swingx.MultiSplitPane;
import org.nlogo.deltatick.xml.Variation;
import org.nlogo.workspace.ModelsLibrary;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/18/13
 * Time: 9:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitDistribution
        extends MultiSplitPane {

    MultiSplitLayout.Node modelRoot;
    String breed;
    String trait;
    ArrayList<String> selectedVariations = new ArrayList<String>();
    HashMap<String, String> selectedVariationsPercent = new HashMap<String, String>();

    public TraitDistribution() {
        this.breed = "filler";
        this.trait = "filler_trait";
        this.selectedVariations.toString();//??
        //this.selectedVariations.add("All " + breed);
        //this.selectedVariations.add("No variations selected");

        //initComponents(breed, trait, selectedVariations);
    }

    public TraitDistribution(String breed, String trait, ArrayList<String> selectedVariations) {
        this.breed = breed;
        this.trait = trait;
        this.selectedVariations = selectedVariations;
        initComponents(breed, trait, selectedVariations);

//    List children =
//                    Arrays.asList(new MultiSplitLayout.Leaf("left"), new MultiSplitLayout.Divider(), new MultiSplitLayout.Leaf("right"));
//            //MultiSplitLayout.Split modelRoot = new MultiSplitLayout.Split();
//            modelRoot.setChildren(children);//
//            multiSplitPane.add(new JLabel("Left Column"), "left");//
//            multiSplitPane.add(new JLabel("Right Column"), "right");
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
        this.setMinimumSize(new Dimension(350, 30));    //to fix size of trait distribution
        this.setMaximumSize(new Dimension(350, 30));

        this.breed = breed;
        this.trait = trait;
        this.selectedVariations = selectedVariations;

        String s = new String();
        String layout = new String();
        layout = "(ROW ";
        double weights = (double ) 1.0 / selectedVariations.size();
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
                //leaf.setWeight(1.0);
                if (addDummy) {
                    JButton leaf = new JButton("all " + breed + " have " + variation + " " + trait);
                    this.add(leaf, variation);
                    JButton dummy = new JButton("dummy");
                    //leaf.setPreferredSize(this.getMaximumSize());
                    dummy.setPreferredSize(new Dimension(0, 0));
                    //dummy = new MultiSplitLayout.Leaf("dummy");
                    //dummy.setWeight(0);
                    List children = Arrays.asList(leaf, new MultiSplitLayout.Divider(), dummy);
                }
                else {
                    JButton leaf = new JButton(variation + " " + breed);
                    this.add(leaf, variation);
                }
            }
            this.getMultiSplitLayout().setDividerSize(2);
            this.setVisible(true);
            this.revalidate();

            MultiSplitLayout.Split split = (MultiSplitLayout.Split) this.getMultiSplitLayout().getModel().getParent().getChildren().get(0);
            for (MultiSplitLayout.Node node : split.getChildren()){
                if (node instanceof MultiSplitLayout.Leaf) {
                    Rectangle rect = node.getBounds();
                }
            }
            //calculatePercentage();
        }
        else {
            this.setVisible(false);
        }
    }

    //not used
    public void calculatePercentage(Rectangle rect, List<MultiSplitLayout.Node> nodeList) {
        //MultiSplitLayout.Split split = (MultiSplitLayout.Split) this.getMultiSplitLayout().getModel().getParent().getChildren().get(0);
        for (MultiSplitLayout.Node node : nodeList){
            if (node instanceof MultiSplitLayout.Leaf) {
                //Rectangle rect = node.getBounds();
                int width = rect.width;
                int totalDivider = 1;
                if (selectedVariations.size() == 1) {
                    totalDivider = 2;
                    int percentage = (width/ (350 - totalDivider)) * 100;
                }
                else {
                    totalDivider = (selectedVariations.size() - 1) * 2;
                    int percentage = (width/ (350 - totalDivider));
                }
            }
        }
    }

    public void savePercentages(String variation, String percentage) {
        selectedVariationsPercent.put(variation, percentage);
    }

    public HashMap<String, String> getSelectedVariationsPercent() {
        return selectedVariationsPercent;
    }
}
