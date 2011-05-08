package org.nlogo.swing ;

public strictfp class ModalProgressTask
{
	public ModalProgressTask( java.awt.Frame parent , Runnable r , String message )
	{
		org.nlogo.awt.Utils.mustBeEventDispatchThread() ;

		// set up dialog
		final javax.swing.JDialog dialog = new javax.swing.JDialog( parent , true ) ;
		dialog.setResizable( false ) ;
		dialog.setUndecorated( true ) ;

		// make components
		javax.swing.JLabel label =
			new javax.swing.JLabel( message , javax.swing.SwingConstants.CENTER ) ;
		javax.swing.JProgressBar progressBar = new javax.swing.JProgressBar() ;
		progressBar.setIndeterminate( true ) ;

		// lay out dialog
		javax.swing.JPanel panel = new javax.swing.JPanel() ;
		panel.setBorder
			( javax.swing.BorderFactory.createEmptyBorder( 15 , 20 , 15 , 20 ) ) ;
		panel.setLayout( new java.awt.BorderLayout( 0 , 8 ) ) ;
		panel.add( label , java.awt.BorderLayout.NORTH ) ;
		panel.add( progressBar , java.awt.BorderLayout.SOUTH ) ;
		dialog.getContentPane().setLayout( new java.awt.BorderLayout() ) ;
		dialog.getContentPane().add( panel , java.awt.BorderLayout.CENTER ) ;
		dialog.pack() ;
		org.nlogo.awt.Utils.center( dialog , parent ) ;

		// start the boss thread and show the dialog
		Thread bossThread = new Boss( r , dialog ) ;
		bossThread.setPriority( Thread.MAX_PRIORITY ) ;
		bossThread.start() ;
		dialog.setVisible( true ) ;
	}
}

strictfp class Boss
	extends Thread
{
	private final Runnable r ;
	private final javax.swing.JDialog dialog ;
	public Boss( Runnable r , javax.swing.JDialog dialog )
	{
		super( "ModalProgressTask boss" ) ;
		this.r = r ;
		this.dialog = dialog ;
	}
	@Override public void run()
	{
		try
		{
			while( ! dialog.isVisible() )
			{
				sleep( 50 ) ;
			}
			org.nlogo.awt.Utils.invokeAndWait( r ) ;
		}
		catch( InterruptedException e )
		{
			// ignore
		}
		finally
		{
			dialog.setVisible( false ) ;
			dialog.dispose() ;
		}
	}
}
