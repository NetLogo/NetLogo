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
import java.io.Serializable;


public class Variation implements Serializable {
    public String name;
    public String trait;
    public String value;
    public int percent;
    //public String color;

    public Variation(String trait, String name, String value, int percent) {
        this.trait = new String(trait);
        this.name = new String(name);
        this.value = new String(value);
        this.percent = percent;
    }

    //Copy constructor    (April 1, 2013)
    public Variation(Variation variation) {
        this.trait = new String(variation.trait);
        this.name = new String(variation.name);
        this.value = new String(variation.value);
        this.percent = variation.percent;
    }
}
