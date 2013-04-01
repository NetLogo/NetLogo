package org.nlogo.deltatick.xml;

import org.jdesktop.swingx.MultiSplitLayout;
import org.nlogo.app.DeltaTickTab;
import org.nlogo.deltatick.BreedBlock;
import org.parboiled.support.Var;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;
import java.util.HashMap;

import org.w3c.dom.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/31/13
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeltaTickModelReader {
    DeltaTickTab deltaTickTab;
    FileDialog fileLoader;
    String fileName;

    public DeltaTickModelReader(Frame frame, DeltaTickTab deltaTickTab) {
        this.deltaTickTab = deltaTickTab;

        fileLoader = new FileDialog(frame);
        fileLoader.setVisible(true);
        File file = new File(fileLoader.getDirectory() + fileLoader.getFile());
        fileName = new String (fileLoader.getDirectory() + fileLoader.getFile());

        try {
            DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document model = builder.parse(file);

            NodeList usedLibraries = model.getElementsByTagName("usedLibrary");
            for (int i = 0; i < usedLibraries.getLength(); i++) {
                Node usedLibrary = usedLibraries.item(i);
                String path = new String(usedLibrary.getAttributes().getNamedItem("path").getTextContent());
                deltaTickTab.openLibrary(path);
            }

            NodeList breedBlocks = model.getElementsByTagName("breedBlock");
            for (int i = 0; i < breedBlocks.getLength(); i++) {
                Node breedBlock = breedBlocks.item(i);
                String plural = breedBlock.getAttributes().getNamedItem("plural").getTextContent();
                String number = breedBlock.getAttributes().getNamedItem("number").getTextContent();
                String maxAge = new String();
                String maxEnergy = new String();

                NodeList setupNodes = breedBlock.getChildNodes();
                for (int j = 0; j < setupNodes.getLength(); j++) {
                    if (setupNodes.item(j).getNodeName() == "ownVar") {
                        Node ownVar = setupNodes.item(j);
                        if (ownVar.getAttributes().getNamedItem("name").getTextContent().equals("age")) {
                            maxAge = ownVar.getAttributes().getNamedItem("maxReporter").getTextContent();
                        }
                        if (ownVar.getAttributes().getNamedItem("name").getTextContent().equals("energy")) {
                            maxEnergy = ownVar.getAttributes().getNamedItem("maxReporter").getTextContent();
                        }
                    }
                    BreedBlock bBlock = deltaTickTab.makeBreedBlock(plural, number);
                    bBlock.setMaxAge(maxAge);
                    bBlock.setMaxEnergy(maxEnergy);

                    if (setupNodes.item(j).getNodeName() == "trait") {
                        Node trait = setupNodes.item(j);
                        String traitName = new String(trait.getAttributes().getNamedItem("name").getTextContent()); //traitname
                        NodeList variationNodes = trait.getChildNodes();
                        HashMap<String, String> selectedVariationsPercent = new HashMap<String, String>();

                        for (Trait newTrait : deltaTickTab.getBuildPanel().getBgInfo().getTraits()) {
                            if (traitName.equalsIgnoreCase(newTrait.getNameTrait())) {
                                for (int k = 0; k < variationNodes.getLength(); k++) {
                                    if (variationNodes.item(k).getNodeName() == "variation") {
                                        Node variationNode = variationNodes.item(k);
                                        String varName = variationNode.getAttributes().getNamedItem("name").getTextContent();
                                        String varValue = variationNode.getAttributes().getNamedItem("value").getTextContent();
                                        String percentage = variationNode.getAttributes().getNamedItem("percent").getTextContent();
                                        int percent = Integer.parseInt(percentage);
                                        Variation variation = new Variation(traitName, varName, varValue, percent);
                                        //selectedVariationsPercent
                                    }
                                }
                                //TraitState traitState = (newTrait, selectedVariationsPercentHashMap)
                            }
                        }


                        //HashMap<String, TraitState> for TraitPreview to set (March 31, 2013)
                        //TODO: traitBlock setparent using breedBlock (march 31, 2013)
                        // TODO: SpeciesInspectorPanel traitState update with this data (March 31, 2013)


                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
