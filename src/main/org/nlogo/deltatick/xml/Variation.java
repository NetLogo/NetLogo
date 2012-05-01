package org.nlogo.deltatick.xml;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/20/12
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Variation {
    String name;

    public Variation(Node ownNode) {
        name = ownNode.getAttributes().getNamedItem("name").getTextContent();

        /*
        // do we need this for variations? (Feb 20, 2012)
        NodeList info = ownNode.getChildNodes();
        for( int i = 0 ; i < info.getLength() ; i++ ) {
            if( info.item(i).getNodeName() == "setupReporter" ) {
                setupReporter = info.item(i).getTextContent();
            }

            if( info.item(i).getNodeName() == "updateReporter" ) {
                updateReporter = info.item(i).getTextContent();
            }
            */

    }
}
