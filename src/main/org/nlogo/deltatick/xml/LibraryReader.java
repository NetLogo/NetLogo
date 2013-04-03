package org.nlogo.deltatick.xml;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import javax.xml.parsers.*;

import org.nlogo.app.DeltaTickTab;
import org.nlogo.deltatick.*;
import org.w3c.dom.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 2, 2010
 * Time: 6:52:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class LibraryReader {

    //FileDialog class displays a dialog window from which the user can select a file. -A. (sept 13)
    FileDialog fileLoader;
    DeltaTickTab deltaTickTab;
    String fileName;

    CodeBlock block;

    ArrayList<Node> newVariationsList = new ArrayList<Node>();

    public LibraryReader(Frame frame, DeltaTickTab deltaTickTab, String libraryFileName) {
        this.deltaTickTab = deltaTickTab;

        // clear out any existing blocks
       // this.deltaTickTab.clearLibrary(); // TODo commented out on feb 22, 2012- will need to re-think this, might have to bring it back
        File file = null;
        if (libraryFileName == null) {
            fileLoader = new FileDialog(frame);
            fileLoader.setVisible(true);
            file = new File(fileLoader.getDirectory() + fileLoader.getFile());
            fileName = new String (fileLoader.getDirectory() + fileLoader.getFile());
        }
        else {
            fileName = new String(libraryFileName);
            file = new File(libraryFileName);
        }
        //DocumentBuilder converts XML file into Document -A. (sept 13)
        try {
            DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document library = builder.parse(file);

            //needs to be in order as provided as parameters in populate() -A. (sept 13)
            deltaTickTab.getBuildPanel().getBgInfo().populate(
                    library.getElementsByTagName("breed"),
                    library.getElementsByTagName("trait"),
                    library.getElementsByTagName("global"),
                    library.getElementsByTagName("envt"),
                    library.getElementsByTagName("setup"),
                    library.getElementsByTagName("go"),
                    library.getElementsByTagName("library"),
                    library.getElementsByTagName("draw"),
                    library.getElementsByTagName("behavior")
            );

            NodeList behaviors = library.getElementsByTagName("behavior");

            for (int i = 0; i < behaviors.getLength(); i++) {
                Node behavior = behaviors.item(i);
                boolean b = false;
                block = new BehaviorBlock(behavior.getAttributes().getNamedItem("name").getTextContent());
                if (behavior.getAttributes().getNamedItem("mutate") != null) {
                    b = behavior.getAttributes().getNamedItem("mutate").getTextContent().equalsIgnoreCase("true");
                    if (b) {
                        ((BehaviorBlock) block).setIsMutate(true);
                    }
                }
                seekAndAttachInfo(behavior);
                deltaTickTab.getLibraryHolder().addToBehaviorBlocksList((BehaviorBlock) block);
            }

            // make the conditions
            NodeList conditions = library.getElementsByTagName("condition");
            for (int i = 0; i < conditions.getLength(); i++) {
                Node condition = conditions.item(i);
                block = new ConditionBlock(condition.getAttributes().getNamedItem("name").getTextContent());

                seekAndAttachInfo(condition);
                deltaTickTab.getLibraryHolder().addToConditionBlocksList((ConditionBlock) block);
            }

            NodeList traits = library.getElementsByTagName("trait");
            for (int i = 0; i < traits.getLength(); i++) {
                Node trait = traits.item(i);
                seekAndAttachInfo( trait );
            }

            //makes patch blocks appear in library panel -A. (sept 13)
            NodeList patches = library.getElementsByTagName("patch");
            for (int i = 0; i < patches.getLength(); i++) {
                Node patch = patches.item(i);
                block = new PatchBlock(patch.getAttributes().getNamedItem("name").getTextContent());
                seekAndAttachInfo(patch);
            }



            // make the quantities
            NodeList quantities = library.getElementsByTagName("quantity");
            for (int i = 0; i < quantities.getLength(); i++) {
                Node quantity = quantities.item(i);

                boolean histo = false;
                String bars = "0";
                String trait = " ";

                if (quantity.getAttributes().getNamedItem("histo").getTextContent().contains("true")) {
                    histo = true;
                    bars = quantity.getAttributes().getNamedItem("bars").getTextContent();
                }
                block = new QuantityBlock(quantity.getAttributes().getNamedItem("name").getTextContent(), histo, bars, trait);
                seekAndAttachInfo(quantity);
                ((QuantityBlock) block).addColorButton();
            }

            NodeList breeds = library.getElementsByTagName("breed");
            NodeList globals = library.getElementsByTagName("global");

            //deltaTickTab.backgroundInfo = new DeltaTickTab.ModelBackgroundInfo(  );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String reIntroduceLtGt(String input) {
        String output = "";

        output = input.replace("&lt;", "<");
        output = output.replace("&gt;", ">");

        return output;
    }

    // ask salil how this works - A.
    // add patch for this to show up in code
    public void seekAndAttachInfo(Node infoNode) {
        NodeList behaviorInfo = infoNode.getChildNodes();
        for (int j = 0; j < behaviorInfo.getLength(); j++) {
            if (behaviorInfo.item(j).getNodeName() == "commands") {
                //NodeList childNodes = behaviorInfo.item(j).getChildNodes();
                block.setCode(
                        reIntroduceLtGt(
                                behaviorInfo.item(j).getTextContent()));

            } else if (behaviorInfo.item(j).getNodeName() == "test") {
                block.setCode(
                        reIntroduceLtGt(
                                behaviorInfo.item(j).getTextContent()));
            } else if (behaviorInfo.item(j).getNodeName() == "reporter") {
                block.setCode(
                        reIntroduceLtGt(
                                behaviorInfo.item(j).getTextContent()));
            } else if (behaviorInfo.item(j).getNodeName() == "commandsPatch") {
                block.setCode(
                        reIntroduceLtGt(
                                behaviorInfo.item(j).getTextContent()));
            } else if (behaviorInfo.item(j).getNodeName() == "input") {
                //addInput takes 2 parameters: String inputName & default value
                block.addInput(behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent(),
                        behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());
            }
            else if (behaviorInfo.item(j).getNodeName() == "if-condition") {
                block.setIfCode(
                        reIntroduceLtGt(
                                behaviorInfo.item(j).getTextContent()));

            }

            //TODO: Figure out how setCode is fine for ENEGRYINPUT or should I switch to addInput
            else if (behaviorInfo.item(j).getNodeName() == "energyInput") {
                //block.setCode( behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());
                block.addInputEnergy( behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent(),
                        behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());

            }

            else if (behaviorInfo.item(j).getNodeName() == "behaviorInput") {
                block.addBehaviorInput(behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent(),
                        behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent(),
                        behaviorInfo.item(j).getAttributes().getNamedItem("tooltip").getTextContent());
            }
            else if (behaviorInfo.item(j).getNodeName() == "distanceInput") {
                block.addDistanceInput(behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent(),
                        behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());
            }
            else if (behaviorInfo.item(j).getNodeName() == "agentInput") {
                block.addAgentInput(behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent(),
                        behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());
            }
            else if (behaviorInfo.item(j).getNodeName() == "percentInput") {
                block.addPercentInput(behaviorInfo.item(j).getAttributes().getNamedItem("name").getTextContent(),
                        behaviorInfo.item(j).getAttributes().getNamedItem("default").getTextContent());
            }
            else if (behaviorInfo.item(j).getNodeName() == "tooltip") {
                block.setToolTipText("<html><font size=\"3.5\">" + behaviorInfo.item(j).getTextContent() + "</font></html>");
            }
        }

        block.disableInputs();
        deltaTickTab.getLibraryHolder().addBlock( block );
        deltaTickTab.addDragSource(block);

        // the line above is what makes the blocks drag-able (Feb 14, 2012)
    }

    public String getFileName() {
        return fileName;
    }
}
