package org.nlogo.widget ;

import java.awt.event.MouseWheelListener;

import org.nlogo.agent.BooleanConstraint;
import org.nlogo.window.MultiErrorWidget;
import org.nlogo.window.Widget;
import org.nlogo.window.Events;
import org.nlogo.window.InterfaceColors;

public abstract strictfp class Switch
	extends MultiErrorWidget
	implements MouseWheelListener ,
	org.nlogo.window.Events.AfterLoadEvent.Handler
{

	// The constraint defines the default value and ensures the global
	// variable is always a boolean.  We use it here to track wether
	// we are "on" or not. -- CLB
	protected BooleanConstraint constraint = new BooleanConstraint() ;

	// sub-elements of Switch
	protected final Channel channel = new Channel() ;
	protected final Dragger dragger = new Dragger() ;

	// visual parameters
	protected static final int BORDERX = 3 ;
	protected static final int BORDERY = 3 ;
	protected static final int MINWIDTH = 90 ;
	public static final int CHANNEL_WIDTH = 15 ;
	public static final int CHANNEL_HEIGHT = 28 ;
	protected static final int MINHEIGHT = CHANNEL_HEIGHT + 5 ;
	
	public Switch()
	{
		setBackground( InterfaceColors.SWITCH_BACKGROUND ) ;
		setBorder( widgetBorder() ) ;
		setOpaque( true ) ;
		org.nlogo.awt.Utils.adjustDefaultFont( this ) ;
		add( dragger ) ;
		add( channel ) ;
		addMouseWheelListener( this );
		addMouseListener
			( new java.awt.event.MouseAdapter() {
					@Override
					public void mousePressed( java.awt.event.MouseEvent e ) {
						new Events.InputBoxLoseFocusEvent().raise( Switch.this ) ;
					} } ) ;

	}

	public boolean isOn() { return constraint.defaultValue().booleanValue() ; }
	public void isOn( boolean on )
	{
		if( isOn()== on )
		{
			return ;
		}
		constraint.defaultValue_$eq( on ? Boolean.TRUE : Boolean.FALSE ) ;
		updateConstraints() ;
		doLayout() ;
	}

	// don't send an event unless the name of the variable
	// defined changes, which is the only case in which we 
	// want a recompile. ev 6/15/05
	protected boolean nameChanged = false ;
	protected String name = "" ;
	public String name() { return name ; }
	public void name( String name )
	{
		this.name = name ;
		displayName(name) ;
		repaint() ;
	}

	@Override
	public void updateConstraints()
	{
		if (name().length() > 0 )
		{
			new org.nlogo.window.Events.AddBooleanConstraintEvent
				(name, isOn() ? Boolean.TRUE : Boolean.FALSE )
				.raise( this ) ;
		}
	}

	@Override
	public java.awt.Dimension getPreferredSize( java.awt.Font font )
	{
		java.awt.FontMetrics fontMetrics = getFontMetrics( font ) ;
		int height 
			= (fontMetrics.getMaxDescent() + fontMetrics.getMaxAscent() )
			+ 2*BORDERY ;
		int width
			= 6*BORDERX + channel.getWidth()
			+ fontMetrics.stringWidth(displayName() )
			+ fontMetrics.stringWidth("Off") ;
		return new java.awt.Dimension( StrictMath.max( MINWIDTH , width ) ,
									   StrictMath.max( MINHEIGHT , height ) ) ;
	}

	@Override
	public java.awt.Dimension getMinimumSize()
	{
		return new java.awt.Dimension( MINWIDTH , MINHEIGHT ) ;
	}

	@Override
	public java.awt.Dimension getMaximumSize()
	{
		return new java.awt.Dimension( 10000 , MINHEIGHT ) ;
	}

	@Override
	public void doLayout ()
	{
		super.doLayout() ;
		float scaleFactor = (float) getHeight() / (float) MINHEIGHT ;
		channel.setSize
			( (int) ( CHANNEL_WIDTH * scaleFactor ) ,
			  (int) ( CHANNEL_HEIGHT * scaleFactor ) ) ;
		channel.setLocation
			( BORDERX ,
			  ( getHeight() - channel.getHeight() ) / 2 ) ;
		dragger.setSize
			( (int) ( channel.getWidth() * 0.9 ) ,
			  (int) ( channel.getHeight() * 0.35 ) ) ;
		dragger.setLocation
			( BORDERX + ( channel.getWidth() - dragger.getWidth() ) / 2 ,
			  channel.getY() +
			  ( isOn()
				? (int) ( 0.1 * channel.getHeight() )
				: ( channel.getHeight() -
					dragger.getHeight() -
					(int) ( 0.1 * channel.getHeight() ) ) ) ) ;
	}

	@Override
	public void paintComponent( java.awt.Graphics g )
	{
		super.paintComponent( g ) ;
		java.awt.FontMetrics fontMetrics = g.getFontMetrics() ;
		int stringAscent = fontMetrics.getMaxAscent() ;

		java.awt.Rectangle controlRect = channel.getBounds() ;

		g.setColor(getForeground() ) ;
		g.drawString( "On" ,
					  controlRect.width + BORDERX , 
					  ( getHeight() - (2 * stringAscent ) - ( 2 * BORDERY ) ) / 2 + stringAscent + 1 ) ;

		g.drawString( "Off" ,
					  controlRect.width + BORDERX ,
					  ( getHeight() - (2 * stringAscent ) - ( 2 * BORDERY ) ) / 2 + 2 * stringAscent + 1 ) ;

		int controlLabelWidth =
			StrictMath.max( fontMetrics.stringWidth( "On" ) , fontMetrics.stringWidth( "Off" ) ) +
			controlRect.width + 2 * BORDERX ;
		g.setColor( getForeground() ) ;
		g.drawString( org.nlogo.awt.Utils.shortenStringToFit
					  ( displayName() , getWidth() - 3 * BORDERX - controlLabelWidth , fontMetrics ) ,
					  controlLabelWidth + 2 * BORDERX ,
					  ( getHeight() - fontMetrics.getHeight() - ( 2 * BORDERY ) ) / 2 + stringAscent + 1 ) ;
	}

	///
	public void mouseWheelMoved(java.awt.event.MouseWheelEvent e)
	{
		if( e.getWheelRotation() >= 1)
		{
			isOn( false ) ; 
		}
		else
		{
			isOn( true ) ;
		}
	
	}
	
	protected strictfp class Dragger
		extends javax.swing.JPanel
	{
		Dragger()
		{
			setBackground( InterfaceColors.SWITCH_HANDLE ) ;
			setBorder( org.nlogo.swing.Utils.createWidgetBorder() ) ;
			setOpaque( true ) ;
			addMouseListener
				( new java.awt.event.MouseAdapter() {
						@Override
						public void mousePressed( java.awt.event.MouseEvent e ) {
							new Events.InputBoxLoseFocusEvent().raise( Switch.this ) ;
							isOn( ! isOn() ) ;
						}
					} ) ;
		}
	}

	///

	protected strictfp class Channel
		extends javax.swing.JComponent
	{
		Channel()
		{
			setOpaque( false ) ;
			setBackground
				( org.nlogo.awt.Utils.mixColors
				  ( InterfaceColors.SWITCH_BACKGROUND , java.awt.Color.BLACK , 0.5 ) ) ;
			addMouseListener
				( new java.awt.event.MouseAdapter() {
						@Override
						public void mousePressed( java.awt.event.MouseEvent e ) {
							new Events.InputBoxLoseFocusEvent().raise( Channel.this ) ;
							if( org.nlogo.awt.Utils.button1Mask( e ) )
							{
								isOn( ! isOn() ) ;
							}
						} } ) ;
		}
		@Override
		public void paintComponent( java.awt.Graphics g )
		{
			int x = (int) ( getWidth() * 0.2 ) ;
			int y = (int) ( getHeight() * 0.1 ) ;
			int width = (int) ( getWidth() * 0.6 ) ;
			int height = (int) ( getHeight() * 0.8 ) ;
			g.setColor( getBackground() ) ;
			g.fillRect( x , y , width , height ) ;
			org.nlogo.swing.Utils.createWidgetBorder()
				.paintBorder( this , g , x , y , width , height ) ;
		}
	}

}
