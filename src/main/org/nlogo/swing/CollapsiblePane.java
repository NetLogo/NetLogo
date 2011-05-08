package org.nlogo.swing;

public strictfp class CollapsiblePane
	extends javax.swing.JPanel
{
	private final IconHolder open ;
	private final IconHolder closed ;
	private final javax.swing.JComponent element ;
	private final javax.swing.JWindow parent ;

	public CollapsiblePane( javax.swing.JComponent element , javax.swing.JWindow parent )
	{
		this.element = element ;
		this.parent = parent ;
		open = new org.nlogo.swing.IconHolder
			( new javax.swing.ImageIcon
			  ( CollapsiblePane.class.getResource( "/images/popup.gif" ) ) ) ;
		closed = new org.nlogo.swing.IconHolder
			( new javax.swing.ImageIcon
			  ( CollapsiblePane.class.getResource( "/images/closedarrow.gif" ) ) ) ;
		open.addMouseListener
			( new java.awt.event.MouseAdapter() {
					@Override
					public void mouseClicked( java.awt.event.MouseEvent e ) {
						setCollapsed( true ) ;
					} }	) ;	
		closed.addMouseListener
			( new java.awt.event.MouseAdapter() {
					@Override
					public void mouseClicked( java.awt.event.MouseEvent e ) {
						setCollapsed( false ) ;
					} }	) ;
		setLayout( new java.awt.BorderLayout() ) ;
		add( open , java.awt.BorderLayout.NORTH ) ;
		add( element , java.awt.BorderLayout.CENTER ) ;
		setBorder( javax.swing.border.LineBorder.createGrayLineBorder() ) ;
	}

	public void setCollapsed( boolean collapsed )
	{
		element.setVisible( ! collapsed ) ;
		if( collapsed )
		{
			remove( open ) ;
			add( closed , java.awt.BorderLayout.NORTH ) ;
		}
		else
	    {
			remove( closed ) ;
			add( open , java.awt.BorderLayout.NORTH ) ;
		}
		parent.pack() ;
	}

	public boolean isCollapsed()
	{
		return element.isVisible() ;
	}
}
