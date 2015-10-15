// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import javax.swing.border.{EtchedBorder, TitledBorder}
import javax.swing._
import javax.swing.BorderFactory._
import java.awt.{List => AWTList, _}
import org.nlogo.awt.Fonts.platformMonospacedFont
import org.nlogo.swing.{OptionDialog, RichJButton}
import org.nlogo.editor.{Colorizer, EditorField}
import table.{DefaultTableCellRenderer, AbstractTableModel, TableCellEditor, TableCellRenderer}
import org.nlogo.window.{ColorDialog, PlotWidget}
import org.nlogo.plot.{Plot, PlotPen}
import javax.swing.event.{ListSelectionEvent, ListSelectionListener}
import org.nlogo.api.{I18N}
import org.nlogo.core.TokenType

class PlotPensEditor(accessor: PropertyAccessor[List[PlotPen]], colorizer: Colorizer[TokenType])
        extends PropertyEditor(accessor, handlesOwnErrors = true) {

  private implicit val i18nPrefix = I18N.Prefix("edit.plot.pen")

  val PlotPenModes =
    scala.List(I18N.gui("mode.line"),
               I18N.gui("mode.bar"),
               I18N.gui("mode.point"))
  def getPlotPenModeNames = PlotPenModes.toArray

  val plot = accessor.target.asInstanceOf[PlotWidget].plot
  val plotManager = accessor.target.asInstanceOf[PlotWidget].plotManager
  val table = new PlotPensTable()

  setLayout(new BorderLayout)
  setMinimumSize(new Dimension(600, 200))
  setPreferredSize(new Dimension(600, 200))

  add(new JScrollPane(table), BorderLayout.CENTER)
  add(new JPanel {add(RichJButton(I18N.gui("add")) {table.newPen})}, BorderLayout.SOUTH)

  // border
  val title = createTitledBorder(createEtchedBorder(EtchedBorder.LOWERED), I18N.gui("plotPens"))
  title.setTitleJustification(TitledBorder.LEFT)
  setBorder(title)

  def changed() {} // seemingly no need to do anything here
  def set(value: List[PlotPen]) {} // seemingly no need to do anything here either

  private def frame = org.nlogo.awt.Hierarchy.getFrame(this)

  override def get: Option[List[PlotPen]] = {
    if(table.isEditing) table.getCellEditor.stopCellEditing
    val names = table.model.pens.map(_.name)
    // for https://trac.assembla.com/nlogo/ticket/1174
    // i'm deciding that we can have many pens with no names, if we choose.
    // maybe we don't need this duplicate pen name restriction at all,
    // but for now I'll leave it. It would seem weird to have two pens named "turtles"
    // but also weird to allow for one pen to have no name, but only one.
    if((names.toSet - "").size  < (names.toList.filterNot(_ == "")).size){
      org.nlogo.swing.OptionDialog.show(frame, "Invalid Entry", "Pens list contains duplicate names.",
        Array(I18N.gui.get("common.buttons.ok")))
      None
    } else Some(table.getPlotPens)
  }

  override def getConstraints = {
    val c = super.getConstraints
    c.fill = GridBagConstraints.BOTH
    c.gridheight = 1
    c.weighty = 1.0
    c
  }

  object Pen {
    def apply(pp: PlotPen): Pen = {
      new Pen(
        pp,
        pp.name,
        color = ColorInfo(pp.defaultColor),
        interval=pp.defaultInterval,
        mode=pp.defaultMode,
        inLegend=pp.inLegend,
        setupCode = pp.setupCode, updateCode = pp.updateCode,
        setupError = plotManager.getPenSetupError(pp),
        updateError = plotManager.getPenUpdateError(pp))
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
                 setupError: Option[Exception] = None,
                 updateError: Option[Exception] = None) {
    override def toString = "Pen(" + name + ", " + updateCode + "," + color + ")"

    def hasErrors = setupError.isDefined || updateError.isDefined

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
    locally{
      setModel(model)

      setRowHeight(getRowHeight + 10)
      setGridColor(Color.BLACK)
      setShowGrid(true)
      setRowSelectionAllowed(false)
      getTableHeader.setReorderingAllowed(false)

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

      buttonsColumn.setCellRenderer(new ButtonCellEditor)
      buttonsColumn.setCellEditor(new ButtonCellEditor)
      buttonsColumn.setMaxWidth(120)
      buttonsColumn.setMinWidth(120)
      buttonsColumn.setHeaderValue("")

      // finally add all the actual plot pens to the table
      for(p <- plot.pens; if(!p.temporary)){ model.addPen(Pen(p)) }
    }

    // final method call to get al the pens in the table.
    // converts them to real plot pens, though maybe that should be done after.
    def getPlotPens: List[PlotPen] = model.pens.map(_.convertToPlotPen(plot)).toList

    // add a dummy pen to the list so that the user can then modify it.
    def newPen {
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
    def removePen(index: Int) {model.removePen(index)}

    // shows the pens color as a colored rectangle
    class ColorRenderer extends JLabel with TableCellRenderer {
      setOpaque(true)
      def getTableCellRendererComponent(table: JTable, value: Object,
                                        isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int) = {
        setBackground(value.asInstanceOf[ColorInfo].color)
        this
      }
    }

    // pops up the color swatch when the user clicks the cell
    class ColorEditor extends AbstractCellEditor with TableCellEditor {
      var currentColor = ColorInfo(Color.BLACK)
      val button: JButton = RichJButton(""){
        button.setBackground(model.pens(getSelectedRow).color.color)
        val plotPenColorDialog = new ColorDialog(null, true)
        val newColor = plotPenColorDialog.showPlotPenDialog(currentColor.color)
        if (newColor != null) { currentColor = ColorInfo(newColor) }
        fireEditingStopped()
      }
      button.setOpaque(true)
      button.setBorderPainted(false)

      def getCellEditorValue = currentColor
      def getTableCellEditorComponent(table: JTable, value: Object, isSelected: Boolean, row: Int, col: Int) = {
        currentColor = value.asInstanceOf[ColorInfo]
        button.setBackground(currentColor.color)
        button
      }
    }

    // used to display the name. displays in red if there are errors associated with the pen.
    class NameRenderer extends DefaultTableCellRenderer {
      override def getTableCellRendererComponent(table: JTable, value: Object,
                                        isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int) = {
        val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col)
        if(model.pens(row).hasErrors) c.setForeground(Color.RED) else c.setForeground(Color.BLACK)
        c
      }
    }

    def openAdvancedPenEditor(editingPen: Pen) {
      val p = new PlotPenEditorAdvanced(editingPen)
      new org.nlogo.swing.Popup(frame, I18N.gui("editing") + " " + editingPen.name, p, (), {
        p.getResult match {
          case Some(p) =>
            model.pens(getSelectedRow) = p
            table.removeEditor()
            table.repaint()
            true
          case _ => false
        }
      }, I18N.gui.get _).show()
    }

    // renders the delete and edit buttons for each column
    class ButtonCellEditor extends AbstractCellEditor with TableCellRenderer with TableCellEditor {
      val editButton = RichJButton(new javax.swing.ImageIcon(getClass.getResource("/images/edit.gif"))) {
        openAdvancedPenEditor(model.pens(getSelectedRow))
      }
      val deleteButton = RichJButton(new javax.swing.ImageIcon(getClass.getResource("/images/delete.gif"))) {
        val index = getSelectedRow
        removeEditor()
        clearSelection()
        removePen(index)
      }
      editButton.putClientProperty("JComponent.sizeVariant", "small")
      deleteButton.putClientProperty("JComponent.sizeVariant", "small")
      val buttonPanel = new JPanel {add(editButton); add(deleteButton)}
      def getTableCellRendererComponent(table: JTable, value: Object,
                                        isSelected: Boolean, hasFocus: Boolean,
                                        row: Int, col: Int) = buttonPanel
      def getTableCellEditorComponent(table: JTable, value: Object,
                                      isSelected: Boolean, row: Int, col: Int) = buttonPanel
      def getCellEditorValue = ""
    }

    class CodeCellRenderer extends TableCellRenderer {
      val font = new Font(platformMonospacedFont, Font.PLAIN, 12)
      val editor = new EditorField[TokenType](30, font, false, colorizer, I18N.gui.get _)
      def getTableCellRendererComponent(table: JTable, value: Object,
                                        isSelected: Boolean, hasFocus: Boolean, row: Int, col: Int) = {
        editor.setText(value.asInstanceOf[String])
        editor
      }
    }

    class CodeCellEditor extends AbstractCellEditor with TableCellEditor {
      val goodFont = new Font(platformMonospacedFont, Font.PLAIN, 12)
      val editor = new EditorField[TokenType](30, goodFont, false, colorizer, I18N.gui.get _) {
        // for some reason QuaquaTableUI calls setFont(table.getFont()) on cell editors. I don't
        // understand why it does that, but it means that in order to get the right font in our cell
        // editors, we need to do this. - ST 4/29/10
        override def setFont(badFont: java.awt.Font) {
          super.setFont(goodFont)
        }
      }
      def getTableCellEditorComponent(table: JTable, value: Object, isSelected: Boolean, row: Int, col: Int) = {
        editor.setText(value.asInstanceOf[String])
        editor
      }
      def getCellEditorValue = editor.getText()
    }

    class PenTableModel extends AbstractTableModel {
      val columnNames = scala.List(ColorColumnName, NameColumnName, UpdateCommandsColumnName, ButtonsColumnName)
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
      override def setValueAt(value: Object, row: Int, col: Int) {
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

      def addPen(p: Pen) {pens += p; fireTableDataChanged}

      def removePen(index: Int) {
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
      def valueChanged(event: ListSelectionEvent) {
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
      def valueChanged(event: ListSelectionEvent) {
        if (!event.getValueIsAdjusting && getSelectedRow != -1) {
          if(table.getSelectedColumn == 2 && lastColumn != 2)
            if(model.pens(getSelectedRow).updateCode.contains("\n"))
              openAdvancedPenEditor(model.pens(getSelectedRow))
          lastColumn = table.getSelectedColumn
        }
      }
    }
  }

  class PlotPenEditorAdvanced(inputPen: Pen) extends JPanel {
    // pieces of the UI
    private val intervalField = new org.nlogo.swing.TextField("", 8)
    private val penModes = new JComboBox(getPlotPenModeNames.asInstanceOf[Array[Object]])
    private val showPenInLegend = new JCheckBox(I18N.gui("showInLegend"), true)
    val setupCode = CodeEditor(I18N.gui("setupCommands"), colorizer, columns = 65, err=inputPen.setupError)
    val updateCode = CodeEditor(I18N.gui("updateCommands"), colorizer, columns = 65, err=inputPen.updateError)

    // layout all the pieces of the ui
    addWidgets()

    /**
     * set the values of all the inputs to the values of the input pen
     */
    intervalField.setText(inputPen.interval.toString)
    penModes.setSelectedIndex(inputPen.mode)
    showPenInLegend.setSelected(inputPen.inLegend)
    setupCode.set(inputPen.setupCode)
    updateCode.set(inputPen.updateCode)

    /**
     * creates the result by getting values out of each of the inputs
     */
    def getResult: Option[Pen] = {
      val validInterval = {
        try {intervalField.getText.toDouble; true}
        catch {
          case ex: NumberFormatException =>
            OptionDialog.show(org.nlogo.awt.Hierarchy.getWindow(this),
              "Invalid Entry", "Invalid value for the pen interval", Array(I18N.gui.get("common.buttons.ok")))
            false
        }
      }

      if (validInterval)
        Some(inputPen.copy(
          interval = intervalField.getText.toDouble,
          mode = penModes.getSelectedIndex,
          inLegend = showPenInLegend.isSelected,
          setupCode = setupCode.get.getOrElse(""),
          updateCode = updateCode.get.getOrElse("")))
      else None
    }

    private def addWidgets() {
      setLayout(new BorderLayout())
      val title = createTitledBorder(createEtchedBorder(EtchedBorder.LOWERED), I18N.gui("advanced"))
      title.setTitleJustification(TitledBorder.LEFT)
      setBorder(title)
      val topPanel = new JPanel(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
        add(new JPanel(new BorderLayout){
          add(new JLabel(I18N.gui("mode")), BorderLayout.WEST)
          add(penModes, BorderLayout.CENTER)
        })
        add(new JPanel(new BorderLayout){
          add(new JLabel(I18N.gui("interval")), BorderLayout.WEST)
          add(intervalField, BorderLayout.CENTER)
        })
        add(new JPanel(new BorderLayout){ add(showPenInLegend, BorderLayout.WEST) })
      }
      val codePanel = new JPanel(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
        add(new JPanel) // for a little space
        add(setupCode)
        add(updateCode)
      }
      add(topPanel, BorderLayout.NORTH)
      add(codePanel, BorderLayout.CENTER)
    }
  }
}

object ColorInfo {
  import org.nlogo.api.Color._
  lazy val defaults = getColorNamesArray.map{name =>
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
