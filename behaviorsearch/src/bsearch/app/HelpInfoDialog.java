package bsearch.app;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

public class HelpInfoDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;
    private JTextPane jTextPaneContent;
    private JScrollPane jScrollPane1;
    private JButton jButtonOk;

    public HelpInfoDialog(JFrame frame, String title, String htmlText) {
        super(frame, title);

        jTextPaneContent = new JTextPane();
        jTextPaneContent.setContentType("text/html");
        jTextPaneContent.setText(htmlText);
        jTextPaneContent.setEditable(false);
        jTextPaneContent.setMaximumSize(new java.awt.Dimension(620,600));

        jScrollPane1 = new JScrollPane(jTextPaneContent);
        getContentPane().add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.setMaximumSize(new java.awt.Dimension(620,600));

        jButtonOk = new JButton("OK");
        getContentPane().add(jButtonOk, BorderLayout.SOUTH);
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

    public static void showHelp(JFrame parent, String title, String htmlText)
    {
        HelpInfoDialog hdialog = new HelpInfoDialog(parent, title, htmlText);
        hdialog.setLocationRelativeTo(null);
        hdialog.setVisible(true);
    }


}
