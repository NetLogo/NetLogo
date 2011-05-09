package org.nlogo.gl.view ;

import java.util.List;

import org.nlogo.api.Perspective;
import org.nlogo.gl.render.PickListener;
import org.nlogo.window.SyntaxColors;

class Picker implements PickListener, java.awt.event.ActionListener
{

	private final View view ;

	Picker( View view )
	{
		this.view = view ;
	}

	public void pick( java.awt.Point mousePt , List<org.nlogo.api.Agent> agents )
	{
		javax.swing.JPopupMenu menu = new org.nlogo.swing.WrappingPopupMenu() ;
		
		javax.swing.JMenuItem editItem = new javax.swing.JMenuItem( "Edit..." ) ;
		editItem.addActionListener
			( new java.awt.event.ActionListener() {
				public void actionPerformed( java.awt.event.ActionEvent e ) {
					new org.nlogo.window.Events.EditWidgetEvent
						( view.viewManager.workspace.viewWidget.settings() )
						.raise( view ) ;
				} } ) ;
		menu.add( editItem ) ;
		
		menu.add( new javax.swing.JPopupMenu.Separator() ) ;
		
		javax.swing.JMenuItem copyItem =
			new javax.swing.JMenuItem( "Copy View" ) ;
		copyItem.addActionListener
			( new java.awt.event.ActionListener() {
					public void actionPerformed( java.awt.event.ActionEvent e ) {
						java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents
							( new org.nlogo.awt.ImageSelection
							  ( view.exportView() ) ,
							  null ) ;
					} } ) ;
		menu.add( copyItem ) ;
		
		javax.swing.JMenuItem exportItem = new javax.swing.JMenuItem( "Export View..." ) ;
		exportItem.addActionListener
			( new java.awt.event.ActionListener() {
				public void actionPerformed( java.awt.event.ActionEvent e ) {
					view.viewManager.workspace.doExportView( view.viewManager ) ;
				} } ) ;
		menu.add( exportItem ) ;
		menu.add( new javax.swing.JPopupMenu.Separator() ) ;
		javax.swing.JMenuItem resetItem = 
			new javax.swing.JMenuItem( 
					"<html>" 
					+ org.nlogo.awt.Utils.colorize( 
							"reset-perspective" , 
							org.nlogo.window.SyntaxColors.COMMAND_COLOR )	
				) ;
		resetItem.addActionListener
			( new java.awt.event.ActionListener() {
				public void actionPerformed( java.awt.event.ActionEvent e ) {
					view.resetPerspective() ;
				} } ) ;
		menu.add( resetItem ) ;
		if ( view.viewManager.world.observer().atHome3D() )
		{
			resetItem.setEnabled ( false ) ;
			resetItem.setText( "reset-perspective" ) ; 
		}
		Class<? extends org.nlogo.api.Agent> last = null ;
		for( int i = 0 ; i < agents.size() ; i++ )
		{
			org.nlogo.api.Agent agent = agents.get( i ) ;
			
			if ( ( last == null ) || ! last.isInstance( agent ) )
			{
				menu.add( new javax.swing.JPopupMenu.Separator() ) ;
				last = agent.getClass() ;
			}
			
			if ( agent instanceof org.nlogo.agent.Turtle )
			{
				javax.swing.JMenu submenu = new AgentMenu( agent ) ;
				submenu.add( new AgentMenuItem( agent , AgentAction.INSPECT , "inspect" ) ) ;
				submenu.add( new javax.swing.JPopupMenu.Separator() ) ;
				submenu.add( new AgentMenuItem( agent , AgentAction.WATCH , "watch" ) ) ;
				submenu.add( new AgentMenuItem( agent , AgentAction.FOLLOW , "follow" ) ) ;
				submenu.add( new AgentMenuItem( agent , AgentAction.RIDE , "ride" ) ) ;
				
				menu.add( submenu );
			}
			else
			{
				menu.add( new AgentMenuItem( agent , AgentAction.INSPECT , "inspect" ) ) ;
			}
		}
		
		if( menu.getSubElements().length > 0 )
		{
			// move the menu over just a bit from the mouse point, it tends to
			// get in the way in 3D ev 5/12/06
			menu.show( view.canvas , (int) mousePt.getX() + 15 , 
					   (int) mousePt.getY() + 15 ) ;
		}
	}

	/// context menu

	private class AgentMenu
		extends javax.swing.JMenu
	{
		org.nlogo.api.Agent agent ;
		AgentAction action ;
		
		AgentMenu( org.nlogo.api.Agent agent )
		{
			super( agent.toString() ) ;
			this.agent = agent ;
		}
		
		@Override
		public void menuSelectionChanged( boolean isIncluded )
		{
			super.menuSelectionChanged( isIncluded ) ;
			view.renderer.outlineAgent( ( isIncluded ) ? agent : null ) ;
			view.signalViewUpdate();
		}
	}

	private enum AgentAction { INSPECT , FOLLOW , RIDE , WATCH }

	private class AgentMenuItem
		extends javax.swing.JMenuItem
	{
		org.nlogo.api.Agent agent ;
		AgentAction action ;
		boolean submenu ;
		
		AgentMenuItem( org.nlogo.api.Agent agent , AgentAction action , String caption )
		{
			super( "<html>"
				+ org.nlogo.awt.Utils.colorize( 
						caption , 
						SyntaxColors.COMMAND_COLOR )	
				+ " " 
				+ org.nlogo.awt.Utils.colorize( 
						agent.classDisplayName() , 
						SyntaxColors.REPORTER_COLOR )
				+ org.nlogo.awt.Utils.colorize( 
					agent.toString().substring( agent.classDisplayName().length() ) , 
					SyntaxColors.CONSTANT_COLOR )
				) ; 
			this.agent = agent ;
			this.action = action ;
			addActionListener( Picker.this ) ;
		}
		
		@Override
		public void menuSelectionChanged( boolean isIncluded )
		{
			super.menuSelectionChanged( isIncluded ) ;
			if ( ! submenu )
			{
				view.renderer.outlineAgent( ( isIncluded ) ? agent : null ) ;
				view.signalViewUpdate();
			}
		}
	}
	
	public void actionPerformed( java.awt.event.ActionEvent e )
	{
		AgentMenuItem item = (AgentMenuItem) e.getSource() ;
		org.nlogo.agent.Observer observer = view.viewManager.world.observer() ;
		switch( item.action )
		{
			case INSPECT: 
				view.viewManager.workspace.inspectAgent( item.agent , 3 ) ;
				return ; 
			case FOLLOW: 
				observer.setPerspective( Perspective.FOLLOW , item.agent ) ;
				int distance = (int)( (org.nlogo.agent.Turtle) item.agent ).size() * 5 ;
				observer.followDistance(
					StrictMath.max( 1 , StrictMath.min( distance , 100 ) ) ) ;
				break ;
			case RIDE: 
				observer.setPerspective( Perspective.RIDE , item.agent ) ;
				observer.followDistance( 0 ) ;
				break ;
			case WATCH:
				observer.home() ;
				observer.setPerspective( Perspective.WATCH , item.agent ) ;
				break ;
		    default:
				throw new IllegalStateException() ;
		}
		
		// update screen
		view.signalViewUpdate() ;
		view.updatePerspectiveLabel() ; 
	}
	
}
