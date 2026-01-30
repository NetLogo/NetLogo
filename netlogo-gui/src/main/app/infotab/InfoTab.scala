// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.infotab

import java.awt.{ Dimension, BorderLayout, Graphics }
import java.awt.event.{ ActionEvent, FocusEvent, FocusListener }
import java.awt.print.PageFormat
import java.net.URI
import javax.swing.{ AbstractAction, Action, BorderFactory, JComponent, JPanel, JScrollPane, JTextArea,
                     ScrollPaneConstants }
import javax.swing.border.EmptyBorder
import javax.swing.event.{ DocumentListener, DocumentEvent }

import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.input.ScrollEvent
import javafx.scene.Scene
import javafx.scene.web.{ WebEngine, WebView }

import org.nlogo.api.{ ExternalResourceManager, Version }
import org.nlogo.app.common.{ Events => AppEvents, FindDialog, MenuTab, UndoRedoActions }
import org.nlogo.core.I18N
import org.nlogo.editor.EditorConfiguration
import org.nlogo.swing.Implicits._
import org.nlogo.swing.{ ScrollableTextComponent, ScrollPane, TextArea, ToolBar, ToolBarActionButton,
                         ToolBarToggleButton, Printable, PrinterManager, BrowserLauncher, UndoManager, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ Events => WindowEvents, QuickHelp, Zoomable }

import org.w3c.dom.events.{ Event, EventListener, EventTarget }
import org.w3c.dom.html.HTMLAnchorElement

