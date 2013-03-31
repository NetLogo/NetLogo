package org.nlogo.deltatick.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: May 12, 2010
 * Time: 2:18:22 PM
 * To change this template use File | Settings | File Templates.
 */


public class Breed {
    ArrayList<OwnVar> vars = new ArrayList<OwnVar>();
    String startQuant = "100";
    String plural;
    String singular;
    String setupCommands;
    String updateCommands;
    int id;
    LinkedList<OwnVar> ownVars = new LinkedList<OwnVar>();
    LinkedList<String> ownVarNames = new LinkedList<String>();
    ArrayList<Trait> traits = new ArrayList<Trait>();

    public Breed(Node breedNode) {
        // breed attributes
        // breed variables that take information from breednode which is the Node parameter passed with Breed in
        // when breeds are added in ModelBackgroundInfo -A. (Aug 25)
        //taking these from XML file -A. (sept 13)
        // in DTD/XML file, attributes within <> are got here -A. (sept 13)
        singular = breedNode.getAttributes().getNamedItem("singular").getTextContent();
        plural = breedNode.getAttributes().getNamedItem("plural").getTextContent();
        startQuant = breedNode.getAttributes().getNamedItem("setupNumber").getTextContent();
        this.id = id;

        // breed info: setup code, update code, own variables
        // The NodeList interface provides the abstraction of an ordered collection of nodes, without defining or constraining how
        // this collection is implemented   -A. (aug 25)

        // in DTD/XML file, Elements Breed are childNodes of breedNode -A. (sept 13)
        NodeList setupNodes = breedNode.getChildNodes();
        for (int i = 0; i < setupNodes.getLength(); i++) {
            if (setupNodes.item(i).getNodeName() == "ownVar") {
                ownVars.add(new OwnVar(setupNodes.item(i)));
            }

            if (setupNodes.item(i).getNodeName() == "setupCode") {
                setupCommands = setupNodes.item(i).getTextContent();
            }

            if (setupNodes.item(i).getNodeName() == "updateCode") {
                updateCommands = setupNodes.item(i).getTextContent();
            }
        }
    }

    public void setTraitsArrayList(ArrayList <Trait> traits) {
        this.traits = traits;
    }

    public ArrayList<Trait> getTraitsArrayList() {
        return traits;
    }

    public String[] getVariationTypes(String traitName) {
        String [] variations = null;
        for (Trait trait : traits) {
            if (trait.getNameTrait().equals(traitName)) {
                variations = new String[trait.getVariationsList().size()];
                trait.getVariationsList().toArray(variations);
            }
        }
        return variations;
    }

    /*
    public String declareBreed() {
        return "breed [ " + plural + " " + singular + " ]\n";
    }
    */

    public String plural() {
        return plural;
    }

    public String singular() {
        return singular;
    }

    public LinkedList<OwnVar> getOwnVars() {
        return ownVars;
    }

    public LinkedList<String> getOwnVarsName() {
        for (OwnVar ownVar : ownVars) {
            ownVarNames.add(ownVar.name);
        }
        return ownVarNames;
    }

    public String getOwnVarMaxReporter (String ownVarName) {
        String s = new String();
        for (OwnVar ownVar : ownVars) {
            if (ownVar.name.equalsIgnoreCase(ownVarName)) {
                s = ownVar.maxReporter;
            }
        }
        return s;
    }

    public void setOwnVarMaxReporter (String ownVarName, String maxReporter) {
        for (OwnVar ownVar : ownVars) {
            if (ownVar.name.equalsIgnoreCase(ownVarName)) {
                ownVar.maxReporter = maxReporter;
            }
        }
    }

    //goes through OwnVar (linked list) to see if it needs an update block -A. (sept 13)
    public boolean needsUpdateBlock() {
        boolean needs = false;
        for (OwnVar var : ownVars) {
            if (var.updateReporter != null) {
                needs = true;
            }
        }
        if (updateCommands != null) {
            needs = true;
        }
        return needs;
    }

    //goes through OwnVar (linked list) to see if it needs a setUp block -A. (sept 13)
    public boolean needsSetupBlock() {
        boolean needs = false;
        for (OwnVar var : ownVars) {
            if (var.setupReporter != null) {
                needs = true;
            }
        }
        if (setupCommands != null) {
            needs = true;
        }
        return needs;
    }

    public String getSetupCommands() {
        return setupCommands;
    }

    public String getUpdateCommands() {
        return updateCommands;
    }

    public String getStartQuant() {
        return startQuant;
    }
}
