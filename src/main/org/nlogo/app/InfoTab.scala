// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.editor.UndoManager
import org.nlogo.swing.Implicits._
import org.nlogo.swing.{OptionDialog, ToolBar, Printable, PrinterManager, BrowserLauncher, RichJButton}
import org.nlogo.awt.Hierarchy

import java.awt.{Font, Dimension, BorderLayout, Graphics}
import java.awt.event.{ActionEvent, FocusEvent, FocusListener}
import java.awt.print.PageFormat

import javax.swing._
import javax.swing.event.{DocumentListener, HyperlinkListener, DocumentEvent, HyperlinkEvent}
import javax.swing.text.JTextComponent
import org.nlogo.api.{I18N, VersionHistory, ModelSection}
import text.html.HTMLDocument
import java.io.File

class InfoTab(attachModelDir: String => String) extends JPanel with
        DocumentListener with Printable with HyperlinkListener with
        org.nlogo.window.Events.LoadSectionEvent.Handler with
        org.nlogo.window.Events.ZoomedEvent.Handler with
        org.nlogo.window.Zoomable {

  val baseDocUrl: String = {
    val docRoot = System.getProperty("netlogo.docs.dir", "docs")
    docRoot + "/infotab.html"
  }

  private val undoManager = new UndoManager()
  // 90 columns seems reasonable: wide enough to not waste screen real estate, but narrow enough so
  // as not to cause readability problems if the frame is really wide - ST 10/27/03
  private val textArea = new JTextArea(0, 90) { self =>
    addFocusListener(new FocusListener() {
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
    setFont(new Font(org.nlogo.awt.Fonts.platformMonospacedFont,
                     Font.PLAIN, 12))
  }
  private val editorPane = new JEditorPane() { self =>
    addFocusListener(new FocusListener() {
      def focusGained(fe: FocusEvent) { FindDialog.watch(self) }
      def focusLost(fe: FocusEvent) { if (!fe.isTemporary) { FindDialog.dontWatch(self) } }
    })
    setDragEnabled(false)
    setEditable(false)
    getDocument.addDocumentListener(InfoTab.this)
    setContentType("text/html")
    addHyperlinkListener(InfoTab.this)
  }
  private val editableButton = new JToggleButton(new EditableAction(I18N.gui.get("tabs.info.edit")))
  private val helpButton = RichJButton(I18N.gui.get("tabs.info.help")) {
    BrowserLauncher.openURL(this, baseDocUrl, "#information", true)
  }
  helpButton.setIcon(new ImageIcon(classOf[FindDialog].getResource("/images/questionmark.gif")))
  helpButton.setVisible(false)
  private def toggleHelpButton(){ helpButton.setVisible(view == textArea) }

  // this object is used as the html display and the editor
  // it starts off as the html display. when edit is clicked, it becomes the editor.
  // when edit is unclicked, it switches back.
  // there are some funny casts around because of this, and maybe we should clean it up.
  // -JC 9/7/10
  private var view: JTextComponent = editorPane.asInstanceOf[JTextComponent]
  private val scrollPane = new JScrollPane(view,
                                           ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)

  override def zoomTarget = scrollPane

  locally {
    resetBorders()
    setLayout(new BorderLayout())
    add(new ToolBar() {
      override def addControls() {
        this.addAll(new JButton(FindDialog.FIND_ACTION), editableButton, helpButton)
      }
    }, BorderLayout.NORTH)
    add(scrollPane,BorderLayout.CENTER)
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
      val html = InfoFormatter(str, editorPaneFontSize)
      //println(html)
      editorPane.setText(html)
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

  def handle(e: org.nlogo.window.Events.LoadSectionEvent) {
    if(e.section == ModelSection.Info) {
      info(if(VersionHistory.olderThan42pre2(e.version))
             InfoConverter.convert(e.text)
           else e.text)
      resetView()
    }
  }

  private var editorPaneFontSize = InfoFormatter.defaultFontSize
  private var originalFontSize = -1
  override def handle(e: org.nlogo.window.Events.ZoomedEvent) {
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
        val message =
          """The URL you just clicked is invalid. This could
            |mean that it is formatted incorrectly. Click Help
            |to see documentation on using URLs in the Info Tab.""".stripMargin
        val selection = OptionDialog.show(Hierarchy.getFrame(InfoTab.this), "Bad URL", message,
          Array(I18N.gui.get("common.buttons.ok"), I18N.gui.get("common.buttons.help")))
        if(selection == 1 /*Help*/) BrowserLauncher.openURL(this, baseDocUrl, "#infotabLinks", true)
      }
      else BrowserLauncher.openURL(this, e.getURL.toString, false)
    }
  }

  /// DocumentListener
  def changedUpdate(e: DocumentEvent) { changed() }
  def insertUpdate(e: DocumentEvent) { changed() }
  def removeUpdate(e: DocumentEvent) { changed() }
  private def changed() { new org.nlogo.window.Events.DirtyEvent().raise(this) }

  /// Printing
  def print(g: Graphics, pageFormat: PageFormat, pageIndex: Int, printer: PrinterManager) = {
    printer.printText(g, pageFormat, pageIndex, textArea.getText)
  }

  private class EditableAction(label: String) extends AbstractAction(label) {
    putValue(Action.SMALL_ICON, new ImageIcon(classOf[InterfaceTab].getResource("/images/edit.gif")))
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
