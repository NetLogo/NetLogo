package org.nlogo.deltatick;

import javax.swing.*;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nlogo.deltatick.*;
import org.nlogo.deltatick.xml.Trait;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/21/12
 * Time: 8:00 PM
 * To change this template use File | Settings | File Templates.
 */

    // will need an array of tabs/panels so learners can open multiple libraries, and close any of them when they want
    //(Feb 21, 2012)
    //TODO: Get name of file for name of panel and ability to close tabs

public class LibraryHolder extends JPanel {
    private JTabbedPane tabbedPane;
    private int countTabs;
    private int currentTab;
    JComponent panel;
    JButton exit;
    ArrayList<JComponent> arrayPanels = new ArrayList<JComponent>();
    int policy;
    int newPolicy;
    // should eventually have an array of panels that you can add to and remove from (Feb 21, 2012)
    Tab tab;
    List<TraitBlock> traits = new LinkedList<TraitBlock>();;
    List<OperatorBlock> operatorBlocksList = new LinkedList<OperatorBlock>();
    ArrayList<String> variations;


    public LibraryHolder() {
        tabbedPane = new JTabbedPane();
        //policy = tabbedPane.getTabLayoutPolicy();
        //System.out.println("Before " + policy);
        //tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        //newPolicy = tabbedPane.getTabLayoutPolicy();
        //System.out.println("After " + newPolicy);
        add(tabbedPane);
        //exit = new JButton();
        //policy = tabbedPane.getTabLayoutPolicy();
        //System.out.println(policy);


        tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {

      }
        }   );

        countTabs = 0;
        currentTab = 0;
    }

     // This is called only for TraitBlock & operatorBlock
      //TODo This changes the name of the tab to "panel" (March 9)
    public void addLibrarytoTab( TraitBlock block ) {
       tabbedPane.addTab( "panel", panel );
       traits.add(block);
       //getVariation(block);

    }

    public void addOperatortoTab( OperatorBlock oBlock ) {
       tabbedPane.addTab( "panel", panel );
       operatorBlocksList.add(oBlock);
       //getVariation(block);

    }

    //TODO: Get rid of this hard coding (Feb 21, 2012)



    public void makeNewTab() {

        panel = (JComponent) new JPanel();
        panel.setLayout( new BoxLayout (panel, BoxLayout.Y_AXIS) );
        //tabbedPane.add( new JLabel ("name" + countTabs));
        /*
        tabbedPane.addTab( "name" + countTabs, panel);
        tabbedPane.setTabComponentAt( countTabs, new Tab(tabbedPane));
        */ //Commented out on March 1, 2012 (Aditi)
        tabbedPane.addTab( "name", panel );
        JButton close = new JButton();

        //arrayPanels.add(countTabs, panel);
        countTabs++;
       }


    public void addBlock ( CodeBlock block ) {
        panel.add ( block );
    }

    public void setTabName( String name ) {
        // TODO Change to using indexOfTabComponent
        //int i = tabbedPane.indexOfTabComponent(LibraryHolder.this);
        tabbedPane.setTitleAt((countTabs - 1), name);
    }

    public String[] getTrait() {
        String[] names = new String [traits.size()];
        int i = 0;
        for (TraitBlock trait : traits) {
            names[i] = trait.getTraitName();
            i++;
        }
        return names;
    }

    /*
    public String[] getVariation() {
        for (TraitBlock block : traits ) {
            String[] names = new String[block.varList.size()];
            for ( i = 0; i < block.varList.size(); i++ ) {
                names[i] = block.varList.get(i);
            }



            for (String string : block.varList) {
                variations = new String[block.varList.size()];
                variations.add(string);
                public int getSize() {
                return strings.length;
            }
            public Object getElementAt(int i) {
                return strings[i];
            }
            }
        }
        return variations;
    }
    */


}
