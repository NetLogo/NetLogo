// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ BorderLayout, Dimension }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, JDialog }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }

import org.nlogo.core.{ I18N, Shape }
import org.nlogo.swing.{ Button, ButtonPanel, InputOptionPane, OptionPane, ScrollPane, Utils }
import org.nlogo.theme.InterfaceColors

class ImportDialog(parent: JDialog, manager: ManagerDialog[_ <: Shape], list: DrawableList[_ <: Shape])
  extends JDialog(parent, I18N.gui.get("tools.shapesEditor.importFromLibrary"), true) with ListSelectionListener {

  private implicit val i18nPrefix = I18N.Prefix("tools.shapesEditor.import")

  locally {
    val importButton = new Button(I18N.gui.get("tools.shapesEditor.import"), importSelectedShapes)
    val cancelButton = new Button(I18N.gui.get("common.buttons.cancel"), dispose)

    getContentPane.setLayout(new BorderLayout(0, 10))
    getContentPane.add(new ScrollPane(list) {
      setBackground(InterfaceColors.dialogBackground)
    }, BorderLayout.CENTER)
    getContentPane.add(new ButtonPanel(Array(importButton, cancelButton)), BorderLayout.SOUTH)

    getRootPane.setDefaultButton(importButton)
  }

  Utils.addEscKeyAction(this, new AbstractAction {
    def actionPerformed(e: ActionEvent) {
      dispose()
    }
  })

  list.update()

  list.setBackground(InterfaceColors.dialogBackground)

  list.addMouseListener(new MouseAdapter {
    // Listen for double-clicks, and edit the selected shape
    override def mouseClicked(e: MouseEvent) {
      if (e.getClickCount > 1)
        importSelectedShapes()
    }
  })

  getContentPane.setBackground(InterfaceColors.dialogBackground)

  pack()

  setLocation(manager.getLocation().x + 10, manager.getLocation().y + 10)
  setVisible(true)

  // Listen for changes in list selection, and make the edit and delete buttons inoperative if necessary
  def valueChanged(e: ListSelectionEvent) {
    val selected = list.getSelectedIndices

    if (selected.length == 1)
      list.ensureIndexIsVisible(selected(0))
  }

  // Import shapes from another model
  private def importSelectedShapes() {
    val choices = List(I18N.gui("replace"), I18N.gui("rename"), I18N.gui.get("common.buttons.cancel"))

    // For each selected shape, add it to the current model's file and the turtledrawer,
    val shapes = for (index <- list.getSelectedIndices) yield {
      val shape = list.getShape(index).get
      var choice = -1

      // If the shape exists, give the user the chance to overwrite or rename
      while (manager.shapesList.exists(shape.name) && choice != 0) {
        choice = new OptionPane(this, I18N.gui.get("tools.shapesEditor.import"),
                                    I18N.gui("nameConflict", shape.name), choices,
                                    OptionPane.Icons.Warning).getSelectedIndex

        if (choice == 1) { // rename
          val name = new InputOptionPane(this, I18N.gui("importShapes"), I18N.gui("importAs")).getInput

          // if the user cancels the inputdialog, then name could
          // be null causing a nullpointerexception later on
          if (name != null)
            shape.name = name
        }

        else if (choice != 0) // 0 == overwrite
          return
      }

      shape
    }

    shapes.foreach(manager.shapesList.addShape)

    // Now update the shapes manager's list and quit this window
    manager.shapesList.update()
    manager.shapesList.selectShapeName("default")

    dispose()
  }

  // Show a warning dialog to indicate something went wrong when importing
  def sendImportWarning(message: String) {
    new OptionPane(this, I18N.gui.get("tools.shapesEditor.import"), message, OptionPane.Options.Ok,
                   OptionPane.Icons.Warning)
  }

  override def getPreferredSize: Dimension =
    new Dimension(super.getPreferredSize.width.max(260), super.getPreferredSize.height)
}
