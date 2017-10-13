// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

// uses an index of the models in the Models Library and the first
// paragraph of the info tabs. we read this when we open the dialog
// instead of sifting through all the files at that time cause that's
// super slow. ev 3/26/09

import java.awt.{ Color, Dimension, Frame, Toolkit }
import java.awt.event.{ ActionEvent, ActionListener, KeyAdapter, KeyEvent, MouseAdapter, MouseEvent,
  WindowAdapter, WindowEvent }
import java.io.File
import java.net.URI
import java.util.{ Enumeration, LinkedList, List => JList }
import javax.swing.{ AbstractAction, Action, Box, BorderFactory, BoxLayout,
  InputMap, JButton, JComponent, JDialog, JEditorPane, JLabel, JOptionPane, JPanel,
  JScrollPane, JTextField, JTree, KeyStroke, SwingUtilities, WindowConstants }
import javax.swing.text.{ BadLocationException, DefaultHighlighter }
import javax.swing.tree.{ DefaultMutableTreeNode, DefaultTreeModel, TreePath, TreeSelectionModel }
import javax.swing.event.{ AncestorEvent, AncestorListener, DocumentEvent, DocumentListener,
  HyperlinkEvent, HyperlinkListener, TreeExpansionEvent, TreeExpansionListener,
  TreeSelectionEvent, TreeSelectionListener }

import org.nlogo.core.I18N
import org.nlogo.api.{ FileIO, Version }
import org.nlogo.awt.Positioning
import org.nlogo.swing.{ BrowserLauncher, ModalProgressTask, Utils },
  Utils.{ addEscKeyAction, icon }
import org.nlogo.workspace.ModelsLibrary

import scala.util.Try

import scala.language.implicitConversions

object ModelsLibraryDialog {
  def create(parent: Frame, version: Version): Option[ModelsLibraryDialog] = {
    var dialog: Option[ModelsLibraryDialog] = None
    var error: String = ""
    ModalProgressTask.onUIThread(parent, I18N.gui.get("modelsLibrary.loading"), { () =>
      try {
        dialog = buildRootNode(version).map { node => new ModelsLibraryDialog(parent, node) }
      } catch {
        case e: Exception =>
          println(e.getMessage)
          error = e.getMessage
          e.printStackTrace()
      }
    })
    if (dialog.isEmpty)
      JOptionPane.showMessageDialog(parent,
        I18N.gui.getN("modelsLibrary.error.unableToLoad", error),
        "NetLogo", JOptionPane.ERROR_MESSAGE)
    dialog
  }

  // this *is* called on the background thread. It's probably a bad idea
  // to call it on the UI thread, as it performs many file operations in the background
  private def buildRootNode(version: Version): Option[Node] = {
    ModelsLibrary.scanForModels(version, false)
    val crossReferencedNode =
      for {
        rootNode@ModelsLibrary.Tree(_, _, _) <- ModelsLibrary.rootNodes.get(version)
        xRefConfig <- ModelCrossReferencer.loadConfig()
      } yield ModelCrossReferencer.applyConfig(rootNode, xRefConfig)
    (crossReferencedNode orElse ModelsLibrary.rootNodes.get(version))
      .map(node => new Node(node, ModelsLibraryIndexReader.readInfoMap))
  }


  class Node(from: ModelsLibrary.Node, infoMap: Map[String, String]) extends DefaultMutableTreeNode {
    this.allowsChildren = from.isFolder

    val name: String = translateNameForDisplay(from.name)
    val path: String = from.path

    private def translateNameForDisplay(name: String): String =
      if (name.equalsIgnoreCase("UNVERIFIED"))
        I18N.gui.get("modelsLibrary.unverified")
      else
        removeSuffix(name)

    private def removeSuffix(reference: String): String =
      if (reference.endsWith(".nlogo"))
        reference.substring(0, reference.lastIndexOf(".nlogo"))
      else if (reference.endsWith(".nlogo3d"))
        reference.substring(0, reference.lastIndexOf(".nlogo3d"))
      else
        reference

    val info: String = {
      if (allowsChildren) ""
      else {
        val _info  =
          infoMap.get(
            path.substring(path.indexOf("models"))
              .replace(System.getProperty("file.separator"), "/"))
            .getOrElse("")

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
        val sb = new StringBuilder(_info.length)
        sb.append(_info)
        sb.toString
      }
    }

    locally {
      from match {
        case ModelsLibrary.Tree(_, _, children) =>
          children.foreach { child =>
            add(new Node(child, infoMap))
          }
        case _ =>
      }
    }

    def isFolder: Boolean = allowsChildren
    def getFileURI: URI = new File(path).toURI

