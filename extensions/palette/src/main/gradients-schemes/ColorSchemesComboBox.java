/**
 * The ColorSchemesComboBox can display three different lists of the Schemes with
 * the name and Icon of their Legends. It depends on the ColorScheme subclasses
 * the Divergent, Qualitative, and Sequencial class
 */

//TODO:
// Change the order in which Schemes appear so they appear exacly as color brewer.
//
// remove statements suchs String selectedScheme = "Sequential"; and set the initial
// value from the constructor.

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ImageIcon;

import org.nlogo.api.ExtensionException;

import java.awt.Component;
import java.awt.event.ActionEvent;


import java.lang.reflect.Field;



class ColorSchemesComboBox extends JComboBox<ImageIcon>
{
    private static final long serialVersionUID = 1L;

    DefaultComboBoxModel<ImageIcon> sequentialSchemeModel;
    DefaultComboBoxModel<ImageIcon> qualitativeSchemeModel;
    DefaultComboBoxModel<ImageIcon> divergentSchemeModel;

    final String[] schemeType = { "Sequential", "Divergent", "Qualitative" };
    String selectedScheme = "Sequential";

    String[] sequentialSchemeStrings;
    String[] divergentSchemeStrings;
    String[] qualitativeSchemeStrings;

    ImageIcon[] sequentialSchemeImages;
    ImageIcon[] divergentSchemeImages;
    ImageIcon[] qualitativeSchemeImages;

    ColorSchemesPanel colorSchemesWidget;


    public ColorSchemesComboBox(String ColorSchemeString, ColorSchemesPanel parent)
    {
        super();

        colorSchemesWidget = parent;

        sequentialSchemeStrings = populateSchemeString(Sequential.class);
        sequentialSchemeImages  = populateSchemeImage(Sequential.class);
        sequentialSchemeModel = new DefaultComboBoxModel<ImageIcon>(sequentialSchemeImages);

        divergentSchemeStrings = populateSchemeString(Divergent.class);
        divergentSchemeImages  = populateSchemeImage(Divergent.class);
        divergentSchemeModel = new DefaultComboBoxModel<ImageIcon>(divergentSchemeImages);

        qualitativeSchemeStrings = populateSchemeString(Qualitative.class);
        qualitativeSchemeImages  = populateSchemeImage(Qualitative.class);
        qualitativeSchemeModel = new DefaultComboBoxModel<ImageIcon>(qualitativeSchemeImages);

        addActionListener(this);
        setRenderer(new ComboBoxRenderer());
        setMaximumRowCount(17);
        setModelFromString(ColorSchemeString);
    }

    public void setModelFromString(String ColorSchemeString)
    {
        if (ColorSchemeString == "Sequential")
        {
            setModel(sequentialSchemeModel);
        }
        if (ColorSchemeString == "Divergent")
        {
            setModel(divergentSchemeModel);
        }
        if (ColorSchemeString == "Qualitative")
        {
            setModel(qualitativeSchemeModel);
        }
    }

    public String[] populateSchemeString(Class<? extends ColorSchemes> c)
    {
        Field[] SchemeFields = c.getDeclaredFields();
        String[] s = new String[SchemeFields.length];
        for (int i = 0; i < SchemeFields.length; i++)
        {
            s[i] = SchemeFields[i].getName();
        }
         //System.out.println(java.util.Arrays.deepToString(s));
        return s;
    }

    public ImageIcon[] populateSchemeImage(Class<? extends ColorSchemes> c)
    {
        Field[] schemeFields = c.getDeclaredFields();
        ImageIcon[] legends = new ImageIcon[schemeFields.length];

        for (int i = 0; i < schemeFields.length; i++)
        {
            String fieldName = schemeFields[i].getName();
            int[][] legend = null;

            try
            {
                legend = ColorSchemes.getRGBArray(c.getName(), fieldName, 5);
            }
            catch( ExtensionException ex )
            {

            }

            legends[i] = new ColorSchemesIconImage(legend, 20);
            legends[i].setDescription(fieldName);
        }
        return legends;
    }

    /** Do not remove the actionPerformed  handler, it is necesary for the combobox
     * to work even if it does nothing. Removing it will cause a run time error in
     * the getListCellRendererComponent that will be hard to debug:
     *    When MousePressed value will be an empty String*/
    public void actionPerformed(ActionEvent e) {
        //System.out.println(e);
        //System.out.println(e.getSource());
    }

    class ComboBoxRenderer extends JLabel implements ListCellRenderer<ImageIcon>
    {
        private static final long serialVersionUID = 1L;

        public ComboBoxRenderer()
        {
            setOpaque(true);
        }

    @Override
        public Component getListCellRendererComponent(JList<? extends ImageIcon> list,
                                                        ImageIcon icon,
                                                        int index,
                                                        boolean isSelected,
                                                        boolean cellHasFocus)
        {
            setText(icon.getDescription());
            setIcon(icon);
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            // if (!colorSchemesPanel.legendExists(icon.getDescription());
            // System.out.println(icon.getDescription() + ": " +colorSchemesPanel.legendExists(icon.getDescription()));
            // the following line is reponsable for "graying out" the unselectable options.
            boolean  legendExist = false;
            try
            {
                legendExist =  colorSchemesWidget.legendExists(icon.getDescription());
            }
            catch( ExtensionException ex )
            {
                setEnabled(legendExist);
            }

            if (isSelected && legendExist)
            {
                setBackground(list.getBackground());
                setBorder(BorderFactory.createLineBorder(Color.black, 2));
                try
                {
                colorSchemesWidget.displayLegend(icon.getDescription());
                }
                catch( ExtensionException ex )
                {

                }
                setEnabled(legendExist);
            }
            else
            {
                setBackground(list.getBackground());
                setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            }
            return this;
        }
    }

}
