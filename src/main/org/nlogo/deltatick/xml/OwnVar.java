package org.nlogo.deltatick.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: May 12, 2010
 * Time: 10:54:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class OwnVar {
    public String name;
    public String setupReporter;
    public String updateReporter;


    OwnVar(Node ownVarNode) {
        name = ownVarNode.getAttributes().getNamedItem("name").getTextContent();

        NodeList info = ownVarNode.getChildNodes();
        for (int i = 0; i < info.getLength(); i++) {
            if (info.item(i).getNodeName() == "setupReporter") {
                setupReporter = info.item(i).getTextContent();
            }

            if (info.item(i).getNodeName() == "updateReporter") {
                updateReporter = info.item(i).getTextContent();
            }
        }
    }
}