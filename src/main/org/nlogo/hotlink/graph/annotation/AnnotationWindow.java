package org.nlogo.hotlink.graph.annotation;

import org.nlogo.hotlink.dialogs.ShapeIcon;
import org.nlogo.hotlink.dialogs.StackedShapeIcon;
import org.nlogo.app.PlotTab;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JWindow;
import javax.swing.JTextPane;

import javax.swing.JToggleButton;
import org.nlogo.hotlink.main.MainWindow;

import org.nlogo.shape.DrawableShape;

class AnnotationWindow extends JWindow
                        implements KeyListener {
    //String annotationText;
    Annotation annotation;
    JTextPane textPane = new JTextPane();
    Highlighter highlighter;
    private JPanel breedPanel = new JPanel();
    private JButton colorButton = new JButton();
    private JButton exitButton = new JButton();
    private JPanel menuPanel = new JPanel();
    private JScrollPane jScrollPane1 = new JScrollPane();

    /* ****
       * The Mighty Constructors
       ****/

    // when popping up to be populated with text
    public AnnotationWindow( PlotTab plotTab ,
                             Annotation annotation,
                             Point point ) {
        super( (JFrame) plotTab.getAppFrame() );
        this.annotation = annotation;

        // TODO: hack
        //if( mainWindow.getModelReader() != null ) {
            //initComponents( mainWindow.getModelReader().getModelBreedShapes() );
        //} else {
        initComponents( plotTab.getBreedShapes() );
        //}


        setSize( new Dimension( 200 , 100 ));
        setLocation( ((Double) point.getX()).intValue() + 50 ,
                     ((Double) point.getY()).intValue() + 50 );

        textPane.addKeyListener( annotation ); // to save on enter
        textPane.addKeyListener( this ); // to close on enter

        setVisible(true);
        System.out.println("is this getting called?");
    }

    // when text is provided because annotations are being loaded
    AnnotationWindow( PlotTab plotTab,
                      Annotation annotation,
                      String annotationText ,
                      Point point ) {
        super( (JFrame) plotTab.getAppFrame() );
        this.annotation = annotation;

        initComponents( plotTab.getBreedShapes() );
        //initComponents( null );

        setSize( new Dimension( 200 , 100 ));
        setLocation( ((Double) point.getX()).intValue() + 50 ,
                     ((Double) point.getY()).intValue() + 50 );

        textPane.setText(annotationText);
        textPane.addKeyListener( annotation ); // to save on enter
        textPane.addKeyListener( this ); // to close on enter
        
        setVisible(false);
    }

    // if enter is pressed, the
    public void keyPressed(KeyEvent e) {
        if( e.getKeyCode() == KeyEvent.VK_ENTER ) {
            annotation.setText(textPane.getText());
            setVisible(false);
        }
    }
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    public void show( Point point ) {
        System.out.println("show called");
        textPane.setBackground( new Color ( 255 , 255 , 255 , 50 ));
        textPane.setText(annotation.getText());
        this.setBackground( new Color( 255, 255, 255, 50 ) );
        setLocation( ((Double) point.getX()).intValue() + 50 ,
                     ((Double) point.getY()).intValue() + 50 );
        setVisible(true);
    }

    void initComponents( ArrayList<DrawableShape> shapes ) {
        // editor code - more features!
        java.awt.GridBagConstraints gridBagConstraints;

        //setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        textPane.setBackground(new Color(255, 255, 255, 50));
        textPane.setBorder(null);
        jScrollPane1.setViewportView(textPane);
        jScrollPane1.setBorder(null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        menuPanel.setBackground(new java.awt.Color(255, 255, 255));
        menuPanel.setLayout(new java.awt.GridBagLayout());

        colorButton.setAction( colorAction );
        colorButton.setBackground(new java.awt.Color(255, 255, 255));
        colorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nlogo/hotlink/images/Color.jpg"))); // NOI18N
        colorButton.setBorder(null);
        colorButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nlogo/hotlink/images/ColorRO.jpg")));
        colorButton.setBorderPainted(false);
        colorButton.setMargin(new java.awt.Insets(1, 2, 0, 2));
        menuPanel.add(colorButton, new java.awt.GridBagConstraints());
        menuPanel.add( new JSeparator( JSeparator.VERTICAL ) );

        org.jdesktop.layout.GroupLayout breedPanelLayout = new org.jdesktop.layout.GroupLayout(breedPanel);
        FlowLayout layout = new FlowLayout();
        layout.setHgap(2);
        layout.setVgap(2);
        breedPanel.setLayout(layout);
        breedPanelLayout.setHorizontalGroup(
            breedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 91, Short.MAX_VALUE)
        );
        breedPanelLayout.setVerticalGroup(
            breedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 20, Short.MAX_VALUE)
        );

        if( shapes != null ) { // TODO: hack
            for( DrawableShape shape : shapes ) {
                breedPanel.add( new PopulationButton( shape , false ) );
                breedPanel.add( new PopulationButton( shape , true  ) );
            }
        }
        breedPanel.setBackground(new java.awt.Color(255, 255, 255));

        menuPanel.add(breedPanel, new java.awt.GridBagConstraints());
        menuPanel.add( new JSeparator( JSeparator.VERTICAL ) );

        exitButton.setAction(deleteAction);
        exitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nlogo/hotlink/images/Exit.png"))); // NOI18N
        exitButton.setBorder(null);
        exitButton.setBorderPainted(false);
        exitButton.setMargin(new java.awt.Insets(5, 5, 5, 5));
        exitButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nlogo/hotlink/images/ExitRO.png"))); // NOI18N

        menuPanel.add(exitButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(menuPanel, gridBagConstraints);

        pack();
    }

    private final javax.swing.Action colorAction =
		new javax.swing.AbstractAction() {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
            	Color colorReturned = JColorChooser.showDialog(null, "Pick an annotation color...", java.awt.Color.pink.darker().darker() );
                annotation.setColor(colorReturned);
                annotation.annotator.myPlot.refresh();
        }
    };

    private final javax.swing.Action deleteAction =
		new javax.swing.AbstractAction() {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
            	annotation.annotator.removeAnnotation(annotation);
        }
    };

    class PopulationButton extends JToggleButton {
        PopulationButton( DrawableShape shape , boolean multi ) {
            super();

            this.setRolloverEnabled(true);

            
            if( multi ) {
                this.setIcon( new StackedShapeIcon( shape , java.awt.Color.gray ) );
            } else {
                this.setIcon( new ShapeIcon( shape ) );
            }

            this.setFocusPainted(false);
            this.setSize(25, 25);
        }

    }

/*
    class ExitButton extends JButton {
        ExitButton() {
            super();
        }
    }

    class DeleteButton extends JButton {
        DeleteButton() {
            super();

            setBackground(new java.awt.Color(255, 255, 255, 70));
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Exit.png"))); // NOI18N
            setBorder(null);
            //jButton2.setMaximumSize(new java.awt.Dimension(20, 20));
            //jButton2.setMinimumSize(new java.awt.Dimension(20, 20));
            //jButton2.setPreferredSize(new java.awt.Dimension(20, 20));
            setRolloverEnabled(true);
            setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ExitRO.png"))); // NOI18N
        }
    }*/
}