package org.nlogo.api ;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final strictfp class Dump
{

	// this class is not instantiable
	private Dump() { throw new IllegalStateException() ; }

	///

	public static final CSV csv =
		new CSV
		( new CSV.ObjectDumper() {
				public String dump( Object obj ) {
					// We don't allow Integers anymore as Logo values,
					// but it's convenient to be able to dump them in
					// a CSV context... - ST 5/30/06
					if( obj instanceof Integer )
					{
						return obj.toString() ;
					}
					else
					{
						return Dump.logoObject( obj , true , true ) ;
					}
				} } ) ;

	///

	public static boolean isKnownType( Object obj )
	{
		return ( obj instanceof ExtensionObject ||
				 obj instanceof Boolean ||
				 obj instanceof Double ||
				 obj instanceof String ||
				 obj instanceof AgentSet ||
				 obj instanceof Agent ||
				 obj instanceof Nobody ||
				 obj instanceof List ) ;
	}

	public static String logoObject( Object obj )
	{
		return logoObject( obj, false, false ) ;
	}

	public static String logoObject( Object obj , boolean readable , boolean exporting )
	{
		// We need to check this first, otherwise those who subclass from the base types
		// when defining an ExtensionObject will never have their dump(...) called
		if( obj instanceof ExtensionObject )
		{
			// note that unless we directly call Dump.extensionObject we'll always be calling
			// reference = exporting.  I think that works since only the extension itself should
			// be calling !reference ev 2/29/08 
			return extensionObject( (org.nlogo.api.ExtensionObject) obj , readable , exporting , exporting ) ;
		}
		else if( obj instanceof Integer )
		{
			throw new IllegalArgumentException( "Integer: " + obj ) ;
		}
		else if( obj instanceof Boolean )
		{
			return obj.toString() ;
		}
		else if( obj instanceof Double )
		{
			return number( (Double) obj ) ;
		}
		else if( obj instanceof String )
		{
			if( readable )
			{
				return "\"" + StringUtils.escapeString( (String) obj ) + "\"" ;
			}
			else
			{
				return (String) obj ;
			}
		}
		else if( obj instanceof AgentSet )
		{
			return agentset( (AgentSet) obj, exporting ) ;
		}
		else if( obj instanceof Agent )
		{
			return agent( (Agent) obj, exporting ) ;
		}
		else if( obj instanceof Nobody )
		{
			return "nobody" ;
		}
		else if( obj instanceof List )
		{
			return list( (List<?>) obj , readable, exporting ) ;
		}
		else if( obj instanceof Lambda )
		{
			return obj.toString() ;
		}
		else if( obj == null )
		{
			return "<null>" ;
		}
		else
		{
			return "<" + obj.getClass().getName() + ">" ; 
		}
	}	

	public static String extensionObject( ExtensionObject obj, boolean readable, 
										  boolean exporting , boolean reference )
	{
		// The #{extension:type DATA}# format is treated as a literal when tokenizing
		return "{{" + obj.getExtensionName() + ":" + obj.getNLTypeName() + " "
			+ obj.dump( readable, exporting , reference )
			+"}}";
	}
	
	public static String number( Double obj )
	{
		// If there is some more efficient way to test
		// whether a double has no fractional part and
		// lies in IEEE 754's exactly representable range,
		// I would love to know about it. - ST 5/31/06
		double d = obj.doubleValue() ;
		long l = (long) d ;
		return
			( l == d &&
			  l >= -9007199254740992L &&
			  l <= 9007199254740992L )
			? Long.toString( l )
			: Double.toString( d ) ;
	}

	public static String number( double d )
	{
		long l = (long) d ;
		return
			( l == d &&
			  l >= -9007199254740992L &&
			  l <= 9007199254740992L )
			? Long.toString( l )
			: Double.toString( d ) ;
	}

	public static String map( Map<?,?> map )
	{
		return map( map , false, false ) ;
	}
	
	public static String map( Map<?,?> map , boolean readable, boolean exporting)
	{
		StringBuilder buff=new StringBuilder();
		for (Iterator<?> e = map.keySet().iterator() ; e.hasNext() ;)
		{
			String key=(String)e.next();
			//buff.append(key + " = " + hm.get(key) + "\n");
			Object value = map.get(key);
			if(value == null)
			{
				buff.append(key + " = null\n");
			}
			else if(value instanceof AgentSet)
			{
				buff.append(key + " = \n" + agentset( (AgentSet) value, exporting ));
			}
			else if(value instanceof Agent)
			{
				buff.append(key + " = \n" + agent( (Agent) value, exporting ));
			}
			else if(value instanceof List)
			{
				buff.append(key + " = \n" + list( (List<?>) value, readable, exporting ));
			}
			else
			{
				buff.append(key + " = " + value + "\n");
			}
		}
		return buff.toString() ;
	}
	
	public static String list( List<?> list )
	{
		return list( list, false, false ) ;
	}

	public static String list( List<?> list, boolean readable, boolean exporting )
	{
		return iterator( list.iterator(), readable , exporting ) ;
	}

	public static String list( List<?> list, String prefix, String suffix, String delimiter )
	{
		return iterator(list.iterator(), prefix, suffix, delimiter, false, false);
	}

	public static String list( List<?> list, String prefix, String suffix, String delimiter, boolean readable, boolean exporting )
	{
		return iterator(list.iterator(), prefix, suffix, delimiter, readable, exporting);
	}

	public static String iterator( Iterator<?> iter )
	{
		return iterator( iter , "[" , "]" , " " , false , false ) ;
	}

	public static String iterator(Iterator<?> iter, boolean readable, boolean exporting)
	{
		return iterator( iter , "[" , "]" , " " , readable, exporting ) ;
	}

	public static String iterator(Iterator<?> iter, String prefix, String suffix, String delimiter, boolean readable, boolean exporting)
	{
		StringBuilder buff=new StringBuilder();
		buff.append( prefix ) ;
		boolean firstOne = true ;
		while( iter.hasNext() )
		{
			if( firstOne )
			{
				firstOne = false ;
			}
			else
			{
				buff.append( delimiter ) ;
			}
			buff.append(logoObject(iter.next(), readable, exporting));
		}
		buff.append( suffix ) ;
		return buff.toString() ;
	}

	public static String agentset( AgentSet agentset, boolean exporting )
	{
		String printName = agentset.printName() ;
		if( printName != null )
		{
			printName = printName.toLowerCase() ;
		}
		StringBuilder buffer = new StringBuilder() ;
		if( !exporting )
		{
			if( printName != null )
			{
				return printName ;
			}
			buffer.append( "(agentset, " + agentset.count() + " " ) ;
			if( Turtle.class.isAssignableFrom( agentset.type() ) )
			{
				if( agentset.count() == 1 )
				{
					buffer.append( "turtle" ) ;
				}
				else
				{
					buffer.append( "turtles" ) ;
				}
			}
			else if( Patch.class.isAssignableFrom( agentset.type() ))
			{
				if( agentset.count() == 1 )
				{
					buffer.append( "patch" ) ;
				}
				else
				{
					buffer.append( "patches" ) ;
				}
			}
			else if( Observer.class.isAssignableFrom( agentset.type() ) )
			{
				buffer.append( "observer" ) ;
			}
			else if( Link.class.isAssignableFrom( agentset.type() ) )
			{
				if( agentset.count() == 1 )
				{
					buffer.append( "link" ) ;
				}
				else
				{
					buffer.append( "links" ) ;
				}				
			}
			else
			{
				throw new IllegalStateException() ;
			}
			buffer.append( ")" ) ;
		}
		else
		{
			buffer.append( "{");
			if( Turtle.class.isAssignableFrom( agentset.type() ) )
			{
				if( printName != null )
				{
					if( agentset == agentset.world().turtles() )
					{
						buffer.append( "all-" + printName ) ;
					}
					else
					{
						buffer.append( "breed " + printName ) ;
					}
				}
				else
				{
					buffer.append( "turtles" ) ;
					for( Agent a : agentset.agents() )
					{
						buffer.append( " " + ( (Turtle) a ).id() ) ;
					}
				}
			}
			else if( Link.class.isAssignableFrom( agentset.type() ) )
			{
				if( printName != null )
				{
					if( agentset == agentset.world().links() )
					{
						buffer.append( "all-" + printName ) ;
					}
					else
					{
						buffer.append( "breed " + printName ) ;
					}
				}
				else
				{
					buffer.append( "links" ) ;
					for( Agent a : agentset.agents() )
					{
						Link link = (Link) a ;
						buffer.append( " [" + link.end1().id() + " " +
									   link.end2().id() + " " +
									   agentset( link.getBreed() , true ) + "]" ) ;
					}
				}
			}
			else if( Patch.class.isAssignableFrom( agentset.type() ) )
			{
				if( printName != null )
				{
					buffer.append( "all-" + printName ) ;
				}
				else
				{
					buffer.append( "patches" ) ;
					for( Agent a : agentset.agents() )
					{
						Patch patch = (Patch) a ;
						buffer.append( " [" + patch.pxcor() + " " + patch.pycor() + "]") ;
					}
				}
			}
			else if( Observer.class.isAssignableFrom( agentset.type() ) )
			{
				buffer.append( "observer" ) ;
			}
			else
			{
				throw new IllegalStateException() ;
			}
			buffer.append( "}" ) ;
		}

		return buffer.toString() ;
	}

	public static String agent( Agent agent , boolean exporting )
	{
		String openParen = "(" ;
		String closeParen = ")" ;
		if( exporting )
		{
			openParen = "{" ;
			closeParen = "}" ;
		}
		if( agent instanceof Observer )
		{
			return "observer" ;
		}
		else if ( agent instanceof Turtle ||
				  agent instanceof Patch ||
				  agent instanceof Link )
		{			
			if( agent.id() == -1 )
			{
				return "nobody" ;
			}
			return openParen + agent.toString() + closeParen ;
		}
		throw new IllegalArgumentException( agent.toString() ) ;
	}

	public static String typeName( Object obj )
	{
		if ( obj instanceof Agent )
		{
			return typeName( obj.getClass() ) ;
		}
		else if( obj instanceof ExtensionObject )
		{
			return ( (ExtensionObject) obj).getNLTypeName();
		}
		else
		{
			return typeName( obj.getClass() ) ;
		}
	}

	public static String typeName( Class<?>theClass )
	{
		if( theClass == Boolean.class )
		{
			return "true/false" ;
		}
		else if( theClass == Double.class )
		{
			return "number" ;
		}
		else if( theClass == String.class )
		{
			return "string" ;
		}
		else if( AgentSet.class.isAssignableFrom( theClass ) )
		{
			return "agentset" ;
		}
		else if( Turtle.class.isAssignableFrom( theClass ) )
		{
			return "turtle" ;
		}
		else if( Patch.class.isAssignableFrom( theClass ) )
		{
			return "patch" ;
		}
		else if( Link.class.isAssignableFrom( theClass ) )
		{
			return "link" ;
		}
		else if( Observer.class.isAssignableFrom( theClass ) )
		{
			return "observer" ;
		}
		else if( Agent.class.isAssignableFrom( theClass ) )
		{
			return "agent" ;
		}
		else if( LogoList.class.isAssignableFrom( theClass ) )
		{
			return "list" ;
		}
		else if( Nobody.class.isAssignableFrom( theClass ) )
		{
			return "nobody" ;
		}
		else
		{
			return theClass.getName() ;
		}
	}
	
}
