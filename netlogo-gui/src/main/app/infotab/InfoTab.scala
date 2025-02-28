// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.infotab

import java.awt.{ Font, Dimension, BorderLayout, Graphics }
import java.awt.event.{ ActionEvent, FocusEvent, FocusListener }
import java.awt.print.PageFormat
import java.io.File
import java.nio.file.Path
import javax.swing.{ AbstractAction, Action, BorderFactory, JEditorPane, JPanel, JTextArea, ScrollPaneConstants }
import javax.swing.event.{ DocumentListener, HyperlinkListener, DocumentEvent, HyperlinkEvent }
import javax.swing.text.JTextComponent
import javax.swing.text.html.HTMLDocument

import org.nlogo.app.common.{ Events => AppEvents, FindDialog, MenuTab, UndoRedoActions }
import org.nlogo.awt.{ Fonts, Hierarchy }
import org.nlogo.core.I18N
import org.nlogo.editor.UndoManager
import org.nlogo.swing.Implicits._
import org.nlogo.swing.{ OptionPane, ScrollPane, TextArea, ToolBar, ToolBarButton, ToolBarActionButton,
                         ToolBarToggleButton, Printable, PrinterManager, BrowserLauncher, Utils },
  BrowserLauncher.docPath
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ Events => WindowEvents, Zoomable }

