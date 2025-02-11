// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.FlowLayout
import java.awt.event.{ ActionEvent, MouseEvent }
import javax.swing.{ Action, AbstractAction, ButtonGroup, JLabel, JPanel, JToggleButton }
import javax.swing.JToolBar.Separator

import org.jhotdraw.framework.{ DrawingEditor, DrawingView, Figure, FigureSelectionListener, Tool }
import org.jhotdraw.standard.{ CreationTool, DeleteCommand }

import org.nlogo.core.I18N
import org.nlogo.sdm.Model
import org.nlogo.swing.{ Button, InputOptionPane, OptionPane, ToolBar, ToolBarActionButton, ToolBarToggleButton,
                         Transparent, Utils => SwingUtils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class AggregateModelEditorToolBar(editor: AggregateModelEditor, model: Model) extends ToolBar with ThemeSync {
  implicit val i18nPrefix = I18N.Prefix("tools.sdm")

  // Invisible button allows no selection in visible buttongroup
  private val noToolButton = new JToggleButton("")
  private var dtLabel: JLabel = null
  private var dtButton: Button = null

  override def addControls() {
    add(new ToolBarActionButton(editAction))
    add(new ToolBarActionButton(deleteAction))
    add(new Separator)
    add(new ToolBarActionButton(compileAction))
    add(new Separator)

    def makeButton(name:String, image:String, tool:Tool) = {
      new ToolBarToggleButton(new ToolAction(I18N.gui(name.toLowerCase), image, tool))
    }
    val stockButton = makeButton("Stock", "/images/stock.gif", new StockFigureCreationTool(model, editor))
    val variablButton = makeButton("Variable", "/images/converter.gif", new ConverterFigureCreationTool(model, editor))
    val flowButton = makeButton("Flow", "/images/rate.gif", new RateConnectionTool(model, editor, new RateConnection()))
    val linkButton = makeButton("Link", "/images/connector.gif", new AggregateConnectionTool(model, editor, new BindingConnection()))

    val toolButtonGroup = new ButtonGroup() { add(noToolButton) }
    for (b <- List(stockButton, variablButton, flowButton, linkButton)) {
      add(b)
      toolButtonGroup.add(b)
    }
    add(new Separator())

    // dt Panel
    dtLabel = new JLabel("dt = " + model.getDt) { setOpaque(false) }
    add(new JPanel(new FlowLayout()) with Transparent {
      add(dtLabel)
      dtButton = new Button(changeDTAction)
      add(dtButton)
      setAlignmentX(1.0f)
    })
    // Event listeners
    editor.view.addFigureSelectionListener(new FigureSelectionListener() {
      def figureSelectionChanged(view: DrawingView) {
        editAction.setEnabled(view.selectionCount == 1)
        deleteAction.setEnabled(view.selectionCount == 1)
      }
    })

    syncTheme()
  }

  def popButtons() {noToolButton.setSelected(true)}

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.toolbarBackground)

    if (dtLabel != null)
      dtLabel.setForeground(InterfaceColors.toolbarText)

    if (dtButton != null)
      dtButton.syncTheme()
  }

  private class ModelElementCreationTool(model: Model, editor: DrawingEditor, figure: ModelElementFigure with Figure)
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
  private class ConverterFigureCreationTool(model: Model, editor: DrawingEditor) extends ModelElementCreationTool(model, editor, new ConverterFigure()) {
    // We override these to create a fixed-size shape, rather than allow
    // user to drag out the size
    override def mouseDown(e: MouseEvent, x: Int, y: Int) {
      super.mouseDown(e, x, y)
      super.mouseDrag(e, x + 50, y + 50)
    }
    override def mouseDrag(e: MouseEvent, x: Int, y: Int) {}
  }

  private class StockFigureCreationTool(model: Model, editor: DrawingEditor) extends ModelElementCreationTool(model, editor, new StockFigure()) {
    // We override these to create a fixed-size shape, rather than allow
    // user to drag out the size
    override def mouseDown(e: MouseEvent, x: Int, y: Int) {
      super.mouseDown(e, x, y)
      super.mouseDrag(e, x + 60, y + 40)
    }
    override def mouseDrag(e: MouseEvent, x: Int, y: Int) {}
  }

  /// Actions
  abstract class MyAction(name:String, image:String, enableMe: Boolean)
          extends AbstractAction(I18N.gui(name.toLowerCase)) {
    putValue(Action.SMALL_ICON, SwingUtils.iconScaledWithColor(image, 15, 15, InterfaceColors.toolbarImage))
    setEnabled(enableMe)
  }
  val compileAction = new MyAction("Check", "/images/check.png", enableMe = true) {
    def actionPerformed(e: ActionEvent) {new org.nlogo.window.Events.CompileAllEvent().raise(editor)}
  }
  val editAction = new MyAction("Edit", "/images/edit.png", enableMe = false) {
    def actionPerformed(e: ActionEvent) {editor.inspectFigure(editor.view.selection.nextFigure)}
  }
  val deleteAction = new MyAction("Delete", "/images/delete.png", enableMe = false) {
    def actionPerformed(e: ActionEvent) {
      new DeleteCommand(I18N.gui("delete"), editor).execute()
      new org.nlogo.window.Events.CompileAllEvent().raise(editor)
      new org.nlogo.window.Events.DirtyEvent(None).raise(editor)
    }
  }
  val changeDTAction = new AbstractAction(I18N.gui("edit")) {
    def actionPerformed(e: ActionEvent) {
      val newDt = new InputOptionPane(editor, "", "dt", model.getDt.toString).getInput
      try if (newDt != null) {
        model.setDt(newDt.toDouble)
        dtLabel.setText("dt = " + model.getDt)
        new org.nlogo.window.Events.CompileAllEvent().raise(editor)
        new org.nlogo.window.Events.DirtyEvent(None).raise(editor)
      }
      catch {
        case ex: NumberFormatException => new OptionPane(null, I18N.gui.get("common.messages.error"),
                                                         I18N.gui("dtNumberError"), OptionPane.Options.Ok,
                                                         OptionPane.Icons.Error)
        case ex: Model.ModelException => new OptionPane(null, I18N.gui.get("common.messages.error"),
                                                        I18N.gui("dtZeroError"), OptionPane.Options.Ok,
                                                        OptionPane.Icons.Error)
      }
    }
  }
  class ToolAction(toolName: String, iconName: String, tool: Tool) extends AbstractAction(toolName) {
    putValue(Action.SMALL_ICON, SwingUtils.icon(iconName))
    def actionPerformed(e: ActionEvent) {editor.setTool(tool)}
  }
}
