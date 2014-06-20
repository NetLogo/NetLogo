// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.editor.Colorizer
import javax.swing.border.TitledBorder
import org.nlogo.window.{WorldViewSettings, OriginConfiguration}
import java.awt.{GridBagLayout, BorderLayout, GridBagConstraints}
import javax.swing.{JLabel, JComboBox, JPanel}
import java.awt.event.{ItemListener, ItemEvent}
import org.nlogo.api.{I18N, Editable, ParserServices, TokenType}
import collection.JavaConverters._

class WorldEditPanel(widget: Editable, parser: ParserServices, colorizer: Colorizer[TokenType])
  extends EditPanel(widget, parser, colorizer){

  private implicit val i18nPrefix = I18N.Prefix("edit.viewSettings")
  private val previewPanel = new WorldPreview(200, 200)

  private var editors:scala.List[IntegerEditor] = Nil
  private var positionChoices:JComboBox[OriginConfiguration] = null
  private var edgeChoices:JComboBox[OriginConfiguration] = null
  private var cornerChoices:JComboBox[OriginConfiguration] = null

  override def init(): PropertyEditor[_] = {
    setLayout(new BorderLayout())
    val settings = widget.asInstanceOf[WorldViewSettings]
    val panelGridbag = new GridBagLayout()
    val worldPanel = new JPanel(){
      setBorder(new TitledBorder(I18N.gui("world")))
      setLayout(new BorderLayout())
      add(makeButtonPanel(settings), BorderLayout.WEST)
      add(new JPanel() {
        setLayout(new BorderLayout())
        add(previewPanel, BorderLayout.CENTER)
        val worldStaticPropertiesPanel = new JPanel() {
          setLayout(panelGridbag)
        }
        addProperties(worldStaticPropertiesPanel,
                      settings.getWrappingProperties.asScala,
                      panelGridbag)
        add(worldStaticPropertiesPanel, BorderLayout.SOUTH)
      }, BorderLayout.CENTER)
    }

    val viewPanel = new JPanel() {
      setBorder(new TitledBorder(I18N.gui("view")))
      setLayout(panelGridbag)
    }

    addProperties(viewPanel, settings.getViewProperties.asScala, panelGridbag)

    val modelPanel = new JPanel(){
      setBorder(new TitledBorder(I18N.gui("tickCounter")))
      setLayout(panelGridbag)
    }

    addProperties(modelPanel, settings.getModelProperties.asScala, panelGridbag)

    add(worldPanel, BorderLayout.NORTH)
    add(viewPanel, BorderLayout.CENTER)
    add(modelPanel, BorderLayout.SOUTH)

    positionChoices.setSelectedIndex(settings.getSelectedLocation())
    selectPosition(
      positionChoices.getSelectedItem().asInstanceOf[OriginConfiguration], settings.getSelectedConfiguration())

    if(! editors(0).isEnabled) editors(1) else editors(0)
  }

  private def makeButtonPanel(settings: WorldViewSettings) = {
    val buttons = new JPanel()
    val buttonsLayout = new GridBagLayout()
    val c = new GridBagConstraints()
    buttons.setLayout(buttonsLayout)

    buttons.add(new JLabel(I18N.gui("origin.location") + " "))
    positionChoices = new JComboBox()
    for(config <- settings.getOriginConfigurations.asScala)
       positionChoices.addItem(config)
    positionChoices.addItemListener(new LocationItemListener())

    c.gridwidth = GridBagConstraints.REMAINDER
    buttonsLayout.setConstraints(positionChoices, c)
    buttons.add(positionChoices)

    edgeChoices = new JComboBox[OriginConfiguration]()
    for(config <- settings.getEdgeChoices.asScala)
      edgeChoices.addItem(config)
    edgeChoices.addItemListener(new ConfigurationListener())
    buttonsLayout.setConstraints(edgeChoices, c)
    buttons.add(edgeChoices)
    edgeChoices.setVisible(false)

    cornerChoices = new JComboBox[OriginConfiguration]()
    for(config <- settings.getCornerChoices.asScala)
      cornerChoices.addItem(config)
    cornerChoices.addItemListener(new ConfigurationListener())
    buttonsLayout.setConstraints(cornerChoices, c)
    buttons.add(cornerChoices)
    cornerChoices.setVisible(false)

    try
      addProperties(buttons, settings.getDimensionProperties.asScala, buttonsLayout)
    catch {
      case t: Throwable => t.printStackTrace
    }

    editors =
      (for(i <- settings.firstEditor to settings.lastEditor) yield
        propertyEditors(i).asInstanceOf[IntegerEditor]).toList
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
      selectConfiguration(cornerChoices.getSelectedItem().asInstanceOf[OriginConfiguration])
    }
    else if (selection.toString == I18N.gui("origin.location.edge")){
      enableChoices(false, true)
      edgeChoices.setSelectedIndex(index)
      selectConfiguration(edgeChoices.getSelectedItem().asInstanceOf[OriginConfiguration])
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