class InfoTab(attachModelDir: String => String)
  extends JPanel
  with DocumentListener
  with MenuTab
  with Printable
  with HyperlinkListener
  with UndoRedoActions
  with AppEvents.SwitchedTabsEvent.Handler
  with WindowEvents.LoadBeginEvent.Handler
  with WindowEvents.LoadModelEvent.Handler
  with WindowEvents.ZoomedEvent.Handler
  with Zoomable
  with ThemeSync {

  val baseDocPath: Path = docPath("infotab.html")

  private val undoManager = new UndoManager
  // 90 columns seems reasonable: wide enough to not waste screen real estate, but narrow enough so
  // as not to cause readability problems if the frame is really wide - ST 10/27/03
  private val textArea = new TextArea(0, 90) { self =>
    addFocusListener(new FocusListener {
      def focusGained(fe: FocusEvent) { FindDialog.watch(self); UndoManager.setCurrentManager(undoManager) }
      def focusLost(fe: FocusEvent) {
        if (!fe.isTemporary) { FindDialog.dontWatch(self); UndoManager.setCurrentManager(null) }
      }
    })
    setDragEnabled(false)
    setEditable(true)
    setLineWrap(true)
    setWrapStyleWord(true)
    getDocument.addDocumentListener(InfoTab.this)
    getDocument.addUndoableEditListener(undoManager)
    setFont(new Font(Fonts.platformMonospacedFont, Font.PLAIN, 12))
  }
  private val editorPane = new JEditorPane { self =>
    addFocusListener(new FocusListener {
      def focusGained(fe: FocusEvent) { FindDialog.watch(self) }
      def focusLost(fe: FocusEvent) { if (!fe.isTemporary) { FindDialog.dontWatch(self) } }
    })
    setDragEnabled(false)
    setEditable(false)
    getDocument.addDocumentListener(InfoTab.this)
    setContentType("text/html")
    addHyperlinkListener(InfoTab.this)
  }

  private val findButton = new ToolBarActionButton(FindDialog.FIND_ACTION)
  private val editableButton = new ToolBarToggleButton(new EditableAction(I18N.gui.get("tabs.info.edit")))
  private val helpButton = new ToolBarButton(I18N.gui.get("tabs.info.help"),
                                             BrowserLauncher.openPath(this, baseDocPath, "information"))
  helpButton.setIcon(Utils.iconScaledWithColor("/images/help.png", 15, 15, InterfaceColors.toolbarImage))
  helpButton.setVisible(false)
  private def toggleHelpButton(){ helpButton.setVisible(view == textArea) }

  // this object is used as the html display and the editor
  // it starts off as the html display. when edit is clicked, it becomes the editor.
  // when edit is unclicked, it switches back.
  // there are some funny casts around because of this, and maybe we should clean it up.
  // -JC 9/7/10
  private var view: JTextComponent = editorPane.asInstanceOf[JTextComponent]
  private val scrollPane = new ScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)

  scrollPane.setBorder(null)

  override def zoomTarget = scrollPane

  override val activeMenuActions = Seq(undoAction, redoAction)

  private val toolBar = new ToolBar {
    override def addControls() {
      this.addAll(findButton, editableButton, helpButton)
    }
  }

  locally {
    resetBorders()
    setLayout(new BorderLayout)
    add(toolBar, BorderLayout.NORTH)
    scrollPane.getVerticalScrollBar.setUnitIncrement(16)
    add(scrollPane, BorderLayout.CENTER)
  }

  private def resetBorders() {
    val border = BorderFactory.createEmptyBorder(4, 7, 4, 7)
    textArea.setBorder(border)
    editorPane.setBorder(border)
  }

  override def doLayout() {
    // we need to call resetBorders first, otherwise borders left over from last time we laid out
    // may affect the answer returned by textArea.getPreferredScrollableViewportSize, causing the
    // layout to jump around - ST 10/7/09
    resetBorders()
    val extraWidth = StrictMath.max(7, getWidth - textArea.getPreferredScrollableViewportSize.width - 7)
    textArea.setBorder(BorderFactory.createEmptyBorder(4, 7, 4, extraWidth))
    editorPane.setBorder(BorderFactory.createEmptyBorder(4, 7, 4, extraWidth))
    super.doLayout()
  }

  override def requestFocus() { view.requestFocus() }

  def info = textArea.getText
  def info(str: String) {
    if(str != info) {
      textArea.setText(str)
      textArea.setCaretPosition(0)
    }
    undoManager.discardAllEdits()
  }

  private def updateEditorPane() { updateEditorPane(textArea.getText) }

  private def updateEditorPane(str: String) {
    if(str != editorPane.getText) {
      editorPane.getDocument.asInstanceOf[HTMLDocument].setBase(new File(attachModelDir(".")).toURI.toURL)
      editorPane.setText(InfoFormatter(str, editorPaneFontSize))
      editorPane.setCaretPosition(0)
    }
    toggleHelpButton()
  }

  def resetView() {
    if (view.isInstanceOf[JTextArea]) {
      scrollPane.setViewportView(editorPane)
      view = editorPane
      editableButton.setSelected(false)
    }
    updateEditorPane()
  }

  override def syncTheme(): Unit = {
    toolBar.setBackground(InterfaceColors.toolbarBackground)

    editableButton.setIcon(Utils.iconScaledWithColor("/images/edit.png", 15, 15, InterfaceColors.toolbarImage))
    helpButton.setIcon(Utils.iconScaledWithColor("/images/help.png", 15, 15, InterfaceColors.toolbarImage))

    scrollPane.setBackground(InterfaceColors.infoBackground)
    editorPane.setBackground(InterfaceColors.infoBackground)

    textArea.syncTheme()

    updateEditorPane()
  }

  def handle(e: AppEvents.SwitchedTabsEvent) {
    if (e.newTab != this)
      FindDialog.dontWatch(editorPane)
  }

  def handle(e: WindowEvents.LoadBeginEvent) {
    undoManager.discardAllEdits()
  }

  def handle(e: WindowEvents.LoadModelEvent) {
    info(e.model.info)
    resetView()
  }

  private var editorPaneFontSize = InfoFormatter.defaultFontSize
  private var originalFontSize = -1
  override def handle(e: WindowEvents.ZoomedEvent) {
    super.handle(e)
    if(originalFontSize == -1)
      originalFontSize = textArea.getFont.getSize
    textArea.setFont(textArea.getFont.deriveFont(StrictMath.ceil(originalFontSize * zoomFactor).toFloat))
    editorPaneFontSize = StrictMath.ceil(InfoFormatter.defaultFontSize * zoomFactor).toInt
    updateEditorPane()
  }

  // the textArea will give us an outlandlishly large preferred size unless we restrain it
  override def getPreferredSize = new Dimension(100, 100)

  /// Hyperlink listener
  def hyperlinkUpdate(e: HyperlinkEvent) {
    if (e.getEventType == HyperlinkEvent.EventType.ACTIVATED) {
      if (e.getURL == null) {
        if (new OptionPane(Hierarchy.getFrame(InfoTab.this), I18N.gui.get("common.messages.error"),
                           I18N.gui.get("tabs.info.invalidURL"),
                           List(I18N.gui.get("common.buttons.ok"), I18N.gui.get("common.buttons.help")),
                           OptionPane.Icons.Error).getSelectedIndex == 1) // Help
          BrowserLauncher.openPath(this, baseDocPath, "links")
      }
      else BrowserLauncher.openURI(this, e.getURL.toURI)
    }
  }

  /// DocumentListener
  def changedUpdate(e: DocumentEvent) { changed() }
  def insertUpdate(e: DocumentEvent) { changed() }
  def removeUpdate(e: DocumentEvent) { changed() }
  private def changed() { new org.nlogo.window.Events.DirtyEvent(None).raise(this) }

  /// Printing
  def print(g: Graphics, pageFormat: PageFormat, pageIndex: Int, printer: PrinterManager) = {
    printer.printText(g, pageFormat, pageIndex, textArea.getText)
  }

  private class EditableAction(label: String) extends AbstractAction(label) {
    putValue(Action.SMALL_ICON, Utils.iconScaledWithColor("/images/edit.png", 15, 15, InterfaceColors.toolbarImage))
    def actionPerformed(e: ActionEvent) {
      val scrollBar = scrollPane.getVerticalScrollBar
      val (min, max) = (scrollBar.getMinimum, scrollBar.getMaximum)
      val ratio = ((scrollBar.getValue - min).asInstanceOf[Double] / (max - min).asInstanceOf[Double])
      if (view.isInstanceOf[JTextArea]) {
        updateEditorPane()
        scrollPane.setViewportView(editorPane)
        view = editorPane
      } else {
        scrollPane.setViewportView(textArea)
        view = textArea
      }
      toggleHelpButton()
      requestFocus()
      org.nlogo.awt.EventQueue.invokeLater(() => scrollBar.setValue((ratio * (max - min)).toInt))
    }
  }
}
