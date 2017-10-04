// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.nlogo.api.LabProtocol
import org.nlogo.api.{ RefEnumeratedValueSet, LabProtocol }
import org.nlogo.window.{ EditDialogFactoryInterface, MenuBarFactory }
import java.awt.{ Component, Dimension }
import javax.swing.{ JButton, JDialog, JLabel, JList, JMenuBar, JOptionPane, JPanel, JScrollPane, ListCellRenderer }
import org.nlogo.core.I18N

private class ManagerDialog(manager:       LabManager,
                            dialogFactory: EditDialogFactoryInterface,
                            menuFactory:   MenuBarFactory)
  extends JDialog(manager.workspace.getFrame)
  with javax.swing.event.ListSelectionListener
{
  private val jlist = new JList[LabProtocol]
  private val listModel = new javax.swing.DefaultListModel[LabProtocol]
  private implicit val i18NPrefix = I18N.Prefix("tools.behaviorSpace")
  /// actions
  private def action(name: String, fn: ()=>Unit) =
    new javax.swing.AbstractAction(name) {
      def actionPerformed(e: java.awt.event.ActionEvent) { fn() } }
  private val editAction = action(I18N.gui("edit"), edit _)
  private val newAction = action(I18N.gui("new"), makeNew _)
  private val deleteAction = action(I18N.gui("delete"), delete _)
  private val duplicateAction = action(I18N.gui("duplicate"), duplicate _)
  private val closeAction = action(I18N.gui("close"), manager.close _)
  private val runAction = action(I18N.gui("run"), run _)
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
        if(e.getClickCount > 1) edit() } })
    jlist.setCellRenderer(new ProtocolRenderer())
    // Setup the first row of buttons
    val buttonPanel = new JPanel
    val runButton = new JButton(runAction)
    buttonPanel.setLayout(new javax.swing.BoxLayout(buttonPanel, javax.swing.BoxLayout.X_AXIS))
    buttonPanel.add(javax.swing.Box.createHorizontalGlue)
    buttonPanel.add(javax.swing.Box.createHorizontalStrut(20))
    buttonPanel.add(new JButton(newAction))
    buttonPanel.add(javax.swing.Box.createHorizontalStrut(5))
    buttonPanel.add(new JButton(editAction))
    buttonPanel.add(javax.swing.Box.createHorizontalStrut(5))
    buttonPanel.add(new JButton(duplicateAction))
    buttonPanel.add(javax.swing.Box.createHorizontalStrut(5))
    buttonPanel.add(new JButton(deleteAction))
    buttonPanel.add(javax.swing.Box.createHorizontalStrut(5))
    buttonPanel.add(runButton)
    buttonPanel.add(javax.swing.Box.createHorizontalStrut(20))
    buttonPanel.add(javax.swing.Box.createHorizontalGlue)
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
    val count = jlist.getSelectedIndices.length
    editAction.setEnabled(count == 1)
    duplicateAction.setEnabled(count == 1)
    runAction.setEnabled(count == 1)
    deleteAction.setEnabled(count > 0)
  }
  /// action implementations
  private def run(): Unit = {
    try {
      manager.prepareForRun()
      new Supervisor(this, manager.workspace, selectedProtocol, manager.workspaceFactory, dialogFactory).start()
    }
    catch { case ex: org.nlogo.awt.UserCancelException => org.nlogo.api.Exceptions.ignore(ex) }
  }
  private def makeNew(): Unit = {
    editProtocol(
      new LabProtocol(
        "experiment", "setup", "go", "", 1, true, true, 0, "", List("count turtles"),
        manager.workspace.world.synchronized {
          manager.workspace.world.program.interfaceGlobals.toList
          .map{case variableName: String =>
            new RefEnumeratedValueSet(
              variableName, List(manager.workspace.world.getObserverVariableByName(variableName)))}}),
      true)
  }
  private def duplicate() { editProtocol(selectedProtocol, true) }
  private def edit() { editProtocol(selectedProtocol, false) }
  private def editProtocol(protocol: LabProtocol, isNew: Boolean) {
    val editable = new ProtocolEditable(protocol, manager.workspace.getFrame,
                                        manager.workspace.compilerServices, manager.workspace.world)
    if(!dialogFactory.canceled(this, editable)) {
      val newProtocol = editable.get.get
      if(isNew) manager.protocols += newProtocol
      else manager.protocols(selectedIndex) = newProtocol
      update()
      select(newProtocol)
      manager.dirty()
    }
  }
  private def delete() {
    val selected = jlist.getSelectedIndices
    val message = "Are you sure you want to delete " +
      (if(selected.length > 1) "these " + selected.length + " experiments?"
       else "\"" + listModel.getElementAt(selected(0)).asInstanceOf[LabProtocol].name + "\"?")
    if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, message, "Delete", JOptionPane.YES_NO_OPTION)) {
      for(i <- 0 until selected.length)
        manager.protocols -= listModel.getElementAt(selected(i)).asInstanceOf[LabProtocol]
      update()
      // it's annoying if nothing is left selected, so select something
      val newSize = manager.protocols.size
      if(newSize > 0) select(if(selected(0) >= newSize) (selected(0) - 1) min (newSize - 1)
                             else selected(0))
      manager.dirty()
    }
  }
  /// helpers
  def update() {
    listModel.clear
    manager.protocols.foreach(listModel.addElement(_))
    valueChanged(null)
    if(manager.protocols.size > 0) jlist.setSelectedIndices(Array(0))
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
          s"${proto.name} (${proto.countRuns} run${(if(proto.countRuns != 1) "s" else "")})"
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
