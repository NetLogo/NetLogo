// Thanks for Forrest Stonedahl for the code at the bottom
// of this file, which made all the rest of the code
// possible without me crying. :)

package org.nlogo.hotlink.main;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nlogo.agent.BooleanConstraint;
import org.nlogo.agent.ChooserConstraint;
import org.nlogo.agent.SliderConstraint;
import org.nlogo.api.LogoException;
import org.nlogo.api.ValueConstraint;

public class SetupWindow extends JDialog {
	SetupWindow window = this;
	JTextField ticks;
	
	public SetupWindow( Frame owner ) {
		super( owner , true );

		this.add( new JLabel( "Choose your setup parameters..." ) );
		setLayout( new FlowLayout() );
		//setVisible(true);
	}
	/*
	public void show() {
		this.setVisible(true);
	}*/
	
	public void pullModelInfo( String fileName ) {
		this.getContentPane().removeAll();
		
		// open the model
		org.nlogo.headless.HeadlessWorkspace workspace =
			org.nlogo.headless.HeadlessWorkspace.newInstance();
		try {
			workspace.open( fileName );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// add the model variables to the setup interface
		for( InterfaceObject io : getDefaultConstraintsObjects( workspace ) ) {
			if( io != null ) {
				add( io.makeInterfaceObject() );
			}
		}
		
		// and the tick info
		add( new JLabel( "Run for how many ticks?" ) );
		ticks = new JTextField();
		ticks.setText( "30" );
		add( ticks );
		
		JButton runModel = new JButton( runTheModel );

		add( runModel );
		this.setVisible(true);
	}
	
	private final javax.swing.Action runTheModel =
		new javax.swing.AbstractAction( "Run Model" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
            	window.setVisible(false);
        } 
    };
	
	// Thanks Forrest for the constraints code! :)
	// I updated it to spit out an array of InterfaceObjects rather than a string
	public InterfaceObject[] getDefaultConstraintsObjects(org.nlogo.headless.HeadlessWorkspace workspace)
	{
	 	StringBuilder sb = new StringBuilder();
	 	
		int numVars = workspace.world().observer().getVariableCount();
	 	InterfaceObject[] varCollection = new InterfaceObject[numVars];

		String name ="";
		double min =0;
		double max =0;
		double incr =0;
		int type = 0;
		Object value =0;
	 	
		for (int i = 0; i < numVars; i++)
		{
			ValueConstraint con = workspace.world().observer().variableConstraint(i);
			
			if ( con != null) {
				name = workspace.world.observerOwnsNameAt(i);

					//try {
						if (con instanceof SliderConstraint)
						{
							SliderConstraint scon = (SliderConstraint) con;
							min = scon.minimum();
							incr = scon.increment();
							max = scon.maximum();
							type = InterfaceObject.SLIDER;
							value = workspace.world.getObserverVariableByName(name);	
						} 
					//} catch (LogoException ex)	{
							//sb2.setLength(0);
							//sb2.append(org.nlogo.api.Dump.logoObject(con.defaultValue(), true, false));
					//}

					varCollection[i] = this.new InterfaceObject( (int) max , (int) min , incr , name , type , value );
			}
			else if (con instanceof ChooserConstraint)
			{
				ChooserConstraint ccon = (ChooserConstraint) con;
				//for (Object obj: ccon.acceptedValues())
				{
					//sb2.append(org.nlogo.api.Dump.logoObject(obj, true, false));
					//sb2.append(" ");
				}
			}
			else if (con instanceof BooleanConstraint)
			{
				//sb2.append(org.nlogo.api.Dump.logoObject(Boolean.TRUE, true, false));
				//sb2.append(" ");
				//sb2.append(org.nlogo.api.Dump.logoObject(Boolean.FALSE, true, false));
			}
			else
			{
				varCollection[i] = null;
			}
		}
		
		return varCollection;
	}
	
	class InterfaceObject {
		int maximum;
		int minimum;
		Double increment;
		String name;
		int defaultValue;
		int myType;
		
		public static final int 
			SLIDER = 0,
	        CHOOSER = 1,
	        BOOLEAN = 2;
		
