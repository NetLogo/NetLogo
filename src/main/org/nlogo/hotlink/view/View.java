package org.nlogo.hotlink.view;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.event.ChangeEvent;

public class View extends javax.swing.JPanel
					implements javax.swing.event.ChangeListener {
	//private List<BufferedImage> imageList;
	private Image image = null ;
	int currentImageIndex;
    ViewPanel viewPanel;

    //private String tmpDir = System.getProperty("java.io.tmpdir") + "dthl/";

    //File fileSystem = new File("tmp");
	
	public View( ViewPanel viewPanel ) {
        this.viewPanel = viewPanel;
	    setBackground( java.awt.Color.BLACK ) ;
	    setOpaque( true ) ;
        // TODO: set preferred size to the dimension of the image
	    setPreferredSize( new java.awt.Dimension( 250 , 250 ) );
	    setMinimumSize( getPreferredSize() ) ;
	    setMaximumSize( getPreferredSize() ) ;
	}

    public int getImageCount() {
        java.io.File root = new java.io.File("tmp/");
        int max = 0;
        for( java.io.File dir : root.listFiles() ) {
            if( dir.isDirectory() ) {
                if( dir.listFiles().length > max ) {
                    max = dir.listFiles().length;
                }
            }
        }
        return max;
    }

    public int getCurrentImageCount() {
        return (new java.io.File("tmp/" + Integer.toString(viewPanel.getPlotTab().getFocusRunNumber()) )).listFiles().length;
    }

    public int getCurrentImage() {
        // so that it lines up with timeslider, which starts at 1
        return currentImageIndex;
    }
	
	public void renew() {
		showImage(0);
		this.paintComponent(this.getGraphics());
	}

	@Override
	public void paintComponent( java.awt.Graphics g )
	{
	    if( image == null )
		{
		    super.paintComponent( g ) ;
		}
	    else
		{
		    ( (java.awt.Graphics2D) g ).setRenderingHint
			( java.awt.RenderingHints.KEY_RENDERING ,
			  java.awt.RenderingHints.VALUE_RENDER_QUALITY ) ;
		    int width = image.getWidth( null ) ;
		    int height = image.getHeight( null ) ;
		    if( width == height )
			{
			    g.drawImage( image , 0 , 0 , 250 , 250 , this ) ;
			}
		    //TODO this other stuff isn't necessary if I stay at 400x400.
		    // but i don't think i want to.
		    else if( width > height )
			{
                g.drawImage ( image ,
					          0 , 0 ,
					          (int) ( width * ( 250.0 / height ) ) , 250 ,
					          this ) ;
			}
		    else // width < height                                                                                                                                      
			{
		    	g.drawImage ( image ,
		    			      0 , 0 ,
		    			      250 , (int) ( height * ( 250.0 / width ) ) ,
		    			      this ) ;
			}
		}
	}

	public void stateChanged(ChangeEvent e) {
		javax.swing.JSlider caller = (javax.swing.JSlider) e.getSource();
    	showImage( caller.getValue() );
    }
	
	public void play() {
		//while(  )
	}

    public void showImage( int index ) {
        //System.out.println("show image " + index);
        if( getImageCount() > 0 ) {
            image = org.nlogo.awt.Utils.loadImageFile("tmp/" + Integer.toString( viewPanel.getPlotTab().getFocusRunNumber() ) + "/" + Integer.toString( index ), false);
            currentImageIndex = index;
        }
        repaint();
    }
}