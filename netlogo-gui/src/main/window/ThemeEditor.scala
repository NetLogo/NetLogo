// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Color, Dialog, Dimension, Graphics, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ MouseAdapter, MouseEvent, WindowAdapter, WindowEvent }
import javax.swing.{ Box, BoxLayout, JDialog, JLabel, JPanel, ScrollPaneConstants, WindowConstants }
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.border.EmptyBorder

import org.nlogo.core.I18N
import org.nlogo.swing.{ ButtonPanel, CheckBox, CollapsibleArrow, DialogButton, OptionPane, PackedLayout, Positioning,
                         RoundedBorderPanel, ScrollPane, TextField, Transparent, Utils }
import org.nlogo.theme.{ ColorTheme, EditableColor, EditableTheme, InterfaceColors, ThemeSync }

class ThemeEditor(manager: ThemesManager, baseTheme: ColorTheme, appFrame: ThemeSync, isNew: Boolean)
  extends JDialog(manager, I18N.gui.get("menu.tools.themeEditor"), Dialog.ModalityType.DOCUMENT_MODAL) with ThemeSync
  with EditableTheme(baseTheme) {

  private var accepted = false

  private val nameLabel = new JLabel(I18N.gui.get("menu.tools.themeEditor.name"))

  private val nameField = new TextField(text = name) {
    getDocument.addDocumentListener(new DocumentListener {
      override def changedUpdate(e: DocumentEvent): Unit = {
        name = getText.trim
      }

      override def insertUpdate(e: DocumentEvent): Unit = {
        name = getText.trim
      }

      override def removeUpdate(e: DocumentEvent): Unit = {
        name = getText.trim
      }
    })
  }

  private val darkCheckbox = new CheckBox(I18N.gui.get("menu.tools.themeEditor.isDark"), isDark = _) {
    setSelected(isDark)
  }

  private val colorPicker = new ThemeColorPicker(setColor)
  private val divider = new Divider

  private val groupsPanel = new GroupsPanel

  private val scrollPane = new ScrollPane(groupsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
    setBorder(null)

    override def getPreferredSize: Dimension =
      new Dimension(super.getPreferredSize.width.max(250), 500)
  }

  private var currentEditor: Option[ColorEditor] = None

  private val cancelButton = new DialogButton(false, I18N.gui.get("common.buttons.cancel"), _ => cancel())
  private val okButton = new DialogButton(true, I18N.gui.get("common.buttons.ok"), _ => accept())

  private val buttonPanel = new ButtonPanel(Seq(cancelButton, okButton))

  add(new JPanel with Transparent {
    setLayout(new BorderLayout(12, 6))
    setBorder(new EmptyBorder(6, 6, 6, 6))

    add(new PackedLayout(Seq(colorPicker, divider), spacing = 12), BorderLayout.WEST)
    add(scrollPane, BorderLayout.CENTER)
    add(buttonPanel, BorderLayout.SOUTH)
  })

  pack()
  syncTheme()

  Positioning.center(this, manager)

  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = {
      cancel()
    }
  })

  Utils.addEscKeyAction(this, () => cancel())

  colorPicker.setColor(Color.BLACK)

  setMinimumSize(new Dimension(colorPicker.getPreferredSize.width + groupsPanel.getPreferredSize.width + 36,
                               colorPicker.getPreferredSize.height + buttonPanel.getPreferredSize.height + 24))
  setVisible(true)

  private def accept(): Unit = {
    if (name.isEmpty) {
      new OptionPane(this, I18N.gui.get("menu.tools.themeEditor.invalidName"),
                     I18N.gui.getN("menu.tools.themeEditor.emptyName", name), OptionPane.Options.Ok,
                     OptionPane.Icons.Error)
    } else if (isNew && InterfaceColors.themeExists(name)) {
      new OptionPane(this, I18N.gui.get("menu.tools.themeEditor.invalidName"),
                     I18N.gui.getN("menu.tools.themeEditor.duplicateName", name), OptionPane.Options.Ok,
                     OptionPane.Icons.Error)
    } else {
      accepted = true

      setVisible(false)
    }
  }

  private def cancel(): Unit = {
    setVisible(false)
  }

  private def applyPreview(): Unit = {
    InterfaceColors.setTheme(getStaticTheme)

    appFrame.syncTheme()

    syncTheme()
    repaint()
  }

  private def setCurrentEditor(editor: ColorEditor): Unit = {
    currentEditor.foreach(_.setCurrent(false))

    currentEditor = Option(editor)

    currentEditor.foreach { editor =>
      editor.setCurrent(true)

      colorPicker.setColor(editor.getColor)
    }
  }

  def setColor(color: Color): Unit = {
    currentEditor.foreach(_.setColor(color))
    applyPreview()
  }

  def getTheme: Option[ColorTheme] = {
    if (accepted) {
      Some(getStaticTheme)
    } else {
      None
    }
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())
    scrollPane.setBackground(InterfaceColors.dialogBackground())

    nameLabel.setForeground(InterfaceColors.dialogText())
    darkCheckbox.setForeground(InterfaceColors.dialogText())

    colorPicker.syncTheme()
    divider.syncTheme()
    nameField.syncTheme()
    darkCheckbox.syncTheme()
    cancelButton.syncTheme()
    okButton.syncTheme()

    groupsPanel.getComponents.foreach {
      case ts: ThemeSync =>
        ts.syncTheme()

      case _ =>
    }
  }

  private class Divider extends JPanel with ThemeSync {
    override def getPreferredSize: Dimension =
      new Dimension(1, getParent.getHeight)

    override def getMinimumSize: Dimension =
      getPreferredSize

    override def getMaximumSize: Dimension =
      getPreferredSize

    override def syncTheme(): Unit = {
      setBackground(InterfaceColors.toolbarControlBorder())
    }
  }

  private class GroupsPanel extends JPanel with Transparent {
    private val groups = colorGroups.map(new CollapsiblePanel(_, _))

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    setBorder(new EmptyBorder(0, 0, 0, 12))

    add(new PackedLayout(Seq(nameLabel, nameField, darkCheckbox), spacing = 6))
    add(Box.createVerticalStrut(6))

    groups.foreach { group =>
      add(group)
      add(Box.createVerticalStrut(6))
    }

    override def getPreferredSize: Dimension =
      new Dimension(groups.map(_.getPreferredSize.width).max, super.getPreferredSize.height)

    override def getMinimumSize: Dimension =
      getPreferredSize
  }

  private class CollapsiblePanel(name: String, colors: Seq[EditableColor])
    extends JPanel(new GridBagLayout) with Transparent with ThemeSync {

    private val label = new JLabel(name)
    private val arrow = new CollapsibleArrow(false)

    private val colorPanel = new JPanel(new GridBagLayout) with Transparent {
      locally {
        val c = new GridBagConstraints

        c.anchor = GridBagConstraints.WEST

        colors.foreach { color =>
          c.gridx = 0
          c.weightx = 1
          c.insets = new Insets(0, 0, 6, 6)

          add(new JLabel(color.name) with ThemeSync {
            override def syncTheme(): Unit = {
              setForeground(InterfaceColors.dialogText())
            }
          }, c)

          c.gridx = 1
          c.weightx = 0
          c.insets = new Insets(0, 0, 6, 0)

          add(new ColorEditor(color), c)
        }

        setVisible(false)
      }
    }

    locally {
      val c = new GridBagConstraints

      c.gridx = 0
      c.weightx = 1
      c.anchor = GridBagConstraints.NORTHWEST

      add(new PackedLayout(Seq(new JLabel(arrow), label), alignment = PackedLayout.Leading, spacing = 6) {
        addMouseListener(new MouseAdapter {
          override def mouseReleased(e: MouseEvent): Unit = {
            setOpen(!colorPanel.isVisible)
          }
        })
      }, c)

      c.weighty = 1
      c.insets = new Insets(6, arrow.getIconWidth + 6, 0, 0)

      add(colorPanel, c)
    }

    private def setOpen(open: Boolean): Unit = {
      colorPanel.setVisible(open)
      arrow.setOpen(open)
    }

    override def getPreferredSize: Dimension =
      new Dimension(colorPanel.getPreferredSize.width, super.getPreferredSize.height)

    override def getMinimumSize: Dimension =
      getPreferredSize

    override def syncTheme(): Unit = {
      label.setForeground(InterfaceColors.dialogText())

      colorPanel.getComponents.foreach {
        case ts: ThemeSync =>
          ts.syncTheme()

        case _ =>
      }
    }
  }

  private class ColorEditor(color: EditableColor) extends RoundedBorderPanel with ThemeSync {
    private var current = false
    private var borderColor: Color = Color.WHITE

    setColor(color.value)
    setDiameter(6)
    enableHover()
    enablePressed()
    syncTheme()

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        setCurrentEditor(ColorEditor.this)
      }
    })

    def getColor: Color =
      color.value

    def setColor(color: Color): Unit = {
      this.color.value = color

      setBackgroundColor(color)
      setBackgroundHoverColor(color)
      setBackgroundPressedColor(color)

      repaint()
    }

    def setCurrent(current: Boolean): Unit = {
      this.current = current

      repaint()
    }

    override def getPreferredSize: Dimension =
      new Dimension(24, 24)

    override def getMinimumSize: Dimension =
      getPreferredSize

    override def getMaximumSize: Dimension =
      getPreferredSize

    override def paintComponent(g: Graphics): Unit = {
      if (current) {
        setBorderColor(InterfaceColors.toolbarControlBorderSelected())
      } else {
        setBorderColor(borderColor)
      }

      super.paintComponent(g)
    }

    override def syncTheme(): Unit = {
      borderColor = InterfaceColors.toolbarControlBorder()
    }
  }
}
