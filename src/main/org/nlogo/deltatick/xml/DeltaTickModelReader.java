package org.nlogo.deltatick.xml;

import org.jdesktop.swingx.MultiSplitLayout;
import org.nlogo.app.DeltaTickTab;
import org.nlogo.deltatick.BreedBlock;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;
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
//                LibraryReader libraryReader = deltaTickTab.getLibraryReader();
//                libraryReader = new LibraryReader(frame, deltaTickTab, path);
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
                }
                BreedBlock bBlock = deltaTickTab.makeBreedBlock(plural, number);
                bBlock.setMaxAge(maxAge);
                bBlock.setMaxEnergy(maxEnergy);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
