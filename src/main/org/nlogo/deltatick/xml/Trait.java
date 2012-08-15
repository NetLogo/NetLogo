package org.nlogo.deltatick.xml;

import org.parboiled.support.Var;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/20/12
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */

public class Trait {
    String traitName;
    String setupCode;
    String setupReporter;
    HashMap<String, String> variationsValuesList = new HashMap<String,String>();
    ArrayList<String> variationsList = new ArrayList<String>();
    HashMap<String, String> variationsNumbersList = new HashMap<String, String>();
    HashMap<String, String> valuesNumbersList = new HashMap<String, String>();
    HashMap<String, Variation> variationHashMap = new HashMap<String, Variation>();



    public Trait(Node traitNode) {
        traitName = traitNode.getAttributes().getNamedItem("name").getTextContent();

        NodeList traitNodes = traitNode.getChildNodes();
        for (int i = 0; i < traitNodes.getLength(); i++) {
            if (traitNodes.item(i).getNodeName() == "setupReporter") {
                setupReporter = traitNodes.item(i).getTextContent();
            }

            if (traitNodes.item(i).getNodeName() == "variation") {
                Node variationNode = traitNodes.item(i);
                String name = variationNode.getAttributes().getNamedItem("name").getTextContent();
                String value = variationNode.getAttributes().getNamedItem("value").getTextContent();
                String setupNumber = variationNode.getAttributes().getNamedItem("setupNumber").getTextContent();
                Variation variation = new Variation(name, value, Integer.parseInt(setupNumber));

                variationHashMap.put(name, variation);


                variationsValuesList.put(name, value);
                variationsNumbersList.put(name, setupNumber);
                valuesNumbersList.put(value, setupNumber);
                variationsList.add(name);
            }

            if (traitNodes.item(i).getNodeName() == "setupCode") {
                setupCode = traitNodes.item(i).getTextContent();
            }
        }
    }

    public String getNameTrait() {
        return traitName;
    }

    public String getSetupCode() {
        return setupCode;
    }

    public String getSetupReporter() {
        return setupReporter;
    }

    public HashMap<String, Variation> getVariationHashMap() {
        return variationHashMap;
    }

    public HashMap<String, String> getVariationsValuesList() {
        return variationsValuesList;
    }

    public HashMap<String, String> getVariationsNumbersList() {
        return variationsNumbersList;
    }

    public HashMap<String, String> getValuesNumbersList() {
        return valuesNumbersList;
    }

    public ArrayList<String> getVariationsList() {
        return variationsList;
    }
}