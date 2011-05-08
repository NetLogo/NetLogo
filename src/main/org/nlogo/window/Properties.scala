package org.nlogo.window

// Property is more convenient to use from Scala than Java, thanks to named and default arguments,
// so we put all of our Properties for this package here - ST 2/23/10

import org.nlogo.util.JCL.JavaList
import java.awt.GridBagConstraints._
import org.nlogo.api.{I18N, Property => P}

object Properties {
  implicit val i18nPrefix = I18N.Prefix("edit")
  // normal widgets
  val plot = JavaList(
    P("plotName", P.NonEmptyString, I18N.gui("plot.name"), focus = true),
    P("xLabel", P.String, I18N.gui("plot.xLabel"), gridWidth = 1),
    P("defaultXMin", P.Double, I18N.gui("plot.xmin"), gridWidth = RELATIVE),
    P("defaultXMax", P.Double, I18N.gui("plot.xmax")),
    P("yLabel", P.String, I18N.gui("plot.yLabel"), gridWidth = 1),
    P("defaultYMin", P.Double, I18N.gui("plot.ymin"), gridWidth = RELATIVE),
    P("defaultYMax", P.Double, I18N.gui("plot.ymax")),
    P("defaultAutoPlotOn", P.Boolean, I18N.gui("plot.autoScale"), gridWidth = 1),
    P("showLegend", P.Boolean, I18N.gui("plot.showLegend")),
    P("setupCode", P.Commands, I18N.gui("plot.setupCode"), collapsable = true, collapseByDefault = true),
    P("updateCode", P.Commands, I18N.gui("plot.updateCode"), collapsable = true, collapseByDefault = true),
    P("editPlotPens", P.PlotPens, I18N.gui("plot.pen.plotPens"), gridWidth = REMAINDER)
  )
  val button = JavaList(
    P("agentOptions", P.AgentOptions, I18N.gui("button.agents"), gridWidth = RELATIVE),
    P("forever", P.Boolean, I18N.gui("button.forever")),
    P("goTime", P.Boolean, I18N.gui("button.disable")),
    P("wrapSource", P.Commands, I18N.gui("button.commands"), focus = true),
    P("name", P.String, I18N.gui("button.displayName")),
    P("actionKey", P.Key, I18N.gui("button.actionKey"))
  )
  val chooser = JavaList(
    P("nameWrapper", P.Identifier, I18N.gui("chooser.globalVar")),
    P("choicesWrapper", P.LogoListString, I18N.gui("chooser.choices"),
      "<html>example: &quot;a&quot; &quot;b&quot; &quot;c&quot; 3 4 5</html>")
  )
  val slider = JavaList(
    P("nameWrapper", P.Identifier, I18N.gui("slider.globalVar")),
    P("minimumCode", P.ReporterLine, I18N.gui("slider.minimum"),
      "min, increment, and max may be numbers or reporters",
      gridWidth = 1),
    P("incrementCode", P.ReporterLine, I18N.gui("slider.increment"),
      " ", // empty, so layout lines up
      gridWidth = RELATIVE),
    P("maximumCode", P.ReporterLine, I18N.gui("slider.maximum"),
      " "), // empty, so layout lines up
    P("value", P.Double, I18N.gui("slider.value"), gridWidth = RELATIVE),
    P("units", P.String, I18N.gui("slider.units")),
    P("vertical", P.Boolean, I18N.gui("slider.vertical"))
  )
  val monitor = JavaList(
    P("wrapSource", P.Reporter, I18N.gui("monitor.reporter")),
    P("name", P.String, I18N.gui("monitor.name")),
    P("decimalPlaces", P.Integer, I18N.gui("monitor.decimalPlaces"), "full precision is 17", gridWidth = RELATIVE),
    P("fontSize", P.Integer, I18N.gui("monitor.fontSize"))
  )
  val output = JavaList(
    P("fontSize", P.Integer, I18N.gui("output.fontSize"))
  )
  val input = JavaList(
    P("nameWrapper", P.Identifier, I18N.gui("input.globalVar")),
    P("typeOptions", P.InputBoxOptions, I18N.gui("input.type"), gridWidth = RELATIVE)
  )

