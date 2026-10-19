/**
 *
 * This is the main widget of the ColorSchemesWidget contaning two ComboBoxes,
 * a SpinButton and a Button.
 * These widget permit to set a Scheme Legend by specifiying:
 * <li>  a colorSchemeString
 * <li>  a colorSchemeNameString
 * <li>  a colorSchemeSizeInt
 * or get the selected Schemes as an RGB int [][] Array or a awt.Color Array
 *
 * Glossary:
 *
 *  ColorScheme :   The 3 ColorScheme are: Sequential, Diverging, Qualitative.
 *                  Different color schemes for different types of data such as:
 *          Ordinal , Interval/Ratio, Nominal.
 *  ColorLegend:    A color legend contains a set of certain hues for a ColorScheme
 *                  for example:
 *            a Sequential  ColorScheme with a Reds ColorLegend
 *              a Diverging   ColorScheme with a RedBlue ColorLegend
 *                      a Qualitative ColorScheme with a Set1 ColorLegend
 *  ColorSize  :    Color Size specifies the number of data clases that the choosen
 *           ColorLegend contains, all the ColorLegends have a minimum 3
 *          colors and contain a Maximum of 9 to 12 colors.
 */


//TODO:
// * Change ComboBox Model when switching Schemes to have a max corresponding to the model.
// * Automatically reduce number of "classes" when changing ColorScheme.
// * Disable the non valid choices for large number of clases in the Quantitative Schemes.
// * Implement Background color?
// * Put the RGB or HSB values next to the legend. The best would be to use a JTable so the
//     color values can be copied and pasted
// Create the methods to have a general purpose widget such as:
//   - set the size of the color swatches.
//   - set the orientation of the widget.
//  - make the constructor so this it can behave like a standard dialog.

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nlogo.api.ExtensionException ;
import org.nlogo.api.LogoException;

