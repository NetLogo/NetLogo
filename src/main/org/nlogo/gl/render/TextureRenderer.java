package org.nlogo.gl.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

import org.nlogo.api.World;

strictfp class TextureRenderer
{
	int texture = 0 ;
	int textureSize = 0 ;
	boolean newTexture = false ;

	byte[][] tiles = null ;

	final org.nlogo.api.World world ;

	TextureRenderer( World world )
	{
		this.world = world ;
	}

	void deleteTexture()
	{
		textureSize = 0 ;
	}

	byte[][] createTileArray( int width , int height ,
									  int textureSize )
	{
		int numTiles = (int) StrictMath.ceil( (double) width / (double) textureSize ) 
			* (int) StrictMath.ceil( (double) height / (double) textureSize ) ;

		return new byte [ numTiles ][] ;
	}

	int calculateTextureSize( GL gl , int width , int height )
	{
		// OpenGL only allows square textures whose dimension is at least 64...
 		int size = 64 ;
		int maxTextureSize = GL.GL_MAX_TEXTURE_SIZE ;
		
		String glRendererName = gl.glGetString( GL.GL_RENDERER ).toUpperCase() ; 

		// ATI Rage Mobility lies, max texture size is 256
		// http://lists.apple.com/archives/mac-opengl/2003/Jul/msg00135.html
		// experiementally other Rage cards are reporting incorrect
		// numbers as well so let's limit them all to 256 to be safe. ev 6/3/05
		if( glRendererName.startsWith("ATI RAGE" ) )
		{
			maxTextureSize = 256 ;
		}
		// ATI Radeon 7500 lies too: the reported max texture size isn't 
		// actually supported in fullscreen mode. We couldn't find any 
		// documentation on this, but it looks like in fullscreen mode the 
		// card only supports 512. 
		// 
		// This isn't the optimal solution since the smaller limit only 
		// applies to fullscreen mode. Ideally, we'd only do this special 
		// case in fullscreen mode, and use the full (reported) texture size 
		// when not in fullscreen.
		// -- azs 6/13/05
		else if( glRendererName.startsWith("ATI RADEON 7500" ) )
		{
		    maxTextureSize = 512 ;
		}
		// Same goes for the 9200
		else if( glRendererName.startsWith("ATI RADEON 9200" ) )
		{
		    maxTextureSize = 512 ;
		}

 		while( ( width > size || height > size  ) && 
			   size < ( ( 2 + maxTextureSize ) / 2 )  )
		{
			// ...and is a power of two
			size *= 2 ;
		}
		
		return size ;
	}

	// as I've been commenting I realize it may have been easier to just have different cases 
	// for different tile arrangments rather than this massive crazy generalized method.
	// oh well.  ev 6/3/05
	void renderTextureTiles( GL gl , int width , int height , int textureSize ,
									   int[] colors , boolean dirty )
	{
		float scale = Renderer.WORLD_SCALE ;
		
		gl.glPushMatrix() ;
		
		// if we are following/riding an agent, translate the patches so the followed agent's
		// location is at the center of the world.  Then calculate the "wrapping-line" which
		// is where the patches overflows off the world. (note that there can only be at most 
		// two lines).  The wrapping line is actually in a wierd set of coordinates scaled 
		// between 0 and width/height.  The booleans indicate which side of the world the 
		// patches are overflowing on. jrn 6/8/05
		float wrapXLine = 0.0f ;
		float wrapYLine = 0.0f ;
		boolean wrapXLeft = true ;
		boolean wrapYDown = true ;
		double worldWidth  = world.worldWidth() ;
		double worldHeight = world.worldHeight() ;
		double oxcor = world.followOffsetX() ;
		double oycor = world.followOffsetY() ;

		if ( oxcor + worldWidth < worldWidth )
		{
			wrapXLine = (float) ( oxcor + worldWidth ) ;
			wrapXLeft = false ;
		}
		else if ( oxcor > 0.0f )
		{
			wrapXLine = (float) oxcor ;
		}

		wrapXLine *= ( width / (float) worldWidth ) ;
			
		if ( oycor + worldHeight < worldHeight )
		{
			wrapYLine = (float) ( oycor + worldHeight ) ;
			wrapYDown = false ;
		}
		else if ( oycor > 0.0f )
		{
			wrapYLine = (float) oycor ;
		}

		wrapYLine *= ( height / (float) worldHeight ) ;
			
		// translate patches so it is centered around followed agent
		gl.glTranslatef
			( (float) ( - oxcor * scale ) , (float) ( - oycor * scale ) , 0.0f ) ;
		
		// these are extremely poor names but I'm really running out 
		// of names these essentially keep track of whether the texture is
		// bigger than the world or the other way around and we use them in calculations
		// it's different from tileHeight and Width in that it doesn't change per
		// tile.
		float tw = StrictMath.min( ( width ) , textureSize ) ;
		float th = StrictMath.min( ( height ) , textureSize ) ;
		
		// when world size is less than the size of a texture scale down
		// by screen-size otherwise scale down by texture size * ( ratio of screen size
		// to world size ) note that for the patch texture this is 1 and for the 
		// drawing texture this is 1 / patch-size
		gl.glScalef( tw * ( (float) world.worldWidth() / (float) width ) , 
					 th * ( (float) world.worldHeight() / (float) height ) , 1 ) ;

		// translate to the upper left hand corner
		// when there is only one tile this all works out to zero
		// otherwise it's the ratio of difference between the world-size and the 
		// texture width and the texture width / 2 ( because we want the center )
		//( again if world < texture it's all zero )
		gl.glTranslatef( - ( width - tw ) * scale / ( 2 * tw ) , 
						 ( height - th ) * scale / ( 2 * th ), 0.0f ) ;	

		int i = 0 ;
		// keep track of the width and height of the current tile
		int tileWidth = StrictMath.min( width , textureSize ) ;
		int tileHeight = StrictMath.min( height , textureSize ) ; 
		
		// It easier for the yOffset to just count down since we are rendering the tiles
		// from top to bottom anyway - jrn 6/8/05
		for( int yOffset = height ; yOffset > 0 ; )
		{
			gl.glPushMatrix() ;
			for( int xOffset = 0 ; xOffset < width ; )
			{
				tileWidth = StrictMath.min( ( width - xOffset ) , textureSize ) ;
				tileHeight = StrictMath.min( yOffset , textureSize ) ;
				// refill the texture as rarely as possible.  However, if we have more
				// than one tile we have to do it all the time.  it might be better to have lots of
				// textures too but managing that seemed even more complicated + using more 
				// and more resources dunno.  we'll see if we need it.
				if( dirty || tiles.length > 1 ) 
				{
					tiles[ i ] = reuseTexture( gl , tileWidth , tileHeight , 
											   xOffset , ( height - yOffset ) ,
											   width , colors , tiles [ i ] ) ;
				}

				gl.glPushMatrix() ;

				// the only time that this scale really matters is if we are tiling but the 
				// tile is smaller than texture size ( the last tile ) in that case we want to 
				// shrink the plane that we are rendering on.
				gl.glScalef( ( ( tileWidth ) / tw  ) , ( ( tileHeight ) / th ) , 1.0f ) ;
				
				// we need to figure out if the wrapping line(s) intersect the tile, which will
				// cause the tile to split into 2/4's.  The *Split vars indicate where the tile
				// is being split on a scale from 0 to 1 (where 0,0 is top-left corner of the
				// tile) - jrn 6/8/05				
				int horzPieces = 1 ;
				float horzSplit = 1.0f ;
				int vertPieces = 1 ;
				float vertSplit = 1.0f ;

				if ( ( xOffset < wrapXLine ) && ( xOffset + tileWidth > wrapXLine ) )
				{					
					horzPieces = 2 ;
					horzSplit = ( wrapXLine - xOffset ) / tileWidth ;
				}
				if ( ( yOffset > wrapYLine ) && ( yOffset - tileHeight < wrapYLine ) )
				{
					vertPieces = 2 ;
					vertSplit = ( yOffset - wrapYLine ) / tileHeight ;
				}
				
				// render each piece of the tile
				for ( int h = 0 ; h < horzPieces ; h++ )
				{
					gl.glPushMatrix() ;
					
					// locate the size and center (relative to top-left) of the piece
					float horzPieceSize = ( h == 0 ) ? horzSplit : ( 1.0f - horzSplit ) ;
					float horzPieceCenter = ( horzPieceSize / 2 ) + ( ( h == 0 ) ? 
																	  0.0f : horzSplit ) ;
					
					// we need to translate from the center of the tile to the
					// center of the piece
					gl.glTranslatef( ( scale * ( horzPieceCenter - 0.5f ) ) , 0.0f , 0.0f ) ;
					
					// check if the piece is off the world (and should be wrapped)
					if ( ( xOffset + tileWidth * horzPieceCenter < wrapXLine )
						 && wrapXLeft )
					{
						gl.glTranslatef( ( width * scale / tileWidth ) , 0.0f , 0.0f ) ;
					}						
					else if ( ( xOffset + tileWidth * horzPieceCenter > wrapXLine )
							  && ! wrapXLeft )
					{
						gl.glTranslatef( - ( width * scale / tileWidth ) , 0.0f , 0.0f ) ;
					}
					
					for ( int v = 0 ; v < vertPieces ; v++ )
					{
						gl.glPushMatrix() ;
						
						// locate the size and center (relative to top-left) of the piece
						float vertPieceSize = ( v == 0 ) ? vertSplit : ( 1.0f - vertSplit ) ;
						float vertPieceCenter = ( vertPieceSize / 2 ) + ( ( v == 0 ) ? 
																		  0.0f : vertSplit ) ;
						
						// we need to translate from the center of the tile to the
						// center of the piece
						gl.glTranslatef( 0.0f , ( scale * ( ( 1 - vertPieceCenter ) - 0.5f ) ) ,
										 0.0f ) ;
						
						// check if the piece is off the world (and should be wrapped)												
						if ( ( yOffset - tileHeight * vertPieceCenter < wrapYLine )
							 && wrapYDown )
						{
							gl.glTranslatef( 0.0f , ( height * scale / tileHeight ) , 0.0f ) ;
						}
						else if ( ( yOffset - tileHeight * vertPieceCenter > wrapYLine )
								  && ! wrapYDown )
						{
							gl.glTranslatef( 0.0f , - ( height * scale / tileHeight ) , 0.0f ) ;
						}						
						
						// render the piece
						renderInPlane( gl , horzPieceSize , vertPieceSize , 1.0f , 
									   ( horzPieceSize * tileWidth ) , ( vertPieceSize * tileHeight ) , 
									   textureSize , ( ( h == 0 ) ? 0.0f : ( horzSplit * tileWidth ) ) ,
									   ( ( v == 0 ) ? 0.0f : ( vertSplit * tileHeight ) ) ) ;
						
						gl.glPopMatrix() ;
					}
					gl.glPopMatrix() ;
				}
				
				gl.glPopMatrix() ;

				xOffset += textureSize ;
				tileWidth = StrictMath.min( ( width - xOffset ) , textureSize ) ;
				// move to the location for the next tile
				// I figured this out somewhat by knowing the answers to the two important cases
				// ( tileSize = textureWidth and tileSize < textureWidth ) and intuition
				// in the first case we want that icky expression to evaluate to 1 
				// ( by guessing ) I figured out that 0.3 moves us over "one texture".  I'm sure this
				// makes sense given earlier scaling but it's hard for me to wrap my mind around
				// the state enough to explain it.
				// in the second case we have to remember that the image is acutally not on the left
				// side of the texture ( curses ) so we want to move over less than before 
				gl.glTranslatef( ( scale * ( 1 - ( ( ( (float) textureSize - (float) tileWidth ) / 
													 textureSize ) ) / 2 ) ) , 
								 0.0f , 0.0f ) ;
				i++ ;
			}
			gl.glPopMatrix() ;

			yOffset -= textureSize ;
			tileHeight = StrictMath.min( yOffset , textureSize ) ;
			// y is reversed
			gl.glTranslatef( 0.0f , 
							 - ( scale * ( 1 - ( ( ( (float) textureSize - (float) tileHeight ) / 
												   textureSize ) ) / 2 ) ) , 
							 0.0f ) ;
		}
		gl.glPopMatrix() ;
	}

    byte[] reuseTexture( GL gl , int tileWidth , int tileHeight , 
								   int xOffset , int yOffset , int width ,
								   int[] colors , byte[] bytes )
    {
		int i = 0 ;
		int size = ( tileHeight * tileWidth ) * 4 ;

		if( bytes == null || bytes.length != size )
		{
			bytes = new byte[ size ] ;
		}

		// The colors array is ARGB but from what I
		// understand from poking around on the web, there is no GL_*
		// constant corresponding to that type.  So we'll use GL_RGBA
		// and do the conversion from ARGB int[] to RGBA byte[]
		// ourselves.
		// Not positive this is the best/fastest code for this
		// task. - ST 2/9/05, 8/8/05
		// Also, you'd think that since patches don't have alpha, we
		// could save time by using GL_RGB in that case instead of
		// GL_RGBA.  But Esther and I tried it and we got weird results
		// where the patches with pxcor < pycor were OK, but the other
		// half of the patches were the wrong color.  Neither of us
		// could figure out why.  GL_RGB used to work fine, but that
		// was before we were using glTexSubImage2D (at the time we
		// were just making a new texture every time with glTexImage2D).
		// So we have given up and we just use RGBA all the time now.
		// - ST 8/8/05
		for( int x = xOffset ; x < ( tileWidth + xOffset ) ; x++ )
		{
			for( int y = yOffset ; y < ( tileHeight + yOffset ) ; y++ )
			{
				// I tried "optimizing" this by only shifting 8 bits at a time
				// with "rgb >>= 8", but it wasn't any faster - ST 8/8/05
				int rgb = colors[ ( y * width ) + x ] ;
				bytes[ i++ ] = (byte) ( rgb >> 16 ) ; // red
				bytes[ i++ ] = (byte) ( rgb >>  8 ) ; // green
				bytes[ i++ ] = (byte) ( rgb       ) ; // blue
				bytes[ i++ ] = (byte) ( rgb >> 24 ) ; // alpha
			}
		}

		// because the texture coords are reversed swap height and width
		// ev 5/25/05
		gl.glTexSubImage2D( GL.GL_TEXTURE_2D , 0 , 
							0 , 0 , tileHeight , tileWidth , 
							GL.GL_RGBA , GL.GL_UNSIGNED_BYTE , ByteBuffer.wrap( bytes ) ) ;

		return bytes ;
	}

	void renderEmptyPlane( GL gl , float sideX , float sideY , float sideZ )
	{
		sideX *= (Renderer.WORLD_SCALE /2 ) ;
		sideY *= (Renderer.WORLD_SCALE /2 ) ;
		sideZ *= (Renderer.WORLD_SCALE /2 ) ;
		
		gl.glColor4f( 0.0f , 0.0f , 0.0f, 0.5f ) ;
		gl.glBegin(GL.GL_QUADS) ;
		gl.glNormal3f( 0.0f , 0.0f , -1.0f ) ;
		gl.glVertex3f(  - sideX ,  + sideY ,  - sideZ ) ;
		gl.glVertex3f(  - sideX ,  - sideY ,  - sideZ ) ;
		gl.glVertex3f(  + sideX ,  - sideY ,  - sideZ ) ;
		gl.glVertex3f(  + sideX ,  + sideY ,  - sideZ ) ;
		gl.glEnd() ;

		// both sides of the quad. patches are always opaque from both sides
		gl.glColor4f( 0.0f , 0.0f , 0.0f, 0.5f ) ;
		gl.glBegin(GL.GL_QUADS) ;
		gl.glNormal3f( 0.0f , 0.0f , -1.0f ) ;
		gl.glVertex3f(  + sideX ,  + sideY ,  - sideZ ) ;
		gl.glVertex3f(  + sideX ,  - sideY ,  - sideZ ) ;
		gl.glVertex3f(  - sideX ,  - sideY ,  - sideZ ) ;
		gl.glVertex3f(  - sideX ,  + sideY ,  - sideZ ) ;
		gl.glEnd() ;
	}

	void renderInPlane( GL gl , float sideX , float sideY , float sideZ ,
								  float width , float height , float size ,
								  float offsetX , float offsetY )
	{		
		sideX *= ( Renderer.WORLD_SCALE / 2 ) ;
		sideY *= ( Renderer.WORLD_SCALE / 2 ) ;
		sideZ *= ( Renderer.WORLD_SCALE / 2 ) ;

		// set color; not quite sure I understand why this seems to matter,
		// given the texture; oh well - ST 2/9/05
		
		gl.glColor4f( 1.0f , 1.0f , 1.0f , 1.0f ) ;
		
		// the texture is bigger than the world, because of the power-of-2 rule,
		// so some of the pixels in the texture are unused, so we need to magnify
		// the texture so the unused pixels disappear
		float offsetStretchX = ( offsetX / size ) ;
		float offsetStretchY = ( offsetY / size ) ;
		float textureStretchX = ( width / size ) + offsetStretchX ;
		float textureStretchY = ( height / size ) + offsetStretchY ;
		
		gl.glBegin(GL.GL_QUADS) ;
		gl.glNormal3f( 0.0f , 0.0f , - 1.0f ) ;

		gl.glTexCoord2f( offsetStretchY , offsetStretchX ) ;
		gl.glVertex3f( - sideX , sideY , - sideZ ) ;

		gl.glTexCoord2f( textureStretchY , offsetStretchX ) ;
		gl.glVertex3f( - sideX , - sideY , - sideZ ) ;

		gl.glTexCoord2f( textureStretchY , textureStretchX ) ;
		gl.glVertex3f( sideX , - sideY , - sideZ ) ;

		gl.glTexCoord2f( offsetStretchY , textureStretchX ) ;
		gl.glVertex3f( sideX , sideY , - sideZ ) ;
			
		gl.glEnd() ;
	}

	void setParameters( GL gl , boolean blank )
	{
		if( ! blank )
		{
			// We want our patches to have nice sharp edges, not blurry fuzzy edges
			// (that would take longer to render anyway). - ST 2/9/05
			gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_NEAREST);
			gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_NEAREST);
			
			// Theoretically, if we do the scaling right, it won't matter
			// whether the texture is set to clamp or repeat, because the
			// texture will exactly fill its quad.  but just in case we're
			// slightly off, putting the texture in clamp mode seems like
			// it might possibly 1) avoid slightly weird appearance, and 2)
			// slightly improve performance.  So let's do it. - ST 2/9/05
			gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_S,GL.GL_CLAMP);
			gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_T,GL.GL_CLAMP);
		}

	}

    void makeTexture( GL gl , int size )
    {
		gl.glTexImage2D( GL.GL_TEXTURE_2D , 0 , GL.GL_RGBA ,
						 size , size ,
						 0 , GL.GL_RGBA , GL.GL_UNSIGNED_BYTE ,
						 null ) ;
	}

    static int genTexture( GL gl )
    {
		final int[] tmp = new int[ 1 ] ;
		gl.glGenTextures( 1 , IntBuffer.wrap( tmp ) ) ;
		return tmp[ 0 ] ;
    }
}
