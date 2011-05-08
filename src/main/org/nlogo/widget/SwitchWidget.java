package org.nlogo.widget ;

import java.util.ArrayList;
import java.util.List;

import org.nlogo.api.Editable;
import org.nlogo.api.I18N;
import org.nlogo.api.Property;
import org.nlogo.window.Widget;
import org.nlogo.window.InterfaceGlobalWidget;

public strictfp class SwitchWidget
	extends Switch
	implements Editable, InterfaceGlobalWidget ,
	org.nlogo.window.Events.PeriodicUpdateEvent.Handler
{

	@Override
	public String classDisplayName() { return I18N.gui().get("tabs.run.widgets.switch") ; }
	
	public List<Property> propertySet()
	{
		return Properties.swiitch() ;
	}
	
	public Object valueObject()
	{
		return constraint.defaultValue() ;
	}

	public void valueObject( Object value )
	{
		if( value instanceof Boolean )
		{
			isOn( ( (Boolean) value ).booleanValue() ) ;
		}
	}

	public String nameWrapper()
	{
		return name() ;
	}

	public void nameWrapper( String name )
	{
		nameChanged = ! name.equals( name() ) || nameChanged ;
		// don't send an InterfaceGlobalEvent, wait until editFinished() for that, we don't
		// want to recompile yet
		name( name , false ) ;
	}

	@Override
	public boolean editFinished()
	{
		super.editFinished() ;
		name( name() , nameChanged ) ;
		updateConstraints() ;
		nameChanged = false ;
		return true ;
	}


	
	
	// don't send an event unless the name of the variable
	// defined changes, which is the only case in which we 
	// want a recompile. ev 6/15/05
	@Override
	public String name() { return name ; }
	public void name( String name , boolean sendEvent )
	{
		super.name( name ) ;

		if( sendEvent )
		{
			new org.nlogo.window.Events.InterfaceGlobalEvent
				( this , true , true , false , false )
				.raise( this ) ;
		}
	}

	@Override
	public void isOn( boolean on )
	{
		if ( on != isOn() )
		{
			super.isOn( on ) ;
			new org.nlogo.window.Events.InterfaceGlobalEvent
				( this , false , false , true , false )
				.raise( this ) ;
		}
	}

	public void handle( org.nlogo.window.Events.PeriodicUpdateEvent e )
	{
		new org.nlogo.window.Events.InterfaceGlobalEvent
			( this , false , true , false , false )
			.raise( this ) ;
	}

	@Override
	public String save()
	{
		StringBuilder s = new StringBuilder() ;
		s.append( "SWITCH\n" ) ;
		s.append( getBoundsString() ) ;
		if( ( null != displayName() ) && ( !displayName().trim().equals( "" ) ) )
		{
			s.append( displayName() + "\n" ) ;
		}
		else
		{
			s.append( "NIL\n" ) ;
		}
		if( ( null != name() ) && ( !name().trim().equals( "" ) ) )
		{
			s.append( name() + "\n" ) ;
		}
		else
		{
			s.append( "NIL\n" ) ;
		}

		if( isOn() )
		{
			s.append( 0 + "\n" ) ;
		}
		else
		{
			s.append( 1 + "\n" ) ;
		}

		s.append( 1 + "\n" ) ;  // for compatibility
		s.append( -1000 + "\n" ) ; // for compatibility

		return s.toString() ;
	}

	@Override
	public Object load( String[] strings , Widget.LoadHelper helper )
	{
		name( org.nlogo.api.File.restoreLines( strings[ 6 ] ) , true ) ;
		isOn( Double.valueOf( strings[ 7 ] ).doubleValue() == 0 ) ;
		int x1 = Integer.parseInt( strings[ 1 ] ) ;
		int y1 = Integer.parseInt( strings[ 2 ] ) ;
		int x2 = Integer.parseInt( strings[ 3 ] ) ;
		int y2 = Integer.parseInt( strings[ 4 ] ) ;
		setSize( x2 - x1 , y2 - y1 ) ;
		return this ;
	}

	
	public void handle( org.nlogo.window.Events.AfterLoadEvent e )
	{
		updateConstraints() ;
	}


}
