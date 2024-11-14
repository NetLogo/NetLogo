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

import scala.collection.JavaConverters._

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
        setTitleColor(InterfaceColors.DIALOG_TEXT)
      })
      add(makeButtonPanel(settings), BorderLayout.WEST)
      add(new JPanel(new BorderLayout) with Transparent {
        add(previewPanel, BorderLayout.CENTER)
        val worldStaticPropertiesPanel = new JPanel(panelGridbag) with Transparent
        addProperties(worldStaticPropertiesPanel,
                      settings.wrappingProperties.asScala,
                      panelGridbag)
        add(worldStaticPropertiesPanel, BorderLayout.SOUTH)
      }, BorderLayout.CENTER)
    }

    val viewPanel = new JPanel(panelGridbag) with Transparent {
      setBorder(new TitledBorder(I18N.gui("view")) {
        setTitleColor(InterfaceColors.DIALOG_TEXT)
      })
    }

    addProperties(viewPanel, settings.viewProperties.asScala, panelGridbag)

    val modelPanel = new JPanel(panelGridbag) with Transparent {
      setBorder(new TitledBorder(I18N.gui("tickCounter")) {
        setTitleColor(InterfaceColors.DIALOG_TEXT)
      })
    }

    addProperties(modelPanel, settings.modelProperties.asScala, panelGridbag)

    add(worldPanel, BorderLayout.NORTH)
    add(viewPanel, BorderLayout.CENTER)
    add(modelPanel, BorderLayout.SOUTH)

    positionChoices.setSelectedIndex(settings.getSelectedLocation)
    selectPosition(
      positionChoices.getSelectedItem.asInstanceOf[OriginConfiguration], settings.getSelectedConfiguration)

    if(! editors(0).isEnabled) editors(1) else editors(0)
  }

  private def makeButtonPanel(settings: WorldViewSettings) = {
    val buttonsLayout = new GridBagLayout
    val buttons = new JPanel(buttonsLayout) with Transparent
    val c = new GridBagConstraints

    buttons.add(new JLabel(I18N.gui("origin.location") + " ") {
      setForeground(InterfaceColors.DIALOG_TEXT)
    }, c)

    positionChoices = new ComboBox(settings.originConfigurations.asScala.toList) {
      addItemListener(new LocationItemListener)
    }

    c.gridwidth = GridBagConstraints.REMAINDER

    buttons.add(positionChoices, c)

    edgeChoices = new ComboBox(settings.edgeChoices.asScala.toList) {
      addItemListener(new ConfigurationListener)
      setVisible(false)
    }

    buttons.add(edgeChoices, c)

    cornerChoices = new ComboBox(settings.cornerChoices.asScala.toList) {
      addItemListener(new ConfigurationListener)
      setVisible(false)
    }

    buttons.add(cornerChoices, c)

    try
      addProperties(buttons, settings.dimensionProperties.asScala, buttonsLayout)
    catch {
      case t: Throwable => t.printStackTrace
    }

    editors = propertyEditors.take(4).map(_.asInstanceOf[IntegerEditor]).toList

    buttons
  }

  override def previewChanged(field: String, value: Option[Any]) {
    previewPanel.update(field, value)
    if(!value.isDefined) return
    def v = value.get.asInstanceOf[Int]
    if(positionChoices.getSelectedObjects()(0).toString == I18N.gui("origin.location.center") && editors != Nil) {
      if(field == "maxPxcor") editors(0).set(0 - v)
      else if(field == "maxPycor") editors(2).set(0 - v)
      else if(field == "maxPzcor") editors(4).set(0 - v)
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
      selectConfiguration(cornerChoices.getSelectedItem.asInstanceOf[OriginConfiguration])
    }
    else if (selection.toString == I18N.gui("origin.location.edge")){
      enableChoices(false, true)
      edgeChoices.setSelectedIndex(index)
      selectConfiguration(edgeChoices.getSelectedItem.asInstanceOf[OriginConfiguration])
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
