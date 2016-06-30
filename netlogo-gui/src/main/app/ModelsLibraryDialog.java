// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

// uses an index of the models in the Models Library and the first
// paragraph of the info tabs. we read this when we open the dialog
// instead of sifting through all the files at that time cause that's
// super slow. ev 3/26/09

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.HyperlinkEvent;

import org.nlogo.core.I18N;
import org.nlogo.swing.BrowserLauncher;
import org.nlogo.workspace.ModelsLibrary;

strictfp class ModelsLibraryDialog
    extends javax.swing.JDialog
    implements javax.swing.event.TreeSelectionListener,
    javax.swing.event.TreeExpansionListener {
  private static ModelsLibraryDialog me = null;

  public static URI open(java.awt.Frame parent)
      throws org.nlogo.awt.UserCancelException {
    if (me == null)  // NOPMD not threadsafe, but OK since it's event-thread-only
    {
      me = new ModelsLibraryDialog(parent);
      me.tree.setSelectionRow(0);
    }
    me.setVisible(true);
    if (me.sourceURI == null) {
      throw new org.nlogo.awt.UserCancelException();
    }
    return me.sourceURI;
  }

  public static String getModelPath() {
    return me.path;
  }

  //

  private final javax.swing.JTree tree;
  private final ModelPreviewPanel modelPreviewPanel;
  private final JTextField searchField;

  private Node selected = null;
  private URI sourceURI = null;
  private String path = null;

  private String searchText = null;
  private final List<javax.swing.tree.TreePath> savedExpandedPaths =
      new LinkedList<javax.swing.tree.TreePath>();

  Action openAction =
      new AbstractAction(I18N.guiJ().get("modelsLibrary.open")) {
        public void actionPerformed(ActionEvent e) {
          sourceURI = selected.getFileURI();
          setVisible(false);
        }
      };

  Action toggleOrOpenAction =
      new AbstractAction("toggle-or-open") {
        public void actionPerformed(ActionEvent e) {
          if (selected.isFolder()) {
            int row = tree.getSelectionRows()[0];
            if (tree.isExpanded(row)) {
              tree.collapseRow(row);
            } else {
              tree.expandRow(row);
            }
          } else if (openAction.isEnabled()) {
            openAction.actionPerformed(null);
          }
        }
      };

  Action cancelAction =
      new AbstractAction(I18N.guiJ().get("common.buttons.cancel")) {
        public void actionPerformed(ActionEvent e) {
          sourceURI = null;
          path = null;
          setVisible(false);
        }
      };

  Action communityAction =
      new AbstractAction(I18N.guiJ().get("modelsLibrary.community")) {
        public void actionPerformed(ActionEvent e) {
          org.nlogo.swing.BrowserLauncher.openURL
              (me, "http://ccl.northwestern.edu/netlogo/models/community/", false);
        }
      };

  Action focusSearchBoxAction =
      new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          searchField.requestFocusInWindow();
          searchField.selectAll();
        }
      };

  //

  private ModelsLibraryDialog(java.awt.Frame parent) {
    super(parent, I18N.guiJ().get("menu.file.modelsLibrary"), true);
    setResizable(true);
    KeyStroke findKeyStroke =
       KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(findKeyStroke, "focus-search-box");
    getRootPane().getActionMap().put("focus-search-box", focusSearchBoxAction);
    openAction.setEnabled(false);
    toggleOrOpenAction.setEnabled(false);

    // create subcomponents
    javax.swing.JButton communityButton = new javax.swing.JButton(communityAction);
    javax.swing.JButton selectButton = new javax.swing.JButton(openAction);
    javax.swing.JButton cancelButton = new javax.swing.JButton(cancelAction);
    getRootPane().setDefaultButton(selectButton);
    modelPreviewPanel = new ModelPreviewPanel();

    final SearchableModelTree[] smt = new SearchableModelTree[1];
    if (ModelsLibrary.needsModelScan() || smt[0] == null) {
      org.nlogo.swing.ModalProgressTask.apply(
        parent, I18N.guiJ().get("modelsLibrary.loading"),
        new Runnable() {
          public void run() {
            ModelsLibrary.scanForModels(false);
            smt[0] = new SearchableModelTree
              (new Node(ModelsLibrary.rootNode, ModelsLibraryIndexReader.readInfoMap()));
          }});
    }
    tree = new javax.swing.JTree(smt[0]);
    tree.getSelectionModel().setSelectionMode
        (javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setToggleClickCount(1);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    if (tree.getRowCount() > 1 &&
        ((Node) tree.getPathForRow(1).getLastPathComponent())
            .getName().equals("Curricular Models")) {
      tree.expandRow(1);
    }
    if (tree.getRowCount() > 0 &&
        ((Node) tree.getPathForRow(0).getLastPathComponent())
            .getName().equals("Sample Models")) {
      tree.expandRow(0);
    }
    if (tree.getRowCount() > 0 &&
        ((Node) tree.getPathForRow(0).getLastPathComponent())
            .getName().equals("3D")) {
      tree.expandRow(0);
    }
    javax.swing.InputMap inputMap = new javax.swing.InputMap();
    inputMap.setParent(tree.getInputMap());
    inputMap.put
        (KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
            "none"); // don't let space bar toggle selection
    inputMap.put
        (KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            "toggle-or-open");
    tree.setInputMap(javax.swing.JComponent.WHEN_FOCUSED, inputMap);
    tree.getActionMap().put("toggle-or-open", toggleOrOpenAction);
    org.nlogo.swing.Utils.addEscKeyAction(tree, inputMap, cancelAction);
    tree.addTreeSelectionListener(this);
    tree.addTreeExpansionListener(this);
    tree.addMouseListener
        (new java.awt.event.MouseAdapter() {
          @Override
          public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getClickCount() == 2 && openAction.isEnabled()) {
              openAction.actionPerformed(null);
            }
          }
        });

    // lay out content pane & bottom buttons

    javax.swing.Box topPanel =
        new javax.swing.Box(javax.swing.BoxLayout.X_AXIS);

    javax.swing.Box searchPanel =
        new javax.swing.Box(javax.swing.BoxLayout.X_AXIS);
    searchField = new JTextField("");
    searchField.getDocument().addDocumentListener(
        new javax.swing.event.DocumentListener() {
          public void changedUpdate(javax.swing.event.DocumentEvent e) {
            setSearchText(searchField.getText());
          }

          public void insertUpdate(javax.swing.event.DocumentEvent e) {
            setSearchText(searchField.getText());
          }

          public void removeUpdate(javax.swing.event.DocumentEvent e) {
            setSearchText(searchField.getText());
          }
        });
    searchField.addAncestorListener(new AncestorListener() {
        public void ancestorAdded(AncestorEvent e) { searchField.requestFocusInWindow(); }
        public void ancestorRemoved(AncestorEvent e) {}
        public void ancestorMoved(AncestorEvent e) {}
      });
    searchField.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_KP_UP:
            case KeyEvent.VK_KP_DOWN:
              tree.requestFocusInWindow();
          }
        }
      });
    searchField.setMaximumSize(new java.awt.Dimension(
        Short.MAX_VALUE,
        searchField.getMinimumSize().height));
    final javax.swing.JButton clearSearchButton = new javax.swing.JButton(I18N.guiJ().get("modelsLibrary.clear"));
    clearSearchButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        searchField.setText("");
      }
    });
    java.awt.Font buttonFont = clearSearchButton.getFont();
    clearSearchButton.setFont(buttonFont.deriveFont(buttonFont.getSize2D() - 2));
    searchPanel.add(
        new org.nlogo.swing.IconHolder
            (new javax.swing.ImageIcon
                (AboutWindow.class.getResource("/images/magnify.gif"))));
    searchPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(2, 0)));
    searchPanel.add(searchField);
    searchPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(2, 0)));
    searchPanel.add(clearSearchButton);
    searchPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));

    javax.swing.Box treePanel =
        new javax.swing.Box(javax.swing.BoxLayout.Y_AXIS);
    treePanel.add(new javax.swing.JScrollPane(tree));
    treePanel.add(searchPanel);

    topPanel.add(treePanel);
    topPanel.add(new javax.swing.JScrollPane(modelPreviewPanel));

    javax.swing.Box buttonPanel =
        new javax.swing.Box(javax.swing.BoxLayout.X_AXIS);
    buttonPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(40, 0)));
    buttonPanel.add(communityButton);
    buttonPanel.add(javax.swing.Box.createHorizontalGlue());
    if (System.getProperty("os.name").startsWith("Mac")) {
      buttonPanel.add(cancelButton);
      buttonPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(10, 0)));
      buttonPanel.add(selectButton);
    } else {
      buttonPanel.add(selectButton);
      buttonPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(10, 0)));
      buttonPanel.add(cancelButton);
    }
    buttonPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(40, 0)));
    buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));

    javax.swing.JPanel contentPane = new javax.swing.JPanel();
    contentPane.setLayout(new javax.swing.BoxLayout(
        contentPane,
        javax.swing.BoxLayout.Y_AXIS));
    contentPane.add(topPanel);
    contentPane.add(buttonPanel);
    setContentPane(contentPane);

    // add an Esc key handler
    org.nlogo.swing.Utils.addEscKeyAction(this, cancelAction);

    // handle close box
    setDefaultCloseOperation
        (javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener
        (new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e) {
            cancelAction.actionPerformed(null);
          }
        });

    this.setSize(740, 715);
    org.nlogo.awt.Positioning.center(this, parent);

    // This last bit is a fugly stopgap measure: the only way I found so far
    // to trigger a refresh of the modelPreviewPanel with the right size
    // calculations in order to fix https://github.com/NetLogo/NetLogo/issues/750
    // NP 2015-03-31
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        modelPreviewPanel.showModel();
      }
    });
  }

  //

  public String getSearchText() {
    return this.searchText;
  }

  public void setSearchText(String newText) {
    javax.swing.tree.TreePath savedSelectionPath = tree.getSelectionPath();
    boolean wasNull = (this.searchText == null);
    if (newText.length() == 0) {
      this.searchText = null;
    } else {
      this.searchText = newText.toUpperCase();
    }
    boolean isNull = (this.searchText == null);
    Node root = (Node) tree.getModel().getRoot();
    if (wasNull && (!isNull)) {
      savedExpandedPaths.clear();
      for (Enumeration<javax.swing.tree.TreePath> e =
               tree.getExpandedDescendants
                   (new javax.swing.tree.TreePath(root));
           e.hasMoreElements();) {
        savedExpandedPaths.add(e.nextElement());
      }
    }
    ((SearchableModelTree) tree.getModel()).reload(root);
    if (!isNull) {
      Thread expander = new Thread() {
        String text = searchText;

        @Override
        public void run() {
          for (int i = 0; i < tree.getRowCount(); i += 1) {
            if (!text.equals(searchText)) {
              // If the search text has changed, the tree will
              // be reloaded and another expander thread will be
              // created if necessary, so we can stop working
              // immediately.
              return;
            }
            // There is a very small chance that the number of rows
            // has changed in between our call to getRowCount above
            // and here. But the chance is very small and if we do
            // throw a NullPointerException it's not really a big
            // deal since the thread is going to die anyway once it
            // notices that the searchText has changed, so I just
            // don't think it's worth synchronizing. ER - 12/02/07
            final javax.swing.tree.TreePath path = tree.getPathForRow(i);
            Node node = (Node) path.getLastPathComponent();
            if (node.isFolder()) {
              try {
                // It's very bad to call a method that modifies
                // a component from a thread other than the
                // event dispatch thread once that component
                // has been realized on the screen, so we have
                // to use invokeAndWait for this.
                javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                  public void run() {
                    tree.expandPath(path);
                  }
                });
              } catch (java.lang.reflect.InvocationTargetException ex) {
                throw new IllegalStateException(ex);
              } catch (InterruptedException ex) {
                return;
              }
            }
          }
          javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              invalidate();
              validate();
            }
          });
        }
      };
      expander.start();
    } else if (!wasNull) {
      for (Iterator<javax.swing.tree.TreePath> i = savedExpandedPaths.iterator(); i.hasNext();) {
        tree.expandPath(i.next());
      }
      savedExpandedPaths.clear();
      if (savedSelectionPath != null) {
        tree.expandPath(savedSelectionPath);
        tree.setSelectionPath(savedSelectionPath);
        tree.scrollPathToVisible(savedSelectionPath);
      }
    }
  }

  private int[] searchTextIndices(String text) {
    if (searchText == null) {
      return null;
    }
    String ucText = text.toUpperCase();
    List<Integer> indices =
        new LinkedList<Integer>();
    int index = 0;
    while (true) {
      index = ucText.indexOf(searchText, index + 1);
      if (index > -1) {
        indices.add(Integer.valueOf(index));
      } else {
        break;
      }
    }
    int[] result = new int[indices.size()];
    int rIndex = 0;
    for (Iterator<Integer> i = indices.iterator(); i.hasNext();) {
      result[rIndex++] = i.next().intValue();
    }
    return result;
  }

  // Relayout when user expands or collapses a folder (since that changes
  // the JTree's preferred width.  You'd think you could just call
  // revalidate() on the JTree or the JScrollPane it's embedded in,
  // but neither works - ST 8/3/03

  // It's because JScrollPane returns true from isValidateRoot, so
  // validation of the component hierarchy stops when it hits the
  // scroll pane. This is an appropriate solution. - ER 12/02/07

  public void treeExpanded(javax.swing.event.TreeExpansionEvent e) {
    if (searchText == null) {
      invalidate();
      validate();
    }
  }

  public void treeCollapsed(javax.swing.event.TreeExpansionEvent e) {
    if (searchText == null) {
      invalidate();
      validate();
    }
  }

  //

  public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
    if (tree.getSelectionPath() == null) {
      selected = null;
      openAction.setEnabled(false);
      toggleOrOpenAction.setEnabled(false);
    } else {
      selected = (Node) tree.getSelectionPath().getLastPathComponent();
      openAction.setEnabled(!selected.isFolder());
      toggleOrOpenAction.setEnabled(true);
    }
    modelPreviewPanel.showModel();
    invalidate();
    if (tree.getSelectionPath() != null) {
      tree.scrollPathToVisible(tree.getSelectionPath());
    }
  }

  private strictfp class ModelPreviewPanel extends javax.swing.JPanel
    implements javax.swing.event.HyperlinkListener {

    private final GraphicsPreview graphicsPreview;
    private final javax.swing.JEditorPane textArea;

    ModelPreviewPanel() {
      setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

      graphicsPreview = new GraphicsPreview();
      graphicsPreview.setBorder
          (javax.swing.BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY, 1));

      textArea = new javax.swing.JEditorPane();
      textArea.setContentType("text/html");
      textArea.setEditable(false);
      textArea.setOpaque(false);
      textArea.addHyperlinkListener(this);

      add(graphicsPreview);
      add(textArea);
      add(javax.swing.Box.createVerticalGlue());

      setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    void showModel() {
      graphicsPreview.setImage
          (selected == null || selected.isFolder()
              ? null
              : ModelsLibrary.getImagePath(selected.getFilePath()));

      // This is a work-around for Java's inability to set a maximum
      // width for a JEditorPane and let the height be determined by
      // the content. Continues below.
      // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4765285
      // ER - 12/02/07
      textArea.setPreferredSize(null);
      textArea.setMaximumSize(new java.awt.Dimension(390, Short.MAX_VALUE));

      if (selected != null && !selected.isFolder()) {
        textArea.setVisible(true);
        graphicsPreview.setVisible(true);
        // HTMLify the model description
        textArea.setText(
            "<html>\n"
                + "<head>\n"
                + "<style>body {font-family: Dialog; font-size: 12pt; }</style>"
                + "</head>"
                + "<body>\n"
                + "<h2>" + selected.getName() + "</h2>\n"
                + selected.getInfo()
                + "</body>"
                + "</html>"
        );
        try {
          // Calling Document.getText() gives us the text
          // with the html tags removed.
          String text = textArea.getDocument().getText
              (0, textArea.getDocument().getLength());
          int[] indices = searchTextIndices(text);
          if (indices != null) {
            for (int i = 0; i < indices.length; i += 1) {
              // Could use a custom highlight painter here,
              // if we wanted to get fancy.
              textArea.getHighlighter().addHighlight
                  (indices[i],
                      indices[i] + searchText.length(),
                      javax.swing.text.DefaultHighlighter.DefaultPainter);
            }
          }
        } catch (javax.swing.text.BadLocationException ex) {
          throw new IllegalStateException(ex);
        }
        textArea.setCaretPosition(0);
      } else {
        // no preview to show, so show explanatory message instead
        String text = org.nlogo.util.Utils.getResourceAsString("/system/library.html");
        textArea.setText(text);
        textArea.setCaretPosition(0);
        graphicsPreview.setVisible(false);
      }

      // The conclusion of the above-mentioned work-around
      // ER - 12/02/07
      textArea.setPreferredSize(
          new java.awt.Dimension(
              390,
              textArea.getPreferredSize().height));
      invalidate();
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        if (e.getURL() == null) {
          JOptionPane.showMessageDialog(this, "Invalid URL!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
          BrowserLauncher.openURL(this, e.getURL().toString(), false);
        }
      }
    }
  }

  /** */
  private strictfp class SearchableModelTree
      extends javax.swing.tree.DefaultTreeModel {

    public SearchableModelTree(Node node) {
      super(node);
    }

    @Override
    public Object getChild(Object parent, int childIndex) {
      if (searchText == null) {
        return super.getChild(parent, childIndex);
      } else {
        Node node = (Node) parent;
        int index = -1;
        for (Enumeration<?> children = node.children();
             children.hasMoreElements();) {
          Node child = (Node) children.nextElement();
          if (child.isFolder()) {
            if (hasChildren(child)) {
              index += 1;
            }
          } else if (matchesSearchText(child)) {
            index += 1;
          }
          if (index == childIndex) {
            return child;
          }
        }
        return null;
      }
    }

    private boolean hasChildren(Object parent) {
      Node node = (Node) parent;
      if (node.isFolder()) {
        for (Enumeration<?> children = node.children();
             children.hasMoreElements();) {
          Node child = (Node) children.nextElement();
          if (child.isFolder()) {
            if (hasChildren(child)) {
              return true;
            }
          } else if (matchesSearchText(child)) {
            return true;
          }
        }
        return false;
      } else {
        return false;
      }
    }

    @Override
    public int getChildCount(Object parent) {
      if (searchText == null) {
        return super.getChildCount(parent);
      } else {
        Node node = (Node) parent;
        if (node.isFolder()) {
          int result = 0;
          for (Enumeration<?> children = node.children();
               children.hasMoreElements();) {
            Node child = (Node) children.nextElement();
            if (child.isFolder()) {
              if (getChildCount(child) > 0) {
                result += 1;
              }
            } else if (matchesSearchText(child)) {
              result += 1;
            }
          }
          return result;
        } else {
          return 0;
        }
      }
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
      if (searchText == null) {
        return super.getIndexOfChild(parent, child);
      } else {
        Node node = (Node) parent;
        int result = 0;
        for (Enumeration<?> children = node.children();
             children.hasMoreElements();) {
          Node c = (Node) children.nextElement();
          if (c.equals(child)) {
            return result;
          }
          if (c.isFolder()) {
            if (getChildCount(c) > 0) {
              result += 1;
            }
          } else if (matchesSearchText(c)) {
            result += 1;
          }
        }
        return -1;
      }
    }

    private boolean matchesSearchText(Node node) {
      return (node.getName().toUpperCase().indexOf(searchText) > -1) ||
          (node.getInfo().toUpperCase().indexOf(searchText) > -1) ||
          (node.getFilePath().toUpperCase().indexOf(searchText) > -1);
    }
  }

  static strictfp class Node
      extends javax.swing.tree.DefaultMutableTreeNode {

    private final String name;
    private final String path;
    private final String info;

    Node(ModelsLibrary.Node from, java.util.Map<String, String> infoMap) {
      this.name = from.getName();
      this.path = from.getFilePath();
      this.allowsChildren = from.getAllowsChildren();
      if (!allowsChildren) {
        String info =
            infoMap.get(path.substring(path.indexOf("models"))
                .replace(System.getProperty("file.separator"), "/"));
        if (info == null) {
          info = "";
        }

        // remove any leading non alpha-numeric characters
        while (info.length() > 0 && !Character.isLetterOrDigit(info.charAt(0))) {
          info = info.substring(1);
        }

        // All of the above the calls to String.substring and
        // String.trim will leave the char array containing the
        // entire model file contents hard-referenced, which could
        // potentially waste a lot of memory given the number of
        // models in the library. This will force Java to allocate
        // a new char array containing only the stuff we're
        // interested in saving. - ER 12/02/07
        // PMD doesn't like you to call the String constructor,
        // so we'll do it this way instead; the effect is exactly
        // the same - ER 12/03/07
        StringBuilder sb = new StringBuilder(info.length());
        sb.append(info);
        info = sb.toString();
        this.info = info;
      } else {
        this.info = "";
      }
      for (Enumeration<?> children = from.children();
           children.hasMoreElements();) {
        add(new Node((ModelsLibrary.Node) children.nextElement(), infoMap));
      }
    }

    public String getName() {
      return name;
    }

    public String getFilePath() {
      return path;
    }

    public URI getFileURI() {
      return new java.io.File(path).toURI();
    }

    public String getInfo() {
      return info;
    }

    @Override
    public String toString() {
      return name;
    }

    public boolean isFolder() {
      return allowsChildren;
    }

    @Override
    public boolean isLeaf() {
      return !allowsChildren;
    }
  }
}
