// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor

import java.awt.{ BasicStroke, Component, Graphics, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent, WindowAdapter, WindowEvent }
import javax.swing.{ AbstractAction, Icon, JDialog, JOptionPane, JLabel, JPanel, JPopupMenu, JTextField,
                     WindowConstants }

import org.nlogo.core.{ I18N, Shape, ShapeList }
import org.nlogo.shape.{ LinkLine, LinkShape, VectorShape }
import org.nlogo.swing.{ Button, ButtonPanel, DropdownArrow, LabeledComponent, MenuItem, RoundedBorderPanel, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.util.{ Failure, Success, Try }

class LinkEditorDialog(parent: JDialog, list: DrawableList[LinkShape], shape: LinkShape)
  extends JDialog(parent, I18N.gui.get("tools.linkEditor"), true) with EditorDialog.VectorShapeContainer {
  
  private implicit val i18nPrefix = I18N.Prefix("tools.linkEditor")

  private val name = new JTextField(10) with ThemeSync {
    setText(shape.name)

    def syncTheme() {
      setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
      setForeground(InterfaceColors.TOOLBAR_TEXT)
      setCaretColor(InterfaceColors.TOOLBAR_TEXT)
    }
  }

  private val curviness = new JTextField(10) with ThemeSync {
    setText(shape.curviness.toString)

    def syncTheme() {
      setBackground(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
      setForeground(InterfaceColors.TOOLBAR_TEXT)
      setCaretColor(InterfaceColors.TOOLBAR_TEXT)
    }
  }

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
    }
  })

  Utils.addEscKeyAction(this, new AbstractAction {
    def actionPerformed(e: ActionEvent) {
      if (originalShape.toString != getCurrentShape.toString ||
          JOptionPane.showConfirmDialog(LinkEditorDialog.this,
            "You may lose changes made to this shape. Do you want to cancel anyway?",
            "Confirm Cancel", JOptionPane.YES_NO_OPTION) != 0)
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
      new EditorDialog(LinkEditorDialog.this, shape.directionIndicator, getX, getY, false)
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
      JOptionPane.showMessageDialog(this, I18N.gui("nameEmpty"), I18N.gui("invalid"), JOptionPane.PLAIN_MESSAGE)

      return
    }

    // If this is an attempt to overwrite a shape, prompt for permission to do it
    if (list.exists(nameStr) && nameStr != originalShape.name &&
        JOptionPane.showConfirmDialog(this, I18N.gui("nameConflict"), I18N.gui("confirmOverwrite"),
                                      JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
      return
    
    shape.name = nameStr

    Try(curviness.getText.toDouble) match {
      case Success(cv) => shape.curviness = cv
      case Failure(_) =>
        JOptionPane.showMessageDialog(this, I18N.gui("invalidCurviness"), I18N.gui("invalid"),
                                      JOptionPane.PLAIN_MESSAGE)
        
        return
    }

    for (i <- 0 until dashes.size) {
      val index = dashes(i).getSelectedIndex

      shape.setLineVisible(i, index != 0)
      shape.getLine(i).dashes = LinkLine.dashChoices(index)
    }

    list.update(originalShape, shape)

    setVisible(false)
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

  private class DashComboBox(items: List[Array[Float]])
    extends JPanel(new GridBagLayout) with RoundedBorderPanel with ThemeSync {

    private class Dash(private var item: Array[Float]) extends Icon {
      def setItem(item: Array[Float]) {
        this.item = item
      }

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
    }

    setDiameter(6)
    enableHover()

    private var selectedIndex = -1

    private val label = new JLabel
    private val arrow = new DropdownArrow

    private val popup = new JPopupMenu

    locally {
      val c = new GridBagConstraints

      c.fill = GridBagConstraints.HORIZONTAL
      c.weightx = 1
      c.insets = new Insets(3, 6, 3, 6)

      add(label, c)

      c.fill = GridBagConstraints.NONE
      c.weightx = 0
      c.insets = new Insets(3, 0, 3, 6)

      add(arrow, c)

      val mouseListener = new MouseAdapter {
        override def mousePressed(e: MouseEvent) {
          popup.show(DashComboBox.this, 0, getHeight)
        }
      }

      addMouseListener(mouseListener)
      label.addMouseListener(mouseListener)
      arrow.addMouseListener(mouseListener)
    }

    private val dashIcons = items.map(new Dash(_))

    for (i <- 0 until dashIcons.size) {
      popup.add(new MenuItem(new AbstractAction {
        def actionPerformed(e: ActionEvent) {
          selectIndex(i)
        }
      }) {
        setIcon(dashIcons(i))
      })
    }

    selectIndex(0)

    syncTheme()

    def setSelectedIndex(index: Int) {
      if (index >= 0 && index < items.size)
        selectIndex(index)
    }

    def getSelectedIndex: Int =
      selectedIndex
    
    private def selectIndex(index: Int) {
      selectedIndex = index
      label.setIcon(dashIcons(selectedIndex))
    }

    def syncTheme() {
      setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
      setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
      setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)

      popup.setBackground(InterfaceColors.MENU_BACKGROUND)

      popup.getComponents.foreach(_ match {
        case ts: ThemeSync => ts.syncTheme()
      })
    }
  }
}
