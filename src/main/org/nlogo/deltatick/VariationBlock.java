package org.nlogo.deltatick;

import org.nlogo.deltatick.dnd.PrettyInput;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/16/13
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariationBlock
    extends CodeBlock
{
    String variationName = new String();
    String traitName = new String();

    public VariationBlock (String traitName, String myName) {
        this.variationName = myName;
        this.traitName = traitName;
    }


}
