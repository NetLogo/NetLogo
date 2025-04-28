// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ Color, Component, Cursor, Dimension, FlowLayout, Graphics, GridLayout, Insets }
import java.awt.event.{ ActionEvent, WindowAdapter, WindowEvent }
import java.beans.{ PropertyChangeEvent, PropertyChangeListener }
import javax.swing.{ AbstractAction, Action, Box, BoxLayout, ButtonGroup, JDialog, JLabel, JPanel, JToolBar,
                     WindowConstants }
import javax.swing.undo.{ AbstractUndoableEdit, UndoableEdit }

import org.nlogo.api.{ Color => NLColor }
import org.nlogo.awt.ColumnLayout
import org.nlogo.core.{ I18N, Shape }
import org.nlogo.shape.{ Element, VectorShape }
import org.nlogo.swing.{ Button, ButtonPanel, CheckBox, ComboBox, DialogButton, MenuItem, OptionPane, TextField,
                         ToggleButton, Transparent, Utils }
import org.nlogo.theme.InterfaceColors

sealed trait ElementType

object ElementType {
  case object None extends ElementType
  case object Line extends ElementType
  case object Rectangle extends ElementType
  case object Circle extends ElementType
  case object Polygon extends ElementType
}

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

  private var elementType: ElementType = ElementType.None
  private var elementColor = EditorDialog.getColor(shape.getEditableColorIndex)
  private var editingElements = false

  private val previews = Array(
    new ShapePreview(shape, 9, 5),
    new ShapePreview(shape, 12, -4),
    new ShapePreview(shape, 20, 3),
    new ShapePreview(shape, 30, -2),
    new ShapePreview(shape, 50, 1)
  )

  private val nameText = new TextField(4, shape.name) {
    setEnabled(nameEditable)
  }

  private val editElements: ToggleButton = new ToggleButton(new AbstractAction {
    def actionPerformed(e: ActionEvent): Unit = {
      editingElements = editElements.isSelected

      shapeView.deselectAll()

      deleteSelected.setEnabled(false)
      duplicateSelected.setEnabled(false)

      if (editingElements) {
        shapeView.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
      } else {
        shapeView.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
      }

      shapeView.selfFinishPolygon(true)
    }
  }) {
    setIcon(Utils.iconScaledWithColor("/images/shapes-editor/arrow.png", 15, 15, InterfaceColors.toolbarText()))
    setSelected(false)
  }

  // this is where we store the last edit, that we need to able to
  // do.  UndoableEdit is an interface; below we declare various
  // concrete subclasses of AbstractUndoableEdit, which implements
  // UndoableEdit.  This is standard Swing undo API stuff.
  // - SAB/ST 6/11/04
  private var undoableEdit: Option[UndoableEdit] = None

  private val deleteSelected: Button = new Button(I18N.gui("delete"), () => {
    shapeView.getSelectedElement.foreach(element => {
      undoableEdit = Some(new UndoableDeleteEdit(element, shape.getElements.indexOf(element)))

      undoButton.setEnabled(undoableEdit.get.canUndo)
      shape.remove(element)

      shapeView.deselectAll()

      if (shape.getElements.isEmpty) {
        // last element was deleted
        deleteSelected.setEnabled(false)
        bringToFront.setEnabled(false)
        sendToBack.setEnabled(false)

        shape.changed()
      }
    })
  }) {
    setEnabled(false)
  }

  private val duplicateSelected = new Button(I18N.gui("duplicate"), () => {
    shapeView.getSelectedElement.foreach(element => {
      val newElement = element.clone.asInstanceOf[Element]

      shape.add(newElement)
      makeUndoableDraw(newElement)
    })
  }) {
    setEnabled(false)
  }

  private val bringToFront = new Button(I18N.gui("bringToFront"), () => {
    shapeView.getSelectedElement.foreach(element => {
      makeUndoableModification(element, shape.getElements.indexOf(element))

      shape.remove(element)
      shape.add(element)
    })
  }) {
    setEnabled(false)
  }

  private val sendToBack = new Button(I18N.gui("sendToBack"), () => {
    shapeView.getSelectedElement.foreach(element => {
      makeUndoableModification(element, shape.getElements.indexOf(element))

      shape.remove(element)
      shape.addAtPosition(0, element)
    })
  }) {
    setEnabled(false)
  }

  private val undoButton: Button = new Button(I18N.gui("undo"), () => {
    undoableEdit.foreach(edit => {
      edit.undo()

      undoButton.setEnabled(edit.canUndo)

      shapeView.deselectAll()
    })
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

      shapeView.getSelectedElement.foreach(element => {
        if (editingElements) {
          makeUndoableModification(element, shape.getElements.indexOf(element))

          element.awtColor = color
          shapeView.repaint()
        }
      })
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

    new ColorPanel(i)
  }

  private val colorSelection = new ComboBox(colors.toList) {
    addItemListener(_ => setEditableColor())
  }

  colorSelection.setSelectedIndex(shape.getEditableColorIndex)

  private var fillShapes = true
  private var shapeRotatable = shape.isRotatable
  private var snapToGrid = true

  shape.addPropertyChangeListener(this)
  shape.addPropertyChangeListener(shapeView)

  setLocation(parent.getX + 10, parent.getY + 10)
  setResizable(false)
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  private val closingAction = new AbstractAction {
    def actionPerformed(e: ActionEvent): Unit = {
      if (originalShape.toString != getCurrentShape.toString &&
          new OptionPane(EditorDialog.this, I18N.gui("confirmCancel"), I18N.gui("confirmCancel.message"),
                         OptionPane.Options.YesNo, OptionPane.Icons.Question).getSelectedIndex != 0)
        return

      dispose()
    }
  }

  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = {
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

  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("line", ElementType.Line, false))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("rectangleFilled", ElementType.Rectangle, true))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("rectangle", ElementType.Rectangle, false))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("circleFilled", ElementType.Circle, true))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("circle", ElementType.Circle, false))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("polygonFilled", ElementType.Polygon, true))
  addToolBarButton(editingToolBar, editingToolGroup, new CreateAction("polygon", ElementType.Polygon, false))

  private val snapToGridButton = new CheckBox(I18N.gui("snapToGrid"), (selected) => {
    snapToGrid = selected
  }) {
    setForeground(InterfaceColors.dialogText())
    setSelected(true)
  }

  private val rotatableButton = new CheckBox(I18N.gui("rotatable"), (selected) => {
    shapeRotatable = selected

    previews.foreach(_.updateRotation(shapeRotatable))

    shape.setRotatable(shapeRotatable)

    shapeView.repaint()
  }) {
    setForeground(InterfaceColors.dialogText())
    setSelected(shapeRotatable)
  }

  private val rotateLeftButton = new Button(I18N.gui("rotateLeft"), () => {
    shapeView.getSelectedElement match {
      case Some(element) =>
        makeUndoableModification(element, shape.getElements.indexOf(element))

        element.rotateLeft()

      case None =>
        undoableEdit = None

        undoButton.setEnabled(false)

        shape.rotateLeft()
    }

    shapeView.repaint()
  })

  private val rotateRightButton = new Button(I18N.gui("rotateRight"), () => {
    shapeView.getSelectedElement match {
      case Some(element) =>
        makeUndoableModification(element, shape.getElements.indexOf(element))

        element.rotateRight()

      case None =>
        undoableEdit = None

        undoButton.setEnabled(false)

        shape.rotateRight()
    }

    shapeView.repaint()
  })

  private val flipHorizontalButton = new Button(I18N.gui("flipHorizontal"), () => {
    shapeView.getSelectedElement match {
      case Some(element) =>
        makeUndoableModification(element, shape.getElements.indexOf(element))

        element.flipHorizontal()

      case None =>
        undoableEdit = None

        undoButton.setEnabled(false)

        shape.flipHorizontal()
    }

    shapeView.repaint()
  })

  private val flipVerticalButton = new Button(I18N.gui("flipVertical"), () => {
    shapeView.getSelectedElement match {
      case Some(element) =>
        makeUndoableModification(element, shape.getElements.indexOf(element))

        element.flipVertical()

      case None =>
        undoableEdit = None

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
    setForeground(InterfaceColors.dialogText())
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

  private val done = new DialogButton(true, I18N.gui.get("common.buttons.ok"), () => saveShape)
  private val cancel = new DialogButton(false, I18N.gui.get("common.buttons.cancel"), () => dispose)

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

  private val buttonPanel = new ButtonPanel(Seq(done, cancel))
  private val nameLabel = new JLabel(I18N.gui("name")) {
    setForeground(InterfaceColors.dialogText())
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
  getContentPane.setBackground(InterfaceColors.dialogBackground())

  getContentPane.add(Box.createVerticalStrut(10))
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

  def getElementType: ElementType =
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

  def setEditingElements(e: Boolean): Unit = {
    editingElements = e

    editElements.setSelected(e)

    if (e) {
      shapeView.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
    } else {
      shapeView.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))
    }
  }

  def makeUndoableModification(el: Element, z: Int): Unit = {
    val mod = new UndoableModification(el, z)

    undoableEdit = Some(mod)

    undoButton.setEnabled(mod.canUndo)
  }

  def makeUndoableDraw(el: Element): Unit = {
    val draw = new UndoableDraw(el)

    undoableEdit = Some(draw)

    undoButton.setEnabled(draw.canUndo)
  }

  def makeUndoableUnfinishedPolygon(): Unit = {
    val poly = new UndoableUnfinishedPolygon()

    undoableEdit = Some(poly)

    undoButton.setEnabled(poly.canUndo)
  }

  def propertyChange(e: PropertyChangeEvent): Unit = {
    deleteSelected.setEnabled(shapeView.getSelectedElement.isDefined)
    duplicateSelected.setEnabled(shapeView.getSelectedElement.isDefined)
    bringToFront.setEnabled(shapeView.getSelectedElement.isDefined)
    sendToBack.setEnabled(shapeView.getSelectedElement.isDefined)
  }

  // Attempts to save the current shape being drawn, prompting the
  // user if any issues come up
  private def saveShape(): Unit = {
    val name = nameText.getText.trim.toLowerCase

    // Make sure the shape has a name
    if (name.isEmpty) {
      new OptionPane(this, I18N.gui("invalid"), I18N.gui("nameEmpty"), OptionPane.Options.Ok, OptionPane.Icons.Error)

      return
    }

    // If this is an attempt to overwrite a shape, prompt for
    // permission to do it
    if (container.exists(name) && name != originalShape.name &&
        new OptionPane(this, I18N.gui("confirmOverwrite"), I18N.gui("nameConflict"), OptionPane.Options.YesNo,
                       OptionPane.Icons.Question).getSelectedIndex != 0)
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
  private def setEditableColor(): Unit = {
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

  private class ColorPanel(index: Int) extends JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)) with Transparent
                                                                                             with ComboBox.Clone {
    private val label = {
      val name = NLColor.getColorNameByIndex(index)

      new JLabel(name(0).toUpper + name.substring(1))
    }

    add(new JPanel {
      setBackground(EditorDialog.getColor(index))
      setPreferredSize(new Dimension(10, 10))
    })

    add(label)

    override def paintComponent(g: Graphics): Unit = {
      getParent match {
        case item: MenuItem if item.isArmed => label.setForeground(InterfaceColors.menuTextHover())
        case _ => label.setForeground(InterfaceColors.toolbarText())
      }

      super.paintComponent(g)
    }

    def getClone: Component =
      new ColorPanel(index)
  }

  private class CreateAction(name: String, typeID: ElementType, filled: Boolean)
    extends AbstractAction(name) {

    putValue(Action.SMALL_ICON, Utils.iconScaledWithColor("/images/shapes-editor/" + name + ".png", 15, 15,
                                                          InterfaceColors.toolbarText()))
    putValue(Action.SHORT_DESCRIPTION, I18N.gui(name))

    def actionPerformed(e: ActionEvent): Unit = {
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
    private val originalElement = el.clone.asInstanceOf[Element]

    override def undo(): Unit = {
      super.undo()

      shape.remove(el)
      shape.addAtPosition(zOrder, originalElement)

      if (shapeView.getSelectedElement.isDefined)
        shapeView.selectElement(originalElement)
    }
  }

  // this is used when the user creates a new element
  private class UndoableDraw(newElement: Element) extends AbstractUndoableEdit {
    override def undo(): Unit = {
      super.undo()

      shape.remove(newElement)
    }
  }

  // this is used when the user deletes an element
  private class UndoableDeleteEdit(el: Element, zOrder: Int) extends AbstractUndoableEdit {
    private val deletedElement = el.clone().asInstanceOf[Element]

    override def undo(): Unit = {
      super.undo()

      shape.addAtPosition(zOrder, deletedElement)
    }
  }

  // this is used when the user is in the middle of drawing a polygon;
  // if they press undo at that time, we abort the creation of that polygon.
  private class UndoableUnfinishedPolygon extends AbstractUndoableEdit {
    override def undo(): Unit = {
      super.undo()

      shapeView.setTempElement(null)
    }
  }
}
