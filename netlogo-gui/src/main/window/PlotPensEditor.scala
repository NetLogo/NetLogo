// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Color, Cursor, Dimension, Font, Frame, Graphics, GridBagConstraints, GridBagLayout,
                  Insets }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, AbstractCellEditor, JButton, JLabel, JPanel, JTable }
import javax.swing.border.EmptyBorder
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }
import javax.swing.table.{ DefaultTableCellRenderer, AbstractTableModel, TableCellEditor, TableCellRenderer }

import org.nlogo.awt.Hierarchy
import org.nlogo.awt.Fonts.platformMonospacedFont
import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.editor.{ Colorizer, EditorField }
import org.nlogo.plot.{ Plot, PlotManagerInterface, PlotPen }
import org.nlogo.swing.{ Button, OptionPane, Popup, ScrollPane, Transparent, Utils }
import org.nlogo.theme.InterfaceColors

object PlotPensEditor {
  sealed trait CodeType
  case object Setup extends CodeType
  case object Update extends CodeType

  object Pen {
    def apply(pp: PlotPen, plotManager: PlotManagerInterface): Pen = {
      new Pen(
        pp,
        pp.name,
        color = ColorInfo(pp.defaultColor),
        interval=pp.defaultInterval,
        mode=pp.defaultMode,
        inLegend=pp.inLegend,
        setupCode = pp.setupCode, updateCode = pp.updateCode,
        setupError = plotManager.getPenSetupError(pp),
        updateError = plotManager.getPenUpdateError(pp),
        runtimeError = pp.runtimeError)
    }
  }

  case class Pen(originalPen: PlotPen,
                 name: String = "",
                 color: ColorInfo = ColorInfo(Color.BLACK),
                 interval: Double = 1.0,
                 mode: Int = PlotPen.LINE_MODE,
                 inLegend: Boolean = true,
                 setupCode: String = "",
                 updateCode: String = "",
                 setupError: Option[CompilerException] = None,
                 updateError: Option[CompilerException] = None,
                 runtimeError: Option[Exception] = None) {
    override def toString = "Pen(" + name + ", " + updateCode + "," + color + ")"

    def hasErrors = setupError.isDefined || updateError.isDefined || runtimeError.isDefined

    def convertToPlotPen(plot: Plot): PlotPen = {
      val pp = new PlotPen(
        plot = plot,
        name=if(name==null) "" else name,
        temporary=false,
        setupCode=setupCode,
        updateCode=updateCode,
        x=originalPen.x,
        defaultColor=color.rgb,
        _color = color.rgb,
        inLegend = inLegend,
        defaultInterval = interval,
        _interval = interval,
        defaultMode = mode,
        _mode = originalPen.mode,
        penModeChanged = originalPen.penModeChanged,
        _isDown = originalPen.isDown)
      pp.x = originalPen.x
      pp.points = originalPen.points
      pp
    }
  }
}

