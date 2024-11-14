// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ Color, Component, Cursor, Dimension, GridLayout, Insets }
import java.awt.event.{ ActionEvent, ActionListener, WindowAdapter, WindowEvent }
import java.beans.{ PropertyChangeEvent, PropertyChangeListener }
import java.lang.{ Class, Integer }
import javax.swing.{ AbstractAction, Action, Box, BoxLayout, ButtonGroup, JComboBox, JDialog, JLabel, JOptionPane,
                     JPanel, JToolBar, WindowConstants }
import javax.swing.undo.{ AbstractUndoableEdit, UndoableEdit }

import org.nlogo.api.{ Color => NLColor }
import org.nlogo.awt.ColumnLayout
import org.nlogo.core.{ I18N, Shape }
import org.nlogo.shape.{ Circle, Element, Line, Polygon, Rectangle, VectorShape }
import org.nlogo.swing.{ Button, ButtonPanel, CheckBox, TextField, ToggleButton, Transparent, Utils }
import org.nlogo.theme.InterfaceColors

object EditorDialog {
  trait VectorShapeContainer {
    def exists(name: String): Boolean
    def update(original: Shape, newShape: Shape): Unit
  }

  def getColor(index: Int): Color =
    new Color(NLColor.getARGBByIndex(index))
}

