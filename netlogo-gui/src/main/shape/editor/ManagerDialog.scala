// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ FlowLayout, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.MouseEvent
import java.nio.file.Paths
import javax.swing.{ JLabel, JPanel, JDialog }
import javax.swing.event.{ DocumentEvent, DocumentListener, ListSelectionEvent, ListSelectionListener, MouseInputAdapter }

import org.nlogo.api.AbstractModelLoader
import org.nlogo.core.{ AgentKind, I18N, Model, Shape => CoreShape, ShapeList, ShapeListTracker },
  ShapeList.{ shapesToMap, isDefaultShapeName }
import org.nlogo.swing.{ DialogButton, OptionPane, ScrollPane, TextField, Transparent, Utils }
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

  protected var importDialog: Option[ImportDialog] = None

  // abstract defs
  def newShape()
  def editShape()
  def duplicateShape()

  def displayableShapeFromCoreShape(shape: CoreShape): Option[A]

  def modelShapes(m: Model): Seq[CoreShape]

  def shapeKind: AgentKind

  private val newButton = new DialogButton(true, I18N.gui("new"), () => newShape)
  private val modelImportButton = new DialogButton(true, I18N.gui("importFromModel"), () => importFromModel)

  private val editButton = new DialogButton(false, I18N.gui("edit"), () => editShape)
  private val duplicateButton = new DialogButton(false, I18N.gui("duplicate"), () => duplicateShape)
  private val deleteButton = new DialogButton(false, I18N.gui("delete"), () => {
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

  private val scrollPane = new ScrollPane(shapesList)

  private val searchField = new TextField {
    getDocument.addDocumentListener(new DocumentListener {
      override def changedUpdate(e: DocumentEvent): Unit = {}

      override def insertUpdate(e: DocumentEvent): Unit = {
        shapesList.update(searchOption)
      }

      override def removeUpdate(e: DocumentEvent): Unit = {
        shapesList.update(searchOption)
      }
    })
  }

  private val searchIcon = new JLabel(Utils.iconScaledWithColor("/images/find.png", 15, 15,
                                      InterfaceColors.toolbarImage))

  locally {
    getContentPane.setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridx = 0
    c.gridwidth = 3
    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.HORIZONTAL
    c.insets = new Insets(12, 0, 12, 0)

    add(new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)) with Transparent {
      add(newButton)
      add(modelImportButton)
    }, c)

    c.fill = GridBagConstraints.BOTH
    c.weightx = 1
    c.weighty = 1
    c.insets = new Insets(0, 12, 12, 12)

    add(scrollPane, c)

    c.gridx = 0
    c.gridy = 2
    c.gridwidth = 1
    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.weighty = 0

    add(searchIcon, c)

    c.gridx = 1
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(0, 0, 12, 0)

    add(searchField, c)

    c.gridx = 2
    c.anchor = GridBagConstraints.EAST
    c.weightx = 0
    c.insets = new Insets(0, 0, 12, 0)

    add(new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)) with Transparent {
      add(editButton)
      add(duplicateButton)
      add(deleteButton)
    }, c)

    shapesList.addMouseListener(new MouseInputAdapter {
      // double click on a list item will edit it
      override def mouseClicked(e: MouseEvent): Unit = {
        if (e.getClickCount() > 1) editShape()
      }
    })

    Utils.addEscKeyAction(this, () => dispose())

    val maxBounds = getGraphicsConfiguration.getBounds()
    setLocation(maxBounds.x + maxBounds.width / 3, maxBounds.y + maxBounds.height / 3)

    pack()
  }

  // Initialize then display the manager
  def init(title: String) {
    shapesList.update(searchOption)
    shapesList.selectShapeName("default")
    setTitle(title)
    setVisible(true)
  }

  def reset() {
    shapesList.update(searchOption)
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
            new OptionPane(this, I18N.gui("import"), I18N.gui("import.invalidError"), OptionPane.Options.Ok,
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

  def searchOption: Option[String] = {
    if (searchField.getText.trim.isEmpty) {
      None
    } else {
      Some(searchField.getText.trim)
    }
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground)
    scrollPane.setBackground(InterfaceColors.dialogBackground)
    shapesList.setBackground(InterfaceColors.dialogBackground)

    newButton.syncTheme()
    modelImportButton.syncTheme()
    editButton.syncTheme()
    duplicateButton.syncTheme()
    deleteButton.syncTheme()
    searchField.syncTheme()

    searchIcon.setIcon(Utils.iconScaledWithColor("/images/find.png", 15, 15, InterfaceColors.toolbarImage))
  }
}
