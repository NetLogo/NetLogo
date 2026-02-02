// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Color, Dialog, Dimension, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ MouseAdapter, MouseEvent, WindowAdapter, WindowEvent }
import javax.swing.{ Box, BoxLayout, JDialog, JLabel, JPanel, ScrollPaneConstants, WindowConstants }
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.border.EmptyBorder

import org.nlogo.core.I18N
import org.nlogo.swing.{ ButtonPanel, CheckBox, CollapsibleArrow, DialogButton, OptionPane, Positioning,
                         RoundedBorderPanel, ScrollPane, TextField, Transparent, Utils }
import org.nlogo.theme.{ ColorTheme, InterfaceColors, ThemeSync }

class ThemeEditor(manager: ThemesManager, baseTheme: ColorTheme, appFrame: ThemeSync)
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

  private val panel = new JPanel(new GridBagLayout) with Transparent {
    locally {
      val c = new GridBagConstraints

      c.gridx = 0
      c.weightx = 1
      c.anchor = GridBagConstraints.NORTHWEST
      c.insets = new Insets(0, 0, 6, 12)

      add(new JPanel with Transparent {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

        add(nameLabel)
        add(Box.createHorizontalStrut(6))
        add(nameField)
      }, c)

      add(new JPanel with Transparent {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

        add(darkCheckbox)
      }, c)

      c.weighty = 1

      colorGroups.foreach { (name, colors) =>
        add(new CollapsiblePanel(name, colors), c)
      }
    }
  }

  private val scrollPane = new ScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
    setBorder(null)

    override def getPreferredSize: Dimension =
      new Dimension(super.getPreferredSize.width, 500)
  }

  private val cancelButton = new DialogButton(false, I18N.gui.get("common.buttons.cancel"), _ => cancel())
  private val okButton = new DialogButton(true, I18N.gui.get("common.buttons.ok"), _ => accept())

  setLayout(new BorderLayout(6, 6))

  getRootPane.setBorder(new EmptyBorder(6, 6, 6, 6))

  add(scrollPane, BorderLayout.CENTER)
  add(new ButtonPanel(Seq(cancelButton, okButton)), BorderLayout.SOUTH)

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

  setVisible(true)

  private def accept(): Unit = {
    if (name.isEmpty) {
      new OptionPane(this, I18N.gui.get("menu.tools.themeEditor.invalidName"),
                     I18N.gui.getN("menu.tools.themeEditor.emptyName", name), OptionPane.Options.Ok,
                     OptionPane.Icons.Error)
    } else if (name != baseTheme.name && manager.themeExists(name)) {
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
  }

  def getTheme: Option[ColorTheme] = {
    if (accepted) {
      Some(getStaticTheme)
    } else {
      None
    }
  }

  override def syncTheme(): Unit = {
    getRootPane.setBackground(InterfaceColors.dialogBackground())
    getContentPane.setBackground(InterfaceColors.dialogBackground())
    scrollPane.setBackground(InterfaceColors.dialogBackground())

    nameLabel.setForeground(InterfaceColors.dialogText())
    darkCheckbox.setForeground(InterfaceColors.dialogText())

    nameField.syncTheme()
    darkCheckbox.syncTheme()
    cancelButton.syncTheme()
    okButton.syncTheme()

    panel.getComponents.foreach {
      case ts: ThemeSync =>
        ts.syncTheme()

      case _ =>
    }
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

      add(new JPanel with Transparent {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

        add(new JLabel(arrow))
        add(Box.createHorizontalStrut(6))
        add(label)

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
      new Dimension(500, super.getPreferredSize.height)

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
    setColor(color.value)
    setDiameter(6)
    enableHover()
    enablePressed()
    syncTheme()

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        new JFXColorPicker(null, true, RGBAOnly, Some(RGBA.fromJavaColor(color.value)), str => {
          val rgb = """^\[(\d{1,3}) (\d{1,3}) (\d{1,3})\]$""".r
          val rgba = """^\[(\d{1,3}) (\d{1,3}) (\d{1,3}) (\d{1,3})\]$""".r

          val newColor: Color = {
            str match {
              case rgb(r, g, b) =>
                new Color(r.toInt, g.toInt, b.toInt)

              case rgba(r, g, b, a) =>
                new Color(r.toInt, g.toInt, b.toInt, a.toInt)

              case _ =>
                color.value
            }
          }

          setColor(newColor)
          repaint()
          applyPreview()
        }).setVisible(true)
      }
    })

    private def setColor(color: Color): Unit = {
      this.color.value = color

      setBackgroundColor(color)
      setBackgroundHoverColor(color)
      setBackgroundPressedColor(color)
    }

    override def getPreferredSize: Dimension =
      new Dimension(24, 24)

    override def getMinimumSize: Dimension =
      getPreferredSize

    override def getMaximumSize: Dimension =
      getPreferredSize

    override def syncTheme(): Unit = {
      setBorderColor(InterfaceColors.toolbarControlBorder())
    }
  }
}
