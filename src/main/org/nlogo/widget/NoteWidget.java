package org.nlogo.widget ;

import java.util.ArrayList;
import java.util.List;

import org.nlogo.api.Editable;
import org.nlogo.api.I18N;
import org.nlogo.api.Property;
import org.nlogo.window.SingleErrorWidget;
import org.nlogo.window.Widget;
import org.nlogo.window.InterfaceColors;

public strictfp class NoteWidget
	extends SingleErrorWidget
	implements Editable
{ 

	private static final int MIN_WIDTH = 15;
	private static final int DEFAULT_WIDTH = 150;
	private static final int MIN_HEIGHT = 18;
	
	private int width = DEFAULT_WIDTH;
	
	private String text = "" ;
	public String text() { return text ; }
	public void text( String text ) 
	{ 
		this.text = text ;		
		repaint() ;
	}
	
	public boolean transparency()
	{
		return getBackground() == InterfaceColors.TRANSPARENT ;
	}
	public void transparency( boolean trans )
	{
		setBackground( trans ? InterfaceColors.TRANSPARENT 
					   : InterfaceColors.TEXT_BOX_BACKGROUND ) ;
		setOpaque( ! trans ) ;
	}

	///

	public NoteWidget()
	{
		setBackground( InterfaceColors.TRANSPARENT ) ;
		setOpaque( false ) ;
		org.nlogo.awt.Utils.adjustDefaultFont( this ) ;
		fontSize = getFont().getSize();
	}

	public List<Property> propertySet()
	{
		return Properties.text() ;
	}

	private java.awt.Color color = java.awt.Color.black ;
	public void color( java.awt.Color color )
	{ this.color = color ; }
	public java.awt.Color color() { return color ; }

	// initialized in constructor
	private int fontSize;
	public void fontSize ( int size )
	{
		this.fontSize = size ;
		// If we are zoomed, we need to zoom the input font size and then
		// set that as our widget font
		if ( isZoomed() && originalFont() != null ) {
			int zoomDiff = 0;
			zoomDiff = getFont().getSize() - originalFont().getSize() ;
			setFont( getFont().deriveFont( Float.valueOf( size + zoomDiff ).floatValue() ) ) ;
		} else {
			setFont( getFont().deriveFont(Float.valueOf(size).floatValue()) ) ;
		}
		
		if ( originalFont() != null ) {
			originalFont_$eq(originalFont().deriveFont(Float.valueOf(size).floatValue())) ;
		}		
		resetZoomInfo();
		resetSizeInfo();
	}
	
	public int fontSize ()
	{
		return fontSize;
	}


	///

	@Override
	public String classDisplayName() { return I18N.gui().get("tabs.run.widgets.note") ; }

	///

	@Override
	public String displayName() { return text() ; }

	@Override
	public void setBounds( java.awt.Rectangle r )
	{
		// at creation time, we may get spuriously set to 0 size, so
		// ignore that so width stays at DEFAULT_WIDTH - ST 8/17/04
		if( r.width > 0 )
		{
			width = r.width ;
		}
		super.setBounds( r ) ;
	}
	
	@Override
	public void setBounds( int x , int y , int width , int height )
	{
		// at creation time, we may get spuriously set to 0 size, so
		// ignore that so width stays at DEFAULT_WIDTH - ST 8/17/04
		if( width > 0 )
		{
			this.width = width ;
		}
		super.setBounds( x , y , width, height ) ;
	}
	
	@Override
	public java.awt.Dimension getMinimumSize()
	{
		return new java.awt.Dimension( MIN_WIDTH , MIN_HEIGHT );
	}
	
	@Override
	public java.awt.Dimension getPreferredSize( java.awt.Font font )
	{
		java.awt.FontMetrics metrics = getFontMetrics( font ) ;
		int height =
			org.nlogo.awt.Utils.breakLines( text() , metrics , width ).size()
			* ( metrics.getMaxDescent() + metrics.getMaxAscent() ) ;
		return new java.awt.Dimension
			( StrictMath.max( MIN_WIDTH , width ) ,
			  StrictMath.max( MIN_HEIGHT , height ) ) ;
	}

	@Override
	public boolean needsPreferredWidthFudgeFactor()
	{
		return false ;
	}

	@Override
	public boolean isNote()
	{
		return true ;
	}

	///

	@Override
	public void paintComponent( java.awt.Graphics g )
	{
		super.paintComponent( g ) ;
		g.setFont( getFont() ) ;
		java.awt.FontMetrics metrics = g.getFontMetrics() ;
		int stringHeight  = metrics.getMaxDescent() + metrics.getMaxAscent() ;
		int stringAscent = metrics.getMaxAscent() ;
		List<String> lines =
			org.nlogo.awt.Utils.breakLines( text() , metrics , width ) ;
		g.setColor( color ) ;
		for( int i = 0 ; i < lines.size() ; i++ )
		{
			String line = lines.get( i ) ;
			g.drawString( line , 0 , i * stringHeight + stringAscent) ;
		}
	}

	@Override
	public boolean widgetWrapperOpaque()
	{
		return ! transparency() ;
	}

	///

	@Override
	public String save()
	{
		StringBuilder s = new StringBuilder() ;
		s.append( "TEXTBOX\n" ) ;
		s.append( getBoundsString() ) ;
		if( text.trim().equals( "" ) )
		{
			s.append( "NIL\n" ) ;
		}
		else
		{
			s.append( org.nlogo.api.File.stripLines( text() ) + "\n" ) ;
		}
		s.append( fontSize + "\n" ) ;
		s.append( org.nlogo.api.Color.getClosestColorNumberByARGB( color.getRGB() )  + "\n" ) ;
		s.append( (transparency() ? "1" : "0") + "\n" ) ;
		return s.toString() ;
	}

	@Override
	public Object load( String[] strings , Widget.LoadHelper helper )
	{
		if( strings[ 5 ].equals( "NIL" ) )
		{
			text( "" ) ;
		}
		else
		{
			text( org.nlogo.api.File.restoreLines( strings[ 5 ] ) ) ;
		}
		int x1 = Integer.parseInt( strings[ 1 ] ) ;
		int y1 = Integer.parseInt( strings[ 2 ] ) ;
		int x2 = Integer.parseInt( strings[ 3 ] ) ;
		int y2 = Integer.parseInt( strings[ 4 ] ) ;
		if ( strings.length >= 7 )
		{
			fontSize( Integer.parseInt( strings[ 6 ]) ) ;
		}
		if ( strings.length >= 8 )
		{
			color( org.nlogo.api.Color.getColor( Double.parseDouble( strings[ 7 ] ) ) );
		}
		if( strings.length >= 9 )
		{
			transparency( Integer.parseInt( strings[ 8 ] ) != 0 ) ;
		}
		else
		{
			transparency( false ) ;
		}
		setSize( x2 - x1 , y2 - y1 ) ;
		return this ;
	}

}
