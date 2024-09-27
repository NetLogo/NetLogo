// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.{ Component, Dimension }
import javax.swing.{ JButton, JDialog, JLabel, JList, JMenuBar, JOptionPane, JPanel, JScrollPane, ListCellRenderer }

import org.nlogo.api.{ LabProtocol, RefEnumeratedValueSet }
import org.nlogo.core.I18N
import org.nlogo.swing.FileDialog
import org.nlogo.window.{ EditDialogFactoryInterface, MenuBarFactory }

import scala.collection.mutable.Set
import scala.io.Source

private class ManagerDialog(manager:       LabManager,
                            dialogFactory: EditDialogFactoryInterface,
                            menuFactory:   MenuBarFactory)
  extends JDialog(manager.workspace.getFrame)
  with javax.swing.event.ListSelectionListener
{
  def saveProtocol(protocol: LabProtocol): Unit = {
    manager.protocols(editIndex) = protocol
    update()
    select(protocol)
  }
  private val jlist = new JList[LabProtocol]
  private val listModel = new javax.swing.DefaultListModel[LabProtocol]
  private implicit val i18NPrefix = I18N.Prefix("tools.behaviorSpace")
  /// actions
  private def action(name: String, fn: ()=>Unit) =
    new javax.swing.AbstractAction(name) {
      def actionPerformed(e: java.awt.event.ActionEvent) { fn() } }
  private val editAction = action(I18N.gui("edit"), { () => edit() })
  private val newAction = action(I18N.gui("new"), { () => makeNew() })
  private val deleteAction = action(I18N.gui("delete"), { () => delete() })
  private val duplicateAction = action(I18N.gui("duplicate"), { () => duplicate() })
  private val importAction = action(I18N.gui("import"), { () => importnl() })
  private val exportAction = action(I18N.gui("export"), { () => export() })
  private val closeAction = action(I18N.gui("close"), { () => manager.close() })
  private val abortAction = action(I18N.gui("abort"), { () => abort() })
  private val runAction = action(I18N.gui("run"), { () => run() })
  private var blockActions = false
  private var editIndex = 0
  /// initialization
  init()
  private def init() {
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
    addWindowListener(new java.awt.event.WindowAdapter {
      override def windowClosing(e: java.awt.event.WindowEvent) { closeAction.actionPerformed(null) } })
    setTitle(I18N.gui.get("menu.tools.behaviorSpace"))
    // set up the list
    jlist.setVisibleRowCount(5)
    jlist.setModel(listModel)
    jlist.addListSelectionListener(this)
    // Listen for double-clicks, and edit the selected protocol
    jlist.addMouseListener(new javax.swing.event.MouseInputAdapter {
      override def mouseClicked(e: java.awt.event.MouseEvent) {
        if (e.getClickCount > 1 && jlist.getSelectedIndices.length == 1
            && selectedProtocol.runsCompleted == 0 && !blockActions) {
          edit()
        }
      } })
    jlist.setCellRenderer(new ProtocolRenderer())
    // Setup the first row of buttons
    val buttonPanel = new JPanel
    buttonPanel.setLayout(new javax.swing.BoxLayout(buttonPanel, javax.swing.BoxLayout.Y_AXIS))
    val runButton = new JButton(runAction)
    val buttonRow1 = new JPanel
    buttonRow1.setLayout(new javax.swing.BoxLayout(buttonRow1, javax.swing.BoxLayout.X_AXIS))
    buttonRow1.add(javax.swing.Box.createHorizontalGlue)
    buttonRow1.add(javax.swing.Box.createHorizontalStrut(20))
    buttonRow1.add(new JButton(newAction))
    buttonRow1.add(javax.swing.Box.createHorizontalStrut(5))
    buttonRow1.add(new JButton(editAction))
    buttonRow1.add(javax.swing.Box.createHorizontalStrut(5))
    buttonRow1.add(new JButton(duplicateAction))
    buttonRow1.add(javax.swing.Box.createHorizontalStrut(5))
    buttonRow1.add(new JButton(deleteAction))
    buttonRow1.add(javax.swing.Box.createHorizontalStrut(20))
    buttonRow1.add(javax.swing.Box.createHorizontalGlue)
    buttonPanel.add(buttonRow1)
    buttonPanel.add(javax.swing.Box.createVerticalStrut(5))
    val buttonRow2 = new JPanel
    buttonRow2.setLayout(new javax.swing.BoxLayout(buttonRow2, javax.swing.BoxLayout.X_AXIS))
    buttonRow2.add(javax.swing.Box.createHorizontalGlue)
    buttonRow2.add(javax.swing.Box.createHorizontalStrut(20))
    buttonRow2.add(new JButton(importAction))
    buttonRow2.add(javax.swing.Box.createHorizontalStrut(5))
    buttonRow2.add(new JButton(exportAction))
    buttonRow2.add(javax.swing.Box.createHorizontalStrut(5))
    buttonRow2.add(new JButton(abortAction))
    buttonRow2.add(javax.swing.Box.createHorizontalStrut(5))
    buttonRow2.add(runButton)
    buttonRow2.add(javax.swing.Box.createHorizontalStrut(20))
    buttonRow2.add(javax.swing.Box.createHorizontalGlue)
    buttonPanel.add(buttonRow2)
    val listLabel = new JLabel(I18N.gui("experiments"))
    // layout
    buttonPanel.setBorder(new javax.swing.border.EmptyBorder(8, 0, 8, 0))
    listLabel.setBorder(new javax.swing.border.EmptyBorder(8, 0, 0, 0))
    getContentPane.setLayout(new java.awt.BorderLayout(0, 10))
    getContentPane.add(listLabel, java.awt.BorderLayout.NORTH)
    getContentPane.add(new JScrollPane(jlist), java.awt.BorderLayout.CENTER)
    getContentPane.add(buttonPanel, java.awt.BorderLayout.SOUTH)
    pack()
    // set location
    val maxBounds = getGraphicsConfiguration.getBounds
    setLocation(maxBounds.x + maxBounds.width / 3,
                maxBounds.y + maxBounds.height / 2)

    // menu - make a file menu available for saving, but don't show it
    val menus = new JMenuBar() {
      add(menuFactory.createFileMenu)
      setPreferredSize(new Dimension(0, 0))
    }
    setJMenuBar(menus)
    // misc
    org.nlogo.swing.Utils.addEscKeyAction(this, closeAction)
    getRootPane.setDefaultButton(runButton)
  }
  /// implement ListSelectionListener
  def valueChanged(e: javax.swing.event.ListSelectionEvent) {
    if (blockActions) {
      editAction.setEnabled(false)
      newAction.setEnabled(false)
      deleteAction.setEnabled(false)
      duplicateAction.setEnabled(false)
      importAction.setEnabled(false)
      exportAction.setEnabled(false)
      closeAction.setEnabled(false)
      abortAction.setEnabled(false)
      runAction.setEnabled(false)
    }
    else {
      val count = jlist.getSelectedIndices.length
      editAction.setEnabled(count == 1 && selectedProtocol.runsCompleted == 0)
      newAction.setEnabled(true)
      deleteAction.setEnabled(count > 0)
      duplicateAction.setEnabled(count == 1)
      importAction.setEnabled(true)
      exportAction.setEnabled(count > 0)
      closeAction.setEnabled(true)
      abortAction.setEnabled(count == 1 && selectedProtocol.runsCompleted != 0)
      runAction.setEnabled(count == 1)
    }
  }
  /// action implementations
  private def run(): Unit = {
    try {
      editIndex = selectedIndex

      manager.prepareForRun()

      new Supervisor(this, manager.workspace, selectedProtocol, manager.workspaceFactory, dialogFactory, saveProtocol, Supervisor.GUI).start()
    }
    catch { case ex: org.nlogo.awt.UserCancelException => org.nlogo.api.Exceptions.ignore(ex) }
  }
  private def makeNew(): Unit = {
    editProtocol(
      new LabProtocol(constants = manager.workspace.world.synchronized {
                                    manager.workspace.world.program.interfaceGlobals.toList
                                    .map{case variableName: String =>
                                      new RefEnumeratedValueSet(
                                        variableName,
                                        List(manager.workspace.world.getObserverVariableByName(variableName)))}}),
      true)
  }
  private def duplicate() { editProtocol(selectedProtocol.copy(name = selectedProtocol.name + " (copy)",
                                                               runsCompleted = 0), true) }
  private def edit() { editProtocol(selectedProtocol, false) }
  private def editProtocol(protocol: LabProtocol, isNew: Boolean) {
    blockActions = true
    if (!isNew) editIndex = selectedIndex
    update()
    val editable = new ProtocolEditable(protocol, manager.workspace.getFrame,
                                        manager.workspace, manager.workspace.world,
                                        manager.protocols.map(_.name).filter(isNew || _ != protocol.name))
    dialogFactory.create(this, editable, new java.util.function.Consumer[java.lang.Boolean] {
      def accept(success: java.lang.Boolean) {
        blockActions = false
        if (success) {
          val newProtocol = editable.get.get
          if (isNew) manager.protocols += newProtocol
          else manager.protocols(editIndex) = newProtocol
          update()
          select(newProtocol)
          manager.dirty()
        }
        else {
          update()
        }
      }
    }, true)
  }
  private def delete() {
    val selected = jlist.getSelectedIndices
    val message = {
      if (selected.length > 1) I18N.gui("delete.confirm.multiple", selected.length.toString)
      else I18N.gui("delete.confirm.one", listModel.getElementAt(selected(0)).asInstanceOf[LabProtocol].name)
    }
    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, message, I18N.gui("delete"), JOptionPane.YES_NO_OPTION)) {
      for(i <- 0 until selected.length)
        manager.protocols -= listModel.getElementAt(selected(i)).asInstanceOf[LabProtocol]
      update()
      // it's annoying if nothing is left selected, so select something
      val newSize = manager.protocols.size
      if (newSize > 0) select(if (selected(0) >= newSize) (selected(0) - 1) min (newSize - 1)
                             else selected(0))
      manager.dirty()
    }
  }
  private def importnl() {
    try {
      class XMLFilter extends java.io.FilenameFilter {
        def accept(dir: java.io.File, name: String): Boolean = {
          val split = name.split('.')

          split.length == 2 && split(1) == "xml"
        }
      }

      val dialog = new java.awt.FileDialog(manager.workspace.getFrame, I18N.gui("import.dialog"))

      dialog.setDirectory(System.getProperty("user.home"))
      dialog.setFilenameFilter(new XMLFilter)
      dialog.setMultipleMode(true)
      dialog.setVisible(true)

      for (file <- dialog.getFiles)
      {
        try {
          manager.modelLoader.readExperiments(Source.fromFile(file).mkString, true,
                                              manager.protocols.map(_.name).to[Set]).foreach(_.foreach(manager.addProtocol(_)))
        } catch {
          case e: org.xml.sax.SAXParseException => {
            if (!java.awt.GraphicsEnvironment.isHeadless) {
              javax.swing.JOptionPane.showMessageDialog(manager.workspace.getFrame,
                                                        I18N.gui("error.import", file.getName),
                                                        I18N.gui("invalid"),
                                                        javax.swing.JOptionPane.ERROR_MESSAGE)
            }
          }
        }
      }

      update()
    } catch {
      case e: org.nlogo.awt.UserCancelException => org.nlogo.api.Exceptions.ignore(e)
    }
  }
  private def export() {
    try {
      val indices = jlist.getSelectedIndices

      val modelName =
        if (manager.workspace.getModelFileName == null)
          ""
        else
          manager.workspace.getModelFileName.split('.')(0) + '-'

      var path = FileDialog.showFiles(manager.workspace.getFrame, I18N.gui("export.dialog"), java.awt.FileDialog.SAVE,
                  if (indices.length == 1)
                    modelName + selectedProtocol.name + "-experiment.xml"
                  else
                    modelName + "experiments.xml")

      if (!path.endsWith(".xml")) {
        path += ".xml"
      }

      val out = new java.io.PrintWriter(path)

      manager.modelLoader.writeExperiments(manager.protocols.toSeq, out)

      out.close()
    } catch {
      case e: org.nlogo.awt.UserCancelException => org.nlogo.api.Exceptions.ignore(e)
    }
  }
  private def abort() {
    saveProtocol(selectedProtocol.copy(runsCompleted = 0, runOptions = null))
  }
  /// helpers
  def update() {
    listModel.clear
    manager.protocols.foreach(listModel.addElement(_))
    manager.workspace.setBehaviorSpaceExperiments(manager.protocols.toList)
    valueChanged(null)
    if (manager.protocols.size > 0) jlist.setSelectedIndices(Array(0))
  }
  private def select(index: Int) {
    jlist.setSelectedIndices(Array(index))
    jlist.ensureIndexIsVisible(index)
  }
  private def select(targetProtocol: LabProtocol) {
    val index = manager.protocols.indexWhere(_ eq targetProtocol)
    jlist.setSelectedIndices(Array(index))
    jlist.ensureIndexIsVisible(index)
  }
  private def selectedIndex =
    jlist.getSelectedIndices match { case Array(i: Int) => i }
  private def selectedProtocol =
    manager.protocols(jlist.getSelectedIndices()(0))

  class ProtocolRenderer extends JLabel with ListCellRenderer[LabProtocol] {
    def getListCellRendererComponent(list: JList[_ <: LabProtocol],
      proto: LabProtocol, index: Int,
      isSelected: Boolean, cellHasFocus: Boolean): Component = {
        val text =
          if (proto.runsCompleted != 0)
            I18N.gui("inProgress", proto.name, proto.runsCompleted.toString, proto.countRuns.toString)
          else
            s"${proto.name} (${proto.countRuns} run${(if (proto.countRuns != 1) "s" else "")})"
        setText(text)
        if (isSelected) {
          setOpaque(true)
          setForeground(list.getSelectionForeground())
          setBackground(list.getSelectionBackground())
        } else {
          setOpaque(false)
          setForeground(list.getForeground())
          setBackground(list.getBackground())
        }
        this
    }
  }
}
