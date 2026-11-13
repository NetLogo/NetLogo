// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{ Box, JLabel }
import javax.swing.border.TitledBorder

import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, MaximumHeight, PreferredSize, Zoomable, ZoomableBorder }
import org.nlogo.theme.InterfaceColors

class WorldEditPanel3D(target: WorldViewSettings3D) extends WorldEditPanel(target) {
  private val modelTitle: StringEditor =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Model title",
        () => target.modelTitle,
        _.foreach(target.setModelTitle)))

  private val locationLabel = new JLabel(loc"Location of origin:") with Zoomable

  private val minPxcor: NegativeIntegerEditor =
    new NegativeIntegerEditor(
      new PropertyAccessor(
        target,
        "min-pxcor",
        () => target.minPxcor,
        _.foreach(target.minPxcor),
        () => previewChanged("minPxcor", minPxcor.get)))

  private val minPxcorLabeled = new LabeledEditor(minPxcor, loc"minimum x coordinate for patches")

  private val maxPxcor: PositiveIntegerEditor =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        "max-pxcor",
        () => target.maxPxcor,
        _.foreach(target.maxPxcor),
        () => previewChanged("maxPxcor", maxPxcor.get)))

  private val maxPxcorLabeled = new LabeledEditor(maxPxcor, loc"maximum x coordinate for patches")

  private val minPycor: NegativeIntegerEditor =
    new NegativeIntegerEditor(
      new PropertyAccessor(
        target,
        "min-ycor",
        () => target.minPycor,
        _.foreach(target.minPycor),
        () => previewChanged("minPycor", minPycor.get)))

  private val minPycorLabeled = new LabeledEditor(minPycor, loc"minimum y coordinate for patches")

  private val maxPycor: PositiveIntegerEditor =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        "max-pycor",
        () => target.maxPycor,
        _.foreach(target.maxPycor),
        () => previewChanged("maxPycor", maxPycor.get)))

  private val maxPycorLabeled = new LabeledEditor(maxPycor, loc"maximum y coordinate for patches")

  private val minPzcor: NegativeIntegerEditor =
    new NegativeIntegerEditor(
      new PropertyAccessor(
        target,
        "min-pzcor",
        () => target.minPzcor,
        _.foreach(target.minPzcor),
        () => previewChanged("minPzcor", minPzcor.get)))

  private val minPzcorLabeled = new LabeledEditor(minPzcor, loc"minimum z coordinate for patches")

  private val maxPzcor: PositiveIntegerEditor =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        "max-pzcor",
        () => target.maxPzcor,
        _.foreach(target.maxPzcor),
        () => previewChanged("maxPzcor", maxPzcor.get)))

  private val maxPzcorLabeled = new LabeledEditor(maxPzcor, loc"maximum z coordinate for patches")

  // the wrapping properties are here to control the variables but they are not added to the GUI (Isaac B 4/2/25)
  private val wrappingX: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"World wraps X (east/west)",
        () => target.wrappingX,
        _.foreach(target.wrappingX),
        () => previewChanged("wrappingX", wrappingX.get)))

  private val wrappingY: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"World wraps Y (north/south)",
        () => target.wrappingY,
        _.foreach(target.wrappingY),
        () => previewChanged("wrappingY", wrappingY.get)))

  private val wrappingZ: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"World wraps Z (top/bottom)",
        () => target.wrappingZ,
        _.foreach(target.wrappingZ),
        () => previewChanged("wrappingZ", wrappingZ.get)))

  private val patchSize =
    new StrictlyPositiveDoubleEditor(
      new PropertyAccessor(
        target,
        loc"Patch size",
        () => target.patchSize,
        _.foreach(target.patchSize)))

  private val patchSizeLabeled = new LabeledEditor(patchSize, loc"measured in pixels")

  private val fontSize =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        loc"Font size",
        () => target.fontSize,
        _.foreach(target.fontSize)))

  private val fontSizeLabeled = new LabeledEditor(fontSize, loc"of labels on agents")

  private val frameRate =
    new StrictlyPositiveDoubleEditor(
      new PropertyAccessor(
        target,
        loc"Frame rate",
        () => target.frameRate,
        _.foreach(target.frameRate)))

  private val frameRateLabeled = new LabeledEditor(frameRate, loc"Frames per second at normal speed")

  private val smooth =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Smooth edges (slower)",
        () => target.smooth,
        _.foreach(target.smooth)))

  private val smoothLabeled = new LabeledEditor(smooth, loc"only affects 3D view")

  private val wireframe =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Show wire frame",
        () => target.wireframe,
        _.foreach(target.wireframe)))

  private val wireframeLabeled = new LabeledEditor(wireframe, loc"only affects 3D view")

  private val bgColor: ColorEditor =
    new ColorEditor(
      new PropertyAccessor(
        target,
        loc"Background color",
        () => target.getBgColor,
        _.foreach(target.setBgColor),
        () => target.updateBackground(bgColor.get.toOption)), target.workspace.getFrame)

  private val wireframeColor: ColorEditor =
    new ColorEditor(
      new PropertyAccessor(
        target,
        loc"Wire frame color",
        () => target.getWireframeColor,
        _.foreach(target.setWireframeColor),
        () => target.updateWireframe(wireframeColor.get.toOption)), target.workspace.getFrame)

  private val showTickCounter =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Show tick counter",
        () => target.showTickCounter,
        _.foreach(target.showTickCounter)))

  private val tickCounterLabel =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Tick counter label",
        () => target.tickCounterLabel,
        target.tickCounterLabel))

  private val modelBorder = new TitledBorder(loc"Model")
  private val worldBorder = new TitledBorder(loc"World")
  private val viewBorder = new TitledBorder(loc"View")
  private val tickBorder = new TitledBorder(loc"Tick counter")

  locally {
    val configPanel = new BoxColumn(Seq(
      new BoxRow(Seq(locationLabel, originTypes), 6) with MaximumHeight,
      new BoxRow(originConfigs, BoxAlign.End),
      minPxcorLabeled,
      maxPxcorLabeled,
      minPycorLabeled,
      maxPycorLabeled,
      minPzcorLabeled,
      maxPzcorLabeled
    ), 6) {
      setBorder(new ZoomableBorder(6, 6, 6, 6))
    }

    val previewContainer = new BoxColumn(Seq(previewPanel), 6) {
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
        frameRateLabeled,
        new BoxRow(Seq(
          new BoxColumn(Seq(
            smoothLabeled,
            wireframeLabeled
          ), 6) with PreferredSize,
          Box.createHorizontalGlue,
          new BoxColumn(Seq(
            bgColor,
            wireframeColor
          ), 6, BoxAlign.Start)
        ), 6)
      ), 6) {
        setBorder(new ZoomableBorder(6, 6, 6, 6))
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
    Seq(modelTitle, minPxcor, maxPxcor, minPycor, maxPycor, minPzcor, maxPzcor, wrappingX, wrappingY, wrappingZ,
        patchSize, fontSize, frameRate, smooth, wireframe, bgColor, wireframeColor, showTickCounter, tickCounterLabel)

  override def editors: Seq[IntegerEditor] =
    Seq(minPxcor, maxPxcor, minPycor, maxPycor, minPzcor, maxPzcor)

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
    minPzcorLabeled.syncTheme()
    maxPzcorLabeled.syncTheme()
    patchSizeLabeled.syncTheme()
    fontSizeLabeled.syncTheme()
    frameRateLabeled.syncTheme()
    smoothLabeled.syncTheme()
    wireframeLabeled.syncTheme()
  }

  override def zoomComponent(): Unit = {
    modelBorder.setTitleFont(modelBorder.getTitleFont.deriveFont(zoom(12f)))
    worldBorder.setTitleFont(worldBorder.getTitleFont.deriveFont(zoom(12f)))
    viewBorder.setTitleFont(viewBorder.getTitleFont.deriveFont(zoom(12f)))
    tickBorder.setTitleFont(tickBorder.getTitleFont.deriveFont(zoom(12f)))
  }
}
