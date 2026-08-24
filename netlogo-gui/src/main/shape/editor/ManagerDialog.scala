// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ Dimension, Frame }
import java.awt.event.MouseEvent
import java.nio.file.Paths
import javax.swing.{ Box, JLabel, JDialog }
import javax.swing.event.{ DocumentEvent, DocumentListener, ListSelectionEvent, ListSelectionListener, MouseInputAdapter }

import org.nlogo.api.AbstractModelLoader
import org.nlogo.core.{ AgentKind, I18N, Model, Shape => CoreShape, ShapeList, ShapeListTracker },
  ShapeList.{ shapesToMap, isDefaultShapeName }
import org.nlogo.swing.{ BoxColumn, BoxRow, Button, DialogButton, HorizontalStrut, OptionPane, ScrollPane, SyncZoom,
                         TextField, Utils, VerticalStrut, WindowAutomator, ZoomableBorder, ZoomActions }
import org.nlogo.swing.Implicits.thunk2action
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.reflect.ClassTag
import scala.util.{ Failure, Success }

abstract class ManagerDialog[A <: CoreShape](parentFrame: Frame, modelLoader: AbstractModelLoader,
                                             shapeListTracker: ShapeListTracker)(implicit ct: ClassTag[A])
  extends JDialog(parentFrame) with ListSelectionListener with ZoomActions with ThemeSync {

  WindowAutomator.automate(this)

  implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tools.shapesEditor")

  val shapesList = new DrawableList(shapeListTracker, 10, 34, this)

  protected var importDialog: Option[ImportDialog] = None

  // abstract defs
  def newShape(): Unit
  def editShape(): Unit
  def duplicateShape(): Unit

  def displayableShapeFromCoreShape(shape: CoreShape): Option[A]

  def modelShapes(m: Model): Seq[CoreShape]

  def shapeKind: AgentKind

  private val newButton = new DialogButton(false, I18N.gui("new"), () => newShape())
  private val modelImportButton = new DialogButton(false, I18N.gui("importFromModel"), () => importFromModel())

  private val editButton = new DialogButton(false, I18N.gui("edit"), () => editShape())
  private val duplicateButton = new DialogButton(false, I18N.gui("duplicate"), () => duplicateShape())
  private val deleteButton = new DialogButton(false, I18N.gui("delete"), () => {
      shapesList.deleteShapes()
      editButton.setEnabled(true) // Since at most one shape is highlighted now, enable edit
      setEnabled(true)
      val shape = shapesList.getOneSelected
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

    override def getMaximumSize: Dimension =
      new Dimension(super.getMaximumSize.width, getPreferredSize.height)
  }

  private val searchIcon = new JLabel

  locally {
    val contents = new BoxColumn(Seq(
      new BoxRow(Seq(
        newButton,
        new HorizontalStrut(12),
        modelImportButton
      ) ++ additionalButton.fold(Seq())(button => Seq(new HorizontalStrut(12), button)) :+ Box.createHorizontalGlue) {
        override def getMaximumSize: Dimension =
          new Dimension(super.getMaximumSize.width, getPreferredSize.height)
      },
      new VerticalStrut(12),
      scrollPane,
      new VerticalStrut(12),
      new BoxRow(Seq(
        searchIcon,
        new HorizontalStrut(12),
        searchField,
        new HorizontalStrut(12),
        editButton,
        new HorizontalStrut(12),
        duplicateButton,
        new HorizontalStrut(12),
        deleteButton
      )) {
        override def getMaximumSize: Dimension =
          new Dimension(super.getMaximumSize.width, getPreferredSize.height)
      }
    )) with SyncZoom {
      setOpaque(true)
      setBorder(new ZoomableBorder(12, 12, 12, 12))

      override def zoom(oldZoom: Float): Unit = {
        super.zoom(oldZoom)

        setIcons()
        pack()
      }
    }

    setContentPane(contents)

    contents.syncZoom()

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

  protected def additionalButton: Option[Button] = None

  // Initialize then display the manager
  def init(title: String): Unit = {
    shapesList.update(searchOption)
    shapesList.selectShapeName("default")
    setTitle(title)
    setVisible(true)
    pack()
  }

  def reset(): Unit = {
    shapesList.update(searchOption)
    shapesList.selectShapeName("default")
    pack()
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
              importDialog = Some(new ImportDialog(this, this, drawableList))
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
  def valueChanged(e: ListSelectionEvent): Unit = {
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

  private def setIcons(): Unit = {
    val size: Int = Utils.zoom(15)

    searchIcon.setIcon(Utils.iconScaledWithColor("/images/find.png", size, size, InterfaceColors.toolbarImage()))
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())
    scrollPane.setBackground(InterfaceColors.dialogBackground())
    shapesList.setBackground(InterfaceColors.dialogBackground())

    newButton.syncTheme()
    modelImportButton.syncTheme()
    editButton.syncTheme()
    duplicateButton.syncTheme()
    deleteButton.syncTheme()
    searchField.syncTheme()

    additionalButton.foreach(_.syncTheme())

    setIcons()
  }
}
