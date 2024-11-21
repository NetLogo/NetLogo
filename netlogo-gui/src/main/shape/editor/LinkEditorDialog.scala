// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ BasicStroke, Component, Graphics, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ ActionEvent, WindowAdapter, WindowEvent }
import javax.swing.{ AbstractAction, Icon, JDialog, JLabel, WindowConstants }

import org.nlogo.core.{ I18N, Shape, ShapeList }
import org.nlogo.shape.{ LinkLine, LinkShape, VectorShape }
import org.nlogo.swing.{ Button, ButtonPanel, ComboBox, LabeledComponent, OptionPane, TextField, Utils }
import org.nlogo.theme.InterfaceColors

import scala.util.{ Failure, Success, Try }

class LinkEditorDialog(parent: JDialog, list: DrawableList[LinkShape], shape: LinkShape)
  extends JDialog(parent, I18N.gui.get("tools.linkEditor"), true) with EditorDialog.VectorShapeContainer {
  
  private implicit val i18nPrefix = I18N.Prefix("tools.linkEditor")

  private val name = new TextField(shape.name, 10)
  private val curviness = new TextField(shape.curviness.toString, 10)

  private val dashes =
    (for (i <- 0 until 3) yield {
       new DashComboBox(LinkLine.dashChoices) {
         setSelectedIndex(shape.getLine(i).dashIndex)
       }
     }).toArray

  private val originalShape = shape

  def update(originalShape: Shape, newShape: Shape) {
    shape.directionIndicator = newShape.asInstanceOf[VectorShape]
  }

  def exists(name: String): Boolean =
    false

  setResizable(false)
  setLocation(parent.getX + 10, parent.getY + 10)
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent) {
      saveShape()
      dispose()
      setVisible(false)
    }
  })

  Utils.addEscKeyAction(this, new AbstractAction {
    def actionPerformed(e: ActionEvent) {
      if (originalShape.toString != getCurrentShape.toString ||
          new OptionPane(LinkEditorDialog.this, I18N.gui.get("tools.shapesEditor.confirmCancel"),
                         I18N.gui.get("tools.shapesEditor.confirmCancel.message"), OptionPane.Options.YES_NO,
                         OptionPane.Icons.QUESTION).getSelectedIndex != 0)
        return

      dispose()
    }
  })

  setLayout(new GridBagLayout)

  getContentPane.setBackground(InterfaceColors.DIALOG_BACKGROUND)

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.HORIZONTAL
    c.insets = new Insets(6, 6, 6, 6)

    add(new LabeledComponent(I18N.gui("name"), name) {
      setForeground(InterfaceColors.DIALOG_TEXT)
    }, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(new LabeledComponent(I18N.gui("direction"), new Button(I18N.gui("edit"), () => {
      new EditorDialog(LinkEditorDialog.this, LinkEditorDialog.this, shape.directionIndicator, false)
    })) {
      setForeground(InterfaceColors.DIALOG_TEXT)
    }, c)

    add(new LabeledComponent(I18N.gui("curviness"), curviness) {
      setForeground(InterfaceColors.DIALOG_TEXT)
    }, c)

    add(new LabeledComponent(I18N.gui("leftLine"), dashes(2)) {
      setForeground(InterfaceColors.DIALOG_TEXT)
    }, c)

    add(new LabeledComponent(I18N.gui("middleLine"), dashes(1)) {
      setForeground(InterfaceColors.DIALOG_TEXT)
    }, c)

    add(new LabeledComponent(I18N.gui("rightLine"), dashes(0)) {
      setForeground(InterfaceColors.DIALOG_TEXT)
    }, c)

    val cancel = new Button(I18N.gui.get("common.buttons.cancel"), dispose)
    val done = new Button(I18N.gui.get("common.buttons.ok"), saveShape)

    add(new ButtonPanel(Array(done, cancel)), c)

    getRootPane.setDefaultButton(done)
  }

  list.update()

  pack()
  
  // when name is not enabled focus goes to the curviness
  // field instead ev 2/18/08
  if (ShapeList.isDefaultShapeName(shape.name)) {
    name.setEnabled(false)
    curviness.requestFocus()
  }

  else {
    name.setEnabled(true)
    name.requestFocus()
  }

  setVisible(true)

  private def saveShape() {
    val nameStr = name.getText.trim.toLowerCase

    // Make sure the shape has a name
    if (nameStr.isEmpty) {
      new OptionPane(this, I18N.gui("invalid"), I18N.gui("nameEmpty"), OptionPane.Options.OK, OptionPane.Icons.ERROR)

      return
    }

    // If this is an attempt to overwrite a shape, prompt for permission to do it
    if (list.exists(nameStr) && nameStr != originalShape.name &&
        new OptionPane(this, I18N.gui("confirmOverwrite"), I18N.gui("nameConflict"), OptionPane.Options.YES_NO,
                       OptionPane.Icons.QUESTION).getSelectedIndex != 0)
      return
    
    shape.name = nameStr

    Try(curviness.getText.toDouble) match {
      case Success(cv) => shape.curviness = cv
      case Failure(_) =>
        new OptionPane(this, I18N.gui("invalid"), I18N.gui("invalidCurviness"), OptionPane.Options.OK,
                       OptionPane.Icons.ERROR)
        
        return
    }

    for (i <- 0 until dashes.size) {
      val index = dashes(i).getSelectedIndex

      shape.setLineVisible(i, index != 0)
      shape.getLine(i).dashes = LinkLine.dashChoices(index)
    }

    list.update(originalShape, shape)

    dispose()
  }

  private def getCurrentShape: LinkShape = {
    val currentShape = shape.clone.asInstanceOf[LinkShape]
    currentShape.name = name.getText
    currentShape.curviness = curviness.getText.toDouble

    for (i <- 0 until dashes.size) {
      val index = dashes(i).getSelectedIndex

      currentShape.setLineVisible(i, index != 0)
      currentShape.getLine(i).dashes = LinkLine.dashChoices(index)
    }

    currentShape
  }

  private class DashComboBox(items: List[Array[Float]]) extends ComboBox[JLabel] {
    private class DashLabel(item: Array[Float]) extends JLabel with ComboBox.Clone {
      setIcon(new Icon {
        def getIconWidth = 85
        def getIconHeight = 18

        def paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
          if (item(0) != 0) {
            val g2d = Utils.initGraphics2D(g)

            g2d.setColor(InterfaceColors.TOOLBAR_TEXT)
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, item, 0))
            g2d.drawLine(x, y + getIconHeight / 2, x + getIconWidth, y + getIconHeight / 2)
          }
        }
      })

      def getClone: Component =
        new DashLabel(item)
    }

    setItems(items.map(new DashLabel(_)))
  }
}
