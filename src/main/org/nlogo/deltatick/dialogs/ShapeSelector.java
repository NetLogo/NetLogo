package org.nlogo.deltatick.dialogs;

import org.nlogo.api.Shape;
import org.nlogo.api.ShapeList;
import org.nlogo.deltatick.BreedBlock;
import org.nlogo.shape.DrawableShape;
import org.nlogo.shape.VectorShape;
import org.nlogo.shape.editor.DrawableList;
import org.nlogo.shape.editor.ImportDialog;
import org.nlogo.shape.editor.ManagerDialog;
import org.nlogo.shape.editor.ShapeCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 13, 2010
 * Time: 10:54:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShapeSelector
	extends javax.swing.JDialog
	implements javax.swing.event.ListSelectionListener {

	//final ManagerDialog manager ;
	final DrawableList list ;
    ShapeSelector myself = this;

	public ShapeSelector( java.awt.Frame frame ,
				         String [] shapes ,
				         BreedBlock shapeParser )
	{
		// The Java 1.1 version of Swing doesn't allow us to pass a JDialog as the first arg to
		// the JDialog constructor, hence the necessity of passing in the frame instead - ST 3/24/02
        super( frame , "Change shape for " + shapeParser.getName() , true ) ;
		//this.manager = manager;

		List<Shape> importedShapes = shapeParser.parseShapes( shapes , null ) ;
		if( importedShapes == null )
		{
			list = null ;
			// we should have already displayed an error
			dispose() ;
			return ;
		}
		List<Shape> foreignShapes = ShapeList.sortShapes( importedShapes ) ;
		if ( foreignShapes == null )
		{
			list = null ;
			dispose() ;					// Importing failed, so quit
			return ;
		}
		else
		{
			ShapeList shapeList = new ShapeList() ;
			shapeList.replaceShapes( foreignShapes ) ;
			list = new DrawableList( shapeList , null , 10 , 34 ) ;
			list.setParent( this ) ;
			list.setCellRenderer( new ShapeCellRenderer( list ) ) ;
			list.update() ;
		}

		// Create the buttons
		javax.swing.JButton importButton =
			new javax.swing.JButton( "Use this shape" ) ;
		importButton.addActionListener
			( new java.awt.event.ActionListener() {
					public void actionPerformed( java.awt.event.ActionEvent e ) {
						myself.setVisible(false);
					} } ) ;
		javax.swing.Action cancelAction =
			new javax.swing.AbstractAction( "Cancel" ) {
				public void actionPerformed( java.awt.event.ActionEvent e ) {
					dispose() ;
				} } ;
		javax.swing.JButton cancelButton = new javax.swing.JButton( cancelAction ) ;
		org.nlogo.swing.Utils.addEscKeyAction
			( this , cancelAction ) ;

		list.addMouseListener( new javax.swing.event.MouseInputAdapter() {
				// Listen for double-clicks, and edit the selected shape
				@Override
				public void mouseClicked(java.awt.event.MouseEvent e)
				{
					if( e.getClickCount() > 1 )
					{
						myself.setVisible(false);
					}
				} } ) ;

		// Setup the panel
		javax.swing.JPanel panel = new org.nlogo.swing.ButtonPanel
		( new javax.swing.JButton[] { importButton, cancelButton } ) ;

		// Create the scroll pane where the list will be displayed
		javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane( list ) ;

		// Add everything to the window
		getContentPane().setLayout( new java.awt.BorderLayout(0, 10) ) ;
		getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER ) ;
		getContentPane().add(panel, java.awt.BorderLayout.SOUTH ) ;

		pack() ;

		// Set the window location
		//setLocation( manager.getLocation().x + 10, manager.getLocation().y + 10 ) ;

		// set the default button
		getRootPane().setDefaultButton( importButton ) ;
	}

	// Listen for changes in list selection, and make the edit and delete buttons inoperative if necessary
	public void valueChanged(javax.swing.event.ListSelectionEvent e)
	{
		int[] selected = list.getSelectedIndices() ;
		if (selected.length == 1)
		{
			list.ensureIndexIsVisible(selected[0]) ;
		}
	}

		// Now update the shapes manager's list and quit this window

	// Show a warning dialog to indicate something went wrong when importing
	void sendImportWarning(String message)
	{
		javax.swing.JOptionPane.showMessageDialog
			(this, message, "Import", javax.swing.JOptionPane.WARNING_MESSAGE) ;
	}

	public interface ShapeParser
	{
		List<Shape> parseShapes( String [] shapes , String version ) ;
	}

	@Override
	public java.awt.Dimension getPreferredSize()
	{
		java.awt.Dimension d = super.getPreferredSize() ;
		d.width = StrictMath.max( d.width , 260 ) ;
		return d ;
	}

    public String getChosenShape() {
		return list.getSelectedValue().toString();
    }

    public VectorShape getShape() {
        return (VectorShape) list.getShape( list.getSelectedIndex() );
    }

    public int getChosenValue() {
        return list.getSelectedIndex();
    }
}
