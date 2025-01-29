// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Dimension, Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, Action, ButtonGroup, JLabel, JPanel }
import javax.swing.border.EmptyBorder

import org.nlogo.api.Editable
import org.nlogo.app.common.{ Events => AppEvents }
import org.nlogo.core.I18N
import org.nlogo.swing.{ DropdownArrow, HoverDecoration, MenuItem, PopupMenu, RoundedBorderPanel, ToolBarToggleButton,
                         Transparent, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ EditDialogFactoryInterface, Events => WindowEvents, GUIWorkspace, JobWidget, Widget,
                          WidgetInfo }

import scala.collection.mutable.HashSet

class InterfaceWidgetControls(wPanel: WidgetPanel,
                              workspace: GUIWorkspace,
                              WidgetInfos: List[WidgetInfo],
                              frame: Frame,
                              dialogFactory: EditDialogFactoryInterface)
  extends JPanel(new GridBagLayout)
  with Transparent
  with AppEvents.WidgetSelectedEvent.Handler
  with AppEvents.InterfaceModeEvent.Handler
  with WindowEvents.WidgetForegroundedEvent.Handler
  with WindowEvents.WidgetRemovedEvent.Handler
  with WindowEvents.EditWidgetEvent.Handler
  with WindowEvents.WidgetAddedEvent.Handler
  with ThemeSync {

  private val selectedObjects = new HashSet[Widget]

  private val interactButton = new SquareButton(new InteractAction)
  private val selectButton = new SquareButton(new SelectAction)
  private val editButton = new SquareButton(new EditAction)
  private val deleteButton = new SquareButton(new DeleteAction)

  private val widgetMenu = new WidgetMenu
  private val alignmentMenu = new AlignmentMenu

  interactButton.setToolTipText(I18N.gui.get("tabs.run.interactButton.tooltip"))
  selectButton.setToolTipText(I18N.gui.get("tabs.run.selectButton.tooltip"))
  editButton.setToolTipText(I18N.gui.get("tabs.run.editButton.tooltip"))
  deleteButton.setToolTipText(I18N.gui.get("tabs.run.deleteButton.tooltip"))

  locally {
    val group = new ButtonGroup

    group.add(interactButton)
    group.add(selectButton)
    group.add(editButton)
    group.add(deleteButton)

    val c = new GridBagConstraints

    c.anchor = GridBagConstraints.CENTER
    c.fill = GridBagConstraints.VERTICAL
    c.weighty = 1
    c.insets = new Insets(0, 6, 0, 6)

    add(widgetMenu, c)

    c.insets = new Insets(0, 0, 0, 6)

    add(alignmentMenu, c)

    add(interactButton, c)
    add(selectButton, c)
    add(editButton, c)

    c.insets = new Insets(0, 0, 0, 0)

    add(deleteButton, c)
  }

  interactButton.setSelected(true)

  class InteractAction extends AbstractAction {
    def actionPerformed(e: ActionEvent) {
      wPanel.setInteractMode(InteractMode.INTERACT)
    }
  }

  class SelectAction extends AbstractAction {
    def actionPerformed(e: ActionEvent) {
      wPanel.setInteractMode(InteractMode.SELECT)
    }
  }

  class EditAction extends AbstractAction {
    def actionPerformed(e: ActionEvent) {
      if (editButton.isSelected) {
        new WindowEvents.EditWidgetEvent(null).raise(InterfaceWidgetControls.this)

        wPanel.setInteractMode(InteractMode.EDIT)
      }

      else
        editButton.doClick()
    }
  }

  class DeleteAction extends AbstractAction {
    def actionPerformed(e: ActionEvent) {
      if (deleteButton.isSelected) {
        wPanel.deleteSelectedWidgets()
        wPanel.beginDelete()
      }

      else
        deleteButton.doClick()
    }
  }

  private var editTarget: Option[Editable] = None

  def handle(e: WindowEvents.EditWidgetEvent) {
    // this is to support the "Edit..." button in the view control strip - ST 7/18/03
    val targetOption = Option(e.widget).orElse {
      if (!editButton.isEnabled) return
      editTarget
    }.filter(wPanel.contains)
    for(target <- targetOption) {
      def suppress(b: Boolean) {
        target match {
          case w: JobWidget => w.suppressRecompiles(b)
          case _ =>
        }
      }
      suppress(true)
      wPanel.editWidgetFinished(target, dialogFactory.canceled(frame, target, false))
      suppress(false)
    }
  }

  def syncTheme() {
    setBackground(InterfaceColors.TOOLBAR_BACKGROUND)

    widgetMenu.syncTheme()
    alignmentMenu.syncTheme()

    interactButton.setIcon(Utils.iconScaledWithColor("/images/pointer.png", 15, 15, InterfaceColors.TOOLBAR_IMAGE))
    selectButton.setIcon(Utils.iconScaledWithColor("/images/select.png", 15, 15, InterfaceColors.TOOLBAR_IMAGE))
    editButton.setIcon(Utils.iconScaledWithColor("/images/edit.png", 15, 15, InterfaceColors.TOOLBAR_IMAGE))
    deleteButton.setIcon(Utils.iconScaledWithColor("/images/delete.png", 15, 15, InterfaceColors.TOOLBAR_IMAGE))
  }

  def handle(e: WindowEvents.WidgetRemovedEvent) {
    val r = e.widget
    if(selectedObjects.contains(r)) {
      if(r.isInstanceOf[Editable] && editTarget.exists(_ == r.asInstanceOf[Editable]))
        editTarget = None
      selectedObjects.remove(r)
    }
  }

  private def updateActions(widget: Widget) {
    if (wPanel.getWrapper(widget).selected)
      selectedObjects += widget
    else
      selectedObjects -= widget

    updateTarget(widget)
  }

  private def updateTarget(widget: Widget) {
    if (selectedObjects.size == 1)
      editTarget = Some(widget.getEditable).collect { case editable: Editable => editable }
    else
      editTarget = None
  }

  def handle(e: WindowEvents.WidgetAddedEvent) {
    updateActions(e.widget.asInstanceOf[Widget])
  }

  final def handle(e: AppEvents.WidgetSelectedEvent) {
    updateActions(e.widget)
  }

  def handle(e: WindowEvents.WidgetForegroundedEvent) {
    updateTarget(e.widget)
  }

  def handle(e: AppEvents.InterfaceModeEvent) {
    e.mode match {
      case InteractMode.SELECT =>
        selectButton.setSelected(true)

      case InteractMode.EDIT =>
        editButton.setSelected(true)

      case InteractMode.DELETE =>
        deleteButton.setSelected(true)

      case _ =>
        interactButton.setSelected(true)
    }
  }

  class WidgetMenu extends JPanel(new GridBagLayout) with RoundedBorderPanel with ThemeSync with HoverDecoration {
    private val label = new JLabel(I18N.gui.get("tabs.run.addWidget"))
    private val arrow = new DropdownArrow

    setDiameter(6)
    enableHover()

    locally {
      val c = new GridBagConstraints

      c.insets = new Insets(6, 6, 6, 6)

      add(label, c)
      add(arrow, c)
    }

    private val actions =
      WidgetInfos.map(spec => new MenuItem(new AbstractAction(spec.displayName, spec.icon) {
        def actionPerformed(e: ActionEvent) {
          chosenItem = spec.displayName

          wPanel.createShadowWidget(widgetMenu.getSelectedWidget)
        }
      }))

    private var chosenItem = ""

    val popup = new PopupMenu

    popup.add(actions(0))
    popup.addSeparator()
    popup.add(actions(1))
    popup.add(actions(2))
    popup.add(actions(3))
    popup.add(actions(4))
    popup.addSeparator()
    popup.add(actions(5))
    popup.add(actions(6))
    popup.add(actions(7))
    popup.addSeparator()
    popup.add(actions(8))

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        if (!wPanel.addingWidget) {
          actions.foreach(action => action.setEnabled(wPanel.canAddWidget(action.getText)))

          popup.show(WidgetMenu.this, 0, getHeight)
        }
      }
    })

    def getSelectedWidget =
      WidgetInfos.find(_.displayName == chosenItem).get.coreWidget

    def syncTheme() {
      setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
      setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
      setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)

      label.setForeground(InterfaceColors.TOOLBAR_TEXT)

      popup.syncTheme()
    }
  }

  class AlignmentMenu extends JPanel(new GridBagLayout) with RoundedBorderPanel with ThemeSync with HoverDecoration {
    private val label = new JLabel(I18N.gui.get("tabs.run.alignWidgets"))
    private val arrow = new DropdownArrow

    setDiameter(6)
    enableHover()

    locally {
      val c = new GridBagConstraints

      c.insets = new Insets(6, 6, 6, 6)

      add(label, c)
      add(arrow, c)
    }

    private val leftAction = new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.alignLeft")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignLeft(wPanel.getWrapper(selectedObjects.minBy(_.getParent.getX)))
      }
    })

    private val centerHorizontalAction = new MenuItem(
      new AbstractAction(I18N.gui.get("tabs.run.widget.alignCenterHorizontal")) {
        def actionPerformed(e: ActionEvent) {
          wPanel.alignCenterHorizontal(wPanel.getWrapper(selectedObjects.head))
        }
      })

    private val rightAction = new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.alignRight")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignRight(wPanel.getWrapper(selectedObjects.maxBy(_.getParent.getX)))
      }
    })

    private val topAction = new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.alignTop")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignTop(wPanel.getWrapper(selectedObjects.minBy(_.getParent.getY)))
      }
    })

    private val centerVerticalAction = new MenuItem(
      new AbstractAction(I18N.gui.get("tabs.run.widget.alignCenterVertical")) {
        def actionPerformed(e: ActionEvent) {
          wPanel.alignCenterVertical(wPanel.getWrapper(selectedObjects.head))
        }
      })

    private val bottomAction = new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.alignBottom")) {
      def actionPerformed(e: ActionEvent) {
        wPanel.alignBottom(wPanel.getWrapper(selectedObjects.maxBy(_.getParent.getY)))
      }
    })

    private val distributeHorizontalAction = new MenuItem(
      new AbstractAction(I18N.gui.get("tabs.run.widget.distributeHorizontal")) {
        def actionPerformed(e: ActionEvent) {
          wPanel.distributeHorizontal()
        }
      })

    private val distributeVerticalAction = new MenuItem(
      new AbstractAction(I18N.gui.get("tabs.run.widget.distributeVertical")) {
        def actionPerformed(e: ActionEvent) {
          wPanel.distributeVertical()
        }
      })

    private val stretchLeftAction = new MenuItem(
      new AbstractAction(I18N.gui.get("tabs.run.widget.stretchLeft")) {
        def actionPerformed(e: ActionEvent) {
          wPanel.stretchLeft(wPanel.getWrapper(selectedObjects.minBy(w => w.getParent.getX)))
        }
      })

    private val stretchRightAction = new MenuItem(
      new AbstractAction(I18N.gui.get("tabs.run.widget.stretchRight")) {
        def actionPerformed(e: ActionEvent) {
          wPanel.stretchRight(wPanel.getWrapper(selectedObjects.maxBy(w => w.getParent.getX + w.getParent.getWidth)))
        }
      })

    private val popup = new PopupMenu

    popup.add(new JLabel("Arrange selected widgets") {
      setBorder(new EmptyBorder(0, 6, 0, 0))
    }).setEnabled(false)
    popup.addSeparator()
    popup.add(leftAction)
    popup.add(centerHorizontalAction)
    popup.add(rightAction)
    popup.add(topAction)
    popup.add(centerVerticalAction)
    popup.add(bottomAction)
    popup.addSeparator()
    popup.add(distributeHorizontalAction)
    popup.add(distributeVerticalAction)
    popup.addSeparator()
    popup.add(stretchLeftAction)
    popup.add(stretchRightAction)

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        leftAction.setEnabled(selectedObjects.size > 1)
        centerHorizontalAction.setEnabled(selectedObjects.size > 1)
        rightAction.setEnabled(selectedObjects.size > 1)
        topAction.setEnabled(selectedObjects.size > 1)
        centerVerticalAction.setEnabled(selectedObjects.size > 1)
        bottomAction.setEnabled(selectedObjects.size > 1)
        distributeHorizontalAction.setEnabled(selectedObjects.size > 1)
        distributeVerticalAction.setEnabled(selectedObjects.size > 1)
        stretchLeftAction.setEnabled(selectedObjects.size > 1)
        stretchRightAction.setEnabled(selectedObjects.size > 1)

        popup.show(AlignmentMenu.this, 0, getHeight)
      }
    })

    def syncTheme() {
      setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
      setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
      setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)

      label.setForeground(InterfaceColors.TOOLBAR_TEXT)

      popup.syncTheme()

      leftAction.setIcon(Utils.iconScaledWithColor("/images/align-left.png", 16, 16, InterfaceColors.TOOLBAR_IMAGE))
      centerHorizontalAction.setIcon(Utils.iconScaledWithColor("/images/align-horizontal-center.png", 16, 16,
                                                               InterfaceColors.TOOLBAR_IMAGE))
      rightAction.setIcon(Utils.iconScaledWithColor("/images/align-right.png", 16, 16, InterfaceColors.TOOLBAR_IMAGE))
      topAction.setIcon(Utils.iconScaledWithColor("/images/align-top.png", 16, 16, InterfaceColors.TOOLBAR_IMAGE))
      centerVerticalAction.setIcon(Utils.iconScaledWithColor("/images/align-vertical-center.png", 16, 16,
                                                             InterfaceColors.TOOLBAR_IMAGE))
      bottomAction.setIcon(Utils.iconScaledWithColor("/images/align-bottom.png", 16, 16, InterfaceColors.TOOLBAR_IMAGE))
      distributeHorizontalAction.setIcon(Utils.iconScaledWithColor("/images/distribute-horizontal.png", 16, 16,
                                                                   InterfaceColors.TOOLBAR_IMAGE))
      distributeVerticalAction.setIcon(Utils.iconScaledWithColor("/images/distribute-vertical.png", 16, 16,
                                                                 InterfaceColors.TOOLBAR_IMAGE))
    }
  }

  class SquareButton(action: Action) extends ToolBarToggleButton(action) {
    setFocusable(false)

    override def getMinimumSize: Dimension =
      new Dimension(widgetMenu.getPreferredSize.height, widgetMenu.getPreferredSize.height)

    override def getPreferredSize: Dimension =
      getMinimumSize
  }
}
