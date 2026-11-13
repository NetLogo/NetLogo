// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ BasicStroke, Component, Graphics }
import java.awt.event.{ ActionEvent, WindowAdapter, WindowEvent }
import javax.swing.{ AbstractAction, Icon, JDialog, JLabel, WindowConstants }

import org.nlogo.analytics.Analytics
import org.nlogo.core.{ LocaleContext, Shape, ShapeList }
import org.nlogo.shape.{ LinkLine, LinkShape, VectorShape }
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, Button, ButtonPanel, ComboBox, DialogButton, LabeledComponent,
                         OptionPane, TextField, Utils, ZoomableBorder, ZoomableWindow }
import org.nlogo.theme.InterfaceColors

import scala.util.{ Failure, Success, Try }

class LinkEditorDialog(parent: JDialog, list: DrawableList[LinkShape], shape: LinkShape)
  extends JDialog(parent, loc"Link Shape", true) with EditorDialog.VectorShapeContainer
  with ZoomableWindow(Option(parent)) {

  private val name = new TextField(10, shape.name)
  private val curviness = new TextField(10, shape.curviness.toString)

  private val dashes =
    (for (i <- 0 until 3) yield {
       new DashComboBox(LinkLine.dashChoices) {
         setSelectedIndex(shape.getLine(i).dashIndex)
       }
     }).toArray

  private val originalShape = shape

  construct()

  private def construct(): Unit = {
    setResizable(false)
    setLocation(parent.getX + 10, parent.getY + 10)
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

    addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = {
        saveShape()
        dispose()
        setVisible(false)
      }
    })

    Utils.addEscKeyAction(this, new AbstractAction {
      def actionPerformed(e: ActionEvent): Unit = {
        if (originalShape.toString != getCurrentShape.toString ||
            new OptionPane(LinkEditorDialog.this, loc"Confirm Cancel",
                           loc"You may lose changes made to this shape. Do you want to cancel anyway?",
                           OptionPane.Options.YesNo, OptionPane.Icons.question).getSelectedIndex != 0)
          return

        dispose()
      }
    })

    val done = new DialogButton(true, loc"OK", () => saveShape())
    val cancel = new DialogButton(false, loc"Cancel", () => dispose)

    setContentPane(new BoxColumn(Seq(
      new LabeledComponent(loc"Name", name) {
        setForeground(InterfaceColors.dialogText())
      },
      new BoxRow(new LabeledComponent(loc"Direction Indicator", new Button(loc"Edit", () => {
        new EditorDialog(LinkEditorDialog.this, LinkEditorDialog.this, shape.directionIndicator, false)
      })) {
        setForeground(InterfaceColors.dialogText())
      }, BoxAlign.Start),
      new LabeledComponent(loc"Curviness", curviness) {
        setForeground(InterfaceColors.dialogText())
      },
      new LabeledComponent(loc"Left Line", dashes(2)) {
        setForeground(InterfaceColors.dialogText())
      },
      new LabeledComponent(loc"Middle Line", dashes(1)) {
        setForeground(InterfaceColors.dialogText())
      },
      new LabeledComponent(loc"Right Line", dashes(0)) {
        setForeground(InterfaceColors.dialogText())
      },
      new ButtonPanel(Seq(done, cancel))
    ), 6) {
      setOpaque(true)
      setBackground(InterfaceColors.dialogBackground())
      setBorder(new ZoomableBorder(6, 6, 6, 6))
    })

    getRootPane.setDefaultButton(done)

    list.update()

    pack()

    // when name is not enabled focus goes to the curviness
    // field instead ev 2/18/08
    if (ShapeList.isDefaultShapeName(shape.name)) {
      name.setEnabled(false)
      curviness.requestFocus()
    } else {
      name.setEnabled(true)
      name.requestFocus()
    }

    setVisible(true)
  }

  def update(originalShape: Shape, newShape: Shape): Unit = {
    shape.directionIndicator = newShape.asInstanceOf[VectorShape]
  }

  def exists(name: String): Boolean =
    false

  private def saveShape(): Unit = {
    val nameStr = name.getText.trim.toLowerCase

    // Make sure the shape has a name
    if (nameStr.isEmpty) {
      new OptionPane(this, loc"Invalid Parameters", loc"Name cannot be empty.", OptionPane.Options.Ok,
                     OptionPane.Icons.error)

      return
    }

    // If this is an attempt to overwrite a shape, prompt for permission to do it
    if (list.exists(nameStr) && nameStr != originalShape.name &&
        new OptionPane(this, loc"Confirm Overwrite",
                       loc"A shape with this name already exists. Do you want to replace it?", OptionPane.Options.YesNo,
                       OptionPane.Icons.question).getSelectedIndex != 0)
      return

    shape.name = nameStr

    Try(curviness.getText.toDouble) match {
      case Success(cv) => shape.curviness = cv
      case Failure(_) =>
        new OptionPane(this, loc"Invalid Parameters", loc"Curviness must be a valid floating point number.",
                       OptionPane.Options.Ok, OptionPane.Icons.error)

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

  override def setVisible(visible: Boolean): Unit = {
    if (visible)
      Analytics.linkShapeEdit()

    super.setVisible(visible)
  }

  private class DashComboBox(items: List[Array[Float]]) extends ComboBox[JLabel] {
    private class DashLabel(item: Array[Float]) extends JLabel with ComboBox.Clone {
      setIcon(new Icon {
        def getIconWidth = 85
        def getIconHeight = 18

        def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
          if (item(0) != 0) {
            val g2d = Utils.initGraphics2D(g)

            g2d.setColor(InterfaceColors.toolbarText())
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
