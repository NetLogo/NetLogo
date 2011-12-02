package org.nlogo.hotlink.dialogs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.nlogo.agent.BooleanConstraint;

// TODO: This is messy.
public class InterfaceObjectPanel extends JPanel {
    InterfaceObject interfaceObject;
    //JLabel interfaceObjectLabel;
    InterfaceSlider interfaceSlider;
    SliderValueField sliderValue;
    JList interfaceList;
    JCheckBox interfaceSwitch;

    InterfaceObjectPanel(InterfaceObject interfaceObject) {
        super();
        this.interfaceObject = interfaceObject;

        JLabel interfaceObjectLabel = new JLabel( interfaceObject.name );


        if( interfaceObject.myType == InterfaceObject.SLIDER ) {
            interfaceSlider = new InterfaceSlider();
            interfaceSlider.setMinimum(interfaceObject.minimum);
            interfaceSlider.setMaximum(interfaceObject.maximum);
            interfaceSlider.setMajorTickSpacing(interfaceObject.maximum - interfaceObject.minimum);
            interfaceSlider.setPaintLabels(true);
            interfaceSlider.setPaintTicks(true);
            interfaceSlider.setValue((int) interfaceObject.defaultValue);

            sliderValue = new SliderValueField( interfaceSlider );
            sliderValue.setText( "" + interfaceSlider.getValue() );

            add( interfaceObjectLabel );
            add( interfaceSlider );
            add( sliderValue );
        }

        
        if( interfaceObject.myType == InterfaceObject.CHOOSER ) {
            String[] listValues = new String[ interfaceObject.values.length ];
            int index = 0;
            for( Object value : interfaceObject.values ) {
                listValues[index] = org.nlogo.api.Dump.logoObject(value, true, false);
                index++;
            }
            interfaceList = new JList( listValues );
            interfaceList.setSelectedIndex( (int) interfaceObject.defaultValue);
            interfaceList.setVisibleRowCount(2);

            interfaceList.setName( interfaceObject.name );

            add( interfaceList );
        }

        if( interfaceObject.myType == InterfaceObject.BOOLEAN ) {
            interfaceSwitch = new JCheckBox();
            interfaceSwitch.setText(interfaceObject.name);
            if( interfaceObject.defaultValue == 1 ) {
                interfaceSwitch.setSelected(true);
            } else {
                interfaceSwitch.setSelected(false);
            }

            add(interfaceSwitch);
        }
        

    }

    @Override
    public String getName() {
        return interfaceObject.name;
    }

    public Object getValue() {
        if( interfaceObject.myType == InterfaceObject.SLIDER ) {
            return (double) interfaceSlider.getValue();
        }

        else if( interfaceObject.myType == InterfaceObject.CHOOSER ) {
            if( interfaceList.getSelectedIndex() < 0 ) {
                return interfaceObject.defaultValue;
            }
            return interfaceObject.values[interfaceList.getSelectedIndex()];
        }

        else if( interfaceObject.myType == InterfaceObject.BOOLEAN ) {
            return new BooleanConstraint(interfaceSwitch.isSelected()).defaultValue();
        }

        return -1;
    }

    public InterfaceObject getInterfaceObject() {
        return this.interfaceObject;
    }

    class InterfaceSlider extends JSlider implements KeyListener {
        public void keyReleased(KeyEvent e) {
            try {
                this.setValue( Integer.valueOf(((SliderValueField) e.getSource()).getText()) );
            } catch (Exception ex) {
                this.setValue(0);
            }
        }

        public void keyPressed(KeyEvent e) { }
        public void keyTyped(KeyEvent e) { }

    }

    class SliderValueField extends JTextField implements ChangeListener {
        SliderValueField( InterfaceSlider is ) {
            super();
            this.setColumns(4);
            is.addChangeListener(this);
            this.addKeyListener(is);
        }

        public void stateChanged(ChangeEvent e) {
            this.setText( Integer.toString( ((InterfaceSlider) e.getSource()).getValue() ) );
        }
    }
}