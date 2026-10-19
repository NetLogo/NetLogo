/**
 * ColorSchemesIconImage paints a horizontal legend that contains Rectangles2D
 * with a variable color and size passed to the constructor. This class is used
 * for producing a small ColorSchemesIconImage(s) contained in the
 * ColorSchemesCombox and a larger ColorSchemesIconImage displayed in the ColorSchemePanel
 */


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;


public class ColorSchemesIconImage extends ImageIcon{

    private static final long serialVersionUID = 1L;

    /**
     *
     * @param legendRGB An 2D array with triplets of RGB colors
     * @param pixels    The size of the sides of the square that create the legend.
     */

    public ColorSchemesIconImage (int[][] legendRGB, int pixels)
    {
        super();
        BufferedImage image;
        BasicStroke stroke = new BasicStroke(1.0f);
        int x = 0;
        int y = 0;
        int rectHeight;
        int rectWidth;
        rectHeight =  pixels;
        rectWidth =   pixels;
        int legendWidth = legendRGB.length * rectWidth;

        image = new BufferedImage(legendWidth, rectHeight,
                                  BufferedImage.TYPE_INT_RGB);
        setImage(image);
        Graphics g = getImage().getGraphics();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Note that the squares overlap in order to have a one pixel border between
        // each swatch
        for (int i=0; i < legendRGB.length; i++)
        {
            g2.setStroke(stroke);
            g2.setPaint(Color.red);
            g2.setPaint(new Color(legendRGB[i][0], legendRGB[i][1], legendRGB[i][2]));
            g2.fill(new Rectangle2D.Double(x, y, rectWidth, rectHeight));
            g2.setPaint(Color.black);
            g2.draw(new Rectangle2D.Double(x, y, rectWidth, rectHeight - 1));

            x += rectWidth;
        }
        //last vertical line
        g2.draw(new Rectangle2D.Double(x - 1, y, 1, rectHeight - 1));
    }
}