class PlotPensEditor(accessor: PropertyAccessor[List[PlotPen]], colorizer: Colorizer, target: PlotWidget)
  extends PropertyEditor(accessor, true) {

  import PlotPensEditor._

  private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("edit.plot.pen")

  private val plot = target.plot
  private val plotManager = target.plotManager
  private val table = new PlotPensTable()

  private val scrollPane = new ScrollPane(table)

  private val addButton = new Button(I18N.gui("add"), () => table.newPen())

  override def apply(): Unit = {
    super.apply()
    plotManager.compilePlot(plot)
    table.initializePens()
  }

  setLayout(new BorderLayout)
  setMinimumSize(new Dimension(600, 200))
  setPreferredSize(new Dimension(600, 200))

  add(scrollPane, BorderLayout.CENTER)
  add(new JPanel with Transparent {
    add(addButton)
  }, BorderLayout.SOUTH)

  def set(value: List[PlotPen]): Unit = {} // seemingly no need to do anything here

  private def frame: Frame = Hierarchy.getFrame(this)

  override def get: Option[List[PlotPen]] = {
    if (table.isEditing) table.getCellEditor.stopCellEditing
    val names = table.model.pens.map(_.name)
    // It was an intentional decision made Q2 2011 to allow multiple pens with
    // a blank name. - RG 2/21/2018
    val groupedNames = (names.groupBy(_.toUpperCase) - "").toSeq
    val duplicateNames = groupedNames.filter(_._2.length > 1)
    if (duplicateNames.nonEmpty) {
      new OptionPane(this, I18N.gui.get("edit.plot.pen.invalidEntry"),
                     I18N.gui.getN("edit.plot.pen.duplicateNames",
                                   duplicateNames.map(_._1.toUpperCase).mkString(", ")), OptionPane.Options.Ok,
                     OptionPane.Icons.Error)
      None
    } else {
      Some(table.getPlotPens)
    }
  }

  override def syncTheme(): Unit = {
    scrollPane.setBackground(InterfaceColors.dialogBackground())
    table.setBackground(InterfaceColors.dialogBackground())
    table.setGridColor(InterfaceColors.dialogText())

    addButton.syncTheme()
  }

  class PlotPensTable extends JTable { table =>

    val UpdateCommandsColumnName = I18N.gui("updateCommands")
    val NameColumnName = I18N.gui("name")
    val ColorColumnName = I18N.gui("color")
    val ButtonsColumnName = "Buttons"
    def updateCommandsColumn = getColumn(UpdateCommandsColumnName)
    def nameColumn = getColumn(NameColumnName)
    def colorColumn = getColumn(ColorColumnName)
    def buttonsColumn = getColumn(ButtonsColumnName)

    val model = new PenTableModel()

    locally {
      setModel(model)
      setRowHeight(getRowHeight + 14)
      setRowMargin(1)
      setShowGrid(true)
      setRowSelectionAllowed(false)
      getTableHeader.setReorderingAllowed(false)
      getTableHeader.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1))
      getColumnModel().setColumnMargin(1)

      getSelectionModel.addListSelectionListener(new RowListener())
      getColumnModel.getSelectionModel.addListSelectionListener(new ColumnListener())

      nameColumn.setCellRenderer(new NameRenderer)
      nameColumn.setMinWidth(100)
      nameColumn.setMaxWidth(160)

      colorColumn.setCellEditor(new ColorEditor)
      colorColumn.setMaxWidth(40)
      colorColumn.setMinWidth(40)
      colorColumn.setCellRenderer(new ColorRenderer)

      updateCommandsColumn.setCellRenderer(new CodeCellRenderer)
      updateCommandsColumn.setCellEditor(new CodeCellEditor)
      updateCommandsColumn.setMinWidth(250)

      val buttonCell = new ButtonCellEditor

      buttonsColumn.setCellRenderer(buttonCell)
      buttonsColumn.setMaxWidth(buttonCell.buttonPanel.getPreferredSize.width)
      buttonsColumn.setMinWidth(buttonCell.buttonPanel.getPreferredSize.width)
      buttonsColumn.setHeaderValue("")

      // finally add all the actual plot pens to the table
      initializePens()
    }

    def initializePens(): Unit = {
      model.clear()
      for (p <- plot.pens; if(!p.temporary)) { model.addPen(Pen(p, plotManager)) }
    }

    // final method call to get al the pens in the table.
    // converts them to real plot pens, though maybe that should be done after.
    def getPlotPens: List[PlotPen] = model.pens.map(_.convertToPlotPen(plot)).toList

    // add a dummy pen to the list so that the user can then modify it.
    def newPen(): Unit = {
      val nextName = "pen-" + model.pens.size
      val nextColor = {
        val colorsInUse:List[ColorInfo] = model.pens.toList.map(_.color)
        val defaults:List[ColorInfo] = ColorInfo.defaults.toList filterNot (_ == ColorInfo(Color.WHITE))
        val available:List[ColorInfo] = defaults filterNot (colorsInUse.contains)
        if(available.nonEmpty) available.head else ColorInfo(Color.BLACK)
      }
      model.addPen(new Pen(plot.createPlotPen(nextName, false, "", ""), nextName, color = nextColor))
    }

    // someone pressed the delete button in the pens row.
    def removePen(index: Int): Unit = {model.removePen(index)}

    // shows the pens color as a colored rectangle
    class ColorRenderer extends JLabel with TableCellRenderer {
      setOpaque(true)
      def getTableCellRendererComponent(table: JTable, value: Object,
                                        isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int) = {
        if (value != null) {
          setBackground(value.asInstanceOf[ColorInfo].color)
        }
        this
      }
    }

    // pops up the color swatch when the user clicks the cell
    class ColorEditor extends AbstractCellEditor with TableCellEditor {

      private var currentColor = ColorInfo(Color.BLACK)
      private val button = new JButton(new AbstractAction {
        override def actionPerformed(e: ActionEvent): Unit = {
          new JFXColorPicker(frame, true, DoubleOnly, Option(RGBA.fromMask(currentColor.rgb)),
            (x: String) => {
              val num = x.toDouble
              currentColor = ColorInfo(NLNumber(num).toColor)
              fireEditingStopped()
            }, () => {
              fireEditingStopped()
            }
          ).setVisible(true)
        }
      }) {
        override def paintComponent(g: Graphics): Unit = {
          val g2d = Utils.initGraphics2D(g)

          g2d.setColor(currentColor.color)
          g2d.fillRect(0, 0, getWidth, getHeight)
        }
      }

      def getCellEditorValue = currentColor

      def getTableCellEditorComponent(table: JTable, value: Object, isSelected: Boolean, row: Int, col: Int) = {
        if (value != null) {
          currentColor = value.asInstanceOf[ColorInfo]
          button.setBackground(currentColor.color)
        }
        button
      }

    }

    // used to display the name. displays in red if there are errors associated with the pen.
    class NameRenderer extends DefaultTableCellRenderer {
      override def getTableCellRendererComponent(table: JTable, value: Object,
                                        isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int) = {
        val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col)
        if (isSelected)
          c.setBackground(InterfaceColors.dialogBackgroundSelected())
        else
          c.setBackground(InterfaceColors.dialogBackground())
        if (model.pens(row).hasErrors)
          c.setForeground(Color.RED)
        else
          c.setForeground(InterfaceColors.dialogText())
        c
      }
    }

    def showEditorPopup(editingPen: Pen, p: PlotPenEditorAdvanced): Unit = {
      new Popup(frame, I18N.gui("editing") + " " + editingPen.name, p, (), {
        p.getResult match {
          case Some(p) =>
            model.pens(getSelectedRow) = p
            apply()
            true
          case _ => false
        }
      }, I18N.gui.get).show()
    }

    def openAdvancedPenEditor(editingPen: Pen): Unit = {
      showEditorPopup(editingPen, new PlotPenEditorAdvanced(editingPen, colorizer, plotManager))
    }

    // renders the delete and edit buttons for each column
    class ButtonCellEditor extends AbstractCellEditor with TableCellRenderer with TableCellEditor {
      val EditIcon   = Utils.iconScaledWithColor("/images/edit.png", 15, 15, InterfaceColors.toolbarImage())
      val AlertIcon  = Utils.iconScaled("/images/edit-error.png", 15, 15)
      val DeleteIcon = Utils.iconScaledWithColor("/images/delete.png", 15, 15, InterfaceColors.toolbarImage())

      val editButton = new Button("", () => {
        openAdvancedPenEditor(model.pens(getSelectedRow))
      }) {
        setIcon(EditIcon)
        setBorder(new EmptyBorder(6, 6, 6, 6))
      }
      val deleteButton = new Button("", () => {
        val index = getSelectedRow
        removeEditor()
        clearSelection()
        removePen(index)
      }) {
        setIcon(DeleteIcon)
        setBorder(new EmptyBorder(6, 6, 6, 6))
      }
      editButton.putClientProperty("JComponent.sizeVariant", "small")
      deleteButton.putClientProperty("JComponent.sizeVariant", "small")

      val buttonPanel = new JPanel(new GridBagLayout) with Transparent {
        val c = new GridBagConstraints

        c.insets = new Insets(6, 6, 6, 6)

        add(editButton, c)

        c.insets = new Insets(6, 0, 6, 6)

        add(deleteButton, c)
      }

      def showRow(row: Int): JPanel = {
        if (model.pens.length > row) {
          model.pens(row) match {
            case pen: Pen if pen.hasErrors => editButton.setIcon(AlertIcon)
            case pen: Pen => editButton.setIcon(EditIcon)
          }
        }
        buttonPanel
      }

      def getTableCellRendererComponent(table: JTable, value: Object,
                                        isSelected: Boolean, hasFocus: Boolean,
                                        row: Int, col: Int) = showRow(row)

      def getTableCellEditorComponent(table: JTable, value: Object,
                                      isSelected: Boolean, row: Int, col: Int) = showRow(row)
      def getCellEditorValue = ""
    }

    class CodeCellRenderer extends TableCellRenderer {
      val font = new Font(platformMonospacedFont, Font.PLAIN, 12)
      val editor = new EditorField(30, font, true, colorizer)
      setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))
      def getTableCellRendererComponent(table: JTable, value: Object,
                                        isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int) = {
        // This null check is from strange behavior in java
        // http://stackoverflow.com/questions/3054775/jtable-strange-behavior-from-getaccessiblechild-method-resulting-in-null-point
        // RG 2/22/16
        if (value != null) {
          editor.setText(value.asInstanceOf[String])
        }
        editor.setBackground(InterfaceColors.textAreaBackground())
        editor.setCaretColor(InterfaceColors.textAreaText())
        editor
      }
    }

    class CodeCellEditor extends AbstractCellEditor with TableCellEditor {
      val goodFont = new Font(platformMonospacedFont, Font.PLAIN, 12)
      val editor = new EditorField(30, goodFont, true, colorizer)
      def getTableCellEditorComponent(table: JTable, value: Object, isSelected: Boolean, row: Int, col: Int) = {
        editor.setText(value.asInstanceOf[String])
        editor.setBackground(InterfaceColors.textAreaBackground())
        editor.setCaretColor(InterfaceColors.textAreaText())
        editor
      }
      def getCellEditorValue = editor.getText()
    }

    class PenTableModel extends AbstractTableModel {
      val columnNames = scala.List(ColorColumnName, NameColumnName, UpdateCommandsColumnName, ButtonsColumnName)
      def clear() = pens.clear()
      val pens = scala.collection.mutable.ListBuffer[Pen]()

      override def getColumnCount = columnNames.length
      override def getRowCount = pens.length
      override def getColumnName(col: Int) = columnNames(col)
      override def isCellEditable(row: Int, col: Int) = true

      override def getValueAt(row: Int, col: Int) = {
        val p = pens(row)
        columnNames(col) match {
          case NameColumnName => p.name
          case UpdateCommandsColumnName => p.updateCode
          case ColorColumnName => p.color
          case _ => None
        }
      }

      override def getColumnClass(c: Int) = {
        columnNames(c) match {
          case UpdateCommandsColumnName => classOf[CodeCellRenderer]
          case _ => classOf[String]
        }
      }

      override def setValueAt(value: Object, row: Int, col: Int): Unit = {
        if (row < pens.size) {
          val p = pens(row)
          columnNames(col) match {
            case NameColumnName => pens(row) = p.copy(name = value.asInstanceOf[String])
            case UpdateCommandsColumnName => pens(row) = p.copy(updateCode = value.asInstanceOf[String])
            case ColorColumnName => pens(row) = p.copy(color = value.asInstanceOf[ColorInfo])
            case _ =>
          }
          fireTableCellUpdated(row, col)
        }
      }

      def addPen(p: Pen): Unit = {pens += p; fireTableDataChanged}

      def removePen(index: Int): Unit = {
        if (index != -1) {
          pens.remove(index)
          fireTableRowsDeleted(index, index)
          removeEditor
          revalidate
          repaint()
        }
      }
    }

    var lastColumn = 0
    private class RowListener extends ListSelectionListener {
      def valueChanged(event: ListSelectionEvent): Unit = {
        if (!event.getValueIsAdjusting && getSelectedRow != -1) {
          if(table.getSelectedColumn == 2) {
            if(model.pens(getSelectedRow).updateCode.contains("\n"))
              openAdvancedPenEditor(model.pens(getSelectedRow))
          }
          lastColumn = table.getSelectedColumn
        }
      }
    }
    private class ColumnListener extends ListSelectionListener {
      def valueChanged(event: ListSelectionEvent): Unit = {
        if (!event.getValueIsAdjusting && getSelectedRow != -1) {
          if(table.getSelectedColumn == 2 && lastColumn != 2)
            if(model.pens(getSelectedRow).updateCode.contains("\n"))
              openAdvancedPenEditor(model.pens(getSelectedRow))
          lastColumn = table.getSelectedColumn
        }
      }
    }
  }
}

object ColorInfo {
  import org.nlogo.api.Color._
  lazy val defaults = ColorNames.map{name =>
    val rgb = getRGBByName(name)
    new ColorInfo(rgb, new Color(rgb), name)
  }
  def apply(rgb:Int): ColorInfo = {
    defaults.find(c=>c.rgb == rgb).getOrElse{
      new ColorInfo(rgb, new Color(rgb), getClosestColorNameByARGB(rgb))
    }
  }
  def apply(color:Color): ColorInfo = this(color.getRGB)
  def apply(name:String): ColorInfo = this(getRGBByName(name))
}

case class ColorInfo(rgb: Int, color: Color, name: String){
  override def toString = name
}