class InfoTab(getModelDir: () => String, resourceManager: ExternalResourceManager)
  extends JPanel
  with DocumentListener
  with MenuTab
  with Printable
  with UndoRedoActions
  with AppEvents.SwitchedTabsEvent.Handler
  with WindowEvents.LoadBeginEvent.Handler
  with WindowEvents.LoadModelEvent.Handler
  with WindowEvents.ResourcesChangedEvent.Handler
  with WindowEvents.ZoomedEvent.Handler
  with Zoomable
  with ThemeSync {

  private val undoManager = new UndoManager
  // 90 columns seems reasonable: wide enough to not waste screen real estate, but narrow enough so
  // as not to cause readability problems if the frame is really wide - ST 10/27/03
  private val textArea = new ScrollableTextArea
  private val htmlPanel = new HTMLPanel

  private val editableButton = new ToolBarToggleButton(new EditableAction(I18N.gui.get("tabs.info.edit"))) with ThemeSync {
    override def syncTheme(): Unit = {
      setIcon(Utils.iconScaledWithColor("/images/edit.png", 15, 15,
              if (isSelected) {
                InterfaceColors.toolbarImageSelected()
              } else {
                InterfaceColors.toolbarImage()
              }))
    }
  }
  private val findButton = new ToolBarActionButton(FindDialog.FIND_ACTION)
  private val helpButton = new ToolBarActionButton(new AbstractAction(I18N.gui.get("tabs.info.help")) {
    override def actionPerformed(e: ActionEvent): Unit = {
      BrowserLauncher.tryOpenURI(
        InfoTab.this, new URI(s"https://netlogo.org/${Version.versionNumberNo3D}/infotab#information"),
        QuickHelp.docPath(Some("infotab-information")))
    }
  })
  helpButton.setIcon(Utils.iconScaledWithColor("/images/help.png", 15, 15, InterfaceColors.toolbarImage()))
  helpButton.setVisible(false)
  private def toggleHelpButton(): Unit ={ helpButton.setVisible(view == textArea) }

  // this object is used as the html display and the editor
  // it starts off as the html display. when edit is clicked, it becomes the editor.
  // when edit is unclicked, it switches back.
  // there are some funny casts around because of this, and maybe we should clean it up.
  // -JC 9/7/10
  private var view: JComponent = htmlPanel
  private val scrollPane = new ScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

  scrollPane.setBorder(null)

  override def zoomTarget = scrollPane

  override val activeMenuActions = Seq(undoAction, redoAction)

  private val toolBar = new ToolBar {
    setBorder(new EmptyBorder(24, 10, 12, 6))

    override def addControls(): Unit = {
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

  private def resetBorders(): Unit = {
    textArea.setBorder(BorderFactory.createEmptyBorder(4, 7, 4, 7))
  }

  override def doLayout(): Unit = {
    // we need to call resetBorders first, otherwise borders left over from last time we laid out
    // may affect the answer returned by textArea.getPreferredScrollableViewportSize, causing the
    // layout to jump around - ST 10/7/09
    resetBorders()
    val extraWidth = (getWidth - textArea.getPreferredScrollableViewportSize.width - 7).max(7)
    textArea.setBorder(BorderFactory.createEmptyBorder(4, 7, 4, extraWidth))
    super.doLayout()
  }

  override def requestFocus(): Unit = { view.requestFocus() }

  def info = textArea.getText
  def info(str: String): Unit = {
    if(str != info) {
      textArea.setText(str)
      textArea.setCaretPosition(0)
    }
    undoManager.discardAllEdits()
  }

  private def updateEditorPane(force: Boolean = false): Unit = { updateEditorPane(textArea.getText, force) }

  private def updateEditorPane(str: String, force: Boolean): Unit = {
    if (force || str != htmlPanel.getText)
      htmlPanel.setText(InfoFormatter(str, getModelDir(), resourceManager, editorPaneFontSize))

    toggleHelpButton()
  }

  def resetView(): Unit = {
    if (view.isInstanceOf[JTextArea]) {
      scrollPane.setViewportView(htmlPanel)
      setView(htmlPanel)
      editableButton.setSelected(false)
    }
    updateEditorPane()
    scrollPane.getVerticalScrollBar.setValue(0)
  }

  private def setView(view: JComponent): Unit = {
    this.view = view

    // the WebView panel needs to handle its own scrolling instead of Swing's JScrollPane,
    // otherwise it throws a bunch of exceptions about trying to render a view that's
    // too large. (Isaac B 1/29/26)
    if (view == htmlPanel) {
      scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER)
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
    } else {
      scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    }
  }

  override def syncTheme(): Unit = {
    toolBar.setBackground(InterfaceColors.toolbarBackground())

    findButton.syncTheme()
    editableButton.syncTheme()
    helpButton.syncTheme()

    helpButton.setIcon(Utils.iconScaledWithColor("/images/help.png", 15, 15, InterfaceColors.toolbarImage()))

    scrollPane.setBackground(InterfaceColors.infoBackground())

    textArea.syncTheme()

    updateEditorPane()
  }

  def handle(e: AppEvents.SwitchedTabsEvent): Unit = {
    if (e.newTab != this)
      FindDialog.dontWatch()
  }

  def handle(e: WindowEvents.LoadBeginEvent): Unit = {
    undoManager.discardAllEdits()
  }

  def handle(e: WindowEvents.LoadModelEvent): Unit = {
    info(e.model.info)
    resetView()
  }

  override def handle(e: WindowEvents.ResourcesChangedEvent): Unit = {
    updateEditorPane(true)
  }

  private var editorPaneFontSize = InfoFormatter.defaultFontSize
  private var originalFontSize = -1
  override def handle(e: WindowEvents.ZoomedEvent): Unit = {
    super.handle(e)
    if(originalFontSize == -1)
      originalFontSize = textArea.getFont.getSize
    textArea.setFont(textArea.getFont.deriveFont(StrictMath.ceil(originalFontSize * zoomFactor).toFloat))
    editorPaneFontSize = StrictMath.ceil(InfoFormatter.defaultFontSize * zoomFactor).toInt
    updateEditorPane()
  }

  // the textArea will give us an outlandlishly large preferred size unless we restrain it
  override def getPreferredSize = new Dimension(100, 100)

  /// DocumentListener
  def changedUpdate(e: DocumentEvent): Unit = { changed() }
  def insertUpdate(e: DocumentEvent): Unit = { changed() }
  def removeUpdate(e: DocumentEvent): Unit = { changed() }
  private def changed(): Unit = { new org.nlogo.window.Events.DirtyEvent(None).raise(this) }

  /// Printing
  def print(g: Graphics, pageFormat: PageFormat, pageIndex: Int, printer: PrinterManager) = {
    printer.printText(g, pageFormat, pageIndex, textArea.getText)
  }

  private class ScrollableTextArea extends TextArea(0, 90) with ScrollableTextComponent {
    addFocusListener(new FocusListener {
      def focusGained(fe: FocusEvent): Unit = {
        FindDialog.watch(ScrollableTextArea.this)
        UndoManager.setCurrentManager(undoManager)
      }

      def focusLost(fe: FocusEvent): Unit = {
        if (!fe.isTemporary) {
          FindDialog.dontWatch()
          UndoManager.setCurrentManager(null)
        }
      }
    })

    setDragEnabled(false)
    setEditable(true)
    setLineWrap(true)
    setWrapStyleWord(true)
    getDocument.addDocumentListener(InfoTab.this)
    getDocument.addUndoableEditListener(undoManager)
    setFont(EditorConfiguration.getMonospacedFont)

    override def scrollPane: Option[JScrollPane] =
      Option(InfoTab.this.scrollPane)
  }

  private class HTMLPanel extends JFXPanel {
    private var engine: Option[WebEngine] = None
    private var text = ""

    Platform.runLater(() => {
      val webView = new WebView

      webView.setContextMenuEnabled(false)

      webView.addEventFilter(ScrollEvent.SCROLL, event => {
        engine.foreach(_.executeScript(s"window.scrollBy(${-event.getDeltaX / 4}, ${-event.getDeltaY / 4})"))

        event.consume()
      })

      setScene(new Scene(webView))

      val webEngine = webView.getEngine

      webEngine.getLoadWorker.stateProperty.addListener((value, oldState, newState) => {
        val listener = new EventListener {
          override def handleEvent(e: Event): Unit = {
            e.getCurrentTarget match {
              case anchor: HTMLAnchorElement =>
                BrowserLauncher.openURI(InfoTab.this, URI.create(anchor.getHref))

              case _ =>
            }

            e.preventDefault()
          }
        }

        if (newState == Worker.State.SUCCEEDED) {
          val nodes = webEngine.getDocument.getElementsByTagName("a")

          for (i <- 0 until nodes.getLength) {
            nodes.item(i) match {
              case target: EventTarget =>
                target.addEventListener("click", listener, false)

              case _ =>
            }
          }
        }
      })

      engine = Option(webEngine)
    })

    def getText: String =
      text

    def setText(str: String): Unit = {
      this.text = str

      Platform.runLater(() => {
        engine.foreach(_.loadContent(str, "text/html"))
      })
    }
  }

  private class EditableAction(label: String) extends AbstractAction(label) {
    putValue(Action.SMALL_ICON, Utils.iconScaledWithColor("/images/edit.png", 15, 15, InterfaceColors.toolbarImage()))
    def actionPerformed(e: ActionEvent): Unit = {
      val scrollBar = scrollPane.getVerticalScrollBar
      val (min, max) = (scrollBar.getMinimum, scrollBar.getMaximum)
      val ratio = ((scrollBar.getValue - min).asInstanceOf[Double] / (max - min).asInstanceOf[Double])
      if (view.isInstanceOf[JTextArea]) {
        updateEditorPane()
        scrollPane.setViewportView(htmlPanel)
        setView(htmlPanel)
      } else {
        scrollPane.setViewportView(textArea)
        setView(textArea)
      }
      toggleHelpButton()
      requestFocus()
      org.nlogo.awt.EventQueue.invokeLater(() => scrollBar.setValue((ratio * (max - min)).toInt))
      editableButton.syncTheme()
    }
  }
}
