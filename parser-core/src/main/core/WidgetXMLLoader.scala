// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object WidgetXMLLoader {
  def readWidget(element: XMLElement): Widget = {

    element.name match {

      case "button" =>
        val kind =
          element("kind") match {
            case "Observer" => AgentKind.Observer
            case "Patch"    => AgentKind.Patch
            case "Turtle"   => AgentKind.Turtle
            case "Link"     => AgentKind.Link
          }
        Button( element.getOptionalChild("source").map(_.text), element("x").toInt, element("y").toInt
              , element("width").toInt, element("height").toInt, element.get("display"), element("forever").toBoolean
              , kind, element.get("actionKey").map(_.head), element("disableUntilTicks").toBoolean
              )

      case "slider" =>
        val direction =
          element("direction") match {
            case "Horizontal" => Horizontal
            case "Vertical"   => Vertical
          }
        Slider( element.get("variable"), element("x").toInt, element("y").toInt, element("width").toInt
              , element("height").toInt, element.get("display"), element("min"), element("max")
              , element("default").toDouble, element("step"), element.get("units"), direction
              )

      case "view" =>
        val dims =
          new WorldDimensions( element("minPxcor").toInt, element("maxPxcor").toInt, element("minPycor").toInt
                             , element("maxPycor").toInt, element("patchSize").toDouble
                             , element("wrappingAllowedX").toBoolean, element("wrappingAllowedY").toBoolean
                             )
        View( element("x").toInt, element("y").toInt, element("width").toInt, element("height").toInt, dims
            , element("fontSize").toInt, UpdateMode.load(element("updateMode").toInt)
            , element("showTickCounter").toBoolean, element.get("tickCounterLabel"), element("frameRate").toDouble
            )

      case "view3d" =>
        val dims =
          new WorldDimensions3D( element("minPxcor").toInt, element("maxPxcor").toInt
                               , element("minPycor").toInt, element("maxPycor").toInt
                               , element("minPzcor").toInt, element("maxPzcor").toInt
                               , element("patchSize").toDouble, element("wrappingAllowedX").toBoolean
                               , element("wrappingAllowedY").toBoolean, element("wrappingAllowedZ").toBoolean
                               )
        View( element("x").toInt, element("y").toInt, element("width").toInt, element("height").toInt, dims
            , element("fontSize").toInt, UpdateMode.load(element("updateMode").toInt)
            , element("showTickCounter").toBoolean, element.get("tickCounterLabel"), element("frameRate").toDouble
            )

      case "monitor" =>
        Monitor( element.getOptionalChild("source").map(_.text), element("x").toInt, element("y").toInt
               , element("width").toInt, element("height").toInt, element.get("display"), element("precision").toInt
               , element("fontSize").toInt
               )

      case "switch" =>
        Switch( element.get("variable"), element("x").toInt, element("y").toInt, element("width").toInt
              , element("height").toInt, element.get("display"), element("on").toBoolean
              )

      case "plot" =>
        val pens =
          element.getChildren("pen").map(
            el =>
               Pen( el("display"), el("interval").toDouble, el("mode").toInt, el("color").toInt
                  , el("legend").toBoolean, el.getChild("setup").text, el.getChild("update").text
                  )
          ).toList
        Plot( element.get("display"), element("x").toInt, element("y").toInt, element("width").toInt
            , element("height").toInt, element.get("xAxis"), element.get("yAxis"), element("xMin").toDouble
            , element("xMax").toDouble, element("yMin").toDouble, element("yMax").toDouble
            , element("autoplot").toBoolean, element("legend").toBoolean, element.getChild("setup").text
            , element.getChild("update").text, pens
            )

      case "chooser" =>
        val children =
          element.getChildren("choice").map(
            el =>
              el("type") match {
                case "string" =>
                  ChooseableString(el("value"))
                case "double" =>
                  ChooseableDouble(el("value").toDouble)
                case "boolean" =>
                  ChooseableBoolean(el("value").toBoolean)
                case "list" =>
                  ChooseableList(LogoList.fromList(el.getChildren("value").map(_("value")).toList))
              }
          )
        Chooser( element.get("variable"), element("x").toInt, element("y").toInt, element("width").toInt
               , element("height").toInt, element.get("display"), children.toList, element("current").toInt
               )

      case "output" =>
        Output( element("x").toInt, element("y").toInt, element("width").toInt, element("height").toInt
              , element("fontSize").toInt
              )

      case "input" =>
        val input =
          element("type") match {
            case "number" =>
              NumericInput(element.text.toDouble, NumericInput.NumberLabel)
            case "color" =>
              NumericInput(element.text.toDouble, NumericInput.ColorLabel)
            case "string" =>
              StringInput(element.text, StringInput.StringLabel, element("multiline").toBoolean)
            case "reporter" =>
              StringInput(element.text, StringInput.ReporterLabel, element("multiline").toBoolean)
            case "command" =>
              StringInput(element.text, StringInput.CommandLabel, element("multiline").toBoolean)
          }
        InputBox( element.get("variable"), element("x").toInt, element("y").toInt, element("width").toInt
                , element("height").toInt, input
                )

      case "note" =>
        TextBox( element.get("display"), element("x").toInt, element("y").toInt, element("width").toInt
               , element("height").toInt, element("fontSize").toInt, element("color").toDouble
               , element("transparent").toBoolean
               )

    }
  }

  def writeWidget(widget: Widget): XMLElement = {

    def ifDefined[T <: Widget](w: T)(key: String, f: (T) => Option[Any]): Map[String, String] =
      if (f(w).isDefined) Map(key -> f(w).get.toString) else Map()

    widget match {

      case button: Button =>
        val attributes =
          Map( "x"              -> button.x.toString
             , "y"               -> button.y.toString
             , "width"             -> button.width.toString
             , "height"            -> button.height.toString
             , "forever"           -> button.forever.toString
             , "kind"              -> button.buttonKind.toString
             , "disableUntilTicks" -> button.disableUntilTicksStart.toString
             ) ++
          ifDefined(button)(  "display",   _.display) ++
          ifDefined(button)("actionKey", _.actionKey)

        val children =
          if (button.source.isDefined)
            Seq(XMLElement("source", Map(), button.source.get, Seq()))
          else
            Seq()

        XMLElement("button", attributes, "", children)

      case slider: Slider =>
        val attributes =
          Map( "x"      -> slider.x.toString
             , "y"       -> slider.y.toString
             , "width"     -> slider.width.toString
             , "height"    -> slider.height.toString
             , "min"       -> slider.min
             , "max"       -> slider.max
             , "default"   -> slider.default.toString
             , "step"      -> slider.step
             , "direction" -> slider.direction.toString
             ) ++
          ifDefined(slider)( "display",  _.display) ++
          ifDefined(slider)("variable", _.variable) ++
          ifDefined(slider)(   "units",    _.units)
        XMLElement("slider", attributes, "", Seq())

      case view: View =>

        val baseAttributes =
          Map( "x"             -> view.x.toString
             , "y"              -> view.y.toString
             , "width"            -> view.width.toString
             , "height"           -> view.height.toString
             , "minPxcor"         -> view.dimensions.minPxcor.toString
             , "maxPxcor"         -> view.dimensions.maxPxcor.toString
             , "minPycor"         -> view.dimensions.minPycor.toString
             , "maxPycor"         -> view.dimensions.maxPycor.toString
             , "patchSize"        -> view.dimensions.patchSize.toString
             , "wrappingAllowedX" -> view.dimensions.wrappingAllowedInX.toString
             , "wrappingAllowedY" -> view.dimensions.wrappingAllowedInY.toString
             , "fontSize"         -> view.fontSize.toString
             , "updateMode"       -> view.updateMode.save.toString
             , "showTickCounter"  -> view.showTickCounter.toString
             , "frameRate"        -> view.frameRate.toString
             ) ++ ifDefined(view)("tickCounterLabel", _.tickCounterLabel)

        view.dimensions match {
          case d3d: WorldDimensions3D =>
            XMLElement("view3d", baseAttributes ++ Map(
                                   ("minPzcor" -> d3d.minPzcor.toString),
                                   ("maxPzcor" -> d3d.maxPzcor.toString),
                                   ("wrappingAllowedZ" -> d3d.wrappingAllowedInZ.toString)
                                 ), "", Seq())
          case d: WorldDimensions =>
            XMLElement("view", baseAttributes, "", Seq())
        }

      case monitor: Monitor =>
        val attributes =
          Map( "x"      -> monitor.x.toString
             , "y"       -> monitor.y.toString
             , "width"     -> monitor.width.toString
             , "height"    -> monitor.height.toString
             , "precision" -> monitor.precision.toString
             , "fontSize"  -> monitor.fontSize.toString
             ) ++
          ifDefined(monitor)("display", _.display)

        val children =
          if (monitor.source.isDefined)
            List(XMLElement("source", Map(), monitor.source.get, Seq()))
          else
            Seq()

        XMLElement("monitor", attributes, "", children)

      case switch: Switch =>
        val attributes =
          Map( "x"   -> switch.x.toString
             , "y"    -> switch.y.toString
             , "width"  -> switch.width.toString
             , "height" -> switch.height.toString
             , "on"     -> switch.on.toString
             ) ++
          ifDefined(switch)( "display",  _.display) ++
          ifDefined(switch)("variable", _.variable)

        XMLElement("switch", attributes, "", Seq())

      case plot: Plot =>

        val attributes =
          Map( "x"     -> plot.x.toString
             , "y"      -> plot.y.toString
             , "width"    -> plot.width.toString
             , "height"   -> plot.height.toString
             , "xMin"     -> plot.xmin.toString
             , "xMax"     -> plot.xmax.toString
             , "yMin"     -> plot.ymin.toString
             , "yMax"     -> plot.ymax.toString
             , "autoplot" -> plot.autoPlotOn.toString
             , "legend"   -> plot.legendOn.toString
             ) ++
          ifDefined(plot)("display", _.display) ++
          ifDefined(plot)(  "xAxis",   _.xAxis) ++
          ifDefined(plot)(  "yAxis",   _.yAxis)

        val children =
          Seq( XMLElement( "setup", Map(), plot. setupCode, Seq())
             , XMLElement("update", Map(), plot.updateCode, Seq())
             )

        val pens =
          plot.pens.map {
            pen =>

              val attrs =
                Map( "display"  -> pen.display
                   , "interval" -> pen.interval.toString
                   , "mode"     -> pen.mode.toString
                   , "color"    -> pen.color.toString
                   , "legend"   -> pen.inLegend.toString
                   )

              val kids =
                Seq( XMLElement( "setup", Map(), pen. setupCode, Seq())
                   , XMLElement("update", Map(), pen.updateCode, Seq())
                   )

              XMLElement("pen", attrs, "", kids)

          }

        XMLElement("plot", attributes, "", children ++ pens)

      case chooser: Chooser =>

        val attributes =
          Map( "x"    -> chooser.x.toString
             , "y"     -> chooser.y.toString
             , "width"   -> chooser.width.toString
             , "height"  -> chooser.height.toString
             , "current" -> chooser.currentChoice.toString
             ) ++
          ifDefined(chooser)( "display",  _.display) ++
          ifDefined(chooser)("variable", _.variable)

        val children =
          chooser.choices.map {
            case ChooseableString(string) =>
              XMLElement("choice", Map("type" ->  "string", "value" ->           string), "", Seq())
            case ChooseableDouble(double) =>
              XMLElement("choice", Map("type" ->  "double", "value" ->  double.toString), "", Seq())
            case ChooseableBoolean(boolean) =>
              XMLElement("choice", Map("type" -> "boolean", "value" -> boolean.toString), "", Seq())
            case ChooseableList(list) =>
              val children = list.map((x) => XMLElement("value", Map("value" -> x.toString), "", Seq()))
              XMLElement("choice", Map("type" -> "list"), "", children)
          }

        XMLElement("chooser", attributes, "", children)

      case output: Output =>
        val attributes =
          Map( "x"     -> output.x.toString
             , "y"      -> output.y.toString
             , "width"    -> output.width.toString
             , "height"   -> output.height.toString
             , "fontSize" -> output.fontSize.toString
             )
        XMLElement("output", attributes, "", Seq())

      case input: InputBox =>

        val typeName =
          input.boxedValue match {
            case NumericInput(_, label) =>
              label match {
                case NumericInput.NumberLabel => "number"
                case NumericInput.ColorLabel  => "color"
              }
            case input: StringInput =>
              input.label match {
                case StringInput.StringLabel   => "string"
                case StringInput.ReporterLabel => "reporter"
                case StringInput.CommandLabel  => "command"
              }
          }

        val attributes =
          Map( "x"      -> input.x.toString
             , "y"       -> input.y.toString
             , "width"     -> input.width.toString
             , "height"    -> input.height.toString
             , "multiline" -> input.multiline.toString
             , "type"      -> typeName
             ) ++
          ifDefined(input)("variable", _.variable)

        XMLElement("input", attributes, input.boxedValue.asString, Seq())

      case note: TextBox =>
        val attributes =
          Map( "x" -> note.x.toString
             , "y"  -> note.y.toString
             , "width" -> note.width.toString
             , "height" -> note.height.toString
             , "fontSize" -> note.fontSize.toString
             , "color" -> note.color.toString
             , "transparent" -> note.transparent.toString
             )

        XMLElement("note", attributes, note.display.getOrElse(""), Seq())

    }

  }

}
