// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ JLabel, JPanel }
import javax.swing.border.{ EmptyBorder, TitledBorder }

import org.nlogo.core.I18N
import org.nlogo.swing.Transparent
import org.nlogo.theme.InterfaceColors

class WorldEditPanel2D(target: WorldViewSettings2D) extends WorldEditPanel(target) {
  private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("edit.viewSettings")

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
        _.foreach(target.tickCounterLabel)))

  private val worldBorder = new TitledBorder(I18N.gui("world"))
  private val viewBorder = new TitledBorder(I18N.gui("view"))
  private val modelBorder = new TitledBorder(I18N.gui("tickCounter"))

  locally {
    setLayout(new BorderLayout)
    setBorder(new EmptyBorder(6, 6, 6, 6))

    val configPanel = new JPanel(new GridBagLayout) with Transparent {
      val c = new GridBagConstraints

      c.gridy = 0
      c.fill = GridBagConstraints.HORIZONTAL
      c.weightx = 1
      c.insets = new Insets(6, 6, 6, 6)

      add(locationLabel, c)

      c.insets = new Insets(6, 0, 6, 6)

      add(originTypes, c)

      c.gridx = 0
      c.gridy = GridBagConstraints.RELATIVE
      c.gridwidth = 2
      c.anchor = GridBagConstraints.EAST
      c.fill = GridBagConstraints.NONE
      c.insets = new Insets(0, 0, 6, 6)

      add(originConfigs, c)

      c.anchor = GridBagConstraints.WEST
      c.fill = GridBagConstraints.HORIZONTAL
      c.insets = new Insets(0, 6, 3, 6)

      add(minPxcor, c)

      c.insets = new Insets(0, 6, 6, 6)

      add(minPxcorLabel, c)

      c.insets = new Insets(0, 6, 3, 6)

      add(maxPxcor, c)

      c.insets = new Insets(0, 6, 6, 6)

      add(maxPxcorLabel, c)

      c.insets = new Insets(0, 6, 3, 6)

      add(minPycor, c)

      c.insets = new Insets(0, 6, 6, 6)

      add(minPycorLabel, c)

      c.insets = new Insets(0, 6, 3, 6)

      add(maxPycor, c)

      c.insets = new Insets(0, 6, 6, 6)

      add(maxPycorLabel, c)
    }

    val previewContainer = new JPanel(new GridBagLayout) with Transparent {
      val c = new GridBagConstraints

      c.gridx = 0
      c.fill = GridBagConstraints.BOTH
      c.weightx = 1
      c.weighty = 1
      c.insets = new Insets(6, 6, 6, 6)

      add(previewPanel, c)

      c.anchor = GridBagConstraints.WEST
      c.fill = GridBagConstraints.NONE
      c.weightx = 0
      c.weighty = 0
      c.insets = new Insets(0, 6, 6, 6)

      add(wrappingX, c)
      add(wrappingY, c)
    }

    val worldPanel = new JPanel(new BorderLayout) with Transparent {
      setBorder(worldBorder)

      add(configPanel, BorderLayout.WEST)
      add(previewContainer, BorderLayout.CENTER)
    }

    val viewPanel = new JPanel(new GridBagLayout) with Transparent {
      setBorder(viewBorder)

      val c = new GridBagConstraints

      c.gridy = 0
      c.anchor = GridBagConstraints.WEST
      c.fill = GridBagConstraints.HORIZONTAL
      c.weightx = 1
      c.insets = new Insets(6, 6, 3, 6)

      add(patchSize, c)

      c.insets = new Insets(6, 0, 3, 6)

      add(fontSize, c)

      c.gridy = 1
      c.insets = new Insets(0, 6, 6, 6)

      add(patchSizeLabel, c)

      c.insets = new Insets(0, 0, 6, 6)

      add(fontSizeLabel, c)

      c.gridy = 2
      c.gridwidth = 2
      c.insets = new Insets(0, 6, 3, 6)

      add(frameRate, c)

      c.gridy = 3
      c.insets = new Insets(0, 6, 6, 6)

      add(frameRateLabel, c)
    }

    val modelPanel = new JPanel(new GridBagLayout) with Transparent {
      setBorder(modelBorder)

      val c = new GridBagConstraints

      c.gridx = 0
      c.fill = GridBagConstraints.HORIZONTAL
      c.weightx = 1
      c.insets = new Insets(6, 6, 6, 6)

      add(showTickCounter, c)

      c.insets = new Insets(0, 6, 6, 6)

      add(tickCounterLabel, c)
    }

    add(worldPanel, BorderLayout.NORTH)
    add(viewPanel, BorderLayout.CENTER)
    add(modelPanel, BorderLayout.SOUTH)

    target.setTypeAndConfig()

    originTypes.setSelectedItem(target.getSelectedType)
    target.getSelectedConfig.foreach(originConfigs.setSelectedItem)

    editors.foreach(_.refresh())
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(minPxcor, maxPxcor, minPycor, maxPycor, wrappingX, wrappingY, patchSize, fontSize, frameRate, showTickCounter,
        tickCounterLabel)

  override def editors: Seq[IntegerEditor] =
    Seq(minPxcor, maxPxcor, minPycor, maxPycor)

  override def syncExtraComponents(): Unit = {
    worldBorder.setTitleColor(InterfaceColors.dialogText())
    viewBorder.setTitleColor(InterfaceColors.dialogText())
    modelBorder.setTitleColor(InterfaceColors.dialogText())

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
