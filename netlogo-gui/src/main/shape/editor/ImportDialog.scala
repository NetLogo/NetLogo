// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ BorderLayout, Dimension }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, JDialog }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }

import org.nlogo.core.{ LocaleContext, Shape }
import org.nlogo.swing.{ ButtonPanel, DialogButton, InputOptionPane, OptionPane, ScrollPane, Utils }
import org.nlogo.theme.InterfaceColors

class ImportDialog(parent: JDialog, manager: ManagerDialog[? <: Shape], list: DrawableList[? <: Shape])
  extends JDialog(parent, loc"Import from Library...", true) with ListSelectionListener {

  locally {
    val importButton = new DialogButton(true, loc"Import", () => importSelectedShapes())
    val cancelButton = new DialogButton(false, loc"Cancel", () => dispose)

    getContentPane.setLayout(new BorderLayout(0, 10))
    getContentPane.add(new ScrollPane(list) {
      setBackground(InterfaceColors.dialogBackground())
    }, BorderLayout.CENTER)
    getContentPane.add(new ButtonPanel(Seq(importButton, cancelButton)), BorderLayout.SOUTH)

    getRootPane.setDefaultButton(importButton)
  }

  Utils.addEscKeyAction(this, new AbstractAction {
    def actionPerformed(e: ActionEvent): Unit = {
      dispose()
    }
  })

  list.update()

  list.setBackground(InterfaceColors.dialogBackground())

  list.addMouseListener(new MouseAdapter {
    // Listen for double-clicks, and edit the selected shape
    override def mouseClicked(e: MouseEvent): Unit = {
      if (e.getClickCount > 1)
        importSelectedShapes()
    }
  })

  getContentPane.setBackground(InterfaceColors.dialogBackground())

  pack()

  setLocation(manager.getLocation().x + 10, manager.getLocation().y + 10)
  setVisible(true)

  // Listen for changes in list selection, and make the edit and delete buttons inoperative if necessary
  def valueChanged(e: ListSelectionEvent): Unit = {
    val selected = list.getSelectedIndices

    if (selected.length == 1)
      list.ensureIndexIsVisible(selected(0))
  }

  // Import shapes from another model
  private def importSelectedShapes(): Unit = {
    val choices = Seq(loc"Replace", loc"Rename", loc"Cancel")

    // For each selected shape, add it to the current model's file and the turtledrawer
    val shapes: Option[Seq[Shape]] = list.getSelectedIndices.foldLeft(Option(Seq[Shape]())) {
      // if the user already canceled, skip the rest of the selected shapes (Isaac B 4/25/25)
      case (None, _) =>
        None

      case (Some(s), index) =>
        val shape = list.getShape(index).get
        var choice = 1

        // If the shape exists, give the user the chance to overwrite or rename
        while (manager.shapesList.exists(shape.name) && choice == 1) {
          val message = loc"""|A shape with the name "${shape.name}" already exists in this model.\n
                              |Do you want to replace the existing shape or rename the imported one?"""

          choice = new OptionPane(this, loc"Import", message, choices, OptionPane.Icons.warning).getSelectedIndex

          if (choice == 1) { // rename
            val name = new InputOptionPane(this, loc"Import Shapes", loc"Import Shape As").getInput

            // if the user cancels the inputdialog, then name could
            // be null causing a nullpointerexception later on
            if (name != null)
              shape.name = name
          }
        }

        if (choice == -1 || choice == 2) {
          None
        } else {
          Some(s :+ shape)
        }
    }

    shapes.foreach { s =>
      s.foreach(manager.shapesList.addShape)

      // Now update the shapes manager's list and quit this window
      manager.shapesList.update()
      manager.shapesList.selectShapeName("default")

      dispose()
    }
  }

  // Show a warning dialog to indicate something went wrong when importing
  def sendImportWarning(message: String): Unit = {
    new OptionPane(this, loc"Import", message, OptionPane.Options.Ok,
                   OptionPane.Icons.warning)
  }

  override def getPreferredSize: Dimension =
    new Dimension(super.getPreferredSize.width.max(260), super.getPreferredSize.height)
}
