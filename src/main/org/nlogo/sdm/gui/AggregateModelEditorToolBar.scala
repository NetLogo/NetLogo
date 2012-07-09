// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.jhotdraw.framework.{DrawingEditor, DrawingView, FigureSelectionListener, Tool}
import org.jhotdraw.standard.{CreationTool, DeleteCommand}
import javax.swing.JToolBar.Separator
import java.awt.event.{ActionEvent, MouseEvent}
import javax.swing.{JOptionPane, ImageIcon, Action, AbstractAction, ButtonGroup, JButton, JLabel, JPanel, JToggleButton}
import org.nlogo.sdm.Model
import org.nlogo.api.I18N

class AggregateModelEditorToolBar(editor: AggregateModelEditor) extends org.nlogo.swing.ToolBar {
  // Invisible button allows no selection in visible buttongroup
  private val noToolButton = new JToggleButton("")
  private var dtLabel: JLabel = null
  implicit val i18nPrefix = I18N.Prefix("tools.sdm")

  override def addControls() {
    add(new JButton(editAction))
    add(new JButton(deleteAction))
    add(new Separator())
    add(new JButton(compileAction))
    add(new Separator())

    def makeButton(name:String, image:String, tool:Tool) = {
      new JToggleButton(new ToolAction(I18N.gui(name.toLowerCase), image, tool))
    }
    val stockButton = makeButton("Stock", "/images/stock.gif", new StockFigureCreationTool(editor))
    val variablButton = makeButton("Variable", "/images/converter.gif", new ConverterFigureCreationTool(editor))
    val flowButton = makeButton("Flow", "/images/rate.gif", new RateConnectionTool(editor, new RateConnection()))
    val linkButton = makeButton("Link", "/images/connector.gif", new AggregateConnectionTool(editor, new BindingConnection()))

    val toolButtonGroup = new ButtonGroup(){ add(noToolButton) }
    for (b <- List(stockButton, variablButton, flowButton, linkButton)) {
      add(b)
      toolButtonGroup.add(b)
    }
    add(new Separator())

    // dt Panel
    dtLabel = new JLabel("dt = " + editor.getModel.getDt){ setOpaque(false) }
    add(new JPanel(new java.awt.FlowLayout()) {
      add(dtLabel)
      add(new JButton(changeDTAction))
      setAlignmentX(1.0f)
    })
    // Event listeners
    editor.view.addFigureSelectionListener(new FigureSelectionListener() {
      def figureSelectionChanged(view: DrawingView) {
        editAction.setEnabled(view.selectionCount == 1)
        deleteAction.setEnabled(view.selectionCount == 1)
      }
    })
  }

  def popButtons() {noToolButton.setSelected(true)}

  /// Figure creation tools
  private class ConverterFigureCreationTool(editor: DrawingEditor) extends CreationTool(editor, new ConverterFigure()) {
    // We override these to create a fixed-size shape, rather than allow
    // user to drag out the size
    override def mouseDown(e: MouseEvent, x: Int, y: Int) {
      super.mouseDown(e, x, y)
      super.mouseDrag(e, x + 50, y + 50)
    }
    override def mouseDrag(e: MouseEvent, x: Int, y: Int) {}
  }

  private class StockFigureCreationTool(editor: DrawingEditor) extends CreationTool(editor, new StockFigure()) {
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
          extends AbstractAction(I18N.gui(name.toLowerCase)){
    putValue(Action.SMALL_ICON, new ImageIcon(classOf[AggregateModelEditorToolBar].getResource(image)))
    setEnabled(enableMe)
  }
  val compileAction = new MyAction("Check", "/images/check.gif", enableMe = true) {
    def actionPerformed(e: ActionEvent) {new org.nlogo.window.Events.CompileAllEvent().raise(editor)}
  }
  val editAction = new MyAction("Edit", "/images/edit.gif", enableMe = false) {
    def actionPerformed(e: ActionEvent) {editor.inspectFigure(editor.view.selection.nextFigure)}
  }
  val deleteAction = new MyAction("Delete", "/images/delete.gif", enableMe = false) {
    def actionPerformed(e: ActionEvent) {
      new DeleteCommand(I18N.gui("delete"), editor).execute()
      new org.nlogo.window.Events.CompileAllEvent().raise(editor)
      new org.nlogo.window.Events.DirtyEvent().raise(editor)
    }
  }
  val changeDTAction = new AbstractAction(I18N.gui("edit")) {
    def actionPerformed(e: ActionEvent) {
      val newDt = JOptionPane.showInputDialog(editor, "dt", editor.getModel.getDt)
      try if (newDt != null) {
        editor.getModel.setDt(newDt.toDouble)
        dtLabel.setText("dt = " + editor.getModel.getDt)
        new org.nlogo.window.Events.CompileAllEvent().raise(editor)
        new org.nlogo.window.Events.DirtyEvent().raise(editor)
      }
      catch {
        case ex: NumberFormatException => JOptionPane.showMessageDialog(null, I18N.gui("dtNumberError"))
        case ex: Model.ModelException => JOptionPane.showMessageDialog(null, I18N.gui("dtZeroError"))
      }
    }
  }
  class ToolAction(toolName: String, iconName: String, tool: Tool) extends AbstractAction(toolName) {
    putValue(Action.SMALL_ICON, new ImageIcon(classOf[AggregateModelEditorToolBar].getResource(iconName)))
    def actionPerformed(e: ActionEvent) {editor.setTool(tool)}
  }
}
