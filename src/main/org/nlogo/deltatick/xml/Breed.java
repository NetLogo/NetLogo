package org.nlogo.deltatick.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
        // The NodeList interface provides the abstraction of an
        // ordered collection of nodes, without defining or constraining how
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

    /*
    public String breedVars() {
        String code = "";
        if( ownVars.size() > 0 ){
            code += plural + "-own [\n";
            for( OwnVar var : ownVars ) {
                code += "  " + var.name + "\n";
            }
            code += "]\n";
        }
        return code;
    }
    */

    public LinkedList<OwnVar> getOwnVars() {
        return ownVars;
    }

    /*
    public String setup() {
        String code = "";
        if( needsSetupBlock() ) {
            code += "create-" + plural + " " + startQuant + " [\n";
            if( setupCommands != null ) { code += setupCommands; }
            for( OwnVar var : ownVars ) {
                if( var.setupReporter != null ) {
                    code += "set " + var.name + " " + var.setupReporter + "\n";
                }
            }
            code += "]\n";
        }

        return code;
    }
    */

    /*
    public String update() {
        String code = "";
        if( needsUpdateBlock() ) {
            code += "ask " + plural + " [\n";
            if( updateCommands != null ) { code += updateCommands; }
            for( OwnVar var : ownVars ) {
                if( var.updateReporter != null ) {
                    code += "set " + var.name + " " + var.updateReporter + "\n";
                }
            }
            code += "]\n";
        }

        return code;
    }
    */

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
