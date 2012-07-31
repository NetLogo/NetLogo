package org.nlogo.deltatick.xml;

import org.parboiled.support.Var;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/20/12
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class Trait {
    String nameTrait;
    String setupCommands;
    LinkedList<Variation> variationsList = new LinkedList<Variation>();

    //public Trait ()



    public Trait(Node traitNode) {
        nameTrait = traitNode.getAttributes().getNamedItem("name").getTextContent();

        NodeList traitNodes = traitNode.getChildNodes();
        for (int i = 0; i < traitNodes.getLength(); i++) {
            if (traitNodes.item(i).getNodeName() == "variation") {
                NodeList variationNodes = traitNodes.item(i).getChildNodes();
                for (int j = 0; j < variationNodes.getLength(); j++) {
                    variationsList.add(new Variation(variationNodes.item(j)));
                    System.out.println(variationsList);

                }
                if( traitNodes.item(i).getNodeName() == "variation" ) {
                    System.out.println( "from trait.java " + traitNodes.item(i).getNodeName() == "variation" );
                    variationsList.add(new Variation(traitNodes.item(i)));
                }
                }
                }
    }









    /*
    public LinkedList<Variation> getVariationsList() {
        return variationsList;
    }
    */

    public String nameTrait() {
        return nameTrait;

    }
}
