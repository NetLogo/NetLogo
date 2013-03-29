package org.nlogo.deltatick.xml;

import org.nlogo.deltatick.BehaviorBlock;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/26/13
 * Time: 12:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class Behavior {
    BehaviorBlock block;

    public Behavior (Node beh) {
        block = new BehaviorBlock(beh.getAttributes().getNamedItem("name").getTextContent());
        //seekAndAttachInfo(beh);

    }

    public void seekAndAttachInfo () {

    }
}
