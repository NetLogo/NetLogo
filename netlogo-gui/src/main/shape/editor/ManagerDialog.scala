// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ BorderLayout, Font, Component }
import java.awt.event.MouseEvent
import java.nio.file.Paths
import javax.swing.{ SwingConstants, Box, BoxLayout, JPanel, JLabel, JDialog }
import javax.swing.event.{ ListSelectionEvent, MouseInputAdapter, ListSelectionListener }

import org.nlogo.api.AbstractModelLoader
import org.nlogo.awt.ColumnLayout
import org.nlogo.core.{ AgentKind, I18N, Model, Shape => CoreShape, ShapeList, ShapeListTracker },
  ShapeList.{ shapesToMap, isDefaultShapeName }
import org.nlogo.swing.{ Button, OptionPane, ScrollPane, Transparent, Utils }
import org.nlogo.swing.Implicits.thunk2action
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.reflect.ClassTag
import scala.util.{ Failure, Success }

abstract class ManagerDialog[A <: CoreShape](parentFrame: java.awt.Frame,
  modelLoader: AbstractModelLoader,
  val shapeListTracker: ShapeListTracker)(implicit ct: ClassTag[A])
  extends JDialog(parentFrame)
  with ListSelectionListener with ThemeSync {

  implicit val i18nPrefix = I18N.Prefix("tools.shapesEditor")

  val shapesList = new DrawableList(shapeListTracker, 10, 34, this)

  protected var importDialog = Option.empty[ImportDialog]

  // abstract defs
  def newShape()
  def editShape()
  def duplicateShape()

  def displayableShapeFromCoreShape(shape: CoreShape): Option[A]

  def modelShapes(m: Model): Seq[CoreShape]

  def shapeKind: AgentKind

  def importButtons: Seq[Component] = Seq(modelImportButton)

  // Create the buttons
  private val modelImportButton = new Button(I18N.gui("importFromModel"), importFromModel)
  private val editButton = new Button(I18N.gui("edit"), editShape)
  private val newButton = new Button(I18N.gui("new"), newShape)
  private val deleteButton = new Button(I18N.gui("delete"), () => {
      ManagerDialog.this.shapesList.deleteShapes()
      editButton.setEnabled(true) // Since at most one shape is highlighted now, enable edit
      setEnabled(true)
      val shape = ManagerDialog.this.shapesList.getOneSelected
      // Don't delete the default turtle
      if (shape.map(_.name).exists(isDefaultShapeName)) {
        setEnabled(false)
      }
  }) {
    setEnabled(false)
  }

  private val duplicateButton = new Button(I18N.gui("duplicate"), duplicateShape)

  protected val libraryLabel = new JLabel(I18N.gui("info"), SwingConstants.CENTER) {
    setFont(new Font(org.nlogo.awt.Fonts.platformFont, Font.PLAIN, 10))
  }

  private val buttonPanel = new JPanel {
    setLayout(new ColumnLayout(3, Component.CENTER_ALIGNMENT, Component.TOP_ALIGNMENT))

    val topRow = new JPanel with Transparent {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
      add(Box.createHorizontalGlue())
      add(Box.createHorizontalStrut(20))
      add(newButton)
      add(Box.createHorizontalStrut(5))
      add(editButton)
      add(Box.createHorizontalStrut(5))
      add(duplicateButton)
      add(Box.createHorizontalStrut(5))
      add(deleteButton)
      add(Box.createHorizontalStrut(20))
      add(Box.createHorizontalGlue())
    }

    val bottomRow = new JPanel with Transparent {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
      add(Box.createHorizontalGlue())
      add(Box.createHorizontalStrut(20))
      importButtons.foreach(add)
      add(Box.createHorizontalStrut(20))
      add(Box.createHorizontalGlue())
    }

    add(topRow)
    add(bottomRow)
    add(libraryLabel)
  }

  private val scrollPane = new ScrollPane(shapesList)

  locally {
    shapesList.addMouseListener(new MouseInputAdapter {
      // double click on a list item will edit it
      override def mouseClicked(e: MouseEvent): Unit = {
        if (e.getClickCount() > 1) editShape()
      }
    })

    Utils.addEscKeyAction(this, () => dispose())

    getContentPane.setLayout(new BorderLayout(0, 10))
    getContentPane.add(scrollPane, BorderLayout.CENTER)
    getContentPane.add(buttonPanel, BorderLayout.SOUTH)

    val maxBounds = getGraphicsConfiguration.getBounds()
    setLocation(maxBounds.x + maxBounds.width / 3, maxBounds.y + maxBounds.height / 3)

    getRootPane.setDefaultButton(editButton)

    pack()
  }

  // Initialize then display the manager
  def init(title: String) {
    shapesList.update()
    shapesList.selectShapeName("default")
    setTitle(title)
    setVisible(true)
  }

  def reset() {
    shapesList.update()
    shapesList.selectShapeName("default")
  }

  // Import shapes from another model
  private def importFromModel(): Unit = {
    try {
      val path = org.nlogo.swing.FileDialog.showFiles(parentFrame, I18N.gui("import.note"), java.awt.FileDialog.LOAD)
      val modelUri = Paths.get(path).toUri
      modelLoader.readModel(modelUri)
        .map(modelShapes)
        .map(drawableListFromModelShapes) match {
          case Failure(ex) =>
            new OptionPane(this, I18N.gui.get("import"), I18N.gui.get("import.invalidError"), OptionPane.Options.Ok,
                           OptionPane.Icons.Error)
          case Success(drawableList) =>
            if (drawableList.shapeList.isEmpty)
              importDialog.foreach(_.sendImportWarning(I18N.gui("import.error")))
            else
              importDialog = Some(new ImportDialog(ManagerDialog.this, this, drawableList))
        }
    } catch {
      case e: org.nlogo.awt.UserCancelException => org.nlogo.api.Exceptions.ignore(e)
    }
    shapesList.requestFocus()
  }

  def drawableListFromModelShapes(shapes: Seq[CoreShape]): DrawableList[A] = {
    val sortedShapes = ShapeList.sortShapes(shapes.flatMap(displayableShapeFromCoreShape))
    val shapeListTracker = new ShapeListTracker(shapeKind, shapesToMap(sortedShapes))
    new DrawableList[A](shapeListTracker, 10, 34, this)
  }

  // Listen for changes in list selection, and make the edit and delete buttons inoperative if necessary
  def valueChanged(e: ListSelectionEvent) {
    val selected = shapesList.getSelectedIndices()
    // Only one shape can be edited or copied at a time
    if (selected.length != 1) {
      editButton.setEnabled(false)
      duplicateButton.setEnabled(false)
    }
    else {
      editButton.setEnabled(true)
      duplicateButton.setEnabled(true)
      shapesList.ensureIndexIsVisible(selected(0))
    }

    if (selected.length == 0 ||
       (selected.length == 1 && ShapeList.isDefaultShapeName(shapesList.elementAt(selected(0)).name)))
      deleteButton.setEnabled(false)
    else // You can't delete the default turtle shapes
      deleteButton.setEnabled(true)
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground)
    buttonPanel.setBackground(InterfaceColors.dialogBackground)
    scrollPane.setBackground(InterfaceColors.dialogBackground)
    shapesList.setBackground(InterfaceColors.dialogBackground)

    modelImportButton.syncTheme()
    editButton.syncTheme()
    newButton.syncTheme()
    deleteButton.syncTheme()
    duplicateButton.syncTheme()

    importButtons.foreach(_ match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    })

    libraryLabel.setForeground(InterfaceColors.dialogText)
  }
}
