// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{ Box, BoxLayout, JLabel }
import javax.swing.border.TitledBorder

import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxColumn, BoxRow, Centered, HorizontalStrut, VerticalStrut, ZoomableBorder }
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

  private val locationLabel = new JLabel(I18N.gui("origin.location"))

  private val minPxcor: NegativeIntegerEditor =
    new NegativeIntegerEditor(
      new PropertyAccessor(
        target,
        "min-pxcor",
        () => target.minPxcor,
        _.foreach(target.minPxcor),
        () => previewChanged("minPxcor", minPxcor.get)))

  private val minPxcorLabel = new JLabel(I18N.gui("2D.minPxcor")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val maxPxcor: PositiveIntegerEditor =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        "max-pxcor",
        () => target.maxPxcor,
        _.foreach(target.maxPxcor),
        () => previewChanged("maxPxcor", maxPxcor.get)))

  private val maxPxcorLabel = new JLabel(I18N.gui("2D.maxPxcor")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val minPycor: NegativeIntegerEditor =
    new NegativeIntegerEditor(
      new PropertyAccessor(
        target,
        "min-pycor",
        () => target.minPycor,
        _.foreach(target.minPycor),
        () => previewChanged("minPycor", minPycor.get)))

  private val minPycorLabel = new JLabel(I18N.gui("2D.minPycor")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val maxPycor: PositiveIntegerEditor =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        "max-pycor",
        () => target.maxPycor,
        _.foreach(target.maxPycor),
        () => previewChanged("maxPycor", maxPycor.get)))

  private val maxPycorLabel = new JLabel(I18N.gui("2D.maxPycor")) {
    setFont(getFont.deriveFont(9.0f))
  }

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

  private val patchSizeLabel = new JLabel(I18N.gui("2D.patchSize.info")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val fontSize =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui("2D.fontSize"),
        () => target.fontSize,
        _.foreach(target.fontSize)))

  private val fontSizeLabel = new JLabel(I18N.gui("2D.fontSize.info")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val frameRate =
    new StrictlyPositiveDoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui("2D.frameRate"),
        () => target.frameRate,
        _.foreach(target.frameRate)))

  private val frameRateLabel = new JLabel(I18N.gui("2D.frameRate.info")) {
    setFont(getFont.deriveFont(9.0f))
  }

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
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    setBorder(new ZoomableBorder(6, 6, 6, 6))

    val configPanel = new BoxColumn(Seq(
      new BoxRow(Seq(
        locationLabel,
        new HorizontalStrut(6),
        originTypes
      )),
      new VerticalStrut(6),
      new BoxRow(Seq(
        Box.createHorizontalGlue,
        originConfigs
      )) {
        setBorder(new ZoomableBorder(0, 0, 6, 0))
      },
      minPxcor,
      new VerticalStrut(3),
      new BoxRow(Seq(minPxcorLabel, Box.createHorizontalGlue)),
      new VerticalStrut(6),
      maxPxcor,
      new VerticalStrut(3),
      new BoxRow(Seq(maxPxcorLabel, Box.createHorizontalGlue)),
      new VerticalStrut(6),
      minPycor,
      new VerticalStrut(3),
      new BoxRow(Seq(minPycorLabel, Box.createHorizontalGlue)),
      new VerticalStrut(6),
      maxPycor,
      new VerticalStrut(3),
      new BoxRow(Seq(maxPycorLabel, Box.createHorizontalGlue))
    )) {
      setBorder(new ZoomableBorder(6, 6, 6, 6))
    }

    val previewContainer = new BoxColumn(Seq(
      previewPanel,
      new VerticalStrut(6),
      wrappingX,
      new VerticalStrut(6),
      wrappingY
    )) {
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
      new Centered(configPanel),
      previewContainer
    )) {
      setBorder(worldBorder)
    }

    val viewPanel = new BoxRow(Seq(
      new BoxColumn(Seq(
        new BoxRow(Seq(
          new BoxColumn(Seq(
            patchSize,
            new VerticalStrut(3),
            patchSizeLabel
          )),
          new HorizontalStrut(6),
          new BoxColumn(Seq(
            fontSize,
            new VerticalStrut(3),
            fontSizeLabel
          ))
        )),
        new VerticalStrut(6),
        frameRate,
        new VerticalStrut(3),
        frameRateLabel
      )) {
        setBorder(new ZoomableBorder(6, 6, 6, 6))
      }
    )) {
      setBorder(viewBorder)

      if (enableDualView) {
        add(new VerticalStrut(6))
        add(dualView)
      }
    }

    val tickPanel = new BoxRow(Seq(
      new BoxColumn(Seq(
        showTickCounter,
        new VerticalStrut(6),
        tickCounterLabel
      )) {
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

    minPxcorLabel.setForeground(InterfaceColors.dialogText())
    maxPxcorLabel.setForeground(InterfaceColors.dialogText())
    minPycorLabel.setForeground(InterfaceColors.dialogText())
    maxPycorLabel.setForeground(InterfaceColors.dialogText())

    patchSizeLabel.setForeground(InterfaceColors.dialogText())
    fontSizeLabel.setForeground(InterfaceColors.dialogText())
    frameRateLabel.setForeground(InterfaceColors.dialogText())
  }
}
