// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, GridBagConstraints, GridBagLayout }
import java.awt.event.{ ItemEvent, ItemListener }
import javax.swing.{ JLabel, JPanel }
import javax.swing.border.TitledBorder

import org.nlogo.api.{ CompilerServices, Editable }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ ComboBox, Transparent }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.{ OriginConfiguration, WorldViewSettings }

class WorldEditPanel(widget: Editable, compiler: CompilerServices, colorizer: Colorizer)
  extends EditPanel(widget, compiler, colorizer) {

  private implicit val i18nPrefix = I18N.Prefix("edit.viewSettings")
  private val previewPanel = new WorldPreview(200, 200)

  private var editors: List[IntegerEditor] = Nil
  private var positionChoices: ComboBox[OriginConfiguration] = null
  private var edgeChoices: ComboBox[OriginConfiguration] = null
  private var cornerChoices: ComboBox[OriginConfiguration] = null

  override def init(): PropertyEditor[_] = {
    setLayout(new BorderLayout)
    val settings = widget.asInstanceOf[WorldViewSettings]
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

    positionChoices.setSelectedIndex(settings.getSelectedLocation)
    selectPosition(positionChoices.getSelectedItem.get, settings.getSelectedConfiguration)

    if(! editors(0).isEnabled) editors(1) else editors(0)
  }

  private def makeButtonPanel(settings: WorldViewSettings) = {
    val buttonsLayout = new GridBagLayout
    val buttons = new JPanel(buttonsLayout) with Transparent
    val c = new GridBagConstraints

    buttons.add(new JLabel(I18N.gui("origin.location") + " ") {
      setForeground(InterfaceColors.dialogText)
    }, c)

    positionChoices = new ComboBox(settings.originConfigurations) {
      addItemListener(new LocationItemListener)
    }

    c.gridwidth = GridBagConstraints.REMAINDER
    c.anchor = GridBagConstraints.EAST

    buttons.add(positionChoices, c)

    edgeChoices = new ComboBox(settings.edgeChoices) {
      addItemListener(new ConfigurationListener)
      setVisible(false)
    }

    buttons.add(edgeChoices, c)

    cornerChoices = new ComboBox(settings.cornerChoices) {
      addItemListener(new ConfigurationListener)
      setVisible(false)
    }

    buttons.add(cornerChoices, c)

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

        positionChoices.getSelectedItem match {
          case Some(item) if item.toString == I18N.gui("origin.location.center") && editors.nonEmpty =>
            if (field == "maxPxcor") {
              editors(0).set(0 - i)
            } else if (field == "maxPycor") {
              editors(2).set(0 - i)
            } else if (field == "maxPzcor") {
              editors(4).set(0 - i)
            }

          case _ =>
        }

      case Some(b: Boolean) =>
        previewPanel.updateBoolean(field, b)

      case None =>
        previewPanel.paintError()

      case _ =>
    }
  }

  private class LocationItemListener extends ItemListener{
    def itemStateChanged(e:ItemEvent){
      if(e.getStateChange() == java.awt.event.ItemEvent.SELECTED){
        selectPosition(e.getItem().asInstanceOf[OriginConfiguration], 0)
      }
    }
  }

  def enableChoices(corner:Boolean, edge:Boolean){
    edgeChoices setVisible edge
    cornerChoices setVisible corner
  }

  private def selectPosition(selection:OriginConfiguration, index:Int){
    selectConfiguration(selection)
    if (selection.toString == I18N.gui("origin.location.corner")){
      enableChoices(true, false)
      cornerChoices.setSelectedIndex(index)
      selectConfiguration(cornerChoices.getSelectedItem.get)
    }
    else if (selection.toString == I18N.gui("origin.location.edge")){
      enableChoices(false, true)
      edgeChoices.setSelectedIndex(index)
      selectConfiguration(edgeChoices.getSelectedItem.get)
    }
    else{
      enableChoices(false, false)
    }
  }

  private def selectConfiguration(choice:OriginConfiguration){
    for((editor,i) <- editors.zipWithIndex){
      if(choice.toString != I18N.gui("origin.location.custom")){
        editor.refresh()
      }
      editor.setEnabled(choice.getEditorEnabled(i))
    }
    for((editor,i) <- editors.zipWithIndex){
      if(choice.setValue(i)){
        // this is kind of ugly, but we want to add our magnitude
        // to that of the corresponding field, odd fields are partnered
        // with the field before and even fields are with the field after.
        // ev 5/23/06
        val partner = i + (if ((i % 2) == 0) 1 else -1)
        val otherguy = editors(partner)
        otherguy.set(- editor.get.get.intValue + otherguy.get.get.intValue)
        editor.set(0)
      }
    }
  }

  private class ConfigurationListener extends ItemListener {
    def itemStateChanged(e:ItemEvent){
      if(e.getStateChange() == java.awt.event.ItemEvent.SELECTED){
        selectConfiguration(e.getItem().asInstanceOf[OriginConfiguration])
      }
    }
  }
}
