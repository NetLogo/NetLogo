package org.nlogo.api ;

// This class handles output of CSV files.  Input of CSV files is
// handled elsewhere, in ImportLexer. - ST 7/22/03, 2/13/08

import java.util.List;

public strictfp class CSV
{

	private final ObjectDumper dumper ;

	public CSV( CSV.ObjectDumper dumper )
	{
		this.dumper = dumper ;
	}

	///

	public String blank()
	{
		return "" ;
	}

	///

	// this dumps a object in the exporting format:
	// --if the object is a string, all double quotes are doubled and escape 
	// chars properly escaped.
	// --all objects are returned as a string surrounded by double quotes.
	public String data( Object obj )
	{
		return encode( dumper.dump( obj ) ) ;
	}
	
	public String number( double d )
	{
		return encode( dumper.dump( Double.valueOf( d ) ) ) ;
	}

	public String number( int i )
	{
		return encode( dumper.dump( Integer.valueOf( i ) ) ) ;
	}

	public String dataRow( Object[] objs )
	{
		StringBuilder buf = new StringBuilder() ;
		for( int i = 0 ; i < objs.length ; i++ )
		{
			if( i > 0 )
			{
				buf.append( ',' ) ;
			}
			buf.append( data( objs[ i ] ) ) ;
		}
		return buf.toString() ;
	}

	public String header( String s )
	{
		return encode( s ) ;
	}
	
	public String headerRow( String[] strings )
	{
		StringBuilder buf = new StringBuilder() ;
		for( int i = 0 ; i < strings.length ; i++ )
		{
			if( i > 0 )
			{
				buf.append( ',' ) ;
			}
			buf.append( encode( strings[ i ] ) ) ;
		}
		return buf.toString() ;
	}

	///

	public static final int CELL_WIDTH = 10000 ;
	public static final int MAX_COLUMNS = 2 ;
	
	public String encode( String s )
	{
		if( s == null )
		{
			s = "null" ;
		}

		
		StringBuilder result = new StringBuilder() ;
		result.append( '"' ) ;
		for( int i = 0 ; i < s.length() ; i++ )
		{
			char c = s.charAt( i ) ;
			result.append( c ) ;
			if( c == '"' )
			{
				result.append( '"' ) ;
			}
		}
		result.append( '"' ) ;
		return result.toString() ;
	}

	public void stringToCSV( java.io.PrintWriter writer , String text )
	{
		for( int i = 0 ; i < text.length() ; )
		{
			String line = "" ;
			
			for( int k = 0 ; k < MAX_COLUMNS  && i < text.length() ; )
			{
				int end = StrictMath.min( ( i + CELL_WIDTH ) , ( text.length() ) ) ;
				line += text.substring( i , end )  ;
				
				i += CELL_WIDTH ;
				k++ ;
				
				if( i < text.length() && k < MAX_COLUMNS )
				{
					line += "," ;
				}
			}
			writer.println( data( line ) ) ;
		}
	}
	
	///

	public String variableNameRow( List<String> v )
	{
		StringBuilder result = new StringBuilder() ;
		for( int i = 0 ; i < v.size() ; i++ )
		{
			if( i > 0 )
			{
				result.append( ',' ) ;
			}
			result.append
				( encode( v.get( i ) ).toLowerCase() ) ;
		}
		return result.toString() ;
	}

	public interface ObjectDumper
	{
		String dump( Object obj ) ;
	}

}
