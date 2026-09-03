// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLabel
import javax.swing.border.TitledBorder

import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, MaximumHeight, Utils, Zoomable, ZoomableBorder }
import org.nlogo.theme.InterfaceColors

class WorldEditPanel2D(target: WorldViewSettings2D, enableDualView: Boolean) extends WorldEditPanel(target) {
  private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("edit.viewSettings")

  private val modelTitle: StringEditor =
    new StringEditor(
      new PropertyAccessor(
        target,
        s"${I18N.gui("modelTitle")}:",
        () => target.modelTitle,
        _.foreach(target.setModelTitle)))

  private val locationLabel = new JLabel(I18N.gui("origin.location")) with Zoomable

  private val minPxcor: NegativeIntegerEditor =
    new NegativeIntegerEditor(
      new PropertyAccessor(
        target,
        "min-pxcor",
        () => target.minPxcor,
        _.foreach(target.minPxcor),
        () => previewChanged("minPxcor", minPxcor.get)))

  private val minPxcorLabeled = new LabeledEditor(minPxcor, I18N.gui("2D.minPxcor"))

  private val maxPxcor: PositiveIntegerEditor =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        "max-pxcor",
        () => target.maxPxcor,
        _.foreach(target.maxPxcor),
        () => previewChanged("maxPxcor", maxPxcor.get)))

  private val maxPxcorLabeled = new LabeledEditor(maxPxcor, I18N.gui("2D.maxPxcor"))

  private val minPycor: NegativeIntegerEditor =
    new NegativeIntegerEditor(
      new PropertyAccessor(
        target,
        "min-pycor",
        () => target.minPycor,
        _.foreach(target.minPycor),
        () => previewChanged("minPycor", minPycor.get)))

  private val minPycorLabeled = new LabeledEditor(minPycor, I18N.gui("2D.minPycor"))

  private val maxPycor: PositiveIntegerEditor =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        "max-pycor",
        () => target.maxPycor,
        _.foreach(target.maxPycor),
        () => previewChanged("maxPycor", maxPycor.get)))

  private val maxPycorLabeled = new LabeledEditor(maxPycor, I18N.gui("2D.maxPycor"))

  private val wrappingX: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui("2D.wrapX"),
        () => target.wrappingX,
        _.foreach(target.wrappingX),
        () => previewChanged("wrappingX", wrappingX.get)))

  private val wrappingY: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui("2D.wrapY"),
        () => target.wrappingY,
        _.foreach(target.wrappingY),
        () => previewChanged("wrappingY", wrappingY.get)))

  private val patchSize =
    new StrictlyPositiveDoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui("2D.patchSize"),
        () => target.patchSize,
        _.foreach(target.patchSize)))

  private val patchSizeLabeled = new LabeledEditor(patchSize, I18N.gui("2D.patchSize.info"))

  private val fontSize =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui("2D.fontSize"),
        () => target.fontSize,
        _.foreach(target.fontSize)))

  private val fontSizeLabeled = new LabeledEditor(fontSize, I18N.gui("2D.fontSize.info"))

  private val frameRate =
    new StrictlyPositiveDoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui("2D.frameRate"),
        () => target.frameRate,
        _.foreach(target.frameRate)))

  private val frameRateLabeled = new LabeledEditor(frameRate, I18N.gui("2D.frameRate.info"))

  private val dualView =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui("3D.dualView"),
        () => target.dualView,
        _.foreach(target.dualView)))

  private val showTickCounter =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui("showTickCounter"),
        () => target.showTickCounter,
        _.foreach(target.showTickCounter)))

  private val tickCounterLabel =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui("tickCounterLabel"),
        () => target.tickCounterLabel,
        target.tickCounterLabel))

  private val modelBorder = new TitledBorder(I18N.gui("model"))
  private val worldBorder = new TitledBorder(I18N.gui("world"))
  private val viewBorder = new TitledBorder(I18N.gui("view"))
  private val tickBorder = new TitledBorder(I18N.gui("tickCounter"))

  locally {
    val configPanel = new BoxColumn(Seq(
      new BoxRow(Seq(locationLabel, originTypes), 6) with MaximumHeight,
      new BoxRow(originConfigs, BoxAlign.End),
      minPxcorLabeled,
      maxPxcorLabeled,
      minPycorLabeled,
      maxPycorLabeled
    ), 6) {
      setBorder(new ZoomableBorder(6, 6, 6, 6))
    }

    val previewContainer = new BoxColumn(Seq(
      previewPanel,
      new BoxRow(wrappingX, BoxAlign.Start),
      new BoxRow(wrappingY, BoxAlign.Start)
    ), 6) {
      setBorder(new ZoomableBorder(6, 6, 6, 6))
    }

    val modelPanel = new BoxRow(Seq(
      new BoxRow(Seq(modelTitle)) {
        setBorder(new ZoomableBorder(6, 6, 6, 6))
      }
    )) {
      setBorder(modelBorder)
    }

    val worldPanel = new BoxRow(Seq(
      new BoxColumn(configPanel, BoxAlign.Center),
      previewContainer
    )) {
      setBorder(worldBorder)
    }

    val viewPanel = new BoxRow(Seq(
      new BoxColumn(Seq(
        new BoxRow(Seq(patchSizeLabeled, fontSizeLabeled), 6),
        frameRateLabeled
      ), 6) {
        setBorder(new ZoomableBorder(6, 6, 6, 6))

        if (enableDualView)
          add(new BoxRow(dualView, BoxAlign.Start))
      }
    )) {
      setBorder(viewBorder)
    }

    val tickPanel = new BoxRow(Seq(
      new BoxColumn(Seq(
        new BoxRow(showTickCounter, BoxAlign.Start),
        tickCounterLabel
      ), 6) {
        setBorder(new ZoomableBorder(6, 6, 6, 6))
      }
    )) {
      setBorder(tickBorder)
    }

    add(modelPanel)
    add(worldPanel)
    add(viewPanel)
    add(tickPanel)

    target.setTypeAndConfig()

    originTypes.setSelectedItem(target.getSelectedType)
    target.getSelectedConfig.foreach(originConfigs.setSelectedItem)

    editors.foreach(_.refresh())
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(modelTitle, minPxcor, maxPxcor, minPycor, maxPycor, wrappingX, wrappingY, patchSize, fontSize, frameRate,
        dualView, showTickCounter, tickCounterLabel)

  override def editors: Seq[IntegerEditor] =
    Seq(minPxcor, maxPxcor, minPycor, maxPycor)

  override def syncExtraComponents(): Unit = {
    modelBorder.setTitleColor(InterfaceColors.dialogText())
    worldBorder.setTitleColor(InterfaceColors.dialogText())
    viewBorder.setTitleColor(InterfaceColors.dialogText())
    tickBorder.setTitleColor(InterfaceColors.dialogText())

    locationLabel.setForeground(InterfaceColors.dialogText())

    minPxcorLabeled.syncTheme()
    maxPxcorLabeled.syncTheme()
    minPycorLabeled.syncTheme()
    maxPycorLabeled.syncTheme()
    patchSizeLabeled.syncTheme()
    fontSizeLabeled.syncTheme()
    frameRateLabeled.syncTheme()
  }

  override def zoomComponent(): Unit = {
    modelBorder.setTitleFont(modelBorder.getTitleFont.deriveFont(Utils.zoom(12f)))
    worldBorder.setTitleFont(worldBorder.getTitleFont.deriveFont(Utils.zoom(12f)))
    viewBorder.setTitleFont(viewBorder.getTitleFont.deriveFont(Utils.zoom(12f)))
    tickBorder.setTitleFont(tickBorder.getTitleFont.deriveFont(Utils.zoom(12f)))
  }
}
