// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ JLabel, JPanel }
import javax.swing.border.{ EmptyBorder, TitledBorder }

import org.nlogo.core.I18N
import org.nlogo.swing.Transparent
import org.nlogo.theme.InterfaceColors

class WorldEditPanel3D(target: WorldViewSettings3D) extends WorldEditPanel(target) {
  private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("edit.viewSettings")

  private val locationLabel = new JLabel(I18N.gui("origin.location"))

  private val minPxcor: NegativeIntegerEditor =
    new NegativeIntegerEditor(
      new PropertyAccessor(
        target,
        "min-pxcor",
        () => target.minPxcor,
        target.minPxcor(_),
        () => previewChanged("minPxcor", minPxcor.get)))

  private val minPxcorLabel = new JLabel(I18N.gui("3D.minPxcor")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val maxPxcor: PositiveIntegerEditor =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        "max-pxcor",
        () => target.maxPxcor,
        target.maxPxcor(_),
        () => previewChanged("maxPxcor", maxPxcor.get)))

  private val maxPxcorLabel = new JLabel(I18N.gui("3D.maxPxcor")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val minPycor: NegativeIntegerEditor =
    new NegativeIntegerEditor(
      new PropertyAccessor(
        target,
        "min-ycor",
        () => target.minPycor,
        target.minPycor(_),
        () => previewChanged("minPycor", minPycor.get)))

  private val minPycorLabel = new JLabel(I18N.gui("3D.minPycor")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val maxPycor: PositiveIntegerEditor =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        "max-pycor",
        () => target.maxPycor,
        target.maxPycor(_),
        () => previewChanged("maxPycor", maxPycor.get)))

  private val maxPycorLabel = new JLabel(I18N.gui("3D.maxPycor")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val minPzcor: NegativeIntegerEditor =
    new NegativeIntegerEditor(
      new PropertyAccessor(
        target,
        "min-pzcor",
        () => target.minPzcor,
        target.minPzcor(_),
        () => previewChanged("minPzcor", minPzcor.get)))

  private val minPzcorLabel = new JLabel(I18N.gui("3D.minPzcor")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val maxPzcor: PositiveIntegerEditor =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        "max-pzcor",
        () => target.maxPzcor,
        target.maxPzcor(_),
        () => previewChanged("maxPzcor", maxPzcor.get)))

  private val maxPzcorLabel = new JLabel(I18N.gui("3D.maxPzcor")) {
    setFont(getFont.deriveFont(9.0f))
  }

  // the wrapping properties are here to control the variables but they are not added to the GUI (Isaac B 4/2/25)
  private val wrappingX: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui("3D.wrapX"),
        () => target.wrappingX,
        target.wrappingX(_),
        () => previewChanged("wrappingX", wrappingX.get)))

  private val wrappingY: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui("3D.wrapY"),
        () => target.wrappingY,
        target.wrappingY(_),
        () => previewChanged("wrappingY", wrappingY.get)))

  private val wrappingZ: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui("3D.wrapZ"),
        () => target.wrappingZ,
        target.wrappingZ(_),
        () => previewChanged("wrappingZ", wrappingZ.get)))

  private val patchSize =
    new StrictlyPositiveDoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui("2D.patchSize"),
        () => target.patchSize,
        target.patchSize(_)))

  private val patchSizeLabel = new JLabel(I18N.gui("2D.patchSize.info")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val fontSize =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui("2D.fontSize"),
        () => target.fontSize,
        target.fontSize(_)))

  private val fontSizeLabel = new JLabel(I18N.gui("2D.fontSize.info")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val frameRate =
    new StrictlyPositiveDoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui("2D.frameRate"),
        () => target.frameRate,
        target.frameRate(_)))

  private val frameRateLabel = new JLabel(I18N.gui("2D.frameRate.info")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val smooth =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui("3D.smooth"),
        () => target.smooth,
        target.smooth(_)))

  private val smoothLabel = new JLabel(I18N.gui("3D.affects")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val wireframe =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui("3D.wireFrame"),
        () => target.wireframe,
        target.wireframe(_)))

  private val wireframeLabel = new JLabel(I18N.gui("3D.affects")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val dualView =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui("3D.dualView"),
        () => target.dualView,
        target.dualView(_)))

  private val showTickCounter =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui("showTickCounter"),
        () => target.showTickCounter,
        target.showTickCounter(_)))

  private val tickCounterLabel =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui("tickCounterLabel"),
        () => target.tickCounterLabel,
        target.tickCounterLabel(_)))

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

      c.insets = new Insets(0, 6, 3, 6)

      add(minPzcor, c)

      c.insets = new Insets(0, 6, 6, 6)

      add(minPzcorLabel, c)

      c.insets = new Insets(0, 6, 3, 6)

      add(maxPzcor, c)

      c.insets = new Insets(0, 6, 6, 6)

      add(maxPzcorLabel, c)
    }

    val previewContainer = new JPanel with Transparent {
      add(previewPanel)
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

      c.gridx = 0
      c.gridy = GridBagConstraints.RELATIVE
      c.gridwidth = 2
      c.insets = new Insets(0, 6, 3, 6)

      add(frameRate, c)

      c.insets = new Insets(0, 6, 6, 6)

      add(frameRateLabel, c)

      c.insets = new Insets(0, 6, 3, 6)

      add(smooth, c)

      c.insets = new Insets(0, 6, 6, 6)

      add(smoothLabel, c)

      c.insets = new Insets(0, 6, 3, 6)

      add(wireframe, c)

      c.insets = new Insets(0, 6, 6, 6)

      add(wireframeLabel, c)

      c.insets = new Insets(0, 6, 3, 6)

      add(dualView, c)
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

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(minPxcor, maxPxcor, minPycor, maxPycor, minPzcor, maxPzcor, wrappingX, wrappingY, wrappingZ, patchSize,
        fontSize, frameRate, smooth, wireframe, dualView, showTickCounter, tickCounterLabel)

  override def editors: Seq[IntegerEditor] =
    Seq(minPxcor, maxPxcor, minPycor, maxPycor, minPzcor, maxPzcor)

  override def syncExtraComponents(): Unit = {
    worldBorder.setTitleColor(InterfaceColors.dialogText())
    viewBorder.setTitleColor(InterfaceColors.dialogText())
    modelBorder.setTitleColor(InterfaceColors.dialogText())

    locationLabel.setForeground(InterfaceColors.dialogText())

    minPxcorLabel.setForeground(InterfaceColors.dialogText())
    maxPxcorLabel.setForeground(InterfaceColors.dialogText())
    minPycorLabel.setForeground(InterfaceColors.dialogText())
    maxPycorLabel.setForeground(InterfaceColors.dialogText())
    minPzcorLabel.setForeground(InterfaceColors.dialogText())
    maxPzcorLabel.setForeground(InterfaceColors.dialogText())

    patchSizeLabel.setForeground(InterfaceColors.dialogText())
    fontSizeLabel.setForeground(InterfaceColors.dialogText())
    frameRateLabel.setForeground(InterfaceColors.dialogText())
    smoothLabel.setForeground(InterfaceColors.dialogText())
    wireframeLabel.setForeground(InterfaceColors.dialogText())
  }
}