class EditorDialog(parent: JDialog, container: EditorDialog.VectorShapeContainer, originalShape: VectorShape,
                   nameEditable: Boolean) extends JDialog(parent, I18N.gui.get("tools.shapesEditor"), true)
                                          with PropertyChangeListener {

  private implicit val i18nPrefix = I18N.Prefix("tools.shapesEditor")

  private val shape = originalShape.clone()
  private val shapeView = new ShapeView(this, shape)

  private var elementType: Class[_ <: Element]  = null
  private var elementColor = EditorDialog.getColor(shape.getEditableColorIndex)
  private var editingElements = false

  private val previews = Array(
    new ShapePreview(shape, 9, 5),
    new ShapePreview(shape, 12, -4),
    new ShapePreview(shape, 20, 3),
    new ShapePreview(shape, 30, -2),
    new ShapePreview(shape, 50, 1)
  )

  private val nameText = new TextField(4) {
    setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    setForeground(InterfaceColors.TOOLBAR_TEXT)
    setCaretColor(InterfaceColors.TOOLBAR_TEXT)

    setText(shape.name)
    setEnabled(nameEditable)
  }

  private val editElements: ToggleButton = new ToggleButton(new AbstractAction {
    def actionPerformed(e: ActionEvent) {
      editingElements = editElements.isSelected

      shapeView.deselectAll()

      deleteSelected.setEnabled(false)
      duplicateSelected.setEnabled(false)

      if (editingElements)
        shapeView.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
      else
        shapeView.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
      
      if (shapeView.drawingPolygon)
        shapeView.selfFinishPolygon(true)
    }
  }) {
    setIcon(Utils.icon("/images/shapes-editor/arrow.gif"))
    setSelected(false)
  }

  // this is where we store the last edit, that we need to able to
  // do.  UndoableEdit is an interface; below we declare various
  // concrete subclasses of AbstractUndoableEdit, which implements
  // UndoableEdit.  This is standard Swing undo API stuff.
  // - SAB/ST 6/11/04
  private var undoableEdit: UndoableEdit = null

  private val deleteSelected: Button = new Button(I18N.gui("delete"), () => {
    undoableEdit = new UndoableDeleteEdit(shapeView.getSelectedElement,
                                          shape.getElements.indexOf(shapeView.getSelectedElement))

    undoButton.setEnabled(undoableEdit.canUndo)
    shape.remove(shapeView.getSelectedElement)

    shapeView.deselectAll()

    if (shape.getElements.isEmpty) {
      // last element was deleted
      deleteSelected.setEnabled(false)
      bringToFront.setEnabled(false)
      sendToBack.setEnabled(false)

      shape.changed()
    }
  }) {
    setEnabled(false)
  }

  private val duplicateSelected = new Button(I18N.gui("duplicate"), () => {
    if (shapeView.getSelectedElement != null) {
      val newElement = shapeView.getSelectedElement.clone().asInstanceOf[Element]

      shape.add(newElement)
      makeUndoableDraw(newElement)
    }
  }) {
    setEnabled(false)
  }

  private val bringToFront = new Button(I18N.gui("bringToFront"), () => {
    makeUndoableModification(shapeView.getSelectedElement, shape.getElements.indexOf(shapeView.getSelectedElement))

    shape.remove(shapeView.getSelectedElement)
    shape.add(shapeView.getSelectedElement)
  }) {
    setEnabled(false)
  }

  private val sendToBack = new Button(I18N.gui("sendToBack"), () => {
    makeUndoableModification(shapeView.getSelectedElement, shape.getElements.indexOf(shapeView.getSelectedElement))

    shape.remove(shapeView.getSelectedElement)
    shape.addAtPosition(0, shapeView.getSelectedElement)
  }) {
    setEnabled(false)
  }

  private val undoButton: Button = new Button(I18N.gui("undo"), () => {
    undoableEdit.undo()

    undoButton.setEnabled(undoableEdit.canUndo)

    shapeView.deselectAll()
  }) {
    setEnabled(false)
  }

  private val colorGrid = new JToolBar with Transparent {
    setFloatable(false)
    setLayout(new GridLayout(4, 4, 3, 3))
  }

  private val colorGroup = new ButtonGroup

  private val colors = for (i <- 0 until NLColor.ColorNames.size) yield {
    val name = NLColor.ColorNames(i)
    val color = EditorDialog.getColor(i)

    val button = new ToggleButton(name, () => {
      elementColor = color

      if (editingElements && shapeView.getSelectedElement != null) {
        makeUndoableModification(shapeView.getSelectedElement, shape.getElements.indexOf(shapeView.getSelectedElement))

        shapeView.getSelectedElement.awtColor = color
        shapeView.repaint()
      }
    }) {
      setText(null)
      setBorder(null)
      setToolTipText(I18N.gui("drawIn") + " " + name)
      setBackgroundColor(color)
      setBackgroundHoverColor(color)

      override def getPreferredSize: Dimension =
        new Dimension(20, 20)
    }

    colorGrid.add(button)
    colorGroup.add(button)

    if (i == shape.getEditableColorIndex)
        button.setSelected(true)

    Integer.valueOf(i)
  }

  private val colorSelection = new JComboBox(colors.toArray) {
    setRenderer(new ColorCellRenderer)
    setSelectedIndex(shape.getEditableColorIndex)

    addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        setEditableColor()
      }
    })
  }

  private var fillShapes = true
  private var shapeRotatable = shape.isRotatable
  private var snapToGrid = true

  shape.addPropertyChangeListener(this)
  shape.addPropertyChangeListener(shapeView)

  setLocation(parent.getX + 10, parent.getY + 10)
  setResizable(false)
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  private val closingAction = new AbstractAction {
    def actionPerformed(e: ActionEvent) {
      if (originalShape.toString != getCurrentShape.toString &&
          JOptionPane.showConfirmDialog(EditorDialog.this, I18N.gui("confirmCancel.message"),
                                        I18N.gui("confirmCancel"), JOptionPane.YES_NO_OPTION) != 0)
        return

      dispose()
    }
  }
  
  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent) {
      closingAction.actionPerformed(null)
    }
  })

  Utils.addEscKeyAction(this, closingAction)

  private val leftPanel = new JPanel(new ColumnLayout(0, Component.CENTER_ALIGNMENT, Component.TOP_ALIGNMENT))
                            with Transparent
  private val rightPanel = new JPanel(new ColumnLayout(0, Component.CENTER_ALIGNMENT, Component.TOP_ALIGNMENT))
                             with Transparent

  private val editingToolBar = new JToolBar with Transparent {
    setFloatable(false)
    setLayout(new GridLayout(4, 2, 3, 3))
  }

  private val editingToolGroup = new ButtonGroup

  editElements.setToolTipText(I18N.gui("select"))

  editingToolBar.add(editElements)
  editingToolGroup.add(editElements)

  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("line", classOf[Line], false))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("rectangleFilled", classOf[Rectangle], true))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("rectangle", classOf[Rectangle], false))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("circleFilled", classOf[Circle], true))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("circle", classOf[Circle], false))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("polygonFilled", classOf[Polygon], true))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("polygon", classOf[Polygon], false))

  private val snapToGridButton: CheckBox = new CheckBox(I18N.gui("snapToGrid"), () => {
    snapToGrid = snapToGridButton.isSelected
  }) {
    setForeground(InterfaceColors.DIALOG_TEXT)
    setSelected(true)
  }

  private val rotatableButton: CheckBox = new CheckBox(I18N.gui("rotatable"), () => {
    shapeRotatable = rotatableButton.isSelected

    previews.foreach(_.updateRotation(shapeRotatable))

    shape.setRotatable(shapeRotatable)

    shapeView.repaint()
  }) {
    setForeground(InterfaceColors.DIALOG_TEXT)
    setSelected(shapeRotatable)
  }

  private val rotateLeftButton = new Button(I18N.gui("rotateLeft"), () => {
    if (shapeView.hasSelectedElement) {
      makeUndoableModification(shapeView.getSelectedElement, shape.getElements.indexOf(shapeView.getSelectedElement))

      shapeView.getSelectedElement.rotateLeft()
    }
    
    else {
      undoableEdit = null

      undoButton.setEnabled(false)

      shape.rotateLeft()
    }

    shapeView.repaint()
  })

  private val rotateRightButton = new Button(I18N.gui("rotateRight"), () => {
    if (shapeView.hasSelectedElement) {
      makeUndoableModification(shapeView.getSelectedElement, shape.getElements.indexOf(shapeView.getSelectedElement))

      shapeView.getSelectedElement.rotateRight()
    }
    
    else {
      undoableEdit = null

      undoButton.setEnabled(false)

      shape.rotateRight()
    }

    shapeView.repaint()
  })

  private val flipHorizontalButton = new Button(I18N.gui("flipHorizontal"), () => {
    if (shapeView.hasSelectedElement) {
      makeUndoableModification(shapeView.getSelectedElement, shape.getElements.indexOf(shapeView.getSelectedElement))

      shapeView.getSelectedElement.flipHorizontal()
    }
    
    else {
      undoableEdit = null

      undoButton.setEnabled(false)

      shape.flipHorizontal()
    }

    shapeView.repaint()
  })

  private val flipVerticalButton = new Button(I18N.gui("flipVertical"), () => {
    if (shapeView.hasSelectedElement) {
      makeUndoableModification(shapeView.getSelectedElement, shape.getElements.indexOf(shapeView.getSelectedElement))

      shapeView.getSelectedElement.flipVertical()
    }
    
    else {
      undoableEdit = null

      undoButton.setEnabled(false)

      shape.flipVertical()
    }

    shapeView.repaint()
  })

  leftPanel.add(editingToolBar)
  leftPanel.add(snapToGridButton)
  leftPanel.add(colorGrid)
  leftPanel.add(Box.createVerticalStrut(10))
  leftPanel.add(new JLabel(I18N.gui("colorChanges")) {
    setForeground(InterfaceColors.DIALOG_TEXT)
  })
  leftPanel.add(Box.createVerticalStrut(3))
  leftPanel.add(colorSelection)
  leftPanel.add(deleteSelected)
  leftPanel.add(duplicateSelected)
  leftPanel.add(bringToFront)
  leftPanel.add(sendToBack)
  leftPanel.add(undoButton)
  leftPanel.add(rotatableButton)

  rightPanel.add(rotateLeftButton)
  rightPanel.add(rotateRightButton)
  rightPanel.add(flipHorizontalButton)
  rightPanel.add(flipVerticalButton)

  private val cancel = new Button(I18N.gui.get("common.buttons.cancel"), dispose)
  private val done = new Button(I18N.gui.get("common.buttons.ok"), saveShape)

  previews.foreach(shape.addPropertyChangeListener)

  private val previewPanel = new JPanel {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

    previews.foreach(add)
  }

  private val graphicPanel = new JPanel {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

    add(shapeView)
    add(previewPanel)
  }

  private val drawingPanel = new JPanel with Transparent {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

    add(Box.createHorizontalStrut(10))
    add(leftPanel)
    add(Box.createHorizontalStrut(15))
    add(graphicPanel)
    add(Box.createHorizontalStrut(15))
    add(rightPanel)
    add(Box.createHorizontalStrut(10))
  }

  private val buttonPanel = new ButtonPanel(Array(done, cancel))
  private val nameLabel = new JLabel(I18N.gui("name")) {
    setForeground(InterfaceColors.DIALOG_TEXT)
  }

  private val namePanel = new JPanel with Transparent {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

    add(Box.createHorizontalStrut(10))
    add(nameLabel)
    add(Box.createHorizontalStrut(5))
    add(nameText)
    add(Box.createHorizontalStrut(5))
  }

  getContentPane.setLayout(new BoxLayout(getContentPane, BoxLayout.Y_AXIS))
  getContentPane.setBackground(InterfaceColors.DIALOG_BACKGROUND)

  getContentPane.add(Box.createVerticalStrut(10));
  getContentPane.add(namePanel)
  getContentPane.add(Box.createVerticalStrut(15))
  getContentPane.add(drawingPanel)
  getContentPane.add(Box.createVerticalStrut(15))
  getContentPane.add(buttonPanel)
  getContentPane.add(Box.createVerticalStrut(10))

  getRootPane.setDefaultButton(done)

  previews.foreach(_.updateRotation(shapeRotatable))

  pack()

  nameText.requestFocus()

  setEditingElements(true)
  setVisible(true)

  shape.changed()

  def getElementType: Class[_ <: Element] =
    elementType

  def getElementColor: Color =
    elementColor

  def isFillShapes: Boolean =
    fillShapes

  def isRotatable: Boolean =
    shapeRotatable

  def isSnapToGrid: Boolean =
    snapToGrid

  def isEditingElements: Boolean =
    editingElements

  def setEditingElements(e: Boolean) {
    editingElements = e

    editElements.setSelected(e)

    if (e)
      shapeView.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
    else
      shapeView.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
  }

  def makeUndoableModification(el: Element, z: Int) {
    undoableEdit = new UndoableModification(el, z)

    undoButton.setEnabled(undoableEdit.canUndo)
  }

  def makeUndoableDraw(el: Element) {
    undoableEdit = new UndoableDraw(el)

    undoButton.setEnabled(undoableEdit.canUndo)
  }

  def makeUndoableUnfinishedPolygon() {
    undoableEdit = new UndoableUnfinishedPolygon()

    undoButton.setEnabled(undoableEdit.canUndo)
  }

  def propertyChange(e: PropertyChangeEvent) {
    deleteSelected.setEnabled(shapeView.hasSelectedElement)
    duplicateSelected.setEnabled(shapeView.hasSelectedElement)
    bringToFront.setEnabled(shapeView.hasSelectedElement)
    sendToBack.setEnabled(shapeView.hasSelectedElement)
  }

  // Attempts to save the current shape being drawn, prompting the
  // user if any issues come up
  private def saveShape() {
    val name = nameText.getText.trim.toLowerCase

    // Make sure the shape has a name
    if (name.isEmpty) {
      JOptionPane.showMessageDialog(this, I18N.gui("nameEmpty"), I18N.gui("invalid"), JOptionPane.PLAIN_MESSAGE)

      return
    }

    // If this is an attempt to overwrite a shape, prompt for
    // permission to do it
    if (container.exists(name) && name != originalShape.name &&
        JOptionPane.showConfirmDialog(this, I18N.gui("nameConflict"), I18N.gui("confirmOverwrite"),
                                      JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
      return

    val newShape = shape

    newShape.name = name
    newShape.setRotatable(shapeRotatable)
    newShape.markRecolorableElements(EditorDialog.getColor(shape.getEditableColorIndex), shape.getEditableColorIndex)

    container.update(originalShape, newShape)

    dispose()
  }

  private def getCurrentShape: VectorShape = {
    val currentShape = shape.clone()

    currentShape.name = nameText.getText
    currentShape.setRotatable(shapeRotatable)
    currentShape.markRecolorableElements(EditorDialog.getColor(shape.getEditableColorIndex),
                                         shape.getEditableColorIndex)

    currentShape
  }

  // Sets <editableColor> to whatever the current selection in
  // <colorSelection> is
  private def setEditableColor() {
    shape.setEditableColorIndex(colorSelection.getSelectedIndex)
    shape.markRecolorableElements(EditorDialog.getColor(shape.getEditableColorIndex), shape.getEditableColorIndex)
  }

  private def addToolBarButton(toolbar: JToolBar, group: ButtonGroup, action: Action): ToggleButton = {
    new ToggleButton(action) {
      setBorder(null)
      setText(null)

      toolbar.add(this)
      group.add(this)

      override def getInsets: Insets =
        new Insets(3, 3, 3, 3)
    }
  }

  private class CreateAction(name: String, typeID: Class[_ <: Element], filled: Boolean)
    extends AbstractAction(name) {

    putValue(Action.SMALL_ICON, Utils.icon("/images/shapes-editor/" + name + ".gif"))
    putValue(Action.SHORT_DESCRIPTION, I18N.gui(name))

    def actionPerformed(e: ActionEvent) {
      if (shapeView.drawingPolygon)
        shapeView.selfFinishPolygon(true)
      
      elementType = typeID
      fillShapes = filled
      editingElements = false

      shapeView.deselectAll()
      shapeView.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
    }
  }

  // this is used when the user modifies an existing shape, such as
  // by moving it or dragging a handle
  private class UndoableModification(el: Element, zOrder: Int) extends AbstractUndoableEdit {
    private val originalElement = el.clone().asInstanceOf[Element]

    override def undo() {
      super.undo()

      shape.remove(el)
      shape.addAtPosition(zOrder, originalElement)

      if (shapeView.hasSelectedElement)
        shapeView.selectElement(originalElement)
    }
  }

  // this is used when the user creates a new element
  private class UndoableDraw(newElement: Element) extends AbstractUndoableEdit {
    override def undo() {
      super.undo()

      shape.remove(newElement)
    }
  }

  // this is used when the user deletes an element
  private class UndoableDeleteEdit(el: Element, zOrder: Int) extends AbstractUndoableEdit {
    private val deletedElement = el.clone().asInstanceOf[Element]

    override def undo() {
      super.undo()

      shape.addAtPosition(zOrder, deletedElement)
    }
  }

  // this is used when the user is in the middle of drawing a polygon;
  // if they press undo at that time, we abort the creation of that polygon.
  private class UndoableUnfinishedPolygon extends AbstractUndoableEdit {
    override def undo() {
      super.undo()

      shapeView.setTempElement(null)
    }
  }
}
