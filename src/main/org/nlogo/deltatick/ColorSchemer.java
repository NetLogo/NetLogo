package org.nlogo.deltatick;

import java.awt.Color;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 15, 2010
 * Time: 4:25:06 PM
 * To change this template use File | Settings | File Templates.
 */
public strictfp class ColorSchemer {
    static Color[] colorList1 = new Color[] {
        new Color( Color.HSBtoRGB(
                174.7826f / 360 ,
                0.5823f ,
                0.6196f ) ), // greenish
        new Color( Color.HSBtoRGB(
                308.0769f / 360 ,
                0.2826f ,
                0.7216f ) ), // pinkish
        new Color( Color.HSBtoRGB(
                48.7500f / 360 ,
                0.2612f ,
                0.9608f ) ), // yellowish
        new Color( Color.HSBtoRGB(
                0f ,
                0f ,
                .9608f ) ),
    };

    static Color[] colorList2 = new Color[] {
        new Color( Color.HSBtoRGB(
                239.0000f / 360,
                0.3922f ,
                0.6000f ) ), // purple
        new Color( Color.HSBtoRGB(
                20.1316f / 360 ,
                0.6179f ,
                0.9647f ) ), // orange
        new Color( Color.HSBtoRGB(
                104.7887f / 360 ,
                0.3989f ,
                0.6980f ) ), // green
        new Color( Color.HSBtoRGB(
                0f ,
                0f ,
                .6980f ) ),  // grey
    };

    static Color[] colorList3 = new Color[] {
        new Color( Color.HSBtoRGB(
                174.7826f / 360 ,
                0.5823f ,
                0.6196f ) ), // greenish
        new Color( Color.HSBtoRGB(
                308.0769f / 360 ,
                0.2826f ,
                0.7216f ) ), // pinkish
        new Color( Color.HSBtoRGB(
                48.7500f / 360 ,
                0.2612f ,
                0.9608f ) ),
        new Color( Color.HSBtoRGB(
                0f ,
                0f ,
                .9608f ) ),
    };

    static Color[] colorList4 = new Color[] {
        new Color( Color.HSBtoRGB(
                182.7907f / 360 ,
                0.2966f ,
                0.5686f ) ), // greenish
        new Color( Color.HSBtoRGB(
                47.6712f / 360 ,
                0.2980f ,
                0.9608f ) ), // yellowish
        new Color( Color.HSBtoRGB(
                38.2500f / 360 ,
                0.4678f ,
                0.6706f ) ),  // brownish
        new Color( Color.HSBtoRGB(
                20.1316f / 360 ,
                0.3435f ,
                0.9647f ) ),  // red
    };

    static Color[] colorList5 = new Color[] {
        new Color( Color.HSBtoRGB(
                182.7907f ,
                0.2966f ,
                0.5686f ) ), // purple
        new Color( Color.HSBtoRGB(
                47.6712f ,
                0.2980f ,
                0.9608f ) ), // blue
        new Color( Color.HSBtoRGB(
                38.2500f ,
                0.4678f ,
                0.6706f ) ),  // green
        new Color( Color.HSBtoRGB(
                60.0000f / 360 ,
                0.2911f ,
                0.8435f ) )
    };// yellow

    static Color[] colorList6 = new Color[] {
        new Color( Color.HSBtoRGB(
                280f / 360 ,
                0.4367f ,
                0.6196f ) ),  // purple
        new Color( Color.HSBtoRGB(
                138f / 360 ,
                0.4571f ,
                0.6863f ) ), // green
        new Color( Color.HSBtoRGB(
                8.2569f / 360 ,
                0.4599f ,
                0.9294f ) ),  // red
        new Color( Color.HSBtoRGB(
                0f / 360,
                0f,
                .6304f ) ),   // grey
        new Color( Color.HSBtoRGB(
                60.5505f / 360 ,
                0.4619f ,
                0.9255f ) ), // yellow
    };

    static Color getColor( int color ) {
        return colorList6[color];
    }
}
