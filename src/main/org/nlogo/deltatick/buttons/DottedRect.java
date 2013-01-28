package org.nlogo.deltatick.buttons;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;



/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 1/27/13
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */

public class DottedRect extends JComponent {
//    public void paintComponent( Graphics og ) {
//        Graphics2D g = (Graphics2D)og;
//        Rectangle2D rect = new Rectangle2D.Float( 10, 10, getWidth() - 20, getHeight() - 20 );
//        float[] dash = { 5F, 5F };
//        Stroke dashedStroke = new BasicStroke( 2F, BasicStroke.CAP_SQUARE,
//        BasicStroke.JOIN_MITER, 3F, dash, 0F );
//        g.fill( dashedStroke.createStrokedShape( rect ) );
//        g.setColor(Color.red);
//        g.drawString("Add block here", 10, 10 );
//    }
// }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawRect (10, 10, 50, 50);
        g.setColor(Color.BLUE);



  }
}


