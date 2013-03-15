package org.nlogo.deltatick.dnd;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/14/13
 * Time: 8:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrettierInput extends PrettyInput {
    public PrettierInput(Component parent) {
        super(parent);

        setBorder(BorderFactory.createLoweredBevelBorder());
        //setBackground(Color.white);
        //setForeground(Color.white);
    }
}
