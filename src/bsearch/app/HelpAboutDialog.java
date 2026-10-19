package bsearch.app;

import bsearch.util.GeneralUtils;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

import org.nlogo.swing.BrowserLauncher;

public class HelpAboutDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;
    private JTextPane jTextPaneContent;
    private JScrollPane jScrollPane1;
    private JButton jButtonOk;
    private JButton jButtonWebsite;

    public HelpAboutDialog(JFrame frame, String title) {
        super(frame, title);

        jTextPaneContent = new JTextPane();
        jTextPaneContent.setContentType("text/plain");
        File creditsFile = new File(GeneralUtils.attemptResolvePathFromBSearchRoot("CREDITS.TXT"));
        File licFile = new File(GeneralUtils.attemptResolvePathFromBSearchRoot("LICENSE.TXT"));
        String creditsText, licText;
        try {
            creditsText = GeneralUtils.stringContentsOfFile(creditsFile);
            licText = GeneralUtils.stringContentsOfFile(licFile);
        } catch (FileNotFoundException ex)
        {
            creditsText = "ERROR: Either CREDITS.TXT or LICENSE.TXT file not found.";
            licText = "";
        }

        jTextPaneContent.setText("BehaviorSearch v" + GeneralUtils.getVersionString() + "\n" +
                creditsText + "\n*****\n\n"
                + licText);
        jTextPaneContent.setCaretPosition(0);
        jTextPaneContent.setEditable(false);

        jScrollPane1 = new JScrollPane(jTextPaneContent);
        getContentPane().add(jScrollPane1, BorderLayout.CENTER);

        JPanel panelSouth = new JPanel();
        getContentPane().add(panelSouth, BorderLayout.SOUTH);

        jButtonWebsite= new JButton("Browse BehaviorSearch web site");
        jButtonWebsite.setForeground(java.awt.Color.blue);
        panelSouth.add(jButtonWebsite);
        jButtonWebsite.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                BrowserLauncher.openURI(HelpAboutDialog.this, BrowserLauncher.makeURI(HelpAboutDialog.this, "http://www.behaviorsearch.org/"));
            }
        });

        jButtonOk = new JButton("Close");
        panelSouth.add(jButtonOk);
        jButtonOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
            }
        });



        // make sure the OK button always gets focus, so it's easy to close the help dialog.
        this.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                jButtonOk.requestFocusInWindow();
            }
        });


        this.pack();  // size the window based on the textpane's contents
        // but long lines of text can cause the size of the window to be ridiculous, so we fix it
        if (this.getWidth() > 640)
        {
            this.setSize(640, Math.min(this.getHeight() * 3 / 2, 440));
        }
        if (this.getHeight() > 440)
        {
            this.setSize(Math.min(this.getWidth() * 3 / 2, 640), 440);
        }
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public static void showAboutDialog(JFrame parent)
    {
        HelpAboutDialog hdialog = new HelpAboutDialog(parent, "About BehaviorSearch...");
        hdialog.setLocationRelativeTo(null);
        hdialog.setVisible(true);
    }


}
