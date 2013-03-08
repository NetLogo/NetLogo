package org.nlogo.deltatick.xml;

import org.nlogo.app.DeltaTickTab;

import org.nlogo.deltatick.*;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import javax.xml.parsers.*;

import org.nlogo.window.AbstractPlotWidget;
import org.parboiled.support.Var;
import org.w3c.dom.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/16/12
 * Time: 6:07 PM
 * To change this template use File | Settings | File Templates.
 */

// this one is for envtBlocks

//public class LibraryReader2 {
//
//    FileDialog fileLoader;
//    DeltaTickTab deltaTickTab;
//
//    CodeBlock block;
//
//    ArrayList<Node> newVariationsList = new ArrayList<Node>();
//
//
//    public LibraryReader2(Frame frame, DeltaTickTab deltaTickTab) {
//        this.deltaTickTab = deltaTickTab;
//
//        this.deltaTickTab.clearLibrary2();
//
//        fileLoader = new FileDialog(frame);
//        fileLoader.setVisible(true);
//        File file = new File(fileLoader.getDirectory() + fileLoader.getFile());
//
//        //DocumentBuilder converts XML file into Document -A. (sept 13)
//        try {
//            DocumentBuilder builder =
//                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            Document library = builder.parse(file);
//
//            //needs to be in order as provided as parameters in populate() -A. (sept 13)
//            deltaTickTab.getBuildPanel().getBgInfo2().populate(
//                    //library.getElementsByTagName("breed"),
//                    library.getElementsByTagName("global"),
//                    library.getElementsByTagName("envt"),
//                    library.getElementsByTagName("setup"),
//                    library.getElementsByTagName("go"),
//                    library.getElementsByTagName("library")
//            );
//
//            // make the behavior blocks for each element -A. (Sept 13)
//            NodeList behaviors = library.getElementsByTagName("behavior");
//            for (int i = 0; i < behaviors.getLength(); i++) {
//                Node behavior = behaviors.item(i);
//                block = new BehaviorBlock(behavior.getAttributes().getNamedItem("name").getTextContent());
//
//                seekAndAttachInfo(behavior);
//            }
//
//            /*
//            NodeList traits = library.getElementsByTagName("trait");
//            //NodeList variations = library.getElementsByTagName("variations");
//            for (int i = 0; i < traits.getLength(); i++) {
//                Node trait = traits.item(i);
//                //for (int j = 0 ; j < variations.getLength(); j ++ ) {
//                  //  Node variation = variations.item(j);
//                //}
//                //block = new TraitBlock( trait.getAttributes().getNamedItem("name").getTextContent() );
//                //block = new TraitBlock();
//                block = new TraitBlock( trait );
//                seekAndAttachInfo( trait );
//
//                // block = new TraitBlock( trait.getAttributes().getNamedItem("name").getTextContent() );
//            }
//                */
//            NodeList variations = library.getElementsByTagName("variation");
//            for (int i = 0 ; i < variations.getLength(); i ++) {
//                Node variation = variations.item(i);
//                newVariationsList = new ArrayList<Node>();
//                newVariationsList.add(variation);
//            }
//
//            //makes patch blocks appear in library panel -A. (sept 13)
//            NodeList patches = library.getElementsByTagName("patch");
//            for (int i = 0; i < patches.getLength(); i++) {
//                Node patch = patches.item(i);
//                block = new PatchBlock(patch.getAttributes().getNamedItem("name").getTextContent());
//
//                seekAndAttachInfo(patch);
//            }
//
//            // make the conditions
//            NodeList conditions = library.getElementsByTagName("condition");
//            for (int i = 0; i < conditions.getLength(); i++) {
//                Node condition = conditions.item(i);
//                block = new ConditionBlock(condition.getAttributes().getNamedItem("name").getTextContent());
//
//                seekAndAttachInfo(condition);
//            }
//
//            // make the quantities
//            NodeList quantities = library.getElementsByTagName("quantity");
//            for (int i = 0; i < quantities.getLength(); i++) {
//                Node quantity = quantities.item(i);
//
//                boolean histo = false;
//                String bars = "0";
//                String trait = " ";
//
//                if (quantity.getAttributes().getNamedItem("histo").getTextContent().contains("true")) {
//                    histo = true;
//                    bars = quantity.getAttributes().getNamedItem("bars").getTextContent();
//                }
//                block = new QuantityBlock(quantity.getAttributes().getNamedItem("name").getTextContent(), histo, bars, trait);
//
//
//                seekAndAttachInfo(quantity);
//            }
//
//
//            NodeList globals = library.getElementsByTagName("global");
//
//            //deltaTickTab.backgroundInfo = new DeltaTickTab.ModelBackgroundInfo(  );
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public String reIntroduceLtGt(String input) {
//        String output = "";
//
//        output = input.replace("&lt;", "<");
//        output = output.replace("&gt;", ">");
//
//        return output;
//    }
//
//    // ask salil how this works - A.
//    // add patch for this to show up in code
//    public void seekAndAttachInfo(Node infoNode) {
//        NodeList behaviorInfo = infoNode.getChildNodes();
//        for (int j = 0; j < behaviorInfo.getLength(); j++) {
//            if (behaviorInfo.item(j).getNodeName() == "commands") {
//                block.setCode(
//                        reIntroduceLtGt(
//                                behaviorInfo.item(j).getTextContent()));
//
//
//            } else if (behaviorInfo.item(j).getNodeName() == "test") {
//                block.setCode(
//                        reIntroduceLtGt(
//                                behaviorInfo.item(j).getTextContent()));
//            } else if (behaviorInfo.item(j).getNodeName() == "reporter") {
//                block.setCode(
//                        reIntroduceLtGt(
//                                behaviorInfo.item(j).getTextContent()));
//            } else if (behaviorInfo.item(j).getNodeName() == "commandsPatch") {
//                block.setCode(
//                        reIntroduceLtGt(
//                                behaviorInfo.item(j).getTextContent()));
//
//            } else if (behaviorInfo.item(j).getNodeName() == "input") {
//                block.addInput(behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent(),
//                        behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());
//
//            } else if (behaviorInfo.item(j).getNodeName() == "variation") {
//                block.setCode(
//                        reIntroduceLtGt(
//                                behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent()));
//                //System.out.println( "LR@" + behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent());
//
//            }
//
//        }
//
//        block.disableInputs();
//        deltaTickTab.getLibraryPanel2().add(block);
//        //deltaTickTab.getLibraryPanel().add( block );
//        deltaTickTab.addDragSource(block);
//        // the line above is what makes the blocks drag-able (Feb 14, 2012)
//    }
//}