  // WorldViewSettings
  val model = JavaList(
    P("showTickCounter", P.Boolean, I18N.gui("viewSettings.showTickCounter")),
    P("tickCounterLabel", P.String, I18N.gui("viewSettings.tickCounterLabel"))
  )
  val view2D = JavaList(
    P("patchSize", P.Double,
      I18N.gui("viewSettings.2D.patchSize"), I18N.gui("viewSettings.2D.patchSize.info"),
      gridWidth = RELATIVE),
    P("fontSize", P.Integer,
      I18N.gui("viewSettings.2D.fontSize"), I18N.gui("viewSettings.2D.fontSize.info")),
    P("frameRate", P.StrictlyPositiveDouble,
      I18N.gui("viewSettings.2D.frameRate"), I18N.gui("viewSettings.2D.frameRate.info"))
  )
  val view3D = JavaList(
    P("smooth", P.Boolean, I18N.gui("viewSettings.3D.smooth"), I18N.gui("viewSettings.3D.affects")),
    P("wireframe", P.Boolean, I18N.gui("viewSettings.3D.wireFrame"), I18N.gui("viewSettings.3D.affects")),
    P("dualView", P.Boolean, I18N.gui("viewSettings.3D.dualView"))
  )
  val wrap2D = JavaList(
    P("wrappingX", P.Boolean, I18N.gui("viewSettings.2D.wrapX"), setLive = true),
    P("wrappingY", P.Boolean, I18N.gui("viewSettings.2D.wrapY"), setLive = true)
  )
  val wrap3D = JavaList(
    P("wrappingX", P.Boolean, I18N.gui("viewSettings.3D.wrapX"), setLive = true),
    P("wrappingY", P.Boolean, I18N.gui("viewSettings.3D.wrapY"), setLive = true),
    P("wrappingZ", P.Boolean, I18N.gui("viewSettings.3D.wrapZ"), setLive = true)
  )
  val dims2D = JavaList(
    P("minPxcor", P.NegativeInteger, "min-pxcor", I18N.gui("viewSettings.2D.minPxcor"), setLive = true),
    P("maxPxcor", P.PositiveInteger, "max-pxcor", I18N.gui("viewSettings.2D.maxPxcor"), setLive = true),
    P("minPycor", P.NegativeInteger, "min-pycor", I18N.gui("viewSettings.2D.minPycor"), setLive = true),
    P("maxPycor", P.PositiveInteger, "max-pycor", I18N.gui("viewSettings.2D.maxPycor"), setLive = true)
  )
  val dims3D = JavaList(
    P("minPxcor", P.NegativeInteger, "min-pxcor", I18N.gui("viewSettings.3D.minPxcor"), setLive = true),
    P("maxPxcor", P.PositiveInteger, "max-pxcor", I18N.gui("viewSettings.3D.maxPxcor"), setLive = true),
    P("minPycor", P.NegativeInteger, "min-pycor", I18N.gui("viewSettings.3D.minPycor"), setLive = true),
    P("maxPycor", P.PositiveInteger, "max-pycor", I18N.gui("viewSettings.3D.maxPycor"), setLive = true),
    P("minPzcor", P.NegativeInteger, "min-pzcor", I18N.gui("viewSettings.3D.minPzcor"), setLive = true),
    P("maxPzcor", P.PositiveInteger, "max-pzcor", I18N.gui("viewSettings.3D.maxPzcor"), setLive = true)
  )

  // dummies for HubNet clients
  val dummyButton = JavaList(
    P("name", P.String, I18N.gui("hubnet.tag")),
    P("actionKey", P.Key, I18N.gui("button.actionKey"))
  )
  val dummyChooser = JavaList(
    P("name", P.String, I18N.gui("hubnet.tag")),
    P("choicesWrapper", P.LogoListString, I18N.gui("chooser.choices"),
      "<html>example: &quot;a&quot; &quot;b&quot; &quot;c&quot; 3 4 5</html>")
  )
  val dummyInput = JavaList(
    P("nameWrapper", P.String, I18N.gui("hubnet.tag")),
    P("typeOptions", P.InputBoxOptions, I18N.gui("input.type"), gridWidth = RELATIVE)
  )
  val dummyMonitor = JavaList(
    P("name", P.String, I18N.gui("hubnet.tag")),
    P("decimalPlaces", P.Integer, "Decimal places", gridWidth = RELATIVE)
  )
  val dummyPlot = JavaList(
    P("nameOptions", P.PlotOptions, I18N.gui("plot.name")),
    P("xLabel", P.String, I18N.gui("plot.xLabel"), gridWidth = 1),
    P("defaultXMin", P.Double, I18N.gui("plot.xmin"), gridWidth = RELATIVE),
    P("defaultXMax", P.Double, I18N.gui("plot.xmax")),
    P("yLabel", P.String, I18N.gui("plot.yLabel"), gridWidth = 1),
    P("defaultYMin", P.Double, I18N.gui("plot.ymin"), gridWidth = RELATIVE),
    P("defaultYMax", P.Double, I18N.gui("plot.ymax")),
    P("defaultAutoPlotOn", P.Boolean, I18N.gui("plot.autoScale")),
    P("showLegend", P.Boolean, I18N.gui("plot.showLegend"))
  )
  val dummySlider = JavaList(
    P("name", P.String, I18N.gui("hubnet.tag")),
    P("min", P.Double, I18N.gui("slider.minimum"), gridWidth = 1),
    P("inc", P.Double, I18N.gui("slider.increment"), gridWidth = RELATIVE),
    P("max", P.Double, I18N.gui("slider.maximum")),
    P("value", P.Double, I18N.gui("slider.value"), gridWidth = RELATIVE),
    P("units", P.String, I18N.gui("slider.units")),
    P("vertical", P.Boolean, I18N.gui("slider.vertical"))
  )
  val dummyView = JavaList(
    P("width", P.Integer, I18N.gui("hubnet.view.width")),
    P("height", P.Integer, I18N.gui("hubnet.view.height"))
  )
}
