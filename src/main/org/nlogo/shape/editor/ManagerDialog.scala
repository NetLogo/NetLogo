// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import org.nlogo.swing.Implicits._
import javax.swing.{JScrollPane, SwingConstants, Box, BoxLayout, JPanel, JLabel, JDialog, JButton}
import java.awt.{Font, Component}
import javax.swing.event.{ListSelectionEvent, MouseInputAdapter, ListSelectionListener}
import org.nlogo.shape.{VectorShape, ModelSectionReader}
import org.nlogo.api.{I18N, ShapeList}

abstract class ManagerDialog(parentFrame: java.awt.Frame,
                             sectionReader: ModelSectionReader,
                             val shapesList: DrawableList) extends JDialog(parentFrame)
        with ListSelectionListener with ImportDialog.ShapeParser {

  // abstract defs
  def newShape()
  def editShape()
  def duplicateShape()

  // this is horrible
  private var importDialog: ImportDialog = null

  implicit val i18nPrefix = I18N.Prefix("tools.shapesEditor")

  // Create the buttons
  val importButton = new JButton(I18N.gui("importFromModel")) {addActionListener(() => importFromModel())}
  val libraryButton = new JButton(I18N.gui("importFromLibrary")) {addActionListener(() => importFromLibrary())}
  val editButton = new JButton(I18N.gui("edit")) {addActionListener(() => editShape())}
  val newButton = new JButton(I18N.gui("new")) {addActionListener(() => newShape())}
  val deleteButton = new JButton(I18N.gui("delete")) {
    addActionListener(() => {
      ManagerDialog.this.shapesList.deleteShapes()
      editButton.setEnabled(true) // Since at most one shape is highlighted now, enable edit
      setEnabled(true)
      val shape = ManagerDialog.this.shapesList.getOneSelected
      // Don't delete the default turtle
      if (shape != null && ShapeList.isDefaultShapeName(shape.getName)) setEnabled(false)
    })
    setEnabled(false)
  }
  val duplicateButton = new JButton(I18N.gui("duplicate")) {addActionListener(() => duplicateShape())}

  val libraryLabel = new JLabel(I18N.gui("info"), SwingConstants.CENTER) {
    setFont(new Font(org.nlogo.awt.Fonts.platformFont, Font.PLAIN, 10))
  }

  ///
  locally {
    shapesList.setParent(this)
    shapesList.addMouseListener(new MouseInputAdapter() {
      // Listen for double-clicks, and edit the selected shape
      override def mouseClicked(e: java.awt.event.MouseEvent) {if (e.getClickCount() > 1) editShape()}
    })
    shapesList.setCellRenderer(new ShapeCellRenderer(shapesList))

    org.nlogo.swing.Utils.addEscKeyAction(this, () => dispose())

    // Create the scroll pane where the list will be displayed
    val scrollPane = new JScrollPane(shapesList)

    // Add everything to the window
    getContentPane().setLayout(new java.awt.BorderLayout(0, 10))
    getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER)
    // make a panel to hold the two rows of buttons

    getContentPane().add(new JPanel() {
      setLayout(new org.nlogo.awt.ColumnLayout(3, Component.CENTER_ALIGNMENT, Component.TOP_ALIGNMENT))
      // Setup the first row of buttons
      add(new JPanel() {
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
      })
      // Setup the second row of buttons
      add(new JPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
        add(Box.createHorizontalGlue())
        add(Box.createHorizontalStrut(20))
        add(libraryButton)
        add(Box.createHorizontalStrut(5))
        add(importButton)
        add(Box.createHorizontalStrut(20))
        add(Box.createHorizontalGlue())
      })
      add(libraryLabel)
    }, java.awt.BorderLayout.SOUTH)
    pack()

    // Set the window size
    val maxBounds = getGraphicsConfiguration().getBounds()
    setLocation(maxBounds.x + maxBounds.width / 3, maxBounds.y + maxBounds.height / 3)

    // set the default button
    getRootPane().setDefaultButton(editButton)
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

  // Import shapes from shapes library
  private def importFromLibrary() {
    val defaultShapes = org.nlogo.util.Utils.getResourceAsStringArray("/system/defaultShapes.txt")
    val libraryShapes = org.nlogo.util.Utils.getResourceAsStringArray("/system/libraryShapes.txt")
    val mergedShapes = defaultShapes.toList ::: ("" :: libraryShapes.toList)
    importDialog = new ImportDialog(parentFrame, this, mergedShapes.toArray, null, this)
    shapesList.requestFocus()
  }

  // Import shapes from another model
  private def importFromModel() {
    try {
      val path = org.nlogo.swing.FileDialog.show(parentFrame, I18N.gui("import.note"), java.awt.FileDialog.LOAD)
      val shapesV = sectionReader.read(path)
      if (shapesV.length == 0) importDialog.sendImportWarning(I18N.gui("import.error"))
      else importDialog = new ImportDialog(parentFrame, this, shapesV, path, this)
    }
    catch {
      case e: org.nlogo.awt.UserCancelException => org.nlogo.util.Exceptions.ignore(e)
    }
    shapesList.requestFocus()
  }

  def parseShapes(shapes: Array[String], version: String) = VectorShape.parseShapes(shapes, version)

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
            (selected.length == 1 && ShapeList.isDefaultShapeName(shapesList.elementAt(selected(0)).asInstanceOf[String])))
      deleteButton.setEnabled(false)
    else // You can't delete the default turtle shapes
      deleteButton.setEnabled(true)
  }
}