    override def isLeaf: Boolean = !allowsChildren
    override def toString: String = name
  }

  implicit def asNodeIterator[A](enum: Enumeration[A]): Iterator[Node] = {
    import scala.collection.JavaConverters._
    enum.asScala.collect { case n: Node => n }
  }
}

import ModelsLibraryDialog._

class ModelsLibraryDialog(parent: Frame, node: Node)
  extends JDialog(parent, I18N.gui.get("menu.file.modelsLibrary"), true)
  with TreeSelectionListener
  with TreeExpansionListener {

  private var selected = Option.empty[Node]
  private[nlogo] var sourceURI = Option.empty[URI]
  private val savedExpandedPaths: JList[TreePath] = new LinkedList[TreePath]()
  private val searchField: JTextField = new JTextField("");
  private var searchText = Option.empty[String]

  private val modelPreviewPanel: ModelPreviewPanel = new ModelPreviewPanel()

  private val tree = new JTree(new SearchableModelTree(node))
  tree.setSelectionRow(0)

  private val openAction: Action =
    new AbstractAction(I18N.gui.get("modelsLibrary.open")) {
      def actionPerformed(e: ActionEvent): Unit = {
        sourceURI = selected.map(_.getFileURI)
        setVisible(false)
      }
    }

  private val toggleOrOpenAction: Action =
    new AbstractAction("toggle-or-open") {
      def actionPerformed(e: ActionEvent): Unit = {
        if (selected.exists(_.isFolder)) {
          val row = tree.getSelectionRows()(0)
          if (tree.isExpanded(row)) {
            tree.collapseRow(row)
          } else {
            tree.expandRow(row)
          }
        } else if (openAction.isEnabled()) {
          openAction.actionPerformed(null)
        }
      }
    }

  private val cancelAction: Action =
    new AbstractAction(I18N.gui.get("common.buttons.cancel")) {
      def actionPerformed(e: ActionEvent) {
        sourceURI = None
        setVisible(false)
      }
    }

  private val communityAction: Action =
    new AbstractAction(I18N.gui.get("modelsLibrary.community")) {
      def actionPerformed(e: ActionEvent): Unit = {
        val uri = BrowserLauncher.makeURI(ModelsLibraryDialog.this,
          "http://ccl.northwestern.edu/netlogo/models/community/")
        if (uri != null) {
          BrowserLauncher.openURI(ModelsLibraryDialog.this, uri)
        }
      }
    }

  private val focusSearchBoxAction: Action =
    new AbstractAction() {
      def actionPerformed(e: ActionEvent) {
        searchField.requestFocusInWindow()
        searchField.selectAll()
      }
    }

  locally {
    setResizable(true)
    val findKeyStroke =
       KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit.getMenuShortcutKeyMask)
    getRootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(findKeyStroke, "focus-search-box");
    getRootPane.getActionMap.put("focus-search-box", focusSearchBoxAction)
    openAction.setEnabled(false)
    toggleOrOpenAction.setEnabled(false)

    // create subcomponents
    val communityButton = new JButton(communityAction)
    val selectButton = new JButton(openAction)
    val cancelButton = new JButton(cancelAction)
    getRootPane.setDefaultButton(selectButton)
    tree.getSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
    tree.setToggleClickCount(1)
    tree.setRootVisible(false)
    tree.setShowsRootHandles(true)
    if (tree.getRowCount > 1 &&
      tree.getPathForRow(1).getLastPathComponent.asInstanceOf[Node].name == "Curricular Models") {
        tree.expandRow(1)
    }
    if (tree.getRowCount > 0 &&
      tree.getPathForRow(0).getLastPathComponent.asInstanceOf[Node].name == "Sample Models") {
      tree.expandRow(0)
    }
    if (tree.getRowCount > 0 &&
      tree.getPathForRow(0).getLastPathComponent.asInstanceOf[Node].name == "3D") {
      tree.expandRow(0)
    }
    val inputMap = new InputMap()
    inputMap.setParent(tree.getInputMap)
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "none") // don't let space bar toggle selection
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "toggle-or-open")
    tree.setInputMap(JComponent.WHEN_FOCUSED, inputMap)
    tree.getActionMap().put("toggle-or-open", toggleOrOpenAction)
    addEscKeyAction(tree, inputMap, cancelAction)
    tree.addTreeSelectionListener(this)
    tree.addTreeExpansionListener(this)
    tree.addMouseListener(
      new MouseAdapter() {
        override def mouseClicked(e: MouseEvent): Unit = {
          if (e.getClickCount == 2 && openAction.isEnabled) {
            openAction.actionPerformed(null)
          }
        }
      })

    // lay out content pane & bottom buttons

    val topPanel = new Box(BoxLayout.X_AXIS);

    val searchPanel = new Box(BoxLayout.X_AXIS);
    searchField.getDocument.addDocumentListener(
      new DocumentListener() {
        def changedUpdate(e: DocumentEvent): Unit = {
          setSearchText(searchField.getText)
        }

        def insertUpdate(e: DocumentEvent): Unit = {
          setSearchText(searchField.getText)
        }

        def removeUpdate(e: DocumentEvent): Unit = {
          setSearchText(searchField.getText)
        }
      })
    searchField.addAncestorListener(new AncestorListener() {
      def ancestorAdded(e: AncestorEvent): Unit = { searchField.requestFocusInWindow() }
      def ancestorRemoved(e: AncestorEvent): Unit = {}
      def ancestorMoved(e: AncestorEvent): Unit = {}
    });
    searchField.addKeyListener(new KeyAdapter() {
      override def keyPressed(e: KeyEvent): Unit = {
        // A quick reminder - `|` in scala is "match either" in case patterns and "bitwise or" elsewhere.
        // We're using the former sense here - RG 7/12/2017
        e.getKeyCode match {
          case (KeyEvent.VK_UP | KeyEvent.VK_DOWN | KeyEvent.VK_KP_UP | KeyEvent.VK_KP_DOWN) =>
            tree.requestFocusInWindow()
          case _ =>
        }
      }
    })
    searchField.setMaximumSize(new Dimension(Short.MaxValue, searchField.getMinimumSize.height))
    val clearSearchButton = new JButton(I18N.gui.get("modelsLibrary.clear"))
    clearSearchButton.addActionListener(new ActionListener() {
      def actionPerformed(evt: ActionEvent): Unit = {
        searchField.setText("")
      }
    })
    val buttonFont = clearSearchButton.getFont()
    clearSearchButton.setFont(buttonFont.deriveFont(buttonFont.getSize2D - 2))
    searchPanel.add(new JLabel(icon("/images/magnify.gif")))
    searchPanel.add(Box.createRigidArea(new java.awt.Dimension(2, 0)))
    searchPanel.add(searchField)
    searchPanel.add(Box.createRigidArea(new Dimension(2, 0)))
    searchPanel.add(clearSearchButton);
    searchPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

    val treePanel = new Box(BoxLayout.Y_AXIS);
    treePanel.add(new javax.swing.JScrollPane(tree));
    treePanel.add(searchPanel);

    topPanel.add(treePanel);
    topPanel.add(new JScrollPane(modelPreviewPanel));

    val buttonPanel = new Box(BoxLayout.X_AXIS);
    buttonPanel.add(Box.createRigidArea(new Dimension(40, 0)))
    buttonPanel.add(communityButton)
    buttonPanel.add(Box.createHorizontalGlue)
    if (System.getProperty("os.name").startsWith("Mac")) {
      buttonPanel.add(cancelButton)
      buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)))
      buttonPanel.add(selectButton)
    } else {
      buttonPanel.add(selectButton)
      buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)))
      buttonPanel.add(cancelButton)
    }
    buttonPanel.add(Box.createRigidArea(new Dimension(40, 0)))
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2))

    val contentPane = new JPanel()
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS))
    contentPane.add(topPanel)
    contentPane.add(buttonPanel)
    setContentPane(contentPane)

    // add an Esc key handler
    Utils.addEscKeyAction(this, cancelAction)

    // handle close box
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent): Unit = {
        cancelAction.actionPerformed(null)
      }
    })

    this.setSize(740, 715)
    Positioning.center(this, parent)

    // This last bit is a fugly stopgap measure: the only way I found so far
    // to trigger a refresh of the modelPreviewPanel with the right size
    // calculations in order to fix https://github.com/NetLogo/NetLogo/issues/750
    // NP 2015-03-31
    SwingUtilities.invokeLater(new Runnable() {
      def run(): Unit = {
        modelPreviewPanel.showModel()
      }
    })
  }

  def setSearchText(newText: String): Unit = {
    val savedSelectionPath = tree.getSelectionPath()
    val wasEmpty = searchText.isEmpty
    if (newText.length == 0) {
      searchText = None
    } else {
      searchText = Some(newText.toUpperCase)
    }
    val isEmpty = searchText.isEmpty
    val root = tree.getModel.getRoot.asInstanceOf[Node]
    if (wasEmpty && !isEmpty) {
      savedExpandedPaths.clear()
      val e = tree.getExpandedDescendants(new TreePath(root))
      while (e.hasMoreElements) {
        savedExpandedPaths.add(e.nextElement())
      }
    }
    tree.getModel.asInstanceOf[SearchableModelTree].reload(root)
    if (!isEmpty) {
      val expander = new Thread() {
        val text = searchText.get

        override def run(): Unit = {
          var i = 0
          while (i < tree.getRowCount) {
            if (! searchText.contains(text)) {
              // If the search text has changed, the tree will
              // be reloaded and another expander thread will be
              // created if necessary, so we can stop working
              // immediately.
              return;
            }
            // There is a chance that the number of rows has changed
            // in between our call to getRowCount above and here.
            // Since this thread is going to die anyway, we just
            // null-check and allow later threads to do their job.
            // ER - 12/02/07, RG 5/4/17
            val path = tree.getPathForRow(i)
            if (path == null) {
              return
            }
            val node = path.getLastPathComponent.asInstanceOf[Node]
            if (node.isFolder) {
              try {
                // It's very bad to call a method that modifies
                // a component from a thread other than the
                // event dispatch thread once that component
                // has been realized on the screen, so we have
                // to use invokeAndWait for this.
                SwingUtilities.invokeAndWait(new Runnable() {
                  def run(): Unit = {
                    tree.expandPath(path)
                  }
                })
              } catch {
                case ex: java.lang.reflect.InvocationTargetException =>
                  throw new IllegalStateException(ex)
                case ex: InterruptedException =>
                  return
              }
            }
            i += 1
          }
          SwingUtilities.invokeLater(new Runnable() {
            def run(): Unit = {
              invalidate()
              validate()
            }
          })
        }
      }
      expander.start()
    } else if (!wasEmpty) {
      val i = savedExpandedPaths.iterator
      while (i.hasNext) {
        tree.expandPath(i.next())
      }
      savedExpandedPaths.clear()
      if (savedSelectionPath != null) {
        tree.expandPath(savedSelectionPath)
        tree.setSelectionPath(savedSelectionPath)
        tree.scrollPathToVisible(savedSelectionPath)
      }
    }
  }

  private def searchTextIndices(text: String): Array[Int] = {
    if (searchText.isEmpty) {
      null
    } else {
      val ucText = text.toUpperCase();
      val indices: JList[Integer] = new LinkedList[Integer]()
      var index = 0
      var containsCharacter = true
      while (containsCharacter) {
        index = searchText.map(t => ucText.indexOf(t, index + 1)).getOrElse(-1)
        if (index > -1) {
          indices.add(Int.box(index))
        } else {
          containsCharacter = false
        }
      }
      val result = new Array[Int](indices.size)
      var rIndex = 0
      val i = indices.iterator
      while (i.hasNext) {
        result(rIndex) = i.next().intValue;
        rIndex += 1
      }
      result
    }
  }

  // Relayout when user expands or collapses a folder (since that changes
  // the JTree's preferred width.  You'd think you could just call
  // revalidate() on the JTree or the JScrollPane it's embedded in,
  // but neither works - ST 8/3/03

  // It's because JScrollPane returns true from isValidateRoot, so
  // validation of the component hierarchy stops when it hits the
  // scroll pane. This is an appropriate solution. - ER 12/02/07

  def treeExpanded(e: TreeExpansionEvent): Unit = {
    if (searchText.isEmpty) {
      invalidate()
      validate()
    }
  }

  def treeCollapsed(e: TreeExpansionEvent): Unit = {
    if (searchText.isEmpty) {
      invalidate()
      validate()
    }
  }

  def valueChanged(e: TreeSelectionEvent): Unit = {
    if (tree.getSelectionPath == null) {
      selected = None
      openAction.setEnabled(false)
      toggleOrOpenAction.setEnabled(false)
    } else {
      selected = Some(tree.getSelectionPath.getLastPathComponent.asInstanceOf[Node])
      openAction.setEnabled(selected.map(s => ! s.isFolder).getOrElse(false))
      toggleOrOpenAction.setEnabled(true)
    }
    modelPreviewPanel.showModel()
    invalidate()
    if (tree.getSelectionPath != null) {
      tree.scrollPathToVisible(tree.getSelectionPath)
    }
  }

  private class ModelPreviewPanel
    extends JPanel
    with HyperlinkListener {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

      private val graphicsPreview: GraphicsPreview = new GraphicsPreview()
      graphicsPreview.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1))

      private val textArea = new JEditorPane()
      textArea.setContentType("text/html")
      textArea.setEditable(false)
      textArea.setOpaque(false)
      textArea.addHyperlinkListener(this)

      add(graphicsPreview)
      add(textArea)
      add(Box.createVerticalGlue())

      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))

    def showModel(): Unit = {
      val image =
        selected.filterNot(_.isFolder)
          .map(s => ModelsLibrary.getImagePath(s.path)).orNull

      graphicsPreview.setImage(image)

      // This is a work-around for Java's inability to set a maximum
      // width for a JEditorPane and let the height be determined by
      // the content. Continues below.
      // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4765285
      // ER - 12/02/07
      textArea.setPreferredSize(null)
      textArea.setMaximumSize(new Dimension(390, Short.MaxValue))

      selected match {
        case Some(selection) if ! selection.isFolder =>
          textArea.setVisible(true)
          graphicsPreview.setVisible(true)
          // HTMLify the model description
          val htmlDescription =
            s"""|<html>
                |<head>
                |<style>body {font-family: Dialog; font-size: 12pt; }</style>
                |</head>
                |<body>
                |<h2>${selection.name}</h2>
                |${selection.info}
                |</body>
                |</html>""".stripMargin
          textArea.setText(htmlDescription)
          try {
            // Calling Document.getText() gives us the text
            // with the html tags removed.
            val text = textArea.getDocument.getText(0, textArea.getDocument.getLength)
            val indices = searchTextIndices(text)
            if (indices != null) {
              var i = 0
              while (i < indices.length) {
                textArea.getHighlighter
                  .addHighlight(
                    indices(i),
                    indices(i) + searchText.map(_.length).getOrElse(0),
                    DefaultHighlighter.DefaultPainter)
                  i += 1
              }
            }
          } catch {
            case ex: BadLocationException => throw new IllegalStateException(ex)
          }
          textArea.setCaretPosition(0)
        case _ =>
          // no preview to show, so show explanatory message instead
          val text = FileIO.getResourceAsString("/system/library.html")
          textArea.setText(text)
          textArea.setCaretPosition(0)
          graphicsPreview.setVisible(false)
      }

      // The conclusion of the above-mentioned work-around
      // ER - 12/02/07
      textArea.setPreferredSize(new Dimension(390, textArea.getPreferredSize.height))
      invalidate()
    }

    override def hyperlinkUpdate(e: HyperlinkEvent): Unit = {
      if (e.getEventType == HyperlinkEvent.EventType.ACTIVATED) {
        Option(e.getURL)
          .flatMap(u => Try(u.toURI).toOption) match {
            // TODO: Convert to I18N
            case None => JOptionPane.showMessageDialog(this, "Invalid URL!", "Error", JOptionPane.ERROR_MESSAGE);
            case Some(toOpen) => BrowserLauncher.openURI(this, toOpen)
          }
      }
    }
  }

  private class SearchableModelTree(node: Node)
    extends DefaultTreeModel(node) {

    override def getChild(parent: AnyRef, childIndex: Int): AnyRef = {
      if (searchText.isEmpty) {
        super.getChild(parent, childIndex)
      } else {
        val node = parent.asInstanceOf[Node]
        var index = -1
        node.children.foreach { (child: Node) =>
          if (child.isFolder && hasChildren(child)) {
            index += 1
          } else if (matchesSearchText(child)) {
            index += 1
          }
          if (index == childIndex) {
            return child
          }
        }
        null
      }
    }

    private def hasChildren(parent: Object): Boolean = {
      val node = parent.asInstanceOf[Node]
      node.isFolder && node.children.exists { (child: Node) =>
        (child.isFolder && hasChildren(child)) || matchesSearchText(child)
      }
    }

    override def getChildCount(parent: AnyRef): Int = {
      if (searchText.isEmpty) {
        super.getChildCount(parent)
      } else {
        val node = parent.asInstanceOf[Node]
        if (node.isFolder)
          node.children.count { (child: Node) =>
            (child.isFolder && getChildCount(child) > 0) || matchesSearchText(child)
          }
        else 0
      }
    }

    override def getIndexOfChild(parent: AnyRef, child: AnyRef): Int = {
      if (searchText.isEmpty) {
        super.getIndexOfChild(parent, child)
      } else {
        val node = parent.asInstanceOf[Node]
        var result = 0
        node.children.foreach { (c: Node) =>
          if (c == child) {
            return result
          }
          if ((c.isFolder && getChildCount(c) > 0) || matchesSearchText(c)) {
            result += 1
          }
        }
        return -1
      }
    }

    private def matchesSearchText(node: Node): Boolean = {
      searchText.exists(t =>
          node.name.toUpperCase.contains(t) ||
          node.info.toUpperCase.contains(t) ||
          node.path.toUpperCase.contains(t))
    }
  }
}
