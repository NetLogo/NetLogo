package org.nlogo.gl.view;

import javax.media.opengl.GLCanvas;

import org.nlogo.api.CompilerException;
import org.nlogo.gl.render.ExportRenderer;
import org.nlogo.gl.render.Renderer;
import org.nlogo.gl.render.PickListener;

abstract strictfp class View
	extends java.awt.Frame
	implements org.nlogo.window.Event.LinkChild
{
	final ViewManager viewManager ;
	Renderer renderer ;
	javax.media.opengl.GLCanvas canvas ;
	protected final MouseMotionHandler inputHandler ; 
	private final PickListener picker = new Picker( this ) ;

	enum Mode { ORBIT , ZOOM , TRANSLATE , INTERACT }

	View( String title, ViewManager viewManager , Renderer renderer )
	{
		super( title ) ;
		
		this.viewManager = viewManager ; 
		
		if( org.nlogo.api.Version.is3D() )
		{
			if( renderer == null )
			{
				this.renderer = new org.nlogo.gl.render.Renderer3D
					( viewManager.world , 
					  viewManager.graphicsSettings() , 
					  viewManager.workspace , viewManager ) ;
			}
			else
			{
				renderer.cleanUp() ;
				this.renderer = new org.nlogo.gl.render.Renderer3D( renderer ) ;
			}
		}
		else
		{
			if( renderer == null )
			{
				this.renderer = new Renderer
					( viewManager.world , 
					  viewManager.graphicsSettings() , 
					  viewManager.workspace , viewManager ) ;
			}
			else
			{
				renderer.cleanUp() ;
				this.renderer = new org.nlogo.gl.render.Renderer( renderer ) ;
			}
		}				

		inputHandler = new MouseMotionHandler( this ) ;
		createCanvas( viewManager.antiAliasingOn() ) ;
		setLayout( new java.awt.BorderLayout() ) ;
		add( canvas , java.awt.BorderLayout.CENTER ) ;
		canvas.setCursor( new java.awt.Cursor( java.awt.Cursor.CROSSHAIR_CURSOR ) ) ;
	}
	
	public void updatePerspectiveLabel() {}
	
	private void createCanvas( boolean antiAliasing )
	{
		javax.media.opengl.GLCapabilities capabilities =
			new javax.media.opengl.GLCapabilities() ;
		
		capabilities.setSampleBuffers( antiAliasing ) ;
		capabilities.setNumSamples( 4 ) ;
		capabilities.setStencilBits( 1 ) ;

		canvas = new GLCanvas( capabilities ) ;
		
		canvas.addGLEventListener( renderer ) ;		

      	canvas.addMouseListener( inputHandler ) ;
      	canvas.addMouseMotionListener( inputHandler ) ;
		canvas.addMouseWheelListener( inputHandler ) ;
      	canvas.addKeyListener( new KeyInputHandler() ) ;
	}
	
	class KeyInputHandler extends java.awt.event.KeyAdapter {
		@Override
		public void keyPressed( final java.awt.event.KeyEvent e )
		{
			if ( e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE )
			{
				viewManager.setFullscreen( false ) ;
			}
		}
	}

	public Object getLinkParent()
	{
		return viewManager ;
	}

	void updateRenderer()
	{
		renderer.update() ;
	}

	public void setVisible()
	{
		super.setVisible( true ) ;
		toFront() ;
		canvas.requestFocus() ;
	}

	public void display()
	{
		canvas.display() ; 
	}

	public void invalidateTurtleShape( String shape )
	{
		renderer.invalidateTurtleShape( shape ) ;
	}
	
	public void invalidateLinkShape( String shape )
	{
		renderer.invalidateLinkShape( shape ) ;
	}
	
	protected void signalViewUpdate()
	{
		canvas.repaint() ;	
	}

	public void resetPerspective()
	{
		viewManager.world.observer().resetPerspective() ;
		signalViewUpdate() ;
		updatePerspectiveLabel() ;
	}

	public java.awt.image.BufferedImage exportView()
	{
		ExportRenderer exporter = renderer.createExportRenderer() ;
		canvas.addGLEventListener( exporter ) ;
		canvas.display() ;
		canvas.removeGLEventListener( exporter ) ;

		java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage
			( canvas.getWidth() , canvas.getHeight() , java.awt.image.BufferedImage.TYPE_INT_ARGB);
 
		bufferedImage.setRGB(0, 0, canvas.getWidth() , canvas.getHeight() , 
							 exporter.pixelInts() , 0, canvas.getWidth() );

		return bufferedImage ;
	}

	// From interface Editable
	public CompilerException error() { return null ; }
	
	/// properties

	public boolean editFinished()
	{
		renderer.cleanUp() ;
		display() ;
		return true ;
	}
	
	protected void doPopup( java.awt.event.MouseEvent e )
	{
		renderer.queuePick( e.getPoint() , picker ) ;
		e.consume() ;
	}
	
}

