// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, FileDialog => AWTFileDialog, Font, Frame, Insets }
import java.awt.event.ActionEvent
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.charset.{ MalformedInputException, StandardCharsets }
import java.nio.file.{ Files, Paths }
import java.util.Base64
import javax.swing.{ AbstractAction, JDialog, JLabel, JTable, ListSelectionModel }
import javax.swing.border.MatteBorder
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }
import javax.swing.table.{ DefaultTableModel, TableCellRenderer }

import org.nlogo.api.{ Workspace }
import org.nlogo.awt.{ Positioning, UserCancelException }
import org.nlogo.core.{ ExternalResource, I18N }
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, Button, FileDialog, InputOptionPane, MaximumHeight, OptionPane,
                         ScrollPane, SyncZoom, Utils, WindowAutomator, Zoomable, ZoomableBorder, ZoomActions }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.{ DirtyEvent, ResourcesChangedEvent }

class ResourceManagerDialog(parent: Frame, workspace: Workspace)
  extends JDialog(parent, I18N.gui.get("resource.manager"), true) with ZoomActions with ThemeSync {

  WindowAutomator.automate(this)

  private val manager = workspace.getResourceManager

  private val tableModel = new DefaultTableModel(0, 2)

  private val table = new JTable(tableModel) with Zoomable {
    private val resourceRenderer = new ResourceCellRenderer
    private val headerRenderer = new HeaderCellRenderer

    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    setCellSelectionEnabled(false)
    setRowSelectionAllowed(true)

    getTableHeader.setReorderingAllowed(false)

    setDefaultRenderer(classOf[ResourceCellRenderer], resourceRenderer)

    getSelectionModel.addListSelectionListener(new ListSelectionListener {
      def valueChanged(e: ListSelectionEvent): Unit = {
        enableButtons()
      }
    })

    val nameColumn = getColumnModel.getColumn(0)

    nameColumn.setHeaderValue("Name")
    nameColumn.setCellRenderer(resourceRenderer)
    nameColumn.setHeaderRenderer(headerRenderer)

    val extensionColumn = getColumnModel.getColumn(1)

    extensionColumn.setHeaderValue("Extension")
    extensionColumn.setCellRenderer(resourceRenderer)
    extensionColumn.setHeaderRenderer(headerRenderer)

    override def isCellEditable(row: Int, column: Int): Boolean = false

    override def getRowHeight(row: Int): Int = {
      if (row == 0) {
        headerRenderer.getPreferredSize.height
      } else {
        resourceRenderer.getPreferredSize.height
      }
    }

    override def zoom(oldZoom: Float): Unit = {
      headerRenderer.syncZoom()
      resourceRenderer.syncZoom()
    }
  }

  private val scrollPane = new ScrollPane(table)

  private val addButton = new Button(I18N.gui.get("resource.add"), () => {
    try {
      val file = FileDialog.showFiles(parent, I18N.gui.get("resource.select"), AWTFileDialog.LOAD)
      val path = Paths.get(file)

      val (fileName, extension) = {
        val split = path.getFileName.toString.split('.')

        if (split.size > 1) {
          (split.dropRight(1).mkString("."), split.last)
        } else {
          (split(0), "")
        }
      }

      val name = new InputOptionPane(parent, I18N.gui.get("resource.name"), I18N.gui.get("resource.name"),
                                     fileName).getInput

      if (name != null) {
        val trimmed = name.trim

        if (trimmed.isEmpty) {
          new OptionPane(parent, I18N.gui.get("common.messages.error"), I18N.gui.get("resource.nameEmpty"),
                         OptionPane.Options.Ok, OptionPane.Icons.Error)
        }

        else {
          val bytes = Files.readAllBytes(path)

          val text = {
            try {
              StandardCharsets.UTF_8.newDecoder.decode(ByteBuffer.wrap(bytes)).toString
            } catch {
              case _: MalformedInputException => Base64.getEncoder.encodeToString(bytes)
            }
          }

          val resource = new ExternalResource(trimmed, extension, text)

          if (manager.addResource(resource)) {
            refreshList()

            new DirtyEvent(None).raise(parent)
            new ResourcesChangedEvent().raise(parent)
          }

          else {
            new OptionPane(parent, I18N.gui.get("common.messages.error"), I18N.gui.getN("resource.alreadyExists", trimmed),
                           OptionPane.Options.Ok, OptionPane.Icons.Error)
          }
        }
      }
    }

    catch {
      case e: UserCancelException =>
    }
  })

  private val exportButton = new Button(I18N.gui.get("resource.export"), () => {
    try {
      val resource = manager.getResource(table.getValueAt(table.getSelectedRow, 0).toString).get
      val stream = new FileOutputStream(FileDialog.showFiles(parent, I18N.gui.get("resource.select"),
                                                              AWTFileDialog.SAVE,
                                                              s"${resource.name}.${resource.extension}"))

      stream.write(Base64.getDecoder.decode(resource.data))
      stream.close()
    }

    catch {
      case e: UserCancelException =>
    }
  })

  private val renameButton = new Button(I18N.gui.get("resource.rename"), () => {
    val resource = manager.getResource(table.getValueAt(table.getSelectedRow, 0).toString).get

    val name = new InputOptionPane(parent, I18N.gui.get("resource.newName"), I18N.gui.get("resource.newName"),
                                   resource.name).getInput

    if (name != null) {
      val trimmed = name.trim

      if (trimmed.isEmpty) {
        new OptionPane(parent, I18N.gui.get("common.messages.error"), I18N.gui.get("resource.nameEmpty"),
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
      }

      else if (trimmed != resource.name) {
        manager.removeResource(resource.name)

        if (manager.addResource(resource.copy(name = trimmed))) {
          refreshList()

          new DirtyEvent(None).raise(parent)
          new ResourcesChangedEvent().raise(parent)
        }

        else {
          manager.addResource(resource)

          new OptionPane(parent, I18N.gui.get("common.messages.error"), I18N.gui.getN("resource.alreadyExists", trimmed),
                         OptionPane.Options.Ok, OptionPane.Icons.Error)
        }
      }
    }
  })

  private val removeButton = new Button(I18N.gui.get("resource.remove"), () => {
    manager.removeResource(table.getValueAt(table.getSelectedRow, 0).toString)

    refreshList()

    new DirtyEvent(None).raise(parent)
    new ResourcesChangedEvent().raise(parent)
  })

  private val contents = new BoxColumn(Seq(
    scrollPane,
    new BoxRow(Seq(
      addButton,
      exportButton,
      renameButton,
      removeButton
    ), 6, BoxAlign.Center) with MaximumHeight
  ), 6) with SyncZoom {
    setOpaque(true)
    setBorder(new ZoomableBorder(6, 6, 6, 6))

    override def zoom(oldZoom: Float): Unit = {
      super.zoom(oldZoom)

      pack()
    }
  }

  setContentPane(contents)

  refreshList()

  contents.syncZoom()

  pack()

  Positioning.center(this, parent)

  Utils.addEscKeyAction(this, new AbstractAction {
    override def actionPerformed(e: ActionEvent): Unit = {
      setVisible(false)
    }
  })

  syncTheme()

  override def setVisible(visible: Boolean): Unit = {
    if (visible)
      contents.syncZoom()

    super.setVisible(visible)
  }

  private def refreshList(): Unit = {
    tableModel.setRowCount(0)

    manager.getResources.foreach { resource =>
      tableModel.addRow(Array[Object](resource.name, resource.extension))
    }

    enableButtons()
  }

  private def enableButtons(): Unit = {
    exportButton.setEnabled(table.getSelectedRow != -1)
    renameButton.setEnabled(table.getSelectedRow != -1)
    removeButton.setEnabled(table.getSelectedRow != -1)
  }

  override def syncTheme(): Unit = {
    contents.setBackground(InterfaceColors.dialogBackground())
    scrollPane.setBackground(InterfaceColors.dialogBackground())
    table.setBackground(InterfaceColors.dialogBackground())

    addButton.syncTheme()
    exportButton.syncTheme()
    renameButton.syncTheme()
    removeButton.syncTheme()
  }

  private class ResourceCellRenderer extends BoxRow(BoxAlign.Start) with TableCellRenderer with SyncZoom {
    private val label = new JLabel

    setOpaque(true)

    add(label)

    override def getInsets: java.awt.Insets =
      new Insets(Utils.zoom(3), Utils.zoom(6), Utils.zoom(3), Utils.zoom(6))

    def getTableCellRendererComponent(table: JTable, value: Object, isSelected: Boolean, hasFocus: Boolean, row: Int,
                                      column: Int): Component = {
      label.setText(value.toString)

      if (isSelected) {
        setBackground(InterfaceColors.dialogBackgroundSelected())

        label.setForeground(InterfaceColors.dialogTextSelected())
      }

      else {
        setBackground(InterfaceColors.dialogBackground())

        label.setForeground(InterfaceColors.dialogText())
      }

      if (column == 0) {
        setBorder(new MatteBorder(0, 0, 1, 0, InterfaceColors.dialogText()))
      } else {
        setBorder(new MatteBorder(0, 1, 1, 0, InterfaceColors.dialogText()))
      }

      this
    }
  }

  private class HeaderCellRenderer extends BoxRow(BoxAlign.Start) with TableCellRenderer with SyncZoom {
    private val label = new JLabel {
      setFont(getFont.deriveFont(Font.BOLD))
    }

    setOpaque(true)

    add(label)

    override def getInsets: java.awt.Insets =
      new Insets(Utils.zoom(3), Utils.zoom(6), Utils.zoom(3), Utils.zoom(6))

    def getTableCellRendererComponent(table: JTable, value: Object, isSelected: Boolean, hasFocus: Boolean, row: Int,
                                      column: Int): Component = {
      label.setText(value.toString)

      setBackground(InterfaceColors.dialogBackground())

      if (column == 0) {
        setBorder(new MatteBorder(0, 0, 1, 0, InterfaceColors.dialogText()))
      } else {
        setBorder(new MatteBorder(0, 1, 1, 0, InterfaceColors.dialogText()))
      }

      label.setForeground(InterfaceColors.dialogText())

      this
    }
  }
}
