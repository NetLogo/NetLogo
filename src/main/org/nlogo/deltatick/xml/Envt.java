package org.nlogo.deltatick.xml;

import org.nlogo.deltatick.EnvtBlock;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.nlogo.deltatick.xml.Envt;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Created by IntelliJ IDEA.
 * User: aditi
 * Date: 8/25/11
 * Time: 1:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class Envt {
    //String plural;
    //String singular;
    String nameEnvt;
    String setupCommands;
    String setupReporter;
    String updateCommands;
    LinkedList<OwnVar> ownVars = new LinkedList<OwnVar>();
    //Collection<Envt> usedEnvts = new List<Envt>


    public Envt(Node envtNode) {
        nameEnvt = envtNode.getAttributes().getNamedItem("envtName").getTextContent();


        NodeList setupNodes = envtNode.getChildNodes();
        for (int i = 0; i < setupNodes.getLength(); i++) {
            if (setupNodes.item(i).getNodeName() == "ownVar") {
                ownVars.add(new OwnVar(setupNodes.item(i)));
            }

            if (setupNodes.item(i).getNodeName() == "setupCode") {
                setupCommands = setupNodes.item(i).getTextContent();
            }

            if (setupNodes.item(i).getNodeName() == "setupReporter") {
                setupReporter = setupNodes.item(i).getTextContent();
            }
            if (setupNodes.item(i).getNodeName() == "updateCode") {
                updateCommands = setupNodes.item(i).getTextContent();
            }
        }
    }


    public String nameEnvt() {
        //System.out.println(nameEnvt);
        return nameEnvt;
    }

    public boolean needsSetUpBlock() {
        boolean needs = false;
        if (setupCommands != null) {
            needs = true;
        }
        if (setupReporter != null) {
            needs = true;
        }
        return needs;

    }

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

    public LinkedList<OwnVar> getOwnVars() {
        return ownVars;
    }

    public String getSetupCommands() {
        return setupCommands;
    }

    public String getUpdateCommands() {
        return updateCommands;
    }

    public String getSetupReporter() {
        return setupReporter;
    }


}
