// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ Dimension, Graphics }
import java.awt.event.{ ActionEvent, FocusEvent, KeyAdapter, KeyEvent, MouseEvent }
import javax.swing.{ Action, AbstractAction, ButtonGroup, JLabel, JPanel, JToggleButton, SwingConstants }

import org.jhotdraw.framework.{ DrawingEditor, DrawingView, Figure, FigureSelectionListener, Tool }
import org.jhotdraw.standard.{ CreationTool, DeleteCommand }

import org.nlogo.core.LocaleContext
import org.nlogo.sdm.Model
import org.nlogo.swing.{ BoxAlign, BoxRow, Button, InputOptionPane, OptionPane, ToolBarActionButton,
                         ToolBarToggleButton, Utils => SwingUtils, Zoomable, ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class AggregateModelEditorToolBar(editor: AggregateModelEditor, model: Model)
  extends BoxRow(6, BoxAlign.Start) with ThemeSync {

  // Invisible button allows no selection in visible buttongroup
  private val noToolButton = new JToggleButton("") {
    setFocusable(false)
  }

  private val dtLabel = new JLabel("dt = " + model.getDt) {
    setOpaque(false)
    setFocusable(false)
  }

  val dtButton = new Button(new AbstractAction(loc"Edit") {
    def actionPerformed(e: ActionEvent): Unit = {
      val newDt = new InputOptionPane(editor, loc"Edit", "dt", model.getDt.toString).getInput
      try if (newDt != null) {
        model.setDt(newDt.toDouble)
        dtLabel.setText("dt = " + model.getDt)
        new org.nlogo.window.Events.CompileAllEvent().raise(editor)
        new org.nlogo.window.Events.DirtyEvent(None).raise(editor)
      }
      catch {
        case ex: NumberFormatException => new OptionPane(null, loc"Error", loc"dt must be a number",
                                                         OptionPane.Options.Ok, OptionPane.Icons.error)
        case ex: Model.ModelException => new OptionPane(null, loc"Error", loc"dt must be greater than 0",
                                                        OptionPane.Options.Ok, OptionPane.Icons.error)
      }
    }
  })

  private val compileAction = new MyAction(loc"Check", "/images/check.png", enableMe = true) {
    def actionPerformed(e: ActionEvent): Unit = {new org.nlogo.window.Events.CompileAllEvent().raise(editor)}
  }
  private val editAction = new MyAction(loc"Edit", "/images/edit.png", enableMe = false) {
    def actionPerformed(e: ActionEvent): Unit = {editor.inspectFigure(editor.view.selection.nextFigure)}
  }
  private val deleteAction = new MyAction(loc"Delete", "/images/delete.png", enableMe = false) {
    def actionPerformed(e: ActionEvent): Unit = {
      new DeleteCommand(loc"Delete", editor).execute()
      new org.nlogo.window.Events.CompileAllEvent().raise(editor)
      new org.nlogo.window.Events.DirtyEvent(None).raise(editor)
    }
  }

  private val deleteButton = new ToolBarActionButton(deleteAction)

  val editButton = new ToolBarActionButton(editAction)
  val compileButton = new ToolBarActionButton(compileAction)

  setOpaque(true)
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(editButton)
  add(deleteButton)
  add(new Separator)
  add(compileButton)
  add(new Separator)

  def makeButton(name: String, image: String, tool: Tool) = {
    new ToolBarToggleButton(new ToolAction(name, image, tool)) {
      setVerticalTextPosition(SwingConstants.BOTTOM)
      setHorizontalTextPosition(SwingConstants.CENTER)
      setPrimaryAction(() => editor.view.requestFocus(FocusEvent.Cause.TRAVERSAL))

      addKeyListener(new KeyAdapter {
        override def keyPressed(e: KeyEvent): Unit = {
          if (e.getKeyCode == KeyEvent.VK_TAB) {
            if (e.isShiftDown) {
              transferFocusBackward()
            } else {
              transferFocus()
            }
          }
        }
      })
    }
  }

  val stockButton = makeButton(loc"Stock", "/images/stock.gif", new StockFigureCreationTool(model, editor))
  val variableButton = makeButton(loc"Variable", "/images/converter.gif",
                                  new ConverterFigureCreationTool(model, editor))
  val flowButton = makeButton(loc"Flow", "/images/rate.gif",
                              new RateConnectionTool(model, editor, RateConnection.create()))
  val linkButton = makeButton(loc"Link", "/images/connector.gif",
                              new AggregateConnectionTool(model, editor, BindingConnection.create()))

  val toolButtonGroup = new ButtonGroup() { add(noToolButton) }

  for (b <- List(stockButton, variableButton, flowButton, linkButton)) {
    add(b)
    toolButtonGroup.add(b)
  }

  add(new Separator)
  add(dtLabel)
  add(dtButton)

  // Event listeners
  editor.view.addFigureSelectionListener(new FigureSelectionListener() {
    def figureSelectionChanged(view: DrawingView): Unit = {
      editAction.setEnabled(view.selectionCount == 1)
      deleteAction.setEnabled(view.selectionCount == 1)
    }
  })

  syncTheme()

  def popButtons(): Unit = {noToolButton.setSelected(true)}

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.toolbarBackground())

    editButton.syncTheme()
    deleteButton.syncTheme()
    compileButton.syncTheme()

    dtLabel.setForeground(InterfaceColors.toolbarText())
    dtButton.syncTheme()
  }

  private class ModelElementCreationTool(model: Model, editor: DrawingEditor, figure: ModelElementFigure & Figure)
    extends CreationTool(editor, figure) {
      override protected def setAddedFigure(newAddedFigure: Figure): Unit = {
        super.setAddedFigure(newAddedFigure)
        newAddedFigure match {
          case mef: ModelElementFigure => model.addElement(mef.getModelElement)
          case _ =>
        }
      }
    }

  /// Figure creation tools
  private class ConverterFigureCreationTool(model: Model, editor: DrawingEditor) extends ModelElementCreationTool(model, editor, ConverterFigure.create()) {
    // We override these to create a fixed-size shape, rather than allow
    // user to drag out the size
    override def mouseDown(e: MouseEvent, x: Int, y: Int): Unit = {
      super.mouseDown(e, x, y)
      super.mouseDrag(e, x + 50, y + 50)
    }
    override def mouseDrag(e: MouseEvent, x: Int, y: Int): Unit = {}
  }

  private class StockFigureCreationTool(model: Model, editor: DrawingEditor) extends ModelElementCreationTool(model, editor, new StockFigure()) {
    // We override these to create a fixed-size shape, rather than allow
    // user to drag out the size
    override def mouseDown(e: MouseEvent, x: Int, y: Int): Unit = {
      super.mouseDown(e, x, y)
      super.mouseDrag(e, x + 60, y + 40)
    }
    override def mouseDrag(e: MouseEvent, x: Int, y: Int): Unit = {}
  }

  abstract class MyAction(name: String, image: String, enableMe: Boolean) extends AbstractAction(name) {
    putValue(Action.SMALL_ICON, SwingUtils.iconScaledWithColor(AggregateModelEditorToolBar.this, image, 15, 15,
                                                               () => InterfaceColors.toolbarImage()))
    setEnabled(enableMe)
  }

  class ToolAction(toolName: String, iconName: String, tool: Tool) extends AbstractAction(toolName) {
    putValue(Action.SMALL_ICON, SwingUtils.icon(iconName))
    def actionPerformed(e: ActionEvent): Unit = {editor.setTool(tool)}
  }

  class Separator extends JPanel with Zoomable {
    setBorder(new ZoomableBorder(0, 12, 0, 12))
    setFocusable(false)

    override def getPreferredSize: Dimension =
      new Dimension(1, super.getPreferredSize.height)

    override def getMinimumSize: Dimension =
      new Dimension(1, 0)

    override def getMaximumSize: Dimension =
      new Dimension(1, Int.MaxValue)

    override def paintComponent(g: Graphics): Unit = {
      setBackground(InterfaceColors.toolbarSeparator())

      super.paintComponent(g)
    }
  }
}
