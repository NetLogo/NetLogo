// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, GridBagConstraints, GridBagLayout }
import javax.swing.{ JLabel, JPanel }
import javax.swing.border.TitledBorder

import org.nlogo.api.{ CompilerServices, Editable }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ ComboBox, Transparent }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.{ OriginConfiguration, OriginType, WorldViewSettings }

class WorldEditPanel(widget: Editable, compiler: CompilerServices, colorizer: Colorizer)
  extends EditPanel(widget, compiler, colorizer) {

  private implicit val i18nPrefix = I18N.Prefix("edit.viewSettings")
  private val previewPanel = new WorldPreview(200, 200)

  private var editors: List[IntegerEditor] = Nil

  private val settings = widget.asInstanceOf[WorldViewSettings]

  private val originTypes = new ComboBox[OriginType](settings.originTypes) {
    addItemListener(_ => selectType)
  }

  private val originConfigs = new ComboBox[OriginConfiguration] {
    addItemListener(_ => selectConfig)
  }

  override def init(): PropertyEditor[_] = {
    setLayout(new BorderLayout)
    val panelGridbag = new GridBagLayout
    val worldPanel = new JPanel(new BorderLayout) with Transparent {
      setBorder(new TitledBorder(I18N.gui("world")) {
        setTitleColor(InterfaceColors.dialogText)
      })
      add(makeButtonPanel(settings), BorderLayout.WEST)
      add(new JPanel(new BorderLayout) with Transparent {
        add(previewPanel, BorderLayout.CENTER)
        val worldStaticPropertiesPanel = new JPanel(panelGridbag) with Transparent
        addProperties(worldStaticPropertiesPanel,
                      settings.wrappingProperties,
                      panelGridbag)
        add(worldStaticPropertiesPanel, BorderLayout.SOUTH)
      }, BorderLayout.CENTER)
    }

    val viewPanel = new JPanel(panelGridbag) with Transparent {
      setBorder(new TitledBorder(I18N.gui("view")) {
        setTitleColor(InterfaceColors.dialogText)
      })
    }

    addProperties(viewPanel, settings.viewProperties, panelGridbag)

    val modelPanel = new JPanel(panelGridbag) with Transparent {
      setBorder(new TitledBorder(I18N.gui("tickCounter")) {
        setTitleColor(InterfaceColors.dialogText)
      })
    }

    addProperties(modelPanel, settings.modelProperties, panelGridbag)

    add(worldPanel, BorderLayout.NORTH)
    add(viewPanel, BorderLayout.CENTER)
    add(modelPanel, BorderLayout.SOUTH)

    settings.setTypeAndConfig()

    originTypes.setSelectedItem(settings.getSelectedType)
    settings.getSelectedConfig.foreach(originConfigs.setSelectedItem)

    editors.foreach(_.refresh)

    if (editors(0).isEnabled) editors(0) else editors(1)
  }

  private def makeButtonPanel(settings: WorldViewSettings) = {
    val buttonsLayout = new GridBagLayout
    val buttons = new JPanel(buttonsLayout) with Transparent
    val c = new GridBagConstraints

    buttons.add(new JLabel(I18N.gui("origin.location") + " ") {
      setForeground(InterfaceColors.dialogText)
    }, c)

    c.gridwidth = GridBagConstraints.REMAINDER
    c.anchor = GridBagConstraints.EAST

    buttons.add(originTypes, c)
    buttons.add(originConfigs, c)

    try
      addProperties(buttons, settings.dimensionProperties, buttonsLayout)
    catch {
      case t: Throwable => t.printStackTrace
    }

    editors = propertyEditors.map(_.asInstanceOf[IntegerEditor]).toList

    buttons
  }

  override def previewChanged(field: String, value: Option[Any]): Unit = {
    value match {
      case Some(i: Int) =>
        previewPanel.updateInt(field, i)

        if (originTypes.getSelectedItem.exists(_ == OriginType.Center) && editors.nonEmpty) {
          if (field == "maxPxcor") {
            editors(0).set(0 - i)
          } else if (field == "maxPycor") {
            editors(2).set(0 - i)
          } else if (field == "maxPzcor") {
            editors(4).set(0 - i)
          }
        }

      case Some(b: Boolean) =>
        previewPanel.updateBoolean(field, b)

      case None =>
        if (field == "minPxcor" || field == "maxPxcor" || field == "minPycor" || field == "maxPycor" ||
            field == "minPzcor" || field == "maxPzcor") {
          previewPanel.setError(field)
        }

      case _ =>
    }
  }

  override def apply(): Unit = {
    super.apply()
    settings.apply()
  }

  override def revert(): Unit = {
    super.revert()
    settings.revert()
  }

  private def selectType(): Unit = {
    originTypes.getSelectedItem.foreach { t =>
      settings.setOriginType(t)

      t match {
        case OriginType.Corner =>
          originConfigs.setItems(settings.cornerConfigs)
          originConfigs.setVisible(true)

        case OriginType.Edge =>
          originConfigs.setItems(settings.edgeConfigs)
          originConfigs.setVisible(true)

        case _ =>
          originConfigs.clearSelection()
          originConfigs.setVisible(false)
      }
    }
  }

  private def selectConfig(): Unit = {
    settings.setOriginConfig(originConfigs.getSelectedItem)
    settings.configureEditors(editors)
  }
}
