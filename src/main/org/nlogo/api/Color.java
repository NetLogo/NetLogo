package org.nlogo.api ;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.lang.Number;  // not api.Number

public final strictfp class Color
{

	// this class is not instantiable
	private Color() { throw new IllegalStateException() ; }

	/// window.View uses this to fill the background

	// these define the structure of NetLogo's color space, namely,
	// a range of [0.0,140.0)

	public static final int NUM_HUES = 14 ;
	public static final int MAX_COLOR = 10 * NUM_HUES ;

	// these define NetLogo's color names and how they map
	// into the [0.0,140.0) range

	private static final String[] COLOR_NAMES =
	{
		// the 13 base hues
		"gray" , "red" , "orange" , "brown" ,
		"yellow" , "green" , "lime" , "turquoise" , "cyan" , "sky" ,
		"blue" , "violet" , "magenta" , "pink" ,
		// plus two special cases
		"black" , "white"
	} ;
	
	public static final double BLACK = 0 ;
	public static final double WHITE = 9.9 ;

	public static final Double BOXED_BLACK = Double.valueOf( 0 ) ;
	public static final Double BOXED_WHITE = Double.valueOf( WHITE ) ;

	private static final double[] COLOR_NUMBERS =
	{
		// the 13 base hues
		5.0 , 15.0 , 25.0 , 35.0 , 45.0 , 55.0 , 65.0 ,
		75.0 , 85.0 , 95.0 , 105.0 , 115.0 , 125.0 , 135.0 ,
		// plus two special cases
		BLACK , WHITE
	} ;

	/// this defines how the colors actually look.
	/// note that because of the funky way we scale
	/// the ranges, these differ slightly from
	/// the actual colors that end up on screen, so
	/// remember to never access this directly, only
	/// use it to fill ARGB_CACHE, since we do the
	/// scaling as we fill the cache - ST 5/11/05
	private static final int[] COLORS_RGB =
	{
		140 , 140 , 140 , // gray       (5)
		215 , 48  , 39  , // red       (15)
		241 , 105 , 19  , // orange    (25)
		156 , 109 , 70  , // brown     (35)
		237 , 237 , 47  , // yellow    (45)
		87  , 176 , 58  , // green     (55)
		42  , 209 , 57  , // lime      (65)
		27  , 158 , 119 , // turquoise (75)
		82  , 196 , 196 , // cyan      (85)
		43  , 140 , 190 , // sky       (95)
		50  , 92 ,  168 , // blue     (105)
		123 , 78  , 163 , // violet   (115)
		166 , 25  , 105 , // magenta  (125)
		224 , 126 , 149 , // pink     (135)
		0   , 0   , 0   , // black
		255 , 255 , 255   // white
	} ;


	
	
	// keep the same information in different forms in some extra
	// arrays, for fast access later

	private static final int[] ARGB_CACHE = new int[ MAX_COLOR * 10 ] ;
	static
	{
		for( int i = 0 ; i < MAX_COLOR * 10 ; i++ )
		{
			ARGB_CACHE[ i ] = computeARGBforCache( i ) ;
		}
		// override the entries for white and black to be pure
		// white and pure black instead of gray
		ARGB_CACHE[ 0 ] = 0xff << 24 ;
		ARGB_CACHE[ 99 ] = 0xffffffff ;

		// override the entries for white and black to be pure
		// white and pure black instead of gray
		ARGB_CACHE[ 0 ] = 0xff << 24 ;
		ARGB_CACHE[ 99 ] = 0xffffffff ;
	}

	private static final java.awt.Color AWT_CACHE[] =
		new java.awt.Color[ org.nlogo.api.Color.MAX_COLOR * 10 ] ;
	static
	{
		for( int i = 0 ; i < org.nlogo.api.Color.MAX_COLOR * 10 ; i++ )
		{
			AWT_CACHE[ i ] =
				new java.awt.Color( Color.getARGBbyPremodulatedColorNumber
									( i / 10.0 ) ) ;
		}
	}

	public static java.awt.Color getColor( Object color )
	{
		if( color instanceof Double )
		{
			return AWT_CACHE[ (int) ( ( (Double) color ).doubleValue() * 10 ) ] ;
		}
		else if( color instanceof LogoList )
		{
			LogoList list = (LogoList) color ;
			if( list.size() == 3 )
			{
				return new java.awt.Color( ((Number)list.get( 0 )).intValue() , 
										   ((Number)list.get( 1 )).intValue() , 
										   ((Number)list.get( 2 )).intValue() ) ;
			}
			else if( list.size() == 4 )
			{
				return new java.awt.Color( ((Number)list.get( 0 )).intValue() , 
										   ((Number)list.get( 1 )).intValue() , 
										   ((Number)list.get( 2 )).intValue() ,
										   ((Number)list.get( 3 )).intValue() ) ;
			}
		}
		throw new IllegalStateException() ;
	}

	// also keep a cache of reverse lookups from rgb values to NetLogo
	// color numbers, for the benefit of import-pcolors

	private static final Map<Double,Double> rgbMap =
		new HashMap<Double,Double>() ;
	static
	{
		for( int c = 0 ; c < MAX_COLOR * 10 ; c++ )
		{
			double color = ( c ) / 10.0 ;
			rgbMap.put
				( Double.valueOf
				  ( getARGBbyPremodulatedColorNumber( color ) ) ,
				  Double.valueOf( color ) ) ;
		}
	}

	/// these method names have almost no rhyme or reason to them,
	/// so beware... - ST 4/30/05

	// input: [0-15]
	// output: [0.0-140.0)
	public static double getColorNumberByIndex( int index )
	{
		return COLOR_NUMBERS[ index ] ;
	}

	public static String[] getColorNamesArray()
	{
		return COLOR_NAMES ;
	}

	// input: [0-15]
	public static String getColorNameByIndex( int index )
	{
		return COLOR_NAMES[ index ] ;
	}

	// input: any
	// output: [0-139]
	public static int modulateInteger( int color )
	{
		if( color < 0 || color >= MAX_COLOR )
		{
			color %= MAX_COLOR ;
			if( color < 0 )
			{
				color += MAX_COLOR;
			}
		}
		return color ;
	}    

	// input: any
	// output: [0.0-140.0)
	public static double modulateDouble( Double color )
	{
		return modulateDouble( color.doubleValue() ) ;
	}    
	
	// input: any
	// output: [0.0-140.0)
	public static double modulateDouble( double color )
	{
		if( color < 0 || color >= MAX_COLOR )
		{
			color %= MAX_COLOR ;
			if( color < 0 )
			{
				color += MAX_COLOR;
			}
			// we have to be careful here because extremely small negative values
			// may equal 140 when added to 140.  Gotta love floating point math...
			// - ST 10/20/04
			if( color >= MAX_COLOR )
			{
				color = 139.9999999999999 ;
			}
		}
		return color ;
	}
	
	// input: any
	// output: 0.0 or 5.0 or 15.0 or ... or 135.0
	public static double findCentralColorNumber( double color )   /* all shades of a color return the same color
															* i.e.  blue, blue - 5, blue + 4.9999 will return
															* the same thing
															*/
	{
		if( color < 0 || color >= MAX_COLOR )
		{
			color = modulateDouble( color ) ;
		}
		return ( ( (int) ( color / 10 ) ) + 0.5) * 10 ;
	}

	// given a color in ARGB, function returns a value in the range 0 - 140
    // that represents the color in NetLogo's color scheme
	// input: ARGB
	// output: [0.0-139.9]
	public static double getClosestColorNumberByARGB( int argb )
	{
		Double lookup = rgbMap.get( Double.valueOf( argb ) ) ;
		if( lookup != null )
		{
			return lookup.doubleValue() ;
		}
		return estimateClosestColorNumberByRGB( argb );
	}
	
	// given a color in ARGB, function returns a string in the "range" of  
	// "red - 5" to "magenta + 5" representing the color in NetLogo's color scheme
	// input: ARGB
	// output: ["red - 5" to "magenta + 5"]
	public static String getClosestColorNameByARGB( int argb )
	{
		String colorName = null; 
		String baseColorName;
		NumberFormat formatter = new DecimalFormat("###.####");
		double closest = getClosestColorNumberByARGB( argb );

		if ( closest != BLACK && closest != WHITE)
		{
			int baseColorNumber = (int) findCentralColorNumber(closest);
			double difference = closest - baseColorNumber;
			baseColorName = getColorNameByIndex((baseColorNumber - 5) / 10);
			if (difference == 0) 
			{
				colorName = baseColorName;
			}
			else if (difference > 0) 
			{
				colorName = baseColorName + " + " + 
						    formatter.format(StrictMath.abs(difference));
			}
			else if (difference < 0)
			{
				colorName = baseColorName + " - " + 
				            formatter.format(StrictMath.abs(difference));
			}
		}
		else if (closest == BLACK) 
		{
			colorName = getColorNameByIndex(14);
		}
		else if (closest == WHITE ) 
		{
			colorName = getColorNameByIndex(15);
		}
		
		return colorName;		
	}
	

	// given a color in the HSB spectrum, function returns a value
    // that represents the color in NetLogo's color scheme
	// inputs: clamped to [0.0-1.0]
	// output: [0.0-139.9]
	public static double getClosestColorNumberByHSB( float h , float s , float b )
	{
		// restrict to 0-255 range
		h = StrictMath.max( 0 , StrictMath.min( 255 , h ) ) ;
		s = StrictMath.max( 0 , StrictMath.min( 255 , s ) ) ;
		b = StrictMath.max( 0 , StrictMath.min( 255 , b ) ) ;
		// convert to RGB
		int argb = java.awt.Color.HSBtoRGB( h / 255 , s / 255 , b / 255 ) ;
		Double lookup = rgbMap.get( Double.valueOf( argb ) ) ;
		if( lookup != null )
		{
			return lookup.doubleValue() ;
		}
		// try the new search mechanism
		return estimateClosestColorNumberByRGB( argb );
	}


	private static double estimateClosestColorNumberByRGB( int argb )
	{
		Set<Map.Entry<Double,Double>> supportedColors = rgbMap.entrySet() ;
		long smallestDistance = 100000000;
		double closest = 0;
		for( Map.Entry<Double,Double> entry : supportedColors )
		{
			int candidate = entry.getKey().intValue();
			long dist = colorDistance(argb, candidate); 
			if ( dist < smallestDistance )
			{
				smallestDistance = dist ;
				closest = entry.getValue().doubleValue() ;
			}
		}
		return closest;
	}
	
	// Java code translated from a C snippet at:
	// http://www.compuphase.com/cmetric.htm
	private static long colorDistance( int argb1, int argb2 )
	{
		int r1 = argb1 >> 16 & 0xff ;
		int g1 = argb1 >> 8 & 0xff ;
		int b1 = argb1 & 0xff ;

		int r2 = argb2 >> 16 & 0xff ;
		int g2 = argb2 >> 8 & 0xff ;
		int b2 = argb2 & 0xff ;

		long rmean = r1 + r2 / 2 ;
		long rd = r1 - r2 ;
		long gd = g1 - g2 ;
		long bd = b1 - b2 ;
		return (((512+rmean)*rd*rd)>>8) + 4*gd*gd + (((767-rmean)*bd*bd)>>8);
	}
	
	// input: [0.0-140.0)
	// output: ARGB
	public static int getARGBbyPremodulatedColorNumber( double modulatedColor )
	{
		// note that we're rounding down to the nearest 0.1 - ST 5/30/05
		return ARGB_CACHE[ (int) ( modulatedColor * 10 ) ] ;
	}
	
	// Used only for filling the ARGB_CACHE array.
	// This is the method that determines how color numbers that don't
	// end in 5.0 actually wind up looking on the screen, by adjusting
	// the saturation or brightness according to distance from 5.0.
	// Below 5.0 we decrease brightness; above 5.0 we decrease saturation.
	// (Actually we're working in RGB space not HSB so we just increase
	// or decrease the RGB values.)
	// By "premodulated" it means that this method assumes its input
	// is already in the [0.0-140) range.
	private static int computeARGBforCache( int colorTimesTen )
	{
		int baseIndex = colorTimesTen / 100 ;
		int r = COLORS_RGB[ baseIndex * 3 ] ;
		int g = COLORS_RGB[ baseIndex * 3 + 1 ] ;
		int b = COLORS_RGB[ baseIndex * 3 + 2 ] ;
		// this is sneaky... we want the range of colors we are mapping to get
		// VERY VERY close to black at one end and white at the other, but we
		// don't want to get all the way to actual black or white, because then
		// color-under wouldn't be able to do the reverse mapping back to the
		// original color number.  so instead of dividing by 50, we divide
		// by a slightly larger number; that gives us a slightly narrower range.
		// then we need to move the numbers up a bit to get black away from 0.0
		// without causing the whites (9.9, 19.9, 29.9) to hit pure white.
		// the actual numbers 50.48 and 0.012 were arrived at by trial and error
		// and might not achieve the absolute widest possible spread, I don't know,
		// but they seem good enough. - ST 4/30/05
		double step = ( ( colorTimesTen % 100 - 50 ) ) / 50.48 + 0.012 ;
		if( step < 0.0 )
		{
			r += ( int ) ( r * step ) ;
			g += ( int ) ( g * step ) ;
			b += ( int ) ( b * step ) ; 
		}
		else if( step > 0.0 )
		{
			r += ( int ) ( ( 0xff - r ) * step ) ;
			g += ( int ) ( ( 0xff - g ) * step ) ;
			b += ( int ) ( ( 0xff - b ) * step ) ;
		}
		return ( 0xff << 24 ) + ( r << 16 ) + ( g << 8 ) + b ;
	}

	///

	// input: color name in lowercase
	// output: ARGB
	public static int getRGBByName( String name )
	{
		for( int i = 0 ; i < COLOR_NAMES.length ; i++ )
		{
			if( name.equals( getColorNameByIndex( i ) ) )
			{
				return getARGBByIndex( i ) ;
			}
		}
		throw new IllegalStateException( name ) ;
	}
	
	// input: [0-15]
	// output: ARGB
	public static int getARGBByIndex( int index )
	{
		switch( index )
		{
			case 14 : // black
				return 0xff000000 ;
			case 15 : // white
				return 0xffffffff ;
			default :
				return ARGB_CACHE[ index * 100 + 50 ] ;
		}
	}

	public static LogoList getRGBListByARGB( int argb )
	{
		LogoListBuilder result = new LogoListBuilder() ;
		// 3 is just enough digits of precision so that passing the
		// resulting values through the rgb prim will reconstruct the
		// original number (or rather the floor of the original number
		// to the nearest 0.1) - ST 10/25/05
		result.add
			( Double.valueOf
			  ( org.nlogo.api.Approximate.approximate
				( ( ( argb >> 16 ) & 0xff ) ,
				  3 ) ) ) ;
		result.add
			( Double.valueOf
			  ( org.nlogo.api.Approximate.approximate
				( ( ( argb >>  8 ) & 0xff ) ,
				  3 ) ) ) ;
		result.add
			( Double.valueOf
			  ( org.nlogo.api.Approximate.approximate
				( ( ( argb       ) & 0xff ) ,
				  3 ) ) ) ;
		return result.toLogoList() ;
	}

	public static LogoList getRGBAListByARGB( int argb )
	{
		LogoListBuilder result = new LogoListBuilder() ;
		// 3 is just enough digits of precision so that passing the
		// resulting values through the rgb prim will reconstruct the
		// original number (or rather the floor of the original number
		// to the nearest 0.1) - ST 10/25/05
		result.add
			( Double.valueOf
			  ( org.nlogo.api.Approximate.approximate
				( ( ( argb >> 16 ) & 0xff ) ,
				  3 ) ) ) ;
		result.add
			( Double.valueOf
			  ( org.nlogo.api.Approximate.approximate
				( ( ( argb >>  8 ) & 0xff ) ,
				  3 ) ) ) ;
		result.add
			( Double.valueOf
			  ( org.nlogo.api.Approximate.approximate
				( ( ( argb       ) & 0xff ) ,
				  3 ) ) ) ;
		result.add
			( Double.valueOf
			  ( org.nlogo.api.Approximate.approximate
				( ( ( argb >> 24 ) & 0xff ) ,
				  3 ) ) ) ;
		return result.toLogoList() ;
	}

	public static int getARGBIntByRGBAList( LogoList rgba )
	{
		if( rgba.size() == 4 )
		{
			return ( ( ((Double)rgba.get( 3 )).intValue() << 24 ) |
					 ( ((Double)rgba.get( 0 )).intValue() << 16 ) |
					 ( ((Double)rgba.get( 1 )).intValue() << 8 ) |
					 ( ((Double)rgba.get( 2 )).intValue() ) ) ;
		}
		else
		{
			return ( 255 << 24 | 
					 ( ((Double)rgba.get( 0 )).intValue() << 16 ) |
					 ( ((Double)rgba.get( 1 )).intValue() << 8 ) |
					 ( ((Double)rgba.get( 2 )).intValue() ) ) ;
		}
	}

	public static LogoList getHSBListByARGB( int argb )
	{
		float hsb[] = new float[ 3 ] ;
		java.awt.Color.RGBtoHSB
			( ( argb >> 16 ) & 0xff ,
			  ( argb >>  8 ) & 0xff ,
			  ( argb       ) & 0xff ,
			  hsb ) ;
		LogoListBuilder result = new LogoListBuilder() ;
		// 3 is just enough digits of precision so that passing the
		// resulting values through the hsb prim will reconstruct the
		// original number (or rather the floor of the original number
		// to the nearest 0.1) - ST 10/25/05
		result.add( Double.valueOf
					( org.nlogo.api.Approximate.approximate
					  ( hsb[ 0 ] * 255 , 3 ) ) ) ;
		result.add( Double.valueOf
					( org.nlogo.api.Approximate.approximate
					  ( hsb[ 1 ] * 255 , 3 ) ) ) ;
		result.add( Double.valueOf
					( org.nlogo.api.Approximate.approximate
					  ( hsb[ 2 ] * 255 , 3 ) ) ) ;
		return result.toLogoList() ;
	}

	///

	private static final String COLOR_TRANSLATIONS = "/system/color-translation.txt" ;
	private static final Map<Double,Integer> colorTranslations =
		new HashMap<Double,Integer>() ;
	static
	{
		String[] lines =
			org.nlogo.util.Utils.getResourceAsStringArray( COLOR_TRANSLATIONS ) ;
		for( int i = 0 ; i < lines.length ; i++ )
		{
			String line = lines[ i ].trim() ;
			if( line.equals( "" ) || line.charAt( 0 ) == '#' )
			{
				continue ;
			}
			String[] strs = line.split( "\\s+" ) ;
			Integer index = Integer.valueOf( strs[ 0 ] ) ;
			for( int j = 1 ; j < strs.length ; j++ )
			{
				colorTranslations.put( Double.valueOf( strs[ j ] ) , index ) ;
			}
		}
	}

	// this handles translation from pre-3.0 color palette to new palette.
	// some changes were made also between 3.0pre4 and 3.0pre5.
	// input: ARGB
	// output: ARGB
	public static int translateSavedColor( int color )
	{
		Integer index =
			colorTranslations.get
			( Double.valueOf( color ) ) ;
		return index == null
			? color
			: org.nlogo.api.Color.getARGBByIndex( index.intValue() ) ;
	}

	// this assumes that you have an RGB color that is
	// actually one of the NetLogo colors
	public static Double argbToColor( int argb )
	{
		return Double.valueOf( getClosestColorNumberByARGB( argb ) ) ;
	}
	
	public static int getRGBInt( int r , int g , int b )
	{
		return ((((r << 8) + g) << 8) + b ) ;
	}

	public static int getRGBInt( Object c )
	{
		if( c instanceof LogoList )
		{
			return getARGBIntByRGBAList( (LogoList) c ) ; 
		}
		else if( c instanceof Double )
		{
			return getARGBbyPremodulatedColorNumber( ((Double)c).doubleValue() ) ;			
		}
		else
		{
			throw new IllegalStateException( "Can't get RGB color" ) ;
		}
	}

	public static java.awt.Color getComplement( java.awt.Color color )
	{
		float[] rgb = color.getRGBColorComponents( null ) ;
		return new java.awt.Color( ( rgb[0] + 0.5f ) % 1.0f , ( rgb[1] + 0.5f ) % 1.0f , 
								   ( rgb[2] + 0.5f ) % 1.0f )  ;
	}

}
