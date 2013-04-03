package org.nlogo.deltatick;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.*;

/**
* Created by IntelliJ IDEA.
* User: aditiwagh
* Date: 3/8/13
* Time: 7:27 PM
* To change this template use File | Settings | File Templates.
*/
public class PopupMenu extends JPanel  {
    JMenuItem menuItem;
    JPopupMenu popup;



    public PopupMenu() {
        System.out.println("reaching popupmenu");
        popup = new JPopupMenu();
    ActionListener menuListener = new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        System.out.println("Popup menu item ["
            + event.getActionCommand() + "] was pressed.");
      }
    };
    JMenuItem item;
    popup.add(item = new JMenuItem("Histo", new ImageIcon("1.gif")));
    //item.setHorizontalTextPosition(JMenuItem.RIGHT);
    item.addActionListener(menuListener);
    popup.add(item = new JMenuItem("Center", new ImageIcon("2.gif")));
    item.setHorizontalTextPosition(JMenuItem.RIGHT);
    item.addActionListener(menuListener);
    popup.add(item = new JMenuItem("Right", new ImageIcon("3.gif")));
    item.setHorizontalTextPosition(JMenuItem.RIGHT);
    item.addActionListener(menuListener);
    popup.addPopupMenuListener(new PopupPrintListener());
    addMouseListener(new MousePopupListener());
        this.add(popup);
        this.setVisible(true);

        //createPopupMenu();
    }




class PopupListener extends MouseAdapter {
        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }


    }


  // An inner class to check whether mouse events are the popup trigger
  class MousePopupListener extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      checkPopup(e);
    }

    public void mouseClicked(MouseEvent e) {
      checkPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      checkPopup(e);
    }

    private void checkPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        popup.show(PopupMenu.this, e.getX(), e.getY());
      }
    }
  }

  // An inner class to show when popup events occur
  class PopupPrintListener implements PopupMenuListener {
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      System.out.println("Popup menu will be visible!");
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      System.out.println("Popup menu will be invisible!");
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
      System.out.println("Popup menu is hidden!");
    }
  }
}


