// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.swing.ComboBox

abstract class WorldEditPanel(target: WorldViewSettings) extends EditPanel(target) {
  protected val previewPanel = new WorldPreview(200, 200)

  protected val originTypes = new ComboBox[OriginType](target.originTypes) {
    addItemListener(_ => selectType())
  }

  protected val originConfigs = new ComboBox[OriginConfiguration] {
    addItemListener(_ => selectConfig())
  }

  protected def editors: Seq[IntegerEditor]

  // this is weird old code that probably could be improved, it's just here for compatibility with other old code (Isaac B 4/2/25)
  protected def previewChanged(field: String, value: Option[Any]): Unit = {
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
    target.apply()
  }

  override def revert(): Unit = {
    super.revert()
    target.revert()
  }

  private def selectType(): Unit = {
    originTypes.getSelectedItem.foreach { t =>
      target.setOriginType(t)

      t match {
        case OriginType.Corner =>
          originConfigs.setItems(target.cornerConfigs)
          originConfigs.setVisible(true)

        case OriginType.Edge =>
          originConfigs.setItems(target.edgeConfigs)
          originConfigs.setVisible(true)

        case _ =>
          originConfigs.clearSelection()
          originConfigs.setVisible(false)
      }
    }
  }

  private def selectConfig(): Unit = {
    target.setOriginConfig(originConfigs.getSelectedItem)
    target.configureEditors(editors)
  }
}