		InterfaceObject( int maximum, int minimum, double increment, String name, int type, Object defaultValue ) {
			this.maximum = maximum;
			this.minimum = minimum;
			this.increment = increment;
			this.name = name;
			this.myType = type;
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
		
		JPanel makeInterfaceObject() {
			System.out.println( "makeInterfaceObject for " + name );
			
			JPanel interfaceObjectPanel = new JPanel();
			JLabel interfaceObjectLabel = new JLabel( name );
			
			//if( myType == SLIDER ) {
				InterfaceSlider interfaceSlider = new InterfaceSlider();
				interfaceSlider.setMinimum(minimum);
				interfaceSlider.setMaximum(maximum);
				interfaceSlider.setMajorTickSpacing(maximum - minimum);
				interfaceSlider.setPaintLabels(true);
				interfaceSlider.setPaintTicks(true);
				interfaceSlider.setValue(defaultValue);
				
				SliderValueField sliderValue = new SliderValueField( interfaceSlider );
				sliderValue.setText( "" + interfaceSlider.getValue() );
			//}
			
			/*
			if( myType == CHOOSER ) {
				JList interfaceList = new JList();
				
			}
			
			if( myType == BOOLEAN ) {
			}
			*/
			
			interfaceObjectPanel.add( interfaceObjectLabel );
			interfaceObjectPanel.add( interfaceSlider );
			interfaceObjectPanel.add( sliderValue );
			
			return interfaceObjectPanel;
		}
	}

	public int getTickCount() {
		return Integer.parseInt( ticks.getText() );
	}
}

/* keep in case: Forrest's code.
public static String[] getDefaultConstraintsObjects(org.nlogo.headless.HeadlessWorkspace workspace)
{
 	StringBuilder sb = new StringBuilder();
 	
	int numVars = workspace.world().observer().getVariableCount();
 	String[] varCollection = new String[numVars];
 	
	for (int i = 0; i < numVars; i++)
	{
		ValueConstraint con = workspace.world().observer().variableConstraint(i);
		if ( con != null) {
			String name = workspace.world.observerOwnsNameAt(i);
			
			//sb.append("[ ");
			//sb.append(name);
			//sb.append(" ");
			//StringBuilder sb2 = new StringBuilder();

			if (con instanceof SliderConstraint)
			{
				SliderConstraint scon = (SliderConstraint) con;
				/*try {
					
					//double min = scon.minimum();
					//double incr = scon.increment();
					//double max = scon.maximum();
					//String strIncr = org.nlogo.api.Dump.logoObject(incr, true, false);

                   // if it's a non-integer slider with more than 100 factor
					// levels, let's suggest continuous "C"
					//if (min != StrictMath.floor(min) && incr != StrictMath.floor(incr) &&
					//	StrictMath.abs((max - min) / incr) > 100)
					//{
					//	strIncr = "\"C\"";
					//}
					
					//sb2.append("[ ");
					//sb2.append(org.nlogo.api.Dump.logoObject(min, true, false));
					//sb2.append(" ");
					//sb2.append(strIncr);
					//sb2.append(" ");
					//sb2.append(org.nlogo.api.Dump.logoObject(max, true, false));
					//sb2.append(" ]");
				} catch (LogoException ex)
				{
					//sb2.setLength(0);
					//sb2.append(org.nlogo.api.Dump.logoObject(con.defaultValue(), true, false));
				}
			}
			else if (con instanceof ChooserConstraint)
			{
				ChooserConstraint ccon = (ChooserConstraint) con;
				//for (Object obj: ccon.acceptedValues())
				{
					//sb2.append(org.nlogo.api.Dump.logoObject(obj, true, false));
					//sb2.append(" ");
				}
			}
			else if (con instanceof BooleanConstraint)
			{
				//sb2.append(org.nlogo.api.Dump.logoObject(Boolean.TRUE, true, false));
				//sb2.append(" ");
				//sb2.append(org.nlogo.api.Dump.logoObject(Boolean.FALSE, true, false));
			}
			else
			{
				//sb2.append(org.nlogo.api.Dump.logoObject(con.defaultValue(),true, false));
			}
			//sb.append(sb2);
			//sb.append(" ]");
			//varCollection[i] = sb.toString();
		}
			
		
		sb = new StringBuilder();
	}
	return varCollection;
}

*/