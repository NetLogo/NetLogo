// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import org.nlogo.api.I18N;
import org.nlogo.swing.NonemptyTextFieldActionEnabler;
import org.nlogo.swing.NonemptyTextFieldButtonEnabler;

import javax.swing.JDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;

public strictfp class FindDialog
    extends JDialog
    implements java.awt.event.ActionListener {
  /// STATIC CODE

  private static FindDialog instance;

  public static void init(java.awt.Frame frame) {
    instance = new FindDialog(frame);
  }

  public static FindDialog getInstance() {
    if (instance == null) {
      throw new IllegalStateException("FindDialog was never initialized");
    }
    return instance;
  }

  public static void watch(javax.swing.text.JTextComponent target) {
    FIND_ACTION.setEnabled(true);
    FindDialog findInstance = getInstance();
    findInstance.target = target;
    findInstance.setReplaceEnabled(target.isEditable());
  }

  public static void dontWatch(javax.swing.text.JTextComponent target) {
    FindDialog findInstance = getInstance();
    findInstance.setVisible(false);
    FIND_ACTION.setEnabled(false);
  }

  /// ACTIONS

  public static final javax.swing.Action FIND_ACTION = new FindAction();
  public static final javax.swing.Action FIND_NEXT_ACTION = new FindNextAction();

  public static class FindAction
      extends javax.swing.text.TextAction {
    FindAction() {
      super(I18N.guiJ().get("menu.edit.find"));
      putValue
          (javax.swing.Action.SMALL_ICON,
              new javax.swing.ImageIcon
                  (FindDialog.class.getResource
                      ("/images/magnify.gif")));
      setEnabled(false);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      FindDialog.getInstance().setVisible(true);
      FindDialog.getInstance().findBox.requestFocus();
      FindDialog.getInstance().findBox.selectAll();
      // Setting find field by default to selected text
      FindDialog findDialog = getInstance();
      String selectedText = findDialog.target.getSelectedText();
      if(selectedText != null){
        FindDialog.getInstance().findBox.setText(selectedText);
      }
      FindDialog.getInstance().setLocation
          (instance.owner.getLocation().x + instance.owner.getWidth()
              - instance.getPreferredSize().width,
              instance.owner.getLocation().y + instance.owner.getHeight() / 2
                  - instance.getPreferredSize().height / 2);
      FindDialog.getInstance().notFoundLabel.setVisible(false);
    }
  }

  public static class FindNextAction
      extends javax.swing.text.TextAction {
    FindNextAction() {
      super(I18N.guiJ().get("menu.edit.findNext"));
      putValue
          (javax.swing.Action.SMALL_ICON,
              new javax.swing.ImageIcon
                  (FindDialog.class.getResource
                      ("/images/magnify.gif")));
      setEnabled(false);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      String search = getInstance().findBox.getText();
      if (!getInstance().next(search, getInstance().ignoreCaseCheckBox.isSelected(), getInstance().wrapAroundCheckBox.isSelected())) {
        java.awt.Toolkit.getDefaultToolkit().beep();
      }
    }
  }


  /// INSTANCE CODE

  //     the text component to search
  protected javax.swing.text.JTextComponent target;

  // gui compoenents
  private final javax.swing.JButton nextButton, prevButton;
  NonemptyTextFieldButtonEnabler nextEnabler, prevEnabler;
  private final javax.swing.JButton replaceButton, replaceAndFindButton, replaceAllButton;
  NonemptyTextFieldButtonEnabler replaceEnabler, replaceAndFindEnabler, replaceAllEnabler;
  private final javax.swing.JCheckBox ignoreCaseCheckBox;
  private final javax.swing.JCheckBox wrapAroundCheckBox;
  private final javax.swing.JTextField findBox;
  private final javax.swing.JTextField replaceBox;
  private final javax.swing.JLabel replaceLabel;
  private final javax.swing.JLabel findLabel;
  private final javax.swing.JLabel notFoundLabel;
  private final Frame owner;

  private FindDialog(Frame owner) {
    super(owner, I18N.guiJ().get("dialog.find.title"), false);
    this.owner = owner;
    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);

    // initialize GUI elements

    // text fields
    org.nlogo.swing.TextFieldBox findPanel =
        new org.nlogo.swing.TextFieldBox(javax.swing.SwingConstants.LEFT);

    findBox = new org.nlogo.swing.TextField(25);
    findBox.setEditable(true);
    findLabel = new javax.swing.JLabel(I18N.guiJ().get("dialog.find.find"));

    replaceBox = new org.nlogo.swing.TextField(25);
    replaceBox.setEditable(true);
    replaceLabel = new javax.swing.JLabel(I18N.guiJ().get("dialog.find.replaceWith"));

    // options panel elements
    ignoreCaseCheckBox = new javax.swing.JCheckBox(I18N.guiJ().get("dialog.find.ignoreCase"), true);
    wrapAroundCheckBox = new javax.swing.JCheckBox(I18N.guiJ().get("dialog.find.wrapAround"), true);
    notFoundLabel = new javax.swing.JLabel(I18N.guiJ().get("dialog.find.notFound"));
    notFoundLabel.setFont(notFoundLabel.getFont().deriveFont(java.awt.Font.BOLD));
    notFoundLabel.setForeground(java.awt.Color.RED);

    notFoundLabel.setVisible(false);

    // buttons
    nextButton = new javax.swing.JButton(I18N.guiJ().get("dialog.find.next"));
    prevButton = new javax.swing.JButton(I18N.guiJ().get("dialog.find.previous"));
    replaceAndFindButton = new javax.swing.JButton(I18N.guiJ().get("dialog.find.replaceAndFind"));
    replaceButton = new javax.swing.JButton(I18N.guiJ().get("dialog.find.replace"));
    replaceAllButton = new javax.swing.JButton(I18N.guiJ().get("dialog.find.replaceAll"));

    getRootPane().setDefaultButton(nextButton);

    nextButton.addActionListener(this);
    prevButton.addActionListener(this);
    replaceAndFindButton.addActionListener(this);
    replaceButton.addActionListener(this);
    replaceAllButton.addActionListener(this);

    nextEnabler = new NonemptyTextFieldButtonEnabler(nextButton);
    nextEnabler.addRequiredField(findBox);
    new NonemptyTextFieldActionEnabler(FIND_NEXT_ACTION).addRequiredField(findBox);
    prevEnabler = new NonemptyTextFieldButtonEnabler(prevButton);
    prevEnabler.addRequiredField(findBox);
    replaceEnabler = new NonemptyTextFieldButtonEnabler(replaceAndFindButton);
    replaceEnabler.addRequiredField(findBox);
    replaceAndFindEnabler = new NonemptyTextFieldButtonEnabler(replaceButton);
    replaceAndFindEnabler.addRequiredField(findBox);
    replaceAllEnabler = new NonemptyTextFieldButtonEnabler(replaceAllButton);
    replaceAllEnabler.addRequiredField(findBox);

    org.nlogo.swing.Utils.addEscKeyAction
        (this,
            new javax.swing.AbstractAction() {
              public void actionPerformed(java.awt.event.ActionEvent e) {
                setVisible(false);
              }
            });

    javax.swing.JPanel buttonPanel =
        new org.nlogo.swing.ButtonPanel
            (new javax.swing.JComponent[]
                {
                    nextButton,
                    prevButton,
                    replaceAndFindButton,
                    replaceButton,
                    replaceAllButton
                }
            );

    layout.setHorizontalGroup(layout
        .createSequentialGroup()
        .addGroup(layout.createParallelGroup()
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, // TRAILING makes everything right alinged
                layout.createSequentialGroup()
                    .addGap(replaceLabel.getPreferredSize().width
                        - findLabel.getPreferredSize().width)
                    .addComponent(findLabel)
                    .addComponent(findBox))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                layout.createSequentialGroup()
                    .addComponent(replaceLabel)
                    .addGroup(layout.createParallelGroup()
                        .addComponent(replaceBox)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING,
                            layout.createSequentialGroup()
                                .addComponent(ignoreCaseCheckBox)
                                .addComponent(wrapAroundCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(notFoundLabel))))
            .addGroup(layout.createSequentialGroup()
            .addComponent(nextButton)
            .addComponent(prevButton)
            .addComponent(replaceAndFindButton)
            .addComponent(replaceButton)
            .addComponent(replaceAllButton))
        )
    );
      // Uncommnet to make all the buttons of same size
