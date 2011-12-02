package org.nlogo.hotlink.dialogs;

// TODO: This whole section is a mess. Fix.

import org.nlogo.api.LogoList;


public class InterfaceObject {
    int maximum;
    int minimum;
    Double increment;
    String name;
    double defaultValue;
    int myType;
    Object[] values;

    public static final int
        SLIDER = 0,
        CHOOSER = 1,
        BOOLEAN = 2;

    public InterfaceObject(String name, Object defaultValue) {
        this.name = name;
        this.myType = BOOLEAN;
        if( defaultValue == "true" ) {
            this.defaultValue = 1;
        } else {
            this.defaultValue = 0;
        }
    }

    // the chooser object
    public InterfaceObject( String name, LogoList acceptedValues, int defaultIndex ) {
        this.name = name;
        this.myType = CHOOSER;
        this.defaultValue = defaultIndex;
        values = new String[acceptedValues.size()];
        int index = 0;
        for( Object value : acceptedValues ) {
            values[index] = value;
            index++;
        }
    }

    // the slider object
    public InterfaceObject( int maximum, int minimum, double increment, String name, double defaultValue ) {
        this.maximum = maximum;
        this.minimum = minimum;
        this.increment = increment;
        this.name = name;
        this.myType = SLIDER;
        this.defaultValue = defaultValue;
    }


    InterfaceObjectPanel makeInterfaceObject() {
        InterfaceObjectPanel interfaceObjectPanel = new InterfaceObjectPanel(this);

        return interfaceObjectPanel;
    }
}