public class ColorSchemesPanel
  extends JPanel
  implements ChangeListener, ActionListener {
  private static final long serialVersionUID = 1L;
  JSpinner             colorSizeJSpinner;
  JComboBox<String>    schemeJComboBox;
  ColorSchemesComboBox legendJComboBox;
  JLabel               legendLabel;
  JLabel                statusLabel;

  String colorSchemeType = "Sequential";
  String colorLegendName = "YlOrBr";
  int colorSchemeSize    = 5;
  int[][] colorSchemeRGBArray;

  static final int START_INDEX = 0;
  static final int LEGEND_LABEL_SIZE = 30;
  static final int MAX_LEGEND_LABEL = 12;


  public ColorSchemesPanel (JLabel statusLabel_)
  throws ExtensionException {
    super();

    statusLabel = statusLabel_;
    setLayout(new BorderLayout());
    JPanel topPanel  = new JPanel(new FlowLayout(FlowLayout.LEFT));
    add(topPanel, BorderLayout.CENTER);

    //Create and Populate a JComboBox for the Color Schemes
    String[] schemeType = { "Sequential", "Divergent", "Qualitative" };
    schemeJComboBox = new JComboBox<String>(schemeType);
    schemeJComboBox.setSelectedIndex(START_INDEX);
    topPanel.add(schemeJComboBox);
    schemeJComboBox.addActionListener(this);

    //Create the ComboBox legends
    legendJComboBox = new ColorSchemesComboBox("Sequential", this);
    topPanel.add(this.legendJComboBox);
    legendJComboBox.addActionListener(this);

    //Add the Number of Colors
    int SequencialSchemeMaxSize = ColorSchemes.getMaximumLegendSize("Sequential");
    SpinnerNumberModel colorNumberModel = new SpinnerNumberModel(
                                       5,  //init
                                       3,  //min
                                       SequencialSchemeMaxSize,  //max
                                       1); //step
    colorSizeJSpinner = new JSpinner(colorNumberModel);
    topPanel.add(colorSizeJSpinner);
    colorSizeJSpinner.addChangeListener(this);

    //Create the JLabel legend
    legendLabel = new JLabel();
    ImageIcon initialIcon =
      new ColorSchemesIconImage(
                 ColorSchemes.getRGBArray(colorSchemeType, colorLegendName, colorSchemeSize),
                 LEGEND_LABEL_SIZE);
    legendLabel.setIcon(initialIcon);
    Dimension dim0 = new Dimension(  MAX_LEGEND_LABEL * LEGEND_LABEL_SIZE,
                    LEGEND_LABEL_SIZE);
    legendLabel.setPreferredSize(dim0);
    topPanel.add(legendLabel);

    //Initializing the variables
    colorSchemeSize = colorNumberModel.getNumber().intValue();
    colorSchemeType = (String) schemeJComboBox.getSelectedItem();
    colorLegendName = ((ImageIcon)
                   legendJComboBox.getSelectedItem()).getDescription();

    //FIXME: This could be done cleaner, get read of arbitrary constants
    //       calculate the required height and width better ...
    Dimension dim1 = new Dimension (schemeJComboBox.getPreferredSize().width   +
                        legendJComboBox.getPreferredSize().width   +
                              colorSizeJSpinner.getPreferredSize().width +
                              LEGEND_LABEL_SIZE * MAX_LEGEND_LABEL + 30,
                              LEGEND_LABEL_SIZE + 40 );
    this.setSize(dim1);
    setPreferredSize(dim1);

    statusLabel.setText(" \"" +
        colorSchemeType + "\"  \"" +
        colorLegendName + "\" " +
        colorSchemeSize);
  }

  //TODO: handle exception ...
  public void setLegend(String colorSchemeType, String colorLegendName, int colorSchemeSize)
  throws ExtensionException {
    this.colorSchemeType = colorSchemeType;
    this.colorLegendName = colorLegendName;
    this.colorSchemeSize = colorSchemeSize;

    legendJComboBox.setModelFromString(colorSchemeType);

    colorSizeJSpinner.setValue( Integer.valueOf(colorSchemeSize));

    ImageIcon initialIcon =
      new ColorSchemesIconImage(
               ColorSchemes.getRGBArray(colorSchemeType, colorLegendName, colorSchemeSize),
               LEGEND_LABEL_SIZE);
    legendLabel.setIcon(initialIcon);
  }

  /*
   * returns a an int[][] Array containing the selected
   * ColorScheme. For example,
   */
  public int[][] getColorSchemeRGBArray()
  throws ExtensionException {
    return ColorSchemes.getRGBArray(colorSchemeType,
                    colorLegendName,
                    colorSchemeSize);
  }

  public Color[] getColorSchemeColorArray()
  throws ExtensionException {
    return ColorSchemes.getColorArray(colorSchemeType,
                      colorLegendName,
                      colorSchemeSize);
  }

  public String getColorSchemeString() {
    return (colorSchemeType + " " +
      colorLegendName + " " +
      String.valueOf(colorSchemeSize));
  }

  public boolean legendExists (String LegendName)
  throws ExtensionException {
    colorSchemeRGBArray = ColorSchemes.getRGBArray(
      colorSchemeType,
      LegendName,
      colorSchemeSize);

    if (colorSchemeRGBArray == null) {
      return false;
    }
    else {
      return true;
    }
  }

  // Display the final choosen legend
  public void displayLegend()
  throws ExtensionException {
    colorSchemeRGBArray = ColorSchemes.getRGBArray(
    colorSchemeType,
    colorLegendName,
    colorSchemeSize);

    if (colorSchemeRGBArray != null) {
      legendLabel.setText(null);
      legendLabel.setIcon(new ColorSchemesIconImage(
      ColorSchemes.getRGBArray(
          colorSchemeType, colorLegendName, colorSchemeSize),
          LEGEND_LABEL_SIZE));
    }
    else {
      legendLabel.setIcon(null);
      // Using html for auto wrapping
      if (colorSchemeType.equals("Sequential")) {
        legendLabel.setText("<html>No sequential scheme available, " +
                    "select less or equal than 9</html>");
      }
      if (colorSchemeType.equals("Divergent")) {
        legendLabel.setText("<html>No Divergent scheme available, " +
                    "select less or equal than 11</html>");
      }
      if (colorSchemeType.equals("Qualitative")) {
        legendLabel.setText("<html>Choose an enabled legend</html>");
        if (!(legendJComboBox.isPopupVisible() )) {
          legendJComboBox.showPopup();
        }
      }
    }
  }

  public void displayLegend(String colorLegendName)
  throws ExtensionException {
    colorSchemeRGBArray = ColorSchemes.getRGBArray(
        colorSchemeType,
        colorLegendName,
        colorSchemeSize);

    if (colorSchemeRGBArray != null) {
      legendLabel.setText(null);
      legendLabel.setIcon(new ColorSchemesIconImage(
                ColorSchemes.getRGBArray(
                  colorSchemeType, colorLegendName, colorSchemeSize),
                  LEGEND_LABEL_SIZE));
    }
    else {
      legendLabel.setIcon(null);
      if (colorSchemeType.equals("Sequential")) {
        legendLabel.setText("No sequential scheme available, " +
                    "select less or equal than 9");
      }
      if (colorSchemeType.equals("Divergent")) {
        legendLabel.setText("No Divergent scheme available, " +
                    "select less or equal than 11");
      }
      if (colorSchemeType.equals("Qualitative")) {
        legendLabel.setText("<html>Choose an enabled legend</html>");
        if (!(legendJComboBox.isPopupVisible())) {
          legendJComboBox.showPopup();
        }
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    colorSchemeType = schemeJComboBox.getSelectedItem().toString();
     this.legendJComboBox.setModelFromString(colorSchemeType);
     colorLegendName = ((ImageIcon) legendJComboBox.getSelectedItem()).getDescription();

     SpinnerNumberModel spinnerNumberModel = (SpinnerNumberModel) colorSizeJSpinner.getModel();
     ImageIcon icon = (ImageIcon) legendJComboBox.getSelectedItem();
     colorLegendName = icon.getDescription();
     int schemeArray[][][] = {};
     try {
       schemeArray = ColorSchemes.getRGBArray(colorSchemeType, colorLegendName);
     } catch (ExtensionException em) { }
     colorSchemeSize = schemeArray.length + 2;
     SpinnerNumberModel legendSpinner = new SpinnerNumberModel(Math.min(spinnerNumberModel.getNumber().intValue(), colorSchemeSize), 3, colorSchemeSize, 1);
     colorSchemeSize = Math.min(spinnerNumberModel.getNumber().intValue(), colorSchemeSize); // changing to match the spinner number, displayLegend() will use this
     colorSizeJSpinner.setModel(legendSpinner);
    try {
      displayLegend();
    }
    catch(ExtensionException ex) {

    }
    statusLabel.setText(" \"" +
        colorSchemeType + "\"  \"" +
        colorLegendName + "\" " +
        colorSchemeSize);
  }

  // If the user changed the Size
  public void stateChanged(ChangeEvent e) {
    SpinnerNumberModel numberModel = (SpinnerNumberModel) this.colorSizeJSpinner.getModel();
    colorSchemeSize = numberModel.getNumber().intValue();
    try {
      displayLegend();
    }
    catch(ExtensionException ex) {  }
    legendJComboBox.repaint();
    statusLabel.setText(" \"" +
        colorSchemeType + "\"  \"" +
        colorLegendName + "\" " +
        colorSchemeSize);
  }
}
