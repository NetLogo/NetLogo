// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Color, Component, Cursor, Dimension, Frame, Graphics, GridBagConstraints,
                  GridBagLayout, Insets }
import java.awt.event.ActionEvent
import java.util.Locale
import javax.swing.{ AbstractAction, AbstractCellEditor, JButton, JLabel, JPanel, JTable }
import javax.swing.border.EmptyBorder
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }
import javax.swing.table.{ DefaultTableCellRenderer, AbstractTableModel, TableCellEditor, TableCellRenderer }

import org.nlogo.api.CompilerServices
import org.nlogo.awt.Hierarchy
import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.editor.{ Colorizer, EditorArea, EditorConfiguration }
import org.nlogo.plot.{ Plot, PlotManagerInterface, PlotPen }
import org.nlogo.swing.{ Button, Popup, ScrollPane, Transparent, Utils }
import org.nlogo.theme.InterfaceColors

import scala.collection.mutable.ArrayBuffer
import scala.util.{ Failure, Success, Try }

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

class PlotPensEditor(accessor: PropertyAccessor[List[PlotPen]], compiler: CompilerServices, colorizer: Colorizer,
                     target: PlotWidget) extends PropertyEditor(accessor) {

  import PlotPensEditor._

  private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("edit.plot.pen")

  private val plot = target.plot
  private val plotManager = target.plotManager
  private val table = new PlotPensTable()

  private val scrollPane = new ScrollPane(table)

  private val addButton = new Button(I18N.gui("add"), () => table.newPen())
  private val checkButton = new Button(I18N.gui("check"), () => apply())

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
    add(checkButton)
  }, BorderLayout.SOUTH)

  def set(value: List[PlotPen]): Unit = {} // seemingly no need to do anything here

  private def frame: Frame = Hierarchy.getFrame(this)

  override def get: Try[List[PlotPen]] = {
    if (table.isEditing) table.getCellEditor.stopCellEditing

    // It was an intentional decision made Q2 2011 to allow multiple pens with
    // a blank name. - RG 2/21/2018
    val groupedNames = table.model.pens.map(_.name.toUpperCase(Locale.ENGLISH)).groupBy(name => name) - ""
    val duplicateNames = groupedNames.collect {
      case (name, list) if list.length > 1 =>
        name
    }

    if (duplicateNames.nonEmpty) {
      Failure(new Exception(I18N.gui.getN("edit.plot.pen.duplicateNames", duplicateNames.mkString(", "))))
    } else {
      Success(table.getPlotPens)
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

    private val cellFactory = new CodeCellFactory

    val model = new PenTableModel(cellFactory)

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

      updateCommandsColumn.setCellRenderer(cellFactory)
      updateCommandsColumn.setCellEditor(cellFactory)
      updateCommandsColumn.setMinWidth(250)

      val buttonCell = new ButtonCellEditor

      buttonsColumn.setCellRenderer(buttonCell)
      buttonsColumn.setCellEditor(new ButtonCellEditor)
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
      var nextNum = model.pens.size
      var nextName = s"pen-$nextNum"

      while (model.pens.exists(_.name.toUpperCase(Locale.ENGLISH) == nextName.toUpperCase(Locale.ENGLISH))) {
        nextNum += 1
        nextName = s"pen-$nextNum"
      }

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

          val initialOpt = Option(RGBA.fromMask(currentColor.rgb))
          val colorOpt   = initialOpt.flatMap(NLNumber.fromRGBA).orElse(initialOpt)

          new JFXColorPicker(frame, true, DoubleOnly, colorOpt,
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
            model.updatePen(getSelectedRow, p)
            apply()
            true
          case _ => false
        }
      }, I18N.gui.get).show()
    }

    def openAdvancedPenEditor(editingPen: Pen): Unit = {
      showEditorPopup(editingPen, new PlotPenEditorAdvanced(editingPen, compiler, colorizer, plotManager))
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

      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))

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

    class CodeCellFactory extends AbstractCellEditor with TableCellRenderer with TableCellEditor {
      private val editors = ArrayBuffer[EditorComponent]()

      private var lastEditor: Option[EditorArea] = None

      setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))

      override def getTableCellRendererComponent(table: JTable, value: AnyRef, isSelected: Boolean, hasFocus: Boolean,
                                                 row: Int, col: Int): Component =
        editors(row)

      override def getTableCellEditorComponent(table: JTable, value: AnyRef, isSelected: Boolean, row: Int,
                                               col: Int): Component = {
        val component = editors(row)

        lastEditor = Option(component.editor)

        component
      }

      override def getCellEditorValue: String =
        lastEditor.fold("")(_.getText)

      def addEditor(text: String): Unit = {
        editors += EditorComponent(createEditor(text))
      }

      def removeEditor(row: Int): Unit = {
        editors.remove(row)
      }

      def updateEditor(row: Int, text: String): Unit = {
        editors(row) = EditorComponent(createEditor(text))
      }

      private def createEditor(text: String): EditorArea = {
        new EditorArea(EditorConfiguration.default(1, 30, compiler, colorizer)) {
          setBackground(InterfaceColors.textAreaBackground())
          setCaretColor(InterfaceColors.textAreaText())
          setText(text)

          override def setText(text: String): Unit = {
            super.setText(text)

            resetUndoHistory()
          }
        }
      }

      private case class EditorComponent(editor: EditorArea) extends ScrollPane(editor) {
        setBorder(null)
      }
    }

    class PenTableModel(cellFactory: CodeCellFactory) extends AbstractTableModel {
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
          case UpdateCommandsColumnName => classOf[CodeCellFactory]
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

      def addPen(p: Pen): Unit = {
        pens += p
        fireTableDataChanged()
        cellFactory.addEditor(p.updateCode)
      }

      def removePen(index: Int): Unit = {
        if (index != -1) {
          pens.remove(index)
          fireTableRowsDeleted(index, index)
          cellFactory.removeEditor(index)
          removeEditor()
          revalidate()
          repaint()
        }
      }

      def updatePen(row: Int, pen: Pen): Unit = {
        pens(row) = pen
        fireTableDataChanged()
        cellFactory.updateEditor(row, pen.updateCode)
      }
    }

    var lastColumn = 0
    private class RowListener extends ListSelectionListener {
      def valueChanged(event: ListSelectionEvent): Unit = {
        if (!event.getValueIsAdjusting && getSelectedRow != -1)
          lastColumn = table.getSelectedColumn
      }
    }
    private class ColumnListener extends ListSelectionListener {
      def valueChanged(event: ListSelectionEvent): Unit = {
        if (!event.getValueIsAdjusting && getSelectedRow != -1)
          lastColumn = table.getSelectedColumn
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