//    layout.linkSize(SwingConstants.HORIZONTAL, findButton, replaceButton, replaceAllButton, replaceAndFindButton, previousButton);

    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(findLabel)
            .addComponent(findBox))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(replaceLabel)
            .addComponent(replaceBox))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(ignoreCaseCheckBox)
            .addComponent(wrapAroundCheckBox)
            .addComponent(notFoundLabel))
        .addGroup(layout.createParallelGroup()
            .addComponent(nextButton)
            .addComponent(prevButton)
            .addComponent(replaceAndFindButton)
            .addComponent(replaceButton)
            .addComponent(replaceAllButton))
    );

    getContentPane().setLayout(layout);

    pack();
    setResizable(false);
    setVisible(false);

  }

  public void actionPerformed(ActionEvent e) {
    String search = findBox.getText();

    if (e.getSource().equals(nextButton) || e.getSource().equals(findBox)) {
      if (!next(search, ignoreCaseCheckBox.isSelected(), wrapAroundCheckBox.isSelected())) {
        java.awt.Toolkit.getDefaultToolkit().beep();
        notFoundLabel.setVisible(true);
      } else {
        notFoundLabel.setVisible(false);
      }
    } else if (e.getSource().equals(prevButton)) {
      boolean ok =
          prev(search, ignoreCaseCheckBox.isSelected(), wrapAroundCheckBox.isSelected());
      if (!ok) {
        java.awt.Toolkit.getDefaultToolkit().beep();
        notFoundLabel.setVisible(true);
      } else {
        notFoundLabel.setVisible(false);
      }
    } else if (e.getSource().equals(replaceAndFindButton)) {
      // Stores if the Replace and Find is selected for the first time.
      // Also checks if the text in find field and selected text are same or not.
      boolean firstTime = target.getSelectedText() == null || (target.getSelectedText() != null
                && !(ignoreCaseCheckBox.isSelected() ? target.getSelectedText().equalsIgnoreCase(findBox.getText())
                : target.getSelectedText().equals(findBox.getText())));
      if (!firstTime) {
          replace(replaceBox.getText());
      }
      if (!next(search, ignoreCaseCheckBox.isSelected(), wrapAroundCheckBox.isSelected())) {
        java.awt.Toolkit.getDefaultToolkit().beep();
        notFoundLabel.setVisible(true);
      } else {
        notFoundLabel.setVisible(false);
      }
    } else if (e.getSource().equals(replaceButton)) {
      replace(replaceBox.getText());
    } else if (e.getSource().equals(replaceAllButton)) {
      replaceAll(search, ignoreCaseCheckBox.isSelected(), replaceBox.getText());
    } else {
      notFoundLabel.setVisible(false);
    }
  }

  private boolean next(String search, boolean ignoreCase, boolean wrapAround) {
    int start = target.getSelectionEnd();
    int matchIndex = -1;

    String text = getTargetText();

    if (ignoreCase) {
      // this might get slow with big programs. should be tested. -AZS
      search = search.toUpperCase();
      text = text.toUpperCase();
    }

    matchIndex = text.indexOf(search, start);

    if (matchIndex == -1 && wrapAround) {
      text = text.substring(0, start);
      matchIndex = text.indexOf(search);
    }

    if (matchIndex > -1) {
      target.setSelectionStart(matchIndex);
      target.setSelectionEnd(matchIndex + search.length());
      return true;
    } else {
      return false;
    }
  }

  private boolean prev(String search, boolean ignoreCase, boolean wrapAround) {
    int start = StrictMath.max(0, target.getSelectionStart() - 1);
    int matchIndex = -1;

    String text = getTargetText();

    if (ignoreCase) {
      // this might get slow with big programs. should be tested. -AZS
      search = search.toUpperCase();
      text = text.toUpperCase();
    }

    matchIndex = text.lastIndexOf(search, start);

    if (matchIndex == -1 && wrapAround) {
      text = text.substring(start, text.length());
      if (matchIndex != -1) {
        matchIndex = start + text.lastIndexOf(search);
      }
    }

    if (matchIndex > -1) {
      target.setSelectionStart(matchIndex);
      target.setSelectionEnd(matchIndex + search.length());
      return true;
    } else {
      return false;
    }
  }

  private void replace(String replacement) {
    if (target.getSelectedText() == null || target.getSelectedText().length() == 0) {
      java.awt.Toolkit.getDefaultToolkit().beep();
      return;
    }

    try {
      target.getDocument().remove(
          target.getSelectionStart(),
          target.getSelectionEnd() - target.getSelectionStart()
      );
      target.getDocument().insertString(target.getCaretPosition(), replacement, null);
    } catch (javax.swing.text.BadLocationException ex) {
      java.awt.Toolkit.getDefaultToolkit().beep();
    }
  }

  private int replaceAll(String search, boolean ignoreCase, String replacement) {
    target.setSelectionStart(0);
    target.setSelectionEnd(0);
    int j = 0;
    if (next(search, ignoreCase, false)) {
      j = 1;
      do {
        replace(replacement);
        j++;

        // arbitrary redundant check to avoid infinite loop
        if (j > 50000) {
          throw new IllegalStateException("Replace All replaced too many items.");
        }
      }
      // never wrap around on replace all
      while (next(search, ignoreCase, false));
    }
    return j;
  }

  private String getTargetText() {
    String text;

    if (target instanceof javax.swing.JEditorPane) {
      // we need to get the text this way to avoid returning the HTML
      // tags which screw-up the search - jrn 7/22/05
      try {
        text = target.getText(0, target.getDocument().getLength());
      } catch (javax.swing.text.BadLocationException ex) {
        throw new IllegalStateException(ex);
      }
    } else {
      text = target.getText();
    }
    return text;
  }

  // Disables/Enables all the replace buttons/textfield
  protected void setReplaceEnabled(boolean enabled) {
    replaceEnabler.setEnabled(enabled);
    replaceAndFindEnabler.setEnabled(enabled);
    replaceAllEnabler.setEnabled(enabled);
    replaceBox.setEnabled(enabled);
    replaceLabel.setEnabled(enabled);
  }
}
