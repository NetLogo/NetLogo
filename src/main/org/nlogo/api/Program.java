package org.nlogo.api ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final strictfp class Program
{

	public final List<String> turtlesOwn = new ArrayList<String>() ;
	public final List<String> patchesOwn = new ArrayList<String>() ;
	public final List<String> linksOwn   = new ArrayList<String>() ;
	public final List<String> globals    = new ArrayList<String>() ;

	// use a LinkedHashMap to store the breeds so that the Renderer
	// can retrieve them in order of definition, for proper z-ordering
	// - ST 6/9/04
	// Using LinkedHashMap on the other maps isn't really necessary for
	// proper functioning, but makes writing unit tests easier - ST 1/19/09
	// Yuck on this Object stuff -- should be cleaned up - ST 3/7/08
	public final Map<String,Object> breeds = new LinkedHashMap<String,Object>() ;
	public final Map<String,String> breedsSingular = new LinkedHashMap<String,String>() ;
	public final Map<String,Object> linkBreeds = new LinkedHashMap<String,Object>() ;
	public final Map<String,String> linkBreedsSingular = new LinkedHashMap<String,String>() ;
	public final Map<String,List<String>> breedsOwn = new LinkedHashMap<String,List<String>>() ;
	public final Map<String,List<String>> linkBreedsOwn = new LinkedHashMap<String,List<String>>() ;
	public List<String> interfaceGlobals ;
	
	public final boolean is3D ;

	public Program( boolean is3D )
	{
		this( new ArrayList<String>() , is3D ) ;
	}

	public Program( List<String> interfaceGlobals , boolean is3D )
	{
		this.interfaceGlobals = interfaceGlobals ;
		this.is3D = is3D ;
		globals.addAll( Arrays.asList( AgentVariables.getImplicitObserverVariables() ) ) ;
		for( String s : interfaceGlobals ) { globals.add( s.toUpperCase() ) ; }
		turtlesOwn.addAll( Arrays.asList( AgentVariables.getImplicitTurtleVariables( is3D ) ) ) ;
		patchesOwn.addAll( Arrays.asList( AgentVariables.getImplicitPatchVariables( is3D ) ) ) ;
		linksOwn.addAll( Arrays.asList( AgentVariables.getImplicitLinkVariables() ) ) ;
	}

	///

	public String dump()
	{
		StringBuilder buf = new StringBuilder() ;
		buf.append( "globals " + Dump.list( globals ) + "\n" ) ;
		buf.append( "interfaceGlobals " +
					Dump.list( interfaceGlobals ) + "\n" ) ;
		buf.append( "turtles-own " + Dump.list( turtlesOwn ) + "\n" ) ;
		buf.append( "patches-own " + Dump.list( patchesOwn ) + "\n" ) ;
		buf.append( "links-own " + Dump.list( linksOwn ) + "\n" ) ;
		buf.append( "breeds " + Dump.map( breeds ) + "\n" ) ;
		buf.append( "breeds-own " + Dump.map( breedsOwn ) + "\n" ) ;
		buf.append( "link-breeds " + Dump.map( linkBreeds ) + "\n" ) ;
		buf.append( "link-breeds-own " + Dump.map( linkBreedsOwn ) + "\n" ) ;
		return buf.toString() ;
	}
	
}
