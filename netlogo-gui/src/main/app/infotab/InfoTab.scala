// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.infotab

import java.awt.{ BorderLayout, Component, Dimension, EventQueue, Font, Graphics }
import java.awt.event.{ ActionEvent, FocusEvent, FocusListener, KeyAdapter, KeyEvent }
import java.awt.print.PageFormat
import java.net.URI
import javax.swing.{ AbstractAction, Action, JComponent, JPanel }
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
import org.nlogo.awt.RowLayout
import org.nlogo.core.I18N
import org.nlogo.editor.EditorConfiguration
import org.nlogo.swing.{ FocusRoot, FocusUtils, ScrollableTextComponent, ScrollPane, TextArea, ToolBarActionButton,
                         ToolBarToggleButton, Printable, PrinterManager, BrowserLauncher, QuickHelp, Transparent,
                         UndoManager, UserAction, Utils }, UserAction.MenuAction
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ Events => WindowEvents, Zoomable }

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
  with FocusRoot
  with ThemeSync {

  private val undoManager = new UndoManager
  // 90 columns seems reasonable: wide enough to not waste screen real estate, but narrow enough so
  // as not to cause readability problems if the frame is really wide - ST 10/27/03
  private val textArea = new ScrollableTextArea
  private val htmlPanel = new HTMLPanel

  private val textScroll = new ScrollPane(textArea) {
    setBorder(null)
    setFocusable(false)
  }

  private val editableButton = new ToolBarToggleButton(new EditableAction(I18N.gui.get("tabs.info.edit"))) {
    override def syncTheme(): Unit = {
      super.syncTheme()

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
        InfoTab.this, new URI(s"https://docs.netlogo.org/${Version.versionNumberNo3D}/infotab#information"),
        QuickHelp.docPath(Some("infotab-information")))
    }
  })
  helpButton.setIcon(Utils.iconScaledWithColor("/images/help.png", 15, 15, InterfaceColors.toolbarImage()))
  helpButton.setVisible(false)
  private def toggleHelpButton(): Unit ={ helpButton.setVisible(view == textScroll) }

  // this object is used as the html display and the editor
  // it starts off as the html display. when edit is clicked, it becomes the editor.
  // when edit is unclicked, it switches back.
  // there are some funny casts around because of this, and maybe we should clean it up.
  // -JC 9/7/10
  private var view: JComponent = htmlPanel

  private val container = new InfoContainer

  override def zoomTarget = container

  override val activeMenuActions: Seq[MenuAction] =
    Seq(undoAction, redoAction, FindDialog.FIND_ACTION, FindDialog.FIND_NEXT_ACTION)

  private val toolBar = new JPanel(new RowLayout(10, Component.LEFT_ALIGNMENT, Component.CENTER_ALIGNMENT)) {
    setBorder(new EmptyBorder(24, 10, 12, 6))
    setFocusable(false)

    add(findButton)
    add(editableButton)
    add(helpButton)
  }

  private var codeFont: Option[Font] = None

  locally {
    setLayout(new BorderLayout)
    setCanFocus(false)

    add(toolBar, BorderLayout.NORTH)
    add(container, BorderLayout.CENTER)
  }

  override def getDefaultComponent: Option[Component] =
    Option(view)

  override def getFocusOrder: Map[Component, (Component, Component)] = {
    Map(
      htmlPanel -> (null, editableButton),
      textArea -> (null, findButton),
      findButton -> (textArea, null),
      editableButton -> (if (findButton.isEnabled) findButton else htmlPanel, null)
    )
  }

  override def requestFocus(): Unit = {
    view.requestFocus()
  }

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
      htmlPanel.setText(InfoFormatter(str, getModelDir(), resourceManager, codeFont.fold("monospace")(_.getFamily),
                                      editorPaneFontSize))

    toggleHelpButton()
  }

  def resetView(): Unit = {
    setView(htmlPanel)
  }

  def setCodeFont(font: Font): Unit = {
    codeFont = Option(font)

    textArea.setFont(font)

    updateEditorPane(true)
  }

  private def setView(view: JComponent): Unit = {
    if (this.view != view) {
      this.view = view

      container.replaceView(view)

      if (view == textScroll) {
        FindDialog.watch(textArea)
      } else {
        FindDialog.dontWatch()

        updateEditorPane()
      }
    }
  }

  override def syncTheme(): Unit = {
    toolBar.setBackground(InterfaceColors.toolbarBackground())

    findButton.syncTheme()
    editableButton.syncTheme()
    helpButton.syncTheme()

    helpButton.setIcon(Utils.iconScaledWithColor("/images/help.png", 15, 15, InterfaceColors.toolbarImage()))

    textScroll.setBackground(InterfaceColors.infoBackground())

    textArea.syncTheme()
    htmlPanel.syncTheme()

    updateEditorPane()
  }

  def handle(e: AppEvents.SwitchedTabsEvent): Unit = {
    if (e.newTab == this && view == textScroll) {
      FindDialog.watch(textArea)
    } else {
      FindDialog.dontWatch()
    }
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

  private class InfoContainer extends JPanel(new BorderLayout) with Transparent {
    setFocusable(false)

    add(view, BorderLayout.CENTER)

    def replaceView(component: JComponent): Unit = {
      removeAll()

      add(component, BorderLayout.CENTER)

      repaint()
    }
  }

  private class ScrollableTextArea extends TextArea(0, 90) with ScrollableTextComponent {
    addFocusListener(new FocusListener {
      def focusGained(fe: FocusEvent): Unit = {
        UndoManager.setCurrentManager(undoManager)
      }

      def focusLost(fe: FocusEvent): Unit = {
        if (!fe.isTemporary)
          UndoManager.setCurrentManager(null)
      }
    })

    setDragEnabled(false)
    setEditable(true)
    setLineWrap(true)
    setWrapStyleWord(true)
    getDocument.addDocumentListener(InfoTab.this)
    getDocument.addUndoableEditListener(undoManager)
    setFont(EditorConfiguration.getMonospacedFont)

    override def scrollTo(index: Int): Unit = {
      val pos = modelToView2D(index)
      val xBar = textScroll.getHorizontalScrollBar
      val yBar = textScroll.getVerticalScrollBar

      xBar.setValue((pos.getX - xBar.getVisibleAmount / 2).toInt)
      yBar.setValue((pos.getY - yBar.getVisibleAmount / 2).toInt)
    }
  }

  private class HTMLPanel extends JFXPanel with FocusUtils with ThemeSync {
    private var engine: Option[WebEngine] = None
    private var text = ""

    addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent): Unit = {
        e.getKeyCode match {
          case KeyEvent.VK_TAB =>
            if (e.isShiftDown) {
              transferFocusBackward()
            } else {
              transferFocus()
            }

            e.consume()

          case _ =>
        }
      }
    })

    Platform.runLater(() => {
      val webView = new WebView

      webView.setContextMenuEnabled(false)

      webView.addEventFilter(ScrollEvent.SCROLL, event => {
        engine.foreach(_.executeScript(
          if (event.isShiftDown) {
            s"window.scrollBy(${-event.getDeltaY}, 0)"
          } else {
            s"window.scrollBy(0, ${-event.getDeltaY})"
          }
        ))

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

    override def syncTheme(): Unit = {
      setFocusColor(InterfaceColors.interfaceFocus())
    }
  }

  private class EditableAction(label: String) extends AbstractAction(label) {
    putValue(Action.SMALL_ICON, Utils.iconScaledWithColor("/images/edit.png", 15, 15, InterfaceColors.toolbarImage()))
    def actionPerformed(e: ActionEvent): Unit = {
      val scrollBar = textScroll.getVerticalScrollBar
      val (min, max) = (scrollBar.getMinimum, scrollBar.getMaximum)
      val ratio = (scrollBar.getValue - min).toDouble / (max - min)
      if (view == textScroll) {
        setView(htmlPanel)
      } else {
        setView(textScroll)
      }
      toggleHelpButton()
      requestFocus()
      EventQueue.invokeLater(() => scrollBar.setValue((ratio * (max - min)).toInt))
      editableButton.syncTheme()
    }
  }
}
